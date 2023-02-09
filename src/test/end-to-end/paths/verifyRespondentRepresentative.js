const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {respondentRepresentative} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Leeds Office Individual Case & Execute Respondent Representative');

Scenario('Verify Respondent Representative for a Representative without a myHMCTS account', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await respondentRepresentative(I, eventNames.RESPONDENT_REPRESENTATIVE, false);

}).tag('@RET-BAT').tag('@nightly').tag('@pats');
    //.retry(testConfig.TestRetryScenarios);
