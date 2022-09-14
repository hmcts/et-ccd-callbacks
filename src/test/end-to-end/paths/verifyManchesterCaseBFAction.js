const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {bfAction} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Singles Case & Execute B/F Action');

Scenario('Verify Manchester case B/F Action', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await bfAction(I, eventNames.BF_ACTION);

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
