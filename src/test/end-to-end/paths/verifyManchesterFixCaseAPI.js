const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {fixCaseAPI} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Singles Case & Execute Fix Case API');

Scenario('Verify Manchester Fix Case API', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await fixCaseAPI(I, eventNames.FIX_CASE_API);

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
