const testConfig = require('../config');
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {et3Notification} = require("../helpers/caseHelper");

Feature('ET3 Notification Process');

Scenario('progress application through et3 notification -  happy path England and Wales', async ({ I }) => {
    await processCaseToAcceptedState();
    await et3Notification(I,eventNames.ET3_NOTIFICATION);

}).tag('@toberefactored').retry(testConfig.TestRetryScenarios)
