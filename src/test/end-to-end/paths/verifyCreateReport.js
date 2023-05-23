const testConfig = require('./../../config');
const {generateReport} = require("../helpers/caseHelper");
const commonConfig = require('../data/commonConfig.json');
const {eventNames} = require('../pages/common/constants.js');
const caseListLink = '[href="/cases"]';

Feature('Create Report... ');

Scenario('Generate a Report', async ({I}) => {
    await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);
    I.wait(commonConfig.time_interval_1_second);
    I.waitForElement(this.caseListLink, 30);
    I.click(this.caseListLink);
    await generateReport(I, commonConfig.jurisdictionType, commonConfig.caseType, eventNames.CREATE_REPORT);
    
}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);
