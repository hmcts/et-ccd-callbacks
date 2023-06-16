const testConfig = require('../config');
const {et1Vetting} = require("../helpers/caseHelper");
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToSubmittedState} = require("../helpers/etCaseHepler");

const { I } = inject();


Feature('ET CCD ET1 Vetting Process');

Scenario('ET1 Case Vetting', async ({I}) => {

    const caseNumber = await processCaseToSubmittedState();
    await et1Vetting(I, eventNames.ET1_VETTING);
  
}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);
  
