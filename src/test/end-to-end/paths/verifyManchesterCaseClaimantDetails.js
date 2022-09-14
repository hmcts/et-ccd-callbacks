const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {claimantDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create Manchester A Single Case And Execute Claimant Details...');

Scenario('Verify Claimant Details', async ({I}) => {
    let caseId = await processCaseToAcceptedState();
    await claimantDetails(I, eventNames.CLAIMANT_DETAILS);

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
