const path = require('path');
const axios = require('axios');
const { Logger } = require('@hmcts/nodejs-logging');

const {
    buildHeaders,
    caseMatchesReference,
    env,
    findCasesArray,
    firstPresent,
    getAuthContext,
    getCcdDataStoreUrl,
    getErrorDetails,
    getManifestFile,
    getPreviewServiceFqdn,
    getSeededCases,
    loadManifest,
    pollUntil,
    stripTrailingSlash,
    testConfig,
    writeJsonReport
} = require('./cutoverProbeSupport');

const logger = Logger.getLogger('cutover/verifyCutoverSearch.js');
const caseTypeId = 'ET_EnglandWales';
const defaultOutputFile = path.resolve(process.cwd(), 'functional-output/cutover/search-report.json');

function getOutputFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_SEARCH_OUTPUT_FILE || defaultOutputFile);
}

function getAttempts() {
    return parseInt(process.env.CUTOVER_SEARCH_ATTEMPTS || '20', 10);
}

function getIntervalMs() {
    return parseInt(process.env.CUTOVER_SEARCH_INTERVAL_MS || '15000', 10);
}

function getElasticsearchUrl() {
    return process.env.CUTOVER_ELASTICSEARCH_URL
        ? stripTrailingSlash(process.env.CUTOVER_ELASTICSEARCH_URL)
        : '';
}

function buildReferenceQuery(caseId) {
    return {
        size: 10,
        query: {
            bool: {
                filter: [
                    {
                        terms: {
                            'reference.keyword': [String(caseId)]
                        }
                    }
                ]
            }
        }
    };
}

function buildEthosReferenceQuery(ethosCaseReference) {
    return {
        size: 10,
        query: {
            bool: {
                filter: [
                    {
                        term: {
                            'data.ethosCaseReference.keyword': ethosCaseReference
                        }
                    }
                ]
            }
        }
    };
}

function extractSearchCases(payload) {
    return findCasesArray(payload).map(result => result.case_data ? result : result.caseDetails || result);
}

async function searchCcd(context, query) {
    const response = await axios.post(
        `${getCcdDataStoreUrl()}/searchCases?ctid=${caseTypeId}`,
        query,
        {
            headers: buildHeaders(context)
        }
    );

    return extractSearchCases(response.data || {});
}

async function pollCcdReference(context, seededCase) {
    return pollUntil(async attempt => {
        try {
            const cases = await searchCcd(context, buildReferenceQuery(seededCase.caseId));
            const match = cases.find(candidate => caseMatchesReference(candidate, seededCase.caseId));

            return {
                ok: Boolean(match),
                attempt,
                matchCount: cases.length,
                matchedCaseId: match ? firstPresent(match.id, match.case_id, match.caseId, match.reference) : null
            };
        } catch (error) {
            return {
                ok: false,
                attempt,
                error: getErrorDetails(error)
            };
        }
    }, { attempts: getAttempts(), intervalMs: getIntervalMs() });
}

async function pollCcdEthosReference(context, seededCase) {
    if (!seededCase.ethosCaseReference) {
        return {
            ok: true,
            skipped: true,
            reason: 'Seeded case has no ethosCaseReference'
        };
    }

    return pollUntil(async attempt => {
        try {
            const cases = await searchCcd(context, buildEthosReferenceQuery(seededCase.ethosCaseReference));
            const match = cases.find(candidate => caseMatchesReference(candidate, seededCase.caseId));

            return {
                ok: Boolean(match),
                attempt,
                matchCount: cases.length,
                ethosCaseReference: seededCase.ethosCaseReference,
                matchedCaseId: match ? firstPresent(match.id, match.case_id, match.caseId, match.reference) : null
            };
        } catch (error) {
            return {
                ok: false,
                attempt,
                error: getErrorDetails(error)
            };
        }
    }, { attempts: getAttempts(), intervalMs: getIntervalMs() });
}

function buildRawEsQuery(caseId) {
    return {
        size: 10,
        query: {
            bool: {
                should: [
                    { term: { 'reference': Number(caseId) } },
                    { term: { 'reference.keyword': String(caseId) } },
                    { term: { '_id': String(caseId) } }
                ],
                minimum_should_match: 1
            }
        }
    };
}

async function pollRawElasticsearch(indexName, caseId) {
    const elasticsearchUrl = getElasticsearchUrl();

    if (!elasticsearchUrl) {
        return {
            ok: true,
            skipped: true,
            reason: 'CUTOVER_ELASTICSEARCH_URL not set'
        };
    }

    return pollUntil(async attempt => {
        try {
            const response = await axios.post(
                `${elasticsearchUrl}/${indexName}/_search`,
                buildRawEsQuery(caseId),
                {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            );
            const hits = response.data?.hits?.hits || [];

            return {
                ok: hits.length > 0,
                attempt,
                indexName,
                hitCount: hits.length,
                matchedIds: hits.map(hit => hit._id)
            };
        } catch (error) {
            return {
                ok: false,
                attempt,
                indexName,
                error: getErrorDetails(error)
            };
        }
    }, { attempts: getAttempts(), intervalMs: getIntervalMs() });
}

async function verifySeededCase(context, seededCase) {
    const ccdReferenceSearch = await pollCcdReference(context, seededCase);
    const ccdEthosReferenceSearch = await pollCcdEthosReference(context, seededCase);
    const rawCaseIndexSearch = await pollRawElasticsearch('et_englandwales_cases', seededCase.caseId);
    const rawGlobalSearch = await pollRawElasticsearch('global_search', seededCase.caseId);
    const checks = {
        ccdReferenceSearch,
        ccdEthosReferenceSearch,
        rawCaseIndexSearch,
        rawGlobalSearch
    };
    const failures = [];

    if (!ccdReferenceSearch.ok) {
        failures.push('ccdReferenceSearch');
    }

    if (!ccdEthosReferenceSearch.ok) {
        failures.push('ccdEthosReferenceSearch');
    }

    if (process.env.CUTOVER_SEARCH_REQUIRE_ELASTICSEARCH === 'true' && !rawCaseIndexSearch.ok) {
        failures.push('rawCaseIndexSearch');
    }

    if (process.env.CUTOVER_SEARCH_REQUIRE_GLOBAL === 'true' && !rawGlobalSearch.ok) {
        failures.push('rawGlobalSearch');
    }

    return {
        seedId: seededCase.seedId,
        controllerCategory: seededCase.controllerCategory,
        caseId: seededCase.caseId,
        ethosCaseReference: seededCase.ethosCaseReference || null,
        status: failures.length === 0 ? 'verified' : 'failed',
        failures,
        checks
    };
}

function buildReport({ manifestFile, outputFile, manifest, results }) {
    const failedCaseCount = results.filter(result => result.status === 'failed').length;

    return {
        reportVersion: 1,
        generatedAt: new Date().toISOString(),
        status: failedCaseCount === 0 ? 'complete' : 'failed',
        environment: env,
        testUrl: testConfig.TestUrl,
        ccdApiUrl: getCcdDataStoreUrl(),
        elasticsearchUrl: getElasticsearchUrl() || null,
        previewServiceFqdn: getPreviewServiceFqdn() || null,
        manifestFile,
        outputFile,
        seedManifestGeneratedAt: manifest.generatedAt || null,
        requestedCaseCount: results.length,
        verifiedCaseCount: results.filter(result => result.status === 'verified').length,
        failedCaseCount,
        results
    };
}

async function main() {
    const manifestFile = getManifestFile('CUTOVER_SEARCH_MANIFEST_FILE');
    const outputFile = getOutputFile();
    const manifest = loadManifest(manifestFile);
    const cases = getSeededCases(manifest, 'CUTOVER_SEARCH_PROFILE_IDS');

    if (cases.length === 0) {
        throw new Error(`No seeded cases found in manifest: ${manifestFile}`);
    }

    logger.info(`Checking search/indexing for ${cases.length} cutover case(s)`);
    logger.info(`Writing cutover search report to ${outputFile}`);

    const context = await getAuthContext();
    const results = [];

    writeJsonReport(outputFile, buildReport({ manifestFile, outputFile, manifest, results }));

    for (const seededCase of cases) {
        const result = await verifySeededCase(context, seededCase);
        results.push(result);
        logger.info(`Search check ${result.status}: ${seededCase.seedId} => ${seededCase.caseId}`);
        writeJsonReport(outputFile, buildReport({ manifestFile, outputFile, manifest, results }));
    }

    const report = buildReport({ manifestFile, outputFile, manifest, results });
    writeJsonReport(outputFile, report);

    if (report.failedCaseCount > 0) {
        throw new Error(`Cutover search verification failed for ${report.failedCaseCount} case(s); `
            + `report written to ${outputFile}`);
    }

    console.log(`Cutover search report written to ${outputFile}`);
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
