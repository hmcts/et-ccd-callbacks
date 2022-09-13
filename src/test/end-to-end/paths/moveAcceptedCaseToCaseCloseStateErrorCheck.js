const testConfig = require('./../../config');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {eventNames} = require('../pages/common/constants.js');
const {caseDetailsEvent} = require("../helpers/caseHelper");


Feature('Verify whether the user able to move accepted case to case closed state');

Scenario('Move Accepted case to case closed state error check', async ({I}) => {
    let caseNumber = await processCaseToAcceptedState();
    console.log("... case id =>" +caseNumber);
    await caseDetailsEvent(I, caseNumber, eventNames.CASE_DETAILS, 'A Clerk', 'Case closed', 'Casework Table', 'Standard Track');

}).tag('@wip')
    .retry(testConfig.TestRetryScenarios);
