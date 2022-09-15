const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {caseDetailsEvent} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Verify whether the user able to move accepted case to case closed state');

Scenario('Move Accepted case to case closed state error check', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await caseDetailsEvent(I, caseId, eventNames.CASE_DETAILS, 'A Clerk', 'Case closed', 'Casework Table', 'Standard Track');

}).tag('@nightly').tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
