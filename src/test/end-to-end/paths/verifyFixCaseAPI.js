const testConfig = require('./../../config');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {eventNames} = require('../pages/common/constants.js');
const {fixCaseAPI} = require("../helpers/caseHelper");


Feature('Create a Leeds Singles Case & Execute Fix Case API');

Scenario('Verify Fix Case API', async ({I}) => {
    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);
    await fixCaseAPI(I, eventNames.FIX_CASE_API);

}).tag('@e2e')
    .tag('@nightly');
    //.retry(testConfig.TestRetryScenarios);
