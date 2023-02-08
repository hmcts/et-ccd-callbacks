const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {caseTransfer} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Case & Execute Case Transfer');

Scenario('Verify a Case Transfer for England and Wales', async ({I}) => {
    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await caseTransfer(I, eventNames.CASE_TRANSFER_ENGWAL);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);

Scenario('Verify a Case Transfer for Scotland', async ({I}) => {
    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await caseTransfer(I, eventNames.CASE_TRANSFER_SCOTLAND);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);

Scenario('Verify a Case Transfer for an ECM Case', async ({I}) => {
    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await caseTransfer(I, eventNames.CASE_TRANSFER_ECM);

}).tag('@RET-BAT').tag('@nightly').tag('SC3').retry(testConfig.TestRetryScenarios);