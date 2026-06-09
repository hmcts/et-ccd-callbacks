const fs = require('fs');
const path = require('path');
const axios = require('axios');
const totp = require('totp-generator');

const testConfig = require('../../config.js');
const idamApi = require('../helpers/idamApi');
const { getCcdApiUrl, getPreviewServiceFqdn, getS2sLeaseUrl } = require('./cutoverConfig');

const env = testConfig.TestEnv;
const defaultManifestFile = path.resolve(process.cwd(), 'functional-output/cutover/seed-manifest.json');

function firstPresent(...values) {
    return values.find(value => value !== undefined && value !== null && value !== '');
}

function stripTrailingSlash(value) {
    return value.replace(/\/+$/, '');
}

function getManifestFile(envVarName) {
    return path.resolve(process.cwd(), process.env[envVarName] || defaultManifestFile);
}

function loadManifest(manifestFile) {
    if (!fs.existsSync(manifestFile)) {
        throw new Error(`Cutover seed manifest not found: ${manifestFile}`);
    }

    const manifest = JSON.parse(fs.readFileSync(manifestFile, 'utf8'));

    if (!Array.isArray(manifest.cases)) {
        throw new Error(`Cutover seed manifest does not contain a cases array: ${manifestFile}`);
    }

    return manifest;
}

function getSelectedIds(envVarName) {
    return process.env[envVarName]
        ? process.env[envVarName].split(',').map(profile => profile.trim()).filter(Boolean)
        : [];
}

function getSeededCases(manifest, selectedIdsEnvVar) {
    const selectedIds = getSelectedIds(selectedIdsEnvVar);
    const selectedIdSet = new Set(selectedIds);
    const cases = manifest.cases.filter(seededCase => {
        if (seededCase.status && seededCase.status !== 'seeded') {
            return false;
        }

        return selectedIds.length === 0 || selectedIdSet.has(seededCase.seedId);
    });
    const missingIds = selectedIds.filter(profileId => !cases.some(seededCase => seededCase.seedId === profileId));

    if (missingIds.length > 0) {
        throw new Error(`${selectedIdsEnvVar} not found in manifest: ${missingIds.join(', ')}`);
    }

    return cases;
}

function writeJsonReport(outputFile, report) {
    fs.mkdirSync(path.dirname(outputFile), { recursive: true });
    fs.writeFileSync(outputFile, JSON.stringify(report, null, 2));
}

function buildHeaders(context, additionalHeaders = {}) {
    return {
        Authorization: `Bearer ${context.authToken}`,
        ServiceAuthorization: `Bearer ${context.serviceToken}`,
        'Content-Type': 'application/json',
        ...additionalHeaders
    };
}

async function getCcdGwServiceToken() {
    const oneTimePassword = totp(testConfig.TestCcdGwSecret, { digits: 6, period: 30 });
    const response = await axios.post(
        getS2sLeaseUrl(env),
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

function getCcdDataStoreUrl() {
    return getCcdApiUrl(env);
}

function getPreviewUrl(prefix, envVarName) {
    const configuredUrl = process.env[envVarName];

    if (configuredUrl) {
        return stripTrailingSlash(configuredUrl);
    }

    const previewServiceFqdn = getPreviewServiceFqdn();

    return previewServiceFqdn ? `https://${prefix}-${previewServiceFqdn}` : '';
}

function findCaseData(payload) {
    return payload.case_data || payload.data || payload.caseDetails?.case_data || payload.caseDetails?.data || {};
}

function findCaseDetails(payload) {
    return payload.caseDetails || payload.case_details || payload;
}

function findCasesArray(payload) {
    if (Array.isArray(payload)) {
        return payload;
    }

    if (Array.isArray(payload.cases)) {
        return payload.cases;
    }

    if (Array.isArray(payload.case_details)) {
        return payload.case_details;
    }

    if (Array.isArray(payload.results)) {
        return payload.results;
    }

    if (Array.isArray(payload.data)) {
        return payload.data;
    }

    return [];
}

function getCaseId(candidate) {
    return firstPresent(
        candidate.id,
        candidate.case_id,
        candidate.caseId,
        candidate.reference,
        candidate.case_reference
    );
}

function caseMatchesReference(candidate, caseId) {
    const candidateId = getCaseId(candidate);

    return String(candidateId) === String(caseId);
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

async function sleep(ms) {
    await new Promise(resolve => setTimeout(resolve, ms));
}

async function pollUntil(fn, { attempts, intervalMs }) {
    let lastResult = null;

    for (let attempt = 1; attempt <= attempts; attempt += 1) {
        lastResult = await fn(attempt);

        if (lastResult && lastResult.ok) {
            return lastResult;
        }

        if (attempt < attempts) {
            await sleep(intervalMs);
        }
    }

    return lastResult;
}

module.exports = {
    buildHeaders,
    caseMatchesReference,
    env,
    findCaseData,
    findCaseDetails,
    findCasesArray,
    firstPresent,
    getAuthContext,
    getCcdDataStoreUrl,
    getCaseId,
    getErrorDetails,
    getManifestFile,
    getPreviewServiceFqdn,
    getPreviewUrl,
    getSeededCases,
    loadManifest,
    pollUntil,
    stripTrailingSlash,
    testConfig,
    writeJsonReport
};
