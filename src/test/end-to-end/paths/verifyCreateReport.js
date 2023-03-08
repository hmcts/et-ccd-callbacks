const testConfig = require('./../../config');
const {generateReport} = require("../helpers/caseHelper");
const commonConfig = require('../data/commonConfig.json');
const {eventNames} = require('../pages/common/constants.js');

Feature('Create Report... ');

Scenario('Generate a Report', async ({I}) => {
    //Placed onto here instead of caseHelper
    await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);
    I.wait(commonConfig.time_interval_1_second);
    await generateReport(I, commonConfig.jurisdictionType, commonConfig.caseType, eventNames.CREATE_REPORT);
    
}).tag('@WIP').tag('@nightly').retry(testConfig.TestRetryScenarios);