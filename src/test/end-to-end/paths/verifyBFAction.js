const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {bfAction} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Leeds Singles Case & Execute B/F Action');

Scenario('Verify B/F Action', async ({I}) => {

    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);
    //await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);
    //I.refreshPage();
    //I.wait(commonConfig.time_interval_30_second);

    await bfAction(I, eventNames.BF_ACTION);

}).tag('RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);
