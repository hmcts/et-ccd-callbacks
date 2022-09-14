const testConfig = require('./../../config');
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
const {eventNames} = require('../pages/common/constants.js');
const {listHearing} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Single Case & Execute List Hearing');

Scenario('Verify Manchester case List Hearing', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await listHearing(I, eventNames.LIST_HEARING, 'Manchester');

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
