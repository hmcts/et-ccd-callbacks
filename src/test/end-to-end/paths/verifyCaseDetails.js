const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {caseDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Execute case details event');

Scenario('Verify Case Details ', async ({I}) => {

    let caseNumber = await processCaseToAcceptedState();
    console.log("... case id =>" +caseNumber);
    await caseDetails(I, caseNumber, eventNames.CASE_DETAILS, 'A Clerk', 'Casework Table', 'Leeds');

}).tag('@nightly').tag('@RET-BAT-DISABLED').retry(testConfig.TestRetryScenarios);
