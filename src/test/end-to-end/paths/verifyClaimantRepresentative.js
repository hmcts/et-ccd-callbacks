const testConfig = require('./../../config');
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
const {eventNames} = require('../pages/common/constants.js');
const {claimantRepresentative} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");


Feature('Leeds Singles Case & Execute Claimant Representative...');

Scenario('Verify Claimant Representative', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await claimantRepresentative(I, eventNames.CLAIMANT_REPRESENTATIVE);

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
