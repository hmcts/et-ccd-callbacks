module.exports = {
    TestUrl: process.env.TEST_E2E_URL || 'https://manage-case.aat.platform.hmcts.net',
    IdamBaseUrl: process.env.IDAM_URL || 'https://idam-api.aat.platform.hmcts.net',
    RedirectUri: process.env.REDIRECT_URI || `https://manage-case.aat.platform.hmcts.net/oauth2/callback`,
    TestEnv: process.env.RUNNING_ENV || 'aat',
    TestShowBrowserWindow: process.env.SHOW_BROWSER_WINDOW || false,
    TestRetryFeatures: process.env.RETRY_FEATURES || 0,
    TestRetryScenarios: process.env.RETRY_SCENARIOS || 2,
    TestPathToRun: process.env.E2E_TEST_PATH || './paths/**/*.js',
    TestOutputDir: process.env.E2E_OUTPUT_DIR || './functional-output',
    TestTimeToWaitForText: parseInt(process.env.E2E_TEST_TIME_TO_WAIT_FOR_TEXT || 30),
    TestEnvCWUser: process.env.CCD_CASEWORKER_E2E_EMAIL || 'employment_service@mailinator.com',
    TestEnvCWPassword: process.env.CCD_CASEWORKER_E2E_PASSWORD || 'Nagoya0102',
    TestEnvEtCWUser: process.env.ET_CASEWORKER_EMAIL || 'retest1078@testmail.com',
    TestEnvEtCWPwd: process.env.ET_CASEWORKER_PASSWORD || 'Adventure2019',
    TestEnvEtJudgeUser: process.env.ET_JUDGE_EMAIL || 'retest1078@testmail.com',
    TestEnvEtJudgePwd: process.env.ET_JUDGE_PASSWORD || 'Adventure2019',
    TestForXUI: process.env.TESTS_FOR_XUI_SERVICE === 'true',
    TestForAccessibility: process.env.TESTS_FOR_ACCESSIBILITY === 'true',
    TestForCrossBrowser: process.env.TESTS_FOR_CROSS_BROWSER === 'true',
    TestIdamClientSecret: process.env.IDAM_CLIENT_SECRET || 'ZSu8eMK9Woqc0Tm9',
    TestS2SAuthSecret: process.env.SERVICE_SECRET || '',
    TestCcdGwSecret: process.env.MICROSERVICE_CCD_GW || '',
    oneTimePassword: process.env.ONE_TIME_PASSWORD || '376564',
};
