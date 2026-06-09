const fs = require('fs');
const path = require('path');
const axios = require('axios');
const { randomUUID } = require('crypto');
const { Logger } = require('@hmcts/nodejs-logging');
const totp = require('totp-generator');

const testConfig = require('../../config.js');
const idamApi = require('../helpers/idamApi');
const seedData = require('../data/et-ccd-basic-data.json');
const { seedProfiles } = require('./seedProfiles');
const { getCcdApiUrl, getPreviewServiceFqdn, getS2sLeaseUrl } = require('./cutoverConfig');

const logger = Logger.getLogger('cutover/seedCutoverCases.js');
const env = testConfig.TestEnv;
const jurisdiction = 'EMPLOYMENT';
const caseTypeId = 'ET_EnglandWales';
const ccdApiUrl = getCcdApiUrl(env);
const s2sLeaseUrl = getS2sLeaseUrl(env);
const defaultOutputFile = path.resolve(process.cwd(), 'functional-output/cutover/seed-manifest.json');

function getOutputFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_SEED_OUTPUT_FILE || defaultOutputFile);
}

function getSelectedProfiles() {
    const selectedIds = process.env.CUTOVER_SEED_PROFILE_IDS
        ? process.env.CUTOVER_SEED_PROFILE_IDS.split(',').map(profile => profile.trim()).filter(Boolean)
        : [];

    if (selectedIds.length === 0) {
        return seedProfiles;
    }

    const selectedProfiles = seedProfiles.filter(profile => selectedIds.includes(profile.seedId));
    const missingProfiles = selectedIds.filter(
        profileId => !selectedProfiles.some(profile => profile.seedId === profileId)
    );

    if (missingProfiles.length > 0) {
        throw new Error(`Unknown CUTOVER_SEED_PROFILE_IDS: ${missingProfiles.join(', ')}`);
    }

    return selectedProfiles;
}

function buildHeaders(context, additionalHeaders = {}) {
    return {
        Authorization: `Bearer ${context.authToken}`,
        ServiceAuthorization: `Bearer ${context.serviceToken}`,
        'Content-Type': 'application/json',
        ...additionalHeaders
    };
}

function nextBusinessDayIso() {
    const date = new Date(new Date().setUTCHours(0, 0, 0, 0));

    if (date.getDay() === 0) {
        date.setDate(date.getDate() + 1);
    } else if (date.getDay() === 6) {
        date.setDate(date.getDate() + 2);
    }

    return date.toISOString().slice(0, -1);
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

function buildManifest(outputFile, profiles, cases, status, error = null) {
    return {
        manifestVersion: 1,
        generatedAt: new Date().toISOString(),
        status,
        environment: env,
        testUrl: testConfig.TestUrl,
        ccdApiUrl,
        previewServiceFqdn: getPreviewServiceFqdn() || null,
        outputFile,
        selectedProfileIds: profiles.map(profile => profile.seedId),
        requestedCaseCount: profiles.length,
        caseCount: cases.length,
        successfulCaseCount: cases.filter(seededCase => seededCase.status === 'seeded').length,
        failedCaseCount: cases.filter(seededCase => seededCase.status === 'failed').length,
        error,
        cases
    };
}

function writeManifest(outputFile, profiles, cases, status, error = null) {
    fs.writeFileSync(
        outputFile,
        JSON.stringify(buildManifest(outputFile, profiles, cases, status, error), null, 2)
    );
}

async function getCcdGwServiceToken() {
    const oneTimePassword = totp(testConfig.TestCcdGwSecret, { digits: 6, period: 30 });
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

async function createCase(context) {
    const initiateUrl = `${ccdApiUrl}/caseworkers/${context.userId}/jurisdictions/${jurisdiction}`
        + `/case-types/${caseTypeId}/event-triggers/initiateCase/token`;
    const initiateResponse = await axios.get(initiateUrl, {
        headers: buildHeaders(context)
    });
    const createUrl = `${ccdApiUrl}/caseworkers/${context.userId}/jurisdictions/${jurisdiction}`
        + `/case-types/${caseTypeId}/cases?ignore-warning=false`;
    const createPayload = {
        data: seedData.data,
        event: {
            id: 'initiateCase',
            summary: 'Creating cutover seed case',
            description: 'Cutover seed case'
        },
        event_token: initiateResponse.data.token
    };
    const createResponse = await axios.post(createUrl, createPayload, {
        headers: buildHeaders(context)
    });

    return createResponse.data.id;
}

async function initiateCaseEvent(context, caseId, eventId, useCaseworkerPath = false) {
    const initiateUrl = useCaseworkerPath
        ? `${ccdApiUrl}/caseworkers/${context.userId}/jurisdictions/${jurisdiction}/case-types/${caseTypeId}`
            + `/cases/${caseId}/event-triggers/${eventId}/token`
        : `${ccdApiUrl}/cases/${caseId}/event-triggers/${eventId}?ignore-warning=false`;

    const response = await axios.get(initiateUrl, {
        headers: buildHeaders(context, { experimental: true })
    });

    return response.data;
}

async function submitCaseEvent(context, caseId, eventId, payload, useCaseworkerPath = false) {
    const submitUrl = useCaseworkerPath
        ? `${ccdApiUrl}/caseworkers/${context.userId}/jurisdictions/${jurisdiction}/case-types/${caseTypeId}`
            + `/cases/${caseId}/events`
        : `${ccdApiUrl}/cases/${caseId}/events`;

    const response = await axios.post(
        submitUrl,
        {
            event: {
                id: eventId,
                summary: '',
                description: ''
            },
            ...payload
        },
        {
            headers: buildHeaders(context, { experimental: true })
        }
    );

    return response.data;
}

async function performVetting(context, caseId) {
    const initiation = await initiateCaseEvent(context, caseId, 'et1Vetting');

    await submitCaseEvent(context, caseId, 'et1Vetting', {
        data: initiation.case_details.case_data,
        data_classification: initiation.case_details.data_classification,
        event_token: initiation.token,
        ignore_warning: false,
        draft_id: null
    });
}

async function acceptCase(context, caseId) {
    const initiation = await initiateCaseEvent(context, caseId, 'preAcceptanceCase');

    await submitCaseEvent(context, caseId, 'preAcceptanceCase', {
        data: {
            preAcceptCase: {
                caseAccepted: 'Yes',
                dateAccepted: '2022-08-18'
            }
        },
        event_token: initiation.token
    });
}

async function rejectCase(context, caseId) {
    const initiation = await initiateCaseEvent(context, caseId, 'preAcceptanceCase');

    await submitCaseEvent(context, caseId, 'preAcceptanceCase', {
        data: {
            preAcceptCase: {
                caseAccepted: 'No',
                dateAccepted: null,
                dateRejected: '2022-01-23',
                rejectReason: ['Not on Prescribed Form']
            }
        },
        event_token: initiation.token
    });
}

async function listCase(context, caseId) {
    const initiation = await initiateCaseEvent(context, caseId, 'addAmendHearing', true);

    await submitCaseEvent(context, caseId, 'addAmendHearing', {
        data: {
            hearingCollection: [
                {
                    id: randomUUID(),
                    value: {
                        hearingNumber: '1',
                        Hearing_type: 'Preliminary Hearing',
                        hearingPublicPrivate: 'Public',
                        judicialMediation: 'Yes',
                        Hearing_venue: {
                            value: {
                                code: 'Hull Combined Court Centre',
                                label: 'Hull Combined Court Centre'
                            },
                            selectedLabel: 'Hull Combined Court Centre',
                            selectedCode: 'Hull Combined Court Centre',
                            list_items: [
                                { code: 'Harrogate CJC', label: 'Harrogate CJC' },
                                { code: 'Hull', label: 'Hull' },
                                { code: 'Hull Combined Court Centre', label: 'Hull Combined Court Centre' },
                                { code: 'IAC', label: 'IAC' },
                                { code: 'Leeds', label: 'Leeds' },
                                { code: 'Scarborough', label: 'Scarborough' },
                                { code: 'Sheffield Combi', label: 'Sheffield Combined Court' },
                                { code: 'Teesside ET', label: 'Teesside ET' },
                                { code: 'Wakefield Count', label: 'Wakefield Civil and Family Justice Centre' }
                            ]
                        },
                        hearingEstLengthNum: '1',
                        hearingEstLengthNumType: 'Days',
                        hearingSitAlone: 'Full Panel',
                        Hearing_stage: null,
                        Hearing_notes: null,
                        hearingShowDetails: null,
                        judge: null,
                        hearingERMember: null,
                        hearingEEMember: null,
                        hearingFormat: ['In person'],
                        hearingDateCollection: [
                            {
                                id: randomUUID(),
                                value: {
                                    listedDate: nextBusinessDayIso(),
                                    Hearing_status: null,
                                    Postponed_by: null,
                                    postponedDate: null,
                                    hearingVenueDay: null,
                                    hearingRoom: null,
                                    hearingClerk: null,
                                    hearingCaseDisposed: null,
                                    Hearing_part_heard: null,
                                    Hearing_reserved_judgement: null,
                                    attendee_claimant: null,
                                    attendee_non_attendees: null,
                                    attendee_resp_no_rep: null,
                                    'attendee_resp_&_rep': null,
                                    attendee_rep_only: null,
                                    hearingTimingStart: null,
                                    hearingTimingBreak: null,
                                    hearingTimingResume: null,
                                    hearingTimingFinish: null,
                                    hearingTimingDuration: null,
                                    HearingNotes2: null
                                }
                            }
                        ]
                    }
                }
            ]
        },
        event_token: initiation.token,
        ignore_warning: false
    }, true);
}

async function addJurisdiction(context, caseId) {
    const initiation = await initiateCaseEvent(context, caseId, 'addAmendJurisdiction', true);

    await submitCaseEvent(context, caseId, 'addAmendJurisdiction', {
        data: {
            jurCodesCollection: [
                {
                    id: randomUUID(),
                    value: {
                        juridictionCodesList: 'ADT',
                        judgmentOutcome: 'Input in error',
                        dateNotified: null,
                        disposalDate: null
                    }
                }
            ]
        },
        event_token: initiation.token,
        ignore_warning: false
    }, true);
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

async function applyTransitions(context, caseId, transitions) {
    for (const transition of transitions) {
        if (transition === 'vet') {
            await performVetting(context, caseId);
            continue;
        }

        if (transition === 'accept') {
            await acceptCase(context, caseId);
            continue;
        }

        if (transition === 'reject') {
            await rejectCase(context, caseId);
            continue;
        }

        if (transition === 'list') {
            await listCase(context, caseId);
            continue;
        }

        if (transition === 'jurisdiction') {
            await addJurisdiction(context, caseId);
            continue;
        }

        throw new Error(`Unsupported transition: ${transition}`);
    }
}

async function seedProfile(context, profile) {
    logger.info(`Seeding ${profile.seedId}`);
    const seededAt = new Date().toISOString();
    let caseId = null;

    try {
        caseId = await createCase(context);
        await applyTransitions(context, caseId, profile.transitions);
        const caseDetails = await fetchCaseDetails(context, caseId);

        return {
            ...profile,
            status: 'seeded',
            caseId,
            ethosCaseReference: caseDetails.ethosCaseReference,
            expectedState: profile.targetState,
            observedState: caseDetails.observedState,
            seededAt
        };
    } catch (error) {
        return {
            ...profile,
            status: 'failed',
            caseId,
            expectedState: profile.targetState,
            observedState: null,
            seededAt,
            failedAt: new Date().toISOString(),
            error: getErrorDetails(error)
        };
    }
}

async function main() {
    const outputFile = getOutputFile();
    const profiles = getSelectedProfiles();

    fs.mkdirSync(path.dirname(outputFile), { recursive: true });

    logger.info(`Writing cutover seed manifest to ${outputFile}`);
    logger.info(`Seeding ${profiles.length} cutover cases in ${env}`);
    logger.info(`Using CCD data store URL ${ccdApiUrl}`);

    const context = await getAuthContext();
    const cases = [];
    writeManifest(outputFile, profiles, cases, 'in-progress');

    for (const profile of profiles) {
        const seededCase = await seedProfile(context, profile);
        cases.push(seededCase);
        writeManifest(outputFile, profiles, cases, 'in-progress');

        if (seededCase.status === 'failed') {
            writeManifest(outputFile, profiles, cases, 'failed', seededCase.error);
            throw new Error(`Failed to seed ${profile.seedId}; partial manifest written to ${outputFile}`);
        }

        logger.info(`Seeded ${profile.seedId} => ${seededCase.caseId}`);
    }

    writeManifest(outputFile, profiles, cases, 'complete');
    console.log(`Cutover seed manifest written to ${outputFile}`);
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
