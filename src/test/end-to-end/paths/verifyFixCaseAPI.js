const testConfig = require('./../../config');
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
const {eventNames} = require('../pages/common/constants.js');
const {fixCaseAPI} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Leeds Singles Case & Execute Fix Case API');

Scenario('Verify Fix Case API', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await fixCaseAPI(I, eventNames.FIX_CASE_API);

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
