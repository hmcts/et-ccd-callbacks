const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {bfActionsOutstanding} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Verify whether the CW able to close the case if any bf outstanding actions are exists on the case ');

Scenario('Verify Close Case B/F Outstanding Actions Error Message', async ({I}) => {
    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await bfActionsOutstanding(I, eventNames.BF_ACTION);

}).tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
