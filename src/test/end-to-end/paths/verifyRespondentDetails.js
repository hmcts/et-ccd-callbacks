const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {claimantRespondentDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create Singles Case & Execute Claimant Respondent Details...');

Scenario('Verify Respondent Details', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await claimantRespondentDetails(I, eventNames.CLAIMANT_RESPONDENT_DETAILS);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);
