const testConfig = require('./../../config');
const {eventNames, states} = require('../pages/common/constants.js');
const {caseDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Manchester Single Case and move to Case Details state');

Scenario('Verify Case Details ', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await caseDetails(I, caseId, eventNames.CASE_DETAILS, 'A Clerk', 'Casework Dropback Shelf', 'Standard track');

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
