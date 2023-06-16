const testConfig = require('../config');
const {caseDetailsEvent} = require("../helpers/caseHelper");
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToSubmittedState} = require("../helpers/etCaseHepler");

Feature('Verify whether the user able to move submitted case to case closed state');

Scenario('error message validation check for when user move submitted case to case closed state ', async ({I}) => {

    const caseNumber = await processCaseToSubmittedState();
    await caseDetailsEvent(I, caseNumber, eventNames.CASE_DETAILS, 'A Clerk', 'Case closed', 'Casework Table', 'Standard Track');

}).tag('@nightly').retry(testConfig.TestRetryScenarios);
