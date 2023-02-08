const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {claimantDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Leeds Singles Case And Execute Claimant Details...');

Scenario('Verify Claimant Details', async ({I}) => {

    let caseNumber = await processCaseToAcceptedState();
    console.log("... case id =>" + caseNumber);
    await claimantDetails(I, eventNames.CLAIMANT_DETAILS);

}).tag('@nightly').tag('@RET-BAT').retry(testConfig.TestRetryScenarios);
