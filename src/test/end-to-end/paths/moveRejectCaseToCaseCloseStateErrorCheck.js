const testConfig = require('./../../config');
const {caseDetailsEvent} = require("../helpers/caseHelper");
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToRejectedState} = require("../helpers/etCaseHepler");

Feature('Verify whether the user able to move rejected case to case closed state');

Scenario('Move rejected case to case closed state error check', async ({I}) => {

    let caseId = await processCaseToRejectedState();
    await caseDetailsEvent(I, caseId, eventNames.CASE_DETAILS, 'A Clerk', 'Case closed', 'Casework Table', 'Standard Track');

}).tag('@nightly').retry(testConfig.TestRetryScenarios);
