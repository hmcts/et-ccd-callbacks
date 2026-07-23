const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {fixCaseAPI} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create Singles Case & Execute Fix Case API');

Scenario('Verify Fix Case API', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await fixCaseAPI(I, eventNames.FIX_CASE_API);

}).tag('@toberefactored');
    //.retry(testConfig.TestRetryScenarios);
