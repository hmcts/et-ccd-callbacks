const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {claimantRepresentative} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");


Feature('Validate Claimant Representative');

Scenario('Verify Claimant Representative', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await claimantRepresentative(I, eventNames.CLAIMANT_REPRESENTATIVE);

}).tag('@nightly').tag('@RET-BAT-DISABLED').retry(testConfig.TestRetryScenarios);
