const path = require('path');
const axios = require('axios');
const { Logger } = require('@hmcts/nodejs-logging');

const {
    buildHeaders,
    env,
    findCaseData,
    findCaseDetails,
    firstPresent,
    getAuthContext,
    getCcdDataStoreUrl,
    getCaseId,
    getErrorDetails,
    getManifestFile,
    getPreviewServiceFqdn,
    getSeededCases,
    loadManifest,
    stripTrailingSlash,
    testConfig,
    writeJsonReport
} = require('./cutoverProbeSupport');

const logger = Logger.getLogger('cutover/verifyCutoverNocReadiness.js');
const jurisdiction = 'EMPLOYMENT';
const caseTypeId = 'ET_EnglandWales';
const defaultOutputFile = path.resolve(process.cwd(), 'functional-output/cutover/noc-readiness-report.json');

function getOutputFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_NOC_OUTPUT_FILE || defaultOutputFile);
}

function getCaseAssignmentUrl() {
    const configuredUrl = firstPresent(
        process.env.CUTOVER_NOC_CASE_ASSIGNMENT_URL,
        process.env.CUTOVER_CASE_ASSIGNMENT_URL
    );

    if (configuredUrl) {
        return stripTrailingSlash(configuredUrl);
    }

    return getCcdDataStoreUrl();
}

async function fetchCaseDetails(context, seededCase) {
    const caseUrl = `${getCcdDataStoreUrl()}/caseworkers/${context.userId}/jurisdictions/${jurisdiction}`
        + `/case-types/${caseTypeId}/cases/${seededCase.caseId}`;
    const response = await axios.get(caseUrl, {
        headers: buildHeaders(context)
    });
    const payload = response.data || {};
    const details = findCaseDetails(payload);

    return {
        caseId: getCaseId(details) || seededCase.caseId,
        state: firstPresent(details.state, details.case_state) || null,
        caseData: findCaseData(payload)
    };
}

function hasOrganisationId(policy) {
    const organisation = firstPresent(policy?.Organisation, policy?.organisation) || {};

    return Boolean(firstPresent(
        organisation.OrganisationID,
        organisation.organisationID,
        organisation.organisationId,
        organisation.id,
        policy?.OrgPolicyReference,
        policy?.orgPolicyReference
    ));
}

function summarisePolicy(fieldName, policy) {
    if (!policy) {
        return null;
    }

    const organisation = firstPresent(policy.Organisation, policy.organisation) || {};

    return {
        fieldName,
        caseAssignedRole: firstPresent(
            policy.OrgPolicyCaseAssignedRole,
            policy.orgPolicyCaseAssignedRole
        ) || null,
        organisationId: firstPresent(
            organisation.OrganisationID,
            organisation.organisationID,
            organisation.organisationId,
            organisation.id
        ) || null,
        organisationName: firstPresent(
            organisation.OrganisationName,
            organisation.organisationName,
            organisation.name
        ) || null,
        hasOrganisation: hasOrganisationId(policy)
    };
}

function getOrganisationPolicies(caseData) {
    const policies = [];
    const claimantPolicy = summarisePolicy(
        'claimantRepresentativeOrganisationPolicy',
        caseData.claimantRepresentativeOrganisationPolicy
    );

    if (claimantPolicy) {
        policies.push(claimantPolicy);
    }

    for (let index = 0; index <= 9; index += 1) {
        const fieldName = `respondentOrganisationPolicy${index}`;
        const policy = summarisePolicy(fieldName, caseData[fieldName]);

        if (policy) {
            policies.push(policy);
        }
    }

    return policies;
}

function getCollectionSize(collection) {
    return Array.isArray(collection) ? collection.length : 0;
}

function evaluateNocData(caseData) {
    const policies = getOrganisationPolicies(caseData);
    const respondentPolicyCount = policies.filter(policy => policy.fieldName.startsWith('respondent')).length;
    const claimantPolicyCount = policies.filter(policy => policy.fieldName.startsWith('claimant')).length;
    const populatedOrganisationPolicyCount = policies.filter(policy => policy.hasOrganisation).length;
    const respondentCount = getCollectionSize(caseData.respondentCollection);
    const respondentRepresentativeCount = getCollectionSize(caseData.repCollection);
    const hasClaimantDetails = Boolean(firstPresent(
        caseData.claimantIndType,
        caseData.claimantType,
        caseData.claimant
    ));
    const gaps = [];

    if (claimantPolicyCount === 0) {
        gaps.push('claimantRepresentativeOrganisationPolicy missing');
    }

    if (respondentCount === 0) {
        gaps.push('respondentCollection missing or empty');
    }

    if (respondentPolicyCount === 0) {
        gaps.push('respondentOrganisationPolicy fields missing');
    }

    if (respondentRepresentativeCount === 0) {
        gaps.push('repCollection missing or empty');
    }

    if (populatedOrganisationPolicyCount === 0) {
        gaps.push('no organisation policy contains an organisation id');
    }

    if (!hasClaimantDetails) {
        gaps.push('claimant details missing');
    }

    return {
        ready: gaps.length === 0,
        gaps,
        respondentCount,
        respondentRepresentativeCount,
        claimantPolicyCount,
        respondentPolicyCount,
        populatedOrganisationPolicyCount,
        policies
    };
}

function extractCaseUserAssignments(payload) {
    if (Array.isArray(payload.case_users)) {
        return payload.case_users;
    }

    if (Array.isArray(payload.caseUserAssignments)) {
        return payload.caseUserAssignments;
    }

    if (Array.isArray(payload.case_users_assignments)) {
        return payload.case_users_assignments;
    }

    return [];
}

function summariseAssignment(assignment) {
    return {
        caseId: firstPresent(assignment.case_id, assignment.caseId) || null,
        userId: firstPresent(assignment.user_id, assignment.userId) || null,
        caseRole: firstPresent(assignment.case_role, assignment.caseRole) || null,
        organisationId: firstPresent(assignment.organisation_id, assignment.organisationId) || null
    };
}

async function fetchCaseAssignments(context, caseAssignmentUrl, caseId) {
    try {
        const response = await axios.get(`${caseAssignmentUrl}/case-users?case_ids=${caseId}`, {
            headers: buildHeaders(context, { experimental: true })
        });
        const assignments = extractCaseUserAssignments(response.data || {});

        return {
            ok: true,
            endpoint: `${caseAssignmentUrl}/case-users?case_ids=${caseId}`,
            assignmentCount: assignments.length,
            assignments: assignments.map(summariseAssignment)
        };
    } catch (error) {
        return {
            ok: false,
            endpoint: `${caseAssignmentUrl}/case-users?case_ids=${caseId}`,
            error: getErrorDetails(error)
        };
    }
}

async function verifySeededCase(context, caseAssignmentUrl, seededCase) {
    const caseDetails = await fetchCaseDetails(context, seededCase);
    const nocData = evaluateNocData(caseDetails.caseData);
    const assignments = await fetchCaseAssignments(context, caseAssignmentUrl, seededCase.caseId);
    const failures = [];

    if (process.env.CUTOVER_NOC_REQUIRE_READY === 'true' && !nocData.ready) {
        failures.push('nocDataReadiness');
    }

    if (process.env.CUTOVER_NOC_REQUIRE_ASSIGNMENTS === 'true'
        && (!assignments.ok || assignments.assignmentCount === 0)) {
        failures.push('caseAssignments');
    }

    return {
        seedId: seededCase.seedId,
        controllerCategory: seededCase.controllerCategory,
        caseId: seededCase.caseId,
        state: caseDetails.state,
        status: failures.length === 0 ? 'verified' : 'failed',
        failures,
        checks: {
            nocData,
            assignments
        }
    };
}

function buildReport({ manifestFile, outputFile, manifest, caseAssignmentUrl, results }) {
    const failedCaseCount = results.filter(result => result.status === 'failed').length;
    const notReadyCaseCount = results.filter(result => !result.checks?.nocData?.ready).length;

    return {
        reportVersion: 1,
        generatedAt: new Date().toISOString(),
        status: failedCaseCount === 0 ? 'complete' : 'failed',
        environment: env,
        testUrl: testConfig.TestUrl,
        ccdApiUrl: getCcdDataStoreUrl(),
        caseAssignmentUrl,
        previewServiceFqdn: getPreviewServiceFqdn() || null,
        manifestFile,
        outputFile,
        seedManifestGeneratedAt: manifest.generatedAt || null,
        requestedCaseCount: results.length,
        verifiedCaseCount: results.filter(result => result.status === 'verified').length,
        failedCaseCount,
        notReadyCaseCount,
        requiredEtInputs: [
            'which seeded case should exercise a full Notice of Change journey',
            'old and new professional users to use for NoC',
            'organisation ids and expected case roles before and after NoC',
            'whether case assignment presence should be mandatory for this rehearsal'
        ],
        results
    };
}

async function main() {
    const manifestFile = getManifestFile('CUTOVER_NOC_MANIFEST_FILE');
    const outputFile = getOutputFile();
    const caseAssignmentUrl = getCaseAssignmentUrl();
    const manifest = loadManifest(manifestFile);
    const cases = getSeededCases(manifest, 'CUTOVER_NOC_PROFILE_IDS');

    if (cases.length === 0) {
        throw new Error(`No seeded cases found in manifest: ${manifestFile}`);
    }

    logger.info(`Checking NoC readiness for ${cases.length} cutover case(s)`);
    logger.info(`Writing cutover NoC readiness report to ${outputFile}`);

    const context = await getAuthContext();
    const results = [];

    writeJsonReport(outputFile, buildReport({ manifestFile, outputFile, manifest, caseAssignmentUrl, results }));

    for (const seededCase of cases) {
        const result = await verifySeededCase(context, caseAssignmentUrl, seededCase);
        results.push(result);
        logger.info(`NoC readiness check ${result.status}: ${seededCase.seedId} => ${seededCase.caseId}`);
        writeJsonReport(outputFile, buildReport({ manifestFile, outputFile, manifest, caseAssignmentUrl, results }));
    }

    const report = buildReport({ manifestFile, outputFile, manifest, caseAssignmentUrl, results });
    writeJsonReport(outputFile, report);

    if (report.failedCaseCount > 0) {
        throw new Error(`Cutover NoC readiness verification failed for ${report.failedCaseCount} case(s); `
            + `report written to ${outputFile}`);
    }

    console.log(`Cutover NoC readiness report written to ${outputFile}`);
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
