const testConfig = require('./../../config');
const {generateReport} = require("../helpers/caseHelper");
const commonConfig = require('../data/commonConfig.json');
const {eventNames} = require('../pages/common/constants.js');

Feature('Create Report... ');

Scenario('Generate a Report', async ({I}) => {
    await generateReport(I, commonConfig.jurisdictionType, commonConfig.caseType, eventNames.CREATE_REPORT, testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);