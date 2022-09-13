const testConfig = require('./../../config');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {eventNames} = require('../pages/common/constants.js');
const {claimantRepresentative} = require("../helpers/caseHelper");


Feature('Leeds Singles Case & Execute Claimant Representative...');

Scenario('Verify Claimant Representative', async ({I}) => {
    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);
    await claimantRepresentative(I, eventNames.CLAIMANT_REPRESENTATIVE);

}).tag('@wip')
    .tag('@nightly');
    //.retry(testConfig.TestRetryScenarios);
