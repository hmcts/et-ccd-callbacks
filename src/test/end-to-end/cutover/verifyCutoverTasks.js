const path = require('path');
const axios = require('axios');
const { Logger } = require('@hmcts/nodejs-logging');

const {
    buildHeaders,
    env,
    findCasesArray,
    firstPresent,
    getAuthContext,
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
} = require('./cutoverProbeSupport');

const logger = Logger.getLogger('cutover/verifyCutoverTasks.js');
const defaultOutputFile = path.resolve(process.cwd(), 'functional-output/cutover/tasks-report.json');

function getOutputFile() {
    return path.resolve(process.cwd(), process.env.CUTOVER_TASKS_OUTPUT_FILE || defaultOutputFile);
}

function getAttempts() {
    return parseInt(process.env.CUTOVER_TASKS_ATTEMPTS || '10', 10);
}

function getIntervalMs() {
    return parseInt(process.env.CUTOVER_TASKS_INTERVAL_MS || '15000', 10);
}

function getTaskManagementUrl() {
    const previewUrl = getPreviewUrl('wa-task-management-api', 'CUTOVER_WA_TASK_MANAGEMENT_URL');

    if (previewUrl) {
        return previewUrl;
    }

    return stripTrailingSlash(`http://wa-task-management-api-${env}.service.core-compute-${env}.internal`);
}

function getTaskCandidates(caseId) {
    return [
        {
            method: 'get',
            path: `/task?case_id=${caseId}`
        },
        {
            method: 'get',
            path: `/task?caseId=${caseId}`
        },
        {
            method: 'get',
            path: `/tasks?case_id=${caseId}`
        },
        {
            method: 'get',
            path: `/tasks?caseId=${caseId}`
        },
        {
            method: 'post',
            path: '/task',
            body: {
                search_parameters: [
                    {
                        key: 'caseId',
                        operator: 'IN',
                        values: [String(caseId)]
                    }
                ]
            }
        }
    ];
}

function extractTasks(payload) {
    if (Array.isArray(payload)) {
        return payload;
    }

    if (payload && typeof payload === 'object' && firstPresent(payload.id, payload.task_id, payload.taskId)) {
        return [payload];
    }

    const candidates = [
        payload.tasks,
        payload.task,
        payload.task_required_for_events,
        payload.data,
        payload.results,
        payload
    ];

    for (const candidate of candidates) {
        if (candidate && typeof candidate === 'object' && firstPresent(candidate.id, candidate.task_id, candidate.taskId)) {
            return [candidate];
        }

        const tasks = findCasesArray(candidate || {});

        if (tasks.length > 0) {
            return tasks;
        }
    }

    return [];
}

function summariseTask(task) {
    return {
        id: firstPresent(task.id, task.task_id, task.taskId) || null,
        name: firstPresent(task.name, task.task_name, task.taskName, task.type) || null,
        status: firstPresent(task.status, task.state, task.task_state, task.taskState) || null,
        assignee: firstPresent(task.assignee, task.assignee_user, task.assigneeUser) || null,
        createdDate: firstPresent(task.created_date, task.createdDate, task.created) || null
    };
}

async function queryTaskCandidate(context, taskManagementUrl, candidate) {
    const requestConfig = {
        headers: buildHeaders(context)
    };

    if (candidate.method === 'post') {
        return axios.post(`${taskManagementUrl}${candidate.path}`, candidate.body, requestConfig);
    }

    return axios.get(`${taskManagementUrl}${candidate.path}`, requestConfig);
}

async function findTasksForCase(context, taskManagementUrl, caseId) {
    const errors = [];

    for (const candidate of getTaskCandidates(caseId)) {
        try {
            const response = await queryTaskCandidate(context, taskManagementUrl, candidate);
            const tasks = extractTasks(response.data || {});

            return {
                ok: true,
                endpoint: `${candidate.method.toUpperCase()} ${candidate.path}`,
                taskCount: tasks.length,
                tasks: tasks.map(summariseTask)
            };
        } catch (error) {
            errors.push({
                endpoint: `${candidate.method.toUpperCase()} ${candidate.path}`,
                error: getErrorDetails(error)
            });
        }
    }

    return {
        ok: false,
        errors
    };
}

async function pollTasksForCase(context, taskManagementUrl, seededCase) {
    const requireTasks = process.env.CUTOVER_TASKS_REQUIRE_TASKS === 'true';

    return pollUntil(async attempt => {
        const taskSearch = await findTasksForCase(context, taskManagementUrl, seededCase.caseId);

        return {
            ...taskSearch,
            ok: taskSearch.ok && (!requireTasks || taskSearch.taskCount > 0),
            attempt,
            requireTasks
        };
    }, { attempts: getAttempts(), intervalMs: getIntervalMs() });
}

async function verifySeededCase(context, taskManagementUrl, seededCase) {
    const taskSearch = await pollTasksForCase(context, taskManagementUrl, seededCase);
    const failures = [];

    if (!taskSearch.ok) {
        failures.push(process.env.CUTOVER_TASKS_REQUIRE_TASKS === 'true'
            ? 'taskSearchWithExpectedTasks'
            : 'taskSearchEndpoint');
    }

    return {
        seedId: seededCase.seedId,
        controllerCategory: seededCase.controllerCategory,
        caseId: seededCase.caseId,
        status: failures.length === 0 ? 'verified' : 'failed',
        failures,
        checks: {
            taskSearch
        }
    };
}

function buildReport({ manifestFile, outputFile, manifest, taskManagementUrl, results }) {
    const failedCaseCount = results.filter(result => result.status === 'failed').length;

    return {
        reportVersion: 1,
        generatedAt: new Date().toISOString(),
        status: failedCaseCount === 0 ? 'complete' : 'failed',
        environment: env,
        testUrl: testConfig.TestUrl,
        taskManagementUrl,
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
    const manifestFile = getManifestFile('CUTOVER_TASKS_MANIFEST_FILE');
    const outputFile = getOutputFile();
    const taskManagementUrl = getTaskManagementUrl();
    const manifest = loadManifest(manifestFile);
    const cases = getSeededCases(manifest, 'CUTOVER_TASKS_PROFILE_IDS');

    if (cases.length === 0) {
        throw new Error(`No seeded cases found in manifest: ${manifestFile}`);
    }

    logger.info(`Checking WA task visibility for ${cases.length} cutover case(s)`);
    logger.info(`Writing cutover tasks report to ${outputFile}`);

    const context = await getAuthContext();
    const results = [];

    writeJsonReport(outputFile, buildReport({ manifestFile, outputFile, manifest, taskManagementUrl, results }));

    for (const seededCase of cases) {
        const result = await verifySeededCase(context, taskManagementUrl, seededCase);
        results.push(result);
        logger.info(`Task check ${result.status}: ${seededCase.seedId} => ${seededCase.caseId}`);
        writeJsonReport(outputFile, buildReport({ manifestFile, outputFile, manifest, taskManagementUrl, results }));
    }

    const report = buildReport({ manifestFile, outputFile, manifest, taskManagementUrl, results });
    writeJsonReport(outputFile, report);

    if (report.failedCaseCount > 0) {
        throw new Error(`Cutover WA task verification failed for ${report.failedCaseCount} case(s); `
            + `report written to ${outputFile}`);
    }

    console.log(`Cutover tasks report written to ${outputFile}`);
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
