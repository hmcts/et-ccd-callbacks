const testConfig = require('./../../config');
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
const {eventNames} = require('../pages/common/constants.js');
const {submittedState, caseDetailsEvent} = require("../helpers/caseHelper");

Feature('Verify whether the user able to move submitted case to case closed state');

Scenario('error message validation check for when user move submitted case to case closed state ', async ({I}) => {

    caseNumber = await createCaseInCcd('src/test/end-to-end/data/ccd-case-basic-data.json');
    await submittedState(I, caseNumber);
    await caseDetailsEvent(I, caseNumber, eventNames.CASE_DETAILS, 'A Clerk', 'Case closed', 'Casework Table', 'Standard Track');

}).tag('@wip')
    .retry(testConfig.TestRetryScenarios);
