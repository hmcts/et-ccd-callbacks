const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const { Logger } = require('@hmcts/nodejs-logging');

const testConfig = require('../../config.js');
const { getPreviewServiceFqdn } = require('./cutoverConfig');

const logger = Logger.getLogger('cutover/checkCutoverDbData.js');
const defaultManifestFile = path.resolve(process.cwd(), 'functional-output/cutover/seed-manifest.json');
const defaultOutputFile = path.resolve(process.cwd(), 'functional-output/cutover/db-check-report.json');

function firstPresent(...values) {
    return values.find(value => value !== undefined && value !== null && value !== '');
}

function stripLeadingQuestionMark(value) {
    return value ? value.replace(/^\?/, '') : '';
}

function getPrId() {
    const previewServiceFqdn = getPreviewServiceFqdn();
    const previewPrId = previewServiceFqdn.match(/(?:^|-)pr-(\d+)\./);

    return firstPresent(
        process.env.CUTOVER_PREVIEW_PR_ID,
        process.env.CHANGE_ID,
        process.env.PR_ID,
        previewPrId ? previewPrId[1] : ''
    );
}

function getManifestFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_DB_CHECK_MANIFEST_FILE || defaultManifestFile);
}

function getOutputFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_DB_CHECK_OUTPUT_FILE || defaultOutputFile);
}

function buildConnectionUri({ url, user, host, port, database, connOptions }) {
    if (url) {
        return url;
    }

    if (!user || !host || !port || !database) {
        return '';
    }

    const normalisedOptions = stripLeadingQuestionMark(connOptions);
    const queryString = normalisedOptions ? `?${normalisedOptions}` : '';

    return `postgresql://${user}@${host}:${port}/${database}${queryString}`;
}

function getCcdDbConfig() {
    const prId = getPrId();
    const password = firstPresent(
        process.env.CUTOVER_CCD_DB_PASSWORD,
        process.env.CCD_DATA_STORE_DB_PASSWORD,
        process.env.DATA_STORE_DB_PASSWORD,
        process.env.ET_PREVIEW_FLEXI_DB_PASSWORD,
        process.env.POSTGRES_PASSWORD
    );

    return {
        name: 'ccd',
        password,
        uri: buildConnectionUri({
            url: process.env.CUTOVER_CCD_DB_URL,
            user: firstPresent(
                process.env.CUTOVER_CCD_DB_USER_NAME,
                process.env.CCD_DATA_STORE_DB_USER_NAME,
                process.env.DATA_STORE_DB_USERNAME,
                'hmcts'
            ),
            host: firstPresent(
                process.env.CUTOVER_CCD_DB_HOST,
                process.env.CCD_DATA_STORE_DB_HOST,
                process.env.DATA_STORE_DB_HOST,
                prId ? 'et-preview.postgres.database.azure.com' : ''
            ),
            port: firstPresent(
                process.env.CUTOVER_CCD_DB_PORT,
                process.env.CCD_DATA_STORE_DB_PORT,
                process.env.DATA_STORE_DB_PORT,
                '5432'
            ),
            database: firstPresent(
                process.env.CUTOVER_CCD_DB_NAME,
                process.env.CCD_DATA_STORE_DB_NAME,
                process.env.DATA_STORE_DB_NAME,
                prId ? `pr-${prId}-data-store` : ''
            ),
            connOptions: firstPresent(
                process.env.CUTOVER_CCD_DB_CONN_OPTIONS,
                process.env.CCD_DATA_STORE_DB_CONN_OPTIONS,
                process.env.DATA_STORE_DB_OPTIONS,
                'sslmode=require'
            )
        })
    };
}

function getEtDbConfig() {
    const prId = getPrId();
    const password = firstPresent(
        process.env.CUTOVER_ET_DB_PASSWORD,
        process.env.ET_COS_PREVIEW_DB_PASSWORD,
        process.env.ET_COS_DB_PASSWORD,
        process.env.ET_PREVIEW_FLEXI_DB_PASSWORD,
        process.env.POSTGRES_PASSWORD
    );

    return {
        name: 'et',
        password,
        uri: buildConnectionUri({
            url: process.env.CUTOVER_ET_DB_URL,
            user: firstPresent(
                process.env.CUTOVER_ET_DB_USER_NAME,
                process.env.ET_COS_PREVIEW_DB_USER_NAME,
                process.env.ET_COS_DB_USER_NAME,
                'hmcts'
            ),
            host: firstPresent(
                process.env.CUTOVER_ET_DB_HOST,
                process.env.ET_COS_PREVIEW_DB_HOST,
                process.env.ET_COS_DB_HOST,
                prId ? 'et-preview.postgres.database.azure.com' : ''
            ),
            port: firstPresent(
                process.env.CUTOVER_ET_DB_PORT,
                process.env.ET_COS_PREVIEW_DB_PORT,
                process.env.ET_COS_DB_PORT,
                '5432'
            ),
            database: firstPresent(
                process.env.CUTOVER_ET_DB_NAME,
                process.env.ET_COS_PREVIEW_DB_NAME,
                process.env.ET_COS_DB_NAME,
                prId ? `pr-${prId}-et_cos` : ''
            ),
            connOptions: firstPresent(
                process.env.CUTOVER_ET_DB_CONN_OPTIONS,
                process.env.ET_COS_PREVIEW_DB_CONN_OPTIONS,
                process.env.ET_COS_DB_CONN_OPTIONS,
                'sslmode=require'
            )
        })
    };
}

function validateDbConfig(config) {
    if (!config.uri) {
        throw new Error(`Missing ${config.name.toUpperCase()} DB connection details. Set CUTOVER_`
            + `${config.name.toUpperCase()}_DB_URL or the host/name/user env vars.`);
    }

    if (!config.password) {
        throw new Error(`Missing ${config.name.toUpperCase()} DB password. Set CUTOVER_`
            + `${config.name.toUpperCase()}_DB_PASSWORD or an environment-specific password variable.`);
    }
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

function getSelectedProfileIds() {
    return process.env.CUTOVER_DB_CHECK_PROFILE_IDS
        ? process.env.CUTOVER_DB_CHECK_PROFILE_IDS.split(',').map(profile => profile.trim()).filter(Boolean)
        : [];
}

function getSeededCases(manifest) {
    const selectedProfileIds = getSelectedProfileIds();
    const selectedProfileIdSet = new Set(selectedProfileIds);
    const cases = manifest.cases.filter(seededCase => {
        if (seededCase.status && seededCase.status !== 'seeded') {
            return false;
        }

        return selectedProfileIds.length === 0 || selectedProfileIdSet.has(seededCase.seedId);
    });
    const missingProfileIds = selectedProfileIds.filter(
        profileId => !cases.some(seededCase => seededCase.seedId === profileId)
    );

    if (missingProfileIds.length > 0) {
        throw new Error(`CUTOVER_DB_CHECK_PROFILE_IDS not found in manifest: ${missingProfileIds.join(', ')}`);
    }

    return cases;
}

function getCaseReferences(cases) {
    return cases.map(seededCase => {
        const caseId = String(seededCase.caseId || '');

        if (!/^\d+$/.test(caseId)) {
            throw new Error(`Seed profile ${seededCase.seedId} has an invalid numeric caseId: ${caseId}`);
        }

        return caseId;
    });
}

function buildCaseQuery(caseReferences) {
    const values = caseReferences.map(caseReference => `(${caseReference}::bigint)`).join(',');

    return `
with requested(reference) as (
    values ${values}
),
case_rows as (
    select
        requested.reference as requested_reference,
        cd.id,
        cd.reference,
        cd.case_type_id,
        cd.jurisdiction,
        cd.state,
        cd.security_classification::text as security_classification,
        cd.created_date::text as created_date,
        cd.last_modified::text as last_modified,
        cd.last_state_modified_date::text as last_state_modified_date,
        cd.version,
        cd.case_revision,
        md5(cd.data::text) as data_hash,
        md5(cd.supplementary_data::text) as supplementary_data_hash
    from requested
    left join ccd.case_data cd on cd.reference = requested.reference
),
event_stats as (
    select
        cd.reference,
        count(ce.id)::int as event_count,
        max(ce.case_revision) as max_event_case_revision
    from ccd.case_data cd
    left join ccd.case_event ce on ce.case_data_id = cd.id
    where cd.reference in (select reference from requested)
    group by cd.reference
),
latest_events as (
    select distinct on (cd.reference)
        cd.reference,
        ce.id as latest_event_row_id,
        ce.event_id as latest_event_id,
        ce.state_id as latest_event_state_id,
        ce.created_date::text as latest_event_created_date,
        ce.version as latest_event_version,
        ce.case_revision as latest_event_case_revision,
        md5(ce.data::text) as latest_event_data_hash
    from ccd.case_data cd
    join ccd.case_event ce on ce.case_data_id = cd.id
    where cd.reference in (select reference from requested)
    order by cd.reference, ce.case_revision desc nulls last, ce.id desc
),
result as (
    select
        cr.*,
        coalesce(es.event_count, 0) as event_count,
        es.max_event_case_revision,
        le.latest_event_row_id,
        le.latest_event_id,
        le.latest_event_state_id,
        le.latest_event_created_date,
        le.latest_event_version,
        le.latest_event_case_revision,
        le.latest_event_data_hash
    from case_rows cr
    left join event_stats es on es.reference = cr.reference
    left join latest_events le on le.reference = cr.reference
)
select coalesce(jsonb_agg(to_jsonb(result) order by requested_reference), '[]'::jsonb)::text
from result;
`;
}

function queryCaseRows(config, caseReferences) {
    const query = buildCaseQuery(caseReferences);
    const result = spawnSync(
        'psql',
        [
            '--quiet',
            '--tuples-only',
            '--no-align',
            '--set=ON_ERROR_STOP=1',
            `--dbname=${config.uri}`,
            `--command=${query}`
        ],
        {
            encoding: 'utf8',
            env: {
                ...process.env,
                PGPASSWORD: config.password
            },
            maxBuffer: 1024 * 1024 * 20
        }
    );

    if (result.error) {
        throw new Error(`Failed to run psql for ${config.name}: ${result.error.message}`);
    }

    if (result.status !== 0) {
        throw new Error(`Failed to query ${config.name} DB: ${result.stderr || result.stdout}`);
    }

    return JSON.parse(result.stdout.trim() || '[]');
}

function buildRowLookup(rows) {
    return new Map(rows.map(row => [String(row.requested_reference), row]));
}

function compareValue(field, ccdRow, etRow, mismatches) {
    if (ccdRow[field] !== etRow[field]) {
        mismatches.push({
            field,
            ccd: ccdRow[field],
            et: etRow[field]
        });
    }
}

function compareRows(seededCase, ccdRow, etRow) {
    const mismatches = [];

    if (!ccdRow || !ccdRow.reference) {
        mismatches.push({ field: 'ccd.reference', ccd: null, et: etRow ? etRow.reference : null });
    }

    if (!etRow || !etRow.reference) {
        mismatches.push({ field: 'et.reference', ccd: ccdRow ? ccdRow.reference : null, et: null });
    }

    if (!ccdRow || !ccdRow.reference || !etRow || !etRow.reference) {
        return mismatches;
    }

    [
        'reference',
        'case_type_id',
        'jurisdiction',
        'state',
        'security_classification',
        'version',
        'case_revision',
        'data_hash',
        'supplementary_data_hash',
        'event_count',
        'max_event_case_revision',
        'latest_event_id',
        'latest_event_state_id',
        'latest_event_version',
        'latest_event_case_revision',
        'latest_event_data_hash'
    ].forEach(field => compareValue(field, ccdRow, etRow, mismatches));

    if (seededCase.expectedState && etRow.state !== seededCase.expectedState) {
        mismatches.push({
            field: 'expectedState',
            expected: seededCase.expectedState,
            ccd: ccdRow.state,
            et: etRow.state
        });
    }

    return mismatches;
}

function buildReport({ manifestFile, outputFile, manifest, ccdConfig, etConfig, cases, ccdRows, etRows, status }) {
    const ccdLookup = buildRowLookup(ccdRows);
    const etLookup = buildRowLookup(etRows);
    const results = cases.map(seededCase => {
        const caseId = String(seededCase.caseId);
        const ccdRow = ccdLookup.get(caseId) || null;
        const etRow = etLookup.get(caseId) || null;
        const mismatches = compareRows(seededCase, ccdRow, etRow);

        return {
            seedId: seededCase.seedId,
            controllerCategory: seededCase.controllerCategory,
            caseId,
            expectedState: seededCase.expectedState || seededCase.targetState || null,
            status: mismatches.length === 0 ? 'matched' : 'mismatched',
            mismatches,
            ccd: ccdRow,
            et: etRow
        };
    });
    const mismatchedCaseCount = results.filter(result => result.status === 'mismatched').length;

    return {
        reportVersion: 1,
        generatedAt: new Date().toISOString(),
        status: status || (mismatchedCaseCount === 0 ? 'complete' : 'failed'),
        environment: testConfig.TestEnv,
        testUrl: testConfig.TestUrl,
        previewServiceFqdn: getPreviewServiceFqdn() || null,
        manifestFile,
        outputFile,
        seedManifestGeneratedAt: manifest.generatedAt || null,
        ccdDbUri: ccdConfig.uri.replace(/:\/\/([^:@]+):([^@]+)@/, '://$1:****@'),
        etDbUri: etConfig.uri.replace(/:\/\/([^:@]+):([^@]+)@/, '://$1:****@'),
        requestedCaseCount: results.length,
        matchedCaseCount: results.filter(result => result.status === 'matched').length,
        mismatchedCaseCount,
        results
    };
}

function writeReport(report) {
    fs.mkdirSync(path.dirname(report.outputFile), { recursive: true });
    fs.writeFileSync(report.outputFile, JSON.stringify(report, null, 2));
}

async function main() {
    const manifestFile = getManifestFile();
    const outputFile = getOutputFile();
    const manifest = loadManifest(manifestFile);
    const cases = getSeededCases(manifest);
    const caseReferences = getCaseReferences(cases);
    const ccdConfig = getCcdDbConfig();
    const etConfig = getEtDbConfig();

    validateDbConfig(ccdConfig);
    validateDbConfig(etConfig);

    if (caseReferences.length === 0) {
        throw new Error(`No seeded cases found in manifest: ${manifestFile}`);
    }

    logger.info(`Checking ${caseReferences.length} cutover cases in CCD and ET DBs`);
    logger.info(`Writing cutover DB check report to ${outputFile}`);

    const ccdRows = queryCaseRows(ccdConfig, caseReferences);
    const etRows = queryCaseRows(etConfig, caseReferences);
    const report = buildReport({
        manifestFile,
        outputFile,
        manifest,
        ccdConfig,
        etConfig,
        cases,
        ccdRows,
        etRows
    });

    writeReport(report);

    if (report.mismatchedCaseCount > 0) {
        throw new Error(`Cutover DB check found ${report.mismatchedCaseCount} mismatched case(s); `
            + `report written to ${outputFile}`);
    }

    console.log(`Cutover DB check report written to ${outputFile}`);
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
