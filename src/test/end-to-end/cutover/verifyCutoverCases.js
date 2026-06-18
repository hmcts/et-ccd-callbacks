const fs = require('fs');
const path = require('path');
const axios = require('axios');
const { Logger } = require('@hmcts/nodejs-logging');
const { TOTP } = require('totp-generator');

const testConfig = require('../../config.js');
const idamApi = require('../helpers/idamApi');
const { seedProfiles } = require('./seedProfiles');
const { getCcdApiUrl, getPreviewServiceFqdn, getS2sLeaseUrl } = require('./cutoverConfig');

const logger = Logger.getLogger('cutover/verifyCutoverCases.js');
const env = testConfig.TestEnv;
const jurisdiction = 'EMPLOYMENT';
const caseTypeId = 'ET_EnglandWales';
const ccdApiUrl = getCcdApiUrl(env);
const s2sLeaseUrl = getS2sLeaseUrl(env);
const defaultManifestFile = path.resolve(process.cwd(), 'functional-output/cutover/seed-manifest.json');
const defaultOutputFile = path.resolve(process.cwd(), 'functional-output/cutover/verify-report.json');

function getManifestFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_VERIFY_MANIFEST_FILE || defaultManifestFile);
}

function getOutputFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_VERIFY_OUTPUT_FILE || defaultOutputFile);
}

function getSelectedProfileIds() {
    return process.env.CUTOVER_VERIFY_PROFILE_IDS
        ? process.env.CUTOVER_VERIFY_PROFILE_IDS.split(',').map(profile => profile.trim()).filter(Boolean)
        : [];
}

function buildHeaders(context, additionalHeaders = {}) {
    return {
        Authorization: `Bearer ${context.authToken}`,
        ServiceAuthorization: `Bearer ${context.serviceToken}`,
        'Content-Type': 'application/json',
        ...additionalHeaders
    };
}

function getErrorDetails(error) {
    const details = {
        message: error.message
    };

    if (error.response) {
        details.status = error.response.status;
        details.statusText = error.response.statusText;

        if (error.response.data) {
            details.responseData = error.response.data;
        }
    }

    return details;
}

async function getCcdGwServiceToken() {
    const oneTimePassword = TOTP.generate(testConfig.TestCcdGwSecret, { digits: 6, period: 30 }).otp;
    const response = await axios.post(
        s2sLeaseUrl,
        {
            microservice: 'ccd_gw',
            oneTimePassword
        },
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );

    return response.data;
}

async function getAuthContext() {
    const authToken = await idamApi.getUserToken();
    const userId = await idamApi.getUserId(authToken);
    const serviceToken = await getCcdGwServiceToken();

    return {
        authToken,
        serviceToken,
        userId
    };
}

function loadManifest(manifestFile) {
    if (!fs.existsSync(manifestFile)) {
        throw new Error(`Cutover seed manifest not found: ${manifestFile}`);
    }

    const manifest = JSON.parse(fs.readFileSync(manifestFile, 'utf8'));

    if (!Array.isArray(manifest.cases)) {
        throw new Error(`Cutover seed manifest does not contain a cases array: ${manifestFile}`);
    }

    if (process.env.CUTOVER_VERIFY_REQUIRE_COMPLETE_SEED !== 'false' && manifest.status !== 'complete') {
        throw new Error(
            `Cutover seed manifest status is ${manifest.status}; set CUTOVER_VERIFY_REQUIRE_COMPLETE_SEED=false `
            + 'to verify a partial manifest.'
        );
    }

    return manifest;
}

function buildProfileLookup() {
    return new Map(seedProfiles.map(profile => [profile.seedId, profile]));
}

function getCasesToVerify(manifest) {
    const selectedProfileIds = getSelectedProfileIds();
    const selectedProfileIdSet = new Set(selectedProfileIds);
    const profileLookup = buildProfileLookup();
    const cases = manifest.cases
        .filter(seededCase => selectedProfileIds.length === 0 || selectedProfileIdSet.has(seededCase.seedId))
        .map(seededCase => ({
            ...(profileLookup.get(seededCase.seedId) || {}),
            ...seededCase
        }));
    const missingProfileIds = selectedProfileIds.filter(
        profileId => !cases.some(seededCase => seededCase.seedId === profileId)
    );

    if (missingProfileIds.length > 0) {
        throw new Error(`CUTOVER_VERIFY_PROFILE_IDS not found in manifest: ${missingProfileIds.join(', ')}`);
    }

    return cases;
}

async function fetchCaseDetails(context, caseId) {
    const caseUrl = `${ccdApiUrl}/caseworkers/${context.userId}/jurisdictions/${jurisdiction}/case-types/${caseTypeId}`
        + `/cases/${caseId}`;
    const response = await axios.get(caseUrl, {
        headers: buildHeaders(context)
    });
    const payload = response.data || {};
    const caseData = payload.case_data || payload.data || {};
    const observedState = payload.state || payload.case_state || null;

    if (!observedState) {
        throw new Error(`Case details response for ${caseId} did not include a state`);
    }

    return {
        observedState,
        ethosCaseReference: caseData.ethosCaseReference || caseData.ethos_Ref || null
    };
}

async function initiateVerificationEvent(context, caseId, eventId) {
    const eventUrl = `${ccdApiUrl}/caseworkers/${context.userId}/jurisdictions/${jurisdiction}/case-types/${caseTypeId}`
        + `/cases/${caseId}/event-triggers/${eventId}/token`;
    const response = await axios.get(eventUrl, {
        headers: buildHeaders(context, { experimental: true })
    });
    const payload = response.data || {};

    if (!payload.token) {
        throw new Error(`Event trigger response for ${caseId}/${eventId} did not include a token`);
    }

    return {
        tokenReceived: true,
        eventId,
        responseCaseState: payload.case_details ? payload.case_details.state || null : null,
        returnedCaseData: Boolean(payload.case_details && payload.case_details.case_data)
    };
}

async function verifySeededCase(context, seededCase) {
    const startedAt = new Date().toISOString();

    if (seededCase.status && seededCase.status !== 'seeded') {
        return {
            seedId: seededCase.seedId,
            caseId: seededCase.caseId,
            controllerCategory: seededCase.controllerCategory,
            status: 'skipped',
            reason: `Seed profile status was ${seededCase.status}`,
            startedAt,
            completedAt: new Date().toISOString()
        };
    }

    if (!seededCase.caseId) {
        throw new Error(`Seed profile ${seededCase.seedId} does not contain a caseId`);
    }

    if (!seededCase.verifyEventId) {
        throw new Error(`Seed profile ${seededCase.seedId} does not contain a verifyEventId`);
    }

    const expectedState = seededCase.expectedState || seededCase.targetState;
    const caseDetails = await fetchCaseDetails(context, seededCase.caseId);

    if (expectedState && caseDetails.observedState !== expectedState) {
        throw new Error(
            `Case ${seededCase.caseId} state mismatch: expected ${expectedState}, observed ${caseDetails.observedState}`
        );
    }

    let eventTrigger = null;

    if (process.env.CUTOVER_VERIFY_SKIP_EVENT_TRIGGERS !== 'true') {
        eventTrigger = await initiateVerificationEvent(context, seededCase.caseId, seededCase.verifyEventId);
    }

    return {
        seedId: seededCase.seedId,
        caseId: seededCase.caseId,
        controllerCategory: seededCase.controllerCategory,
        status: 'verified',
        expectedState,
        observedState: caseDetails.observedState,
        ethosCaseReference: caseDetails.ethosCaseReference || seededCase.ethosCaseReference || null,
        verifyEventId: seededCase.verifyEventId,
        eventTrigger,
        scenarioRefs: seededCase.scenarioRefs || [],
        startedAt,
        completedAt: new Date().toISOString()
    };
}

function buildReport(manifestFile, outputFile, manifest, results, status) {
    return {
        reportVersion: 1,
        generatedAt: new Date().toISOString(),
        status,
        environment: env,
        testUrl: testConfig.TestUrl,
        ccdApiUrl,
        previewServiceFqdn: getPreviewServiceFqdn() || null,
        manifestFile,
        outputFile,
        seedManifestGeneratedAt: manifest.generatedAt || null,
        requestedCaseCount: results.length,
        verifiedCaseCount: results.filter(result => result.status === 'verified').length,
        failedCaseCount: results.filter(result => result.status === 'failed').length,
        skippedCaseCount: results.filter(result => result.status === 'skipped').length,
        results
    };
}

function writeReport(outputFile, manifestFile, manifest, results, status) {
    fs.writeFileSync(
        outputFile,
        JSON.stringify(buildReport(manifestFile, outputFile, manifest, results, status), null, 2)
    );
}

async function main() {
    const manifestFile = getManifestFile();
    const outputFile = getOutputFile();
    const manifest = loadManifest(manifestFile);
    const cases = getCasesToVerify(manifest);

    fs.mkdirSync(path.dirname(outputFile), { recursive: true });

    logger.info(`Verifying ${cases.length} cutover seed cases from ${manifestFile}`);
    logger.info(`Writing cutover verification report to ${outputFile}`);
    logger.info(`Using CCD data store URL ${ccdApiUrl}`);

    const context = await getAuthContext();
    const results = [];
    writeReport(outputFile, manifestFile, manifest, results, 'in-progress');

    for (const seededCase of cases) {
        try {
            const result = await verifySeededCase(context, seededCase);
            results.push(result);
            logger.info(`Verified ${seededCase.seedId} => ${seededCase.caseId}`);
        } catch (error) {
            results.push({
                seedId: seededCase.seedId,
                caseId: seededCase.caseId || null,
                controllerCategory: seededCase.controllerCategory,
                status: 'failed',
                expectedState: seededCase.expectedState || seededCase.targetState || null,
                observedState: null,
                verifyEventId: seededCase.verifyEventId || null,
                scenarioRefs: seededCase.scenarioRefs || [],
                error: getErrorDetails(error),
                completedAt: new Date().toISOString()
            });
            logger.error(`Failed ${seededCase.seedId}: ${error.message}`);
        }

        writeReport(outputFile, manifestFile, manifest, results, 'in-progress');
    }

    const failedCaseCount = results.filter(result => result.status === 'failed').length;
    const finalStatus = failedCaseCount === 0 ? 'complete' : 'failed';
    writeReport(outputFile, manifestFile, manifest, results, finalStatus);

    if (failedCaseCount > 0) {
        throw new Error(`Cutover verification failed for ${failedCaseCount} case(s); report written to ${outputFile}`);
    }

    console.log(`Cutover verification report written to ${outputFile}`);
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
