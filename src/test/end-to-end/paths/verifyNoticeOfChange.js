const testConfig = require('../config');
const {eventNames} = require('../pages/common/constants.js');
const {respondentRepresentative} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Individual Case & Execute Respondent Representative');

Scenario('Verify Notice of Change by legal Rep for un assigned cases', async ({I}) => {

  let caseId = await processCaseToAcceptedState();
  console.log("... case id =>" +caseId);
  await respondentRepresentative(I, eventNames.RESPONDENT_REPRESENTATIVE, false);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);