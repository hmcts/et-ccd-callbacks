const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const assert = require('assert');
const {caseDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

const verifyState = (eventResponse, state) => {
    assert.strictEqual(JSON.parse(eventResponse).state, state);
};

Feature('Leeds Singles Case and move to Case Details state');

Scenario('Verify Case Details ', async ({I}) => {

    let caseNumber = await processCaseToAcceptedState();
    console.log("... case id =>" +caseNumber);
    await caseDetails(I, caseNumber, eventNames.CASE_DETAILS, 'A Clerk', 'Casework Table', 'Standard Track');

}).tag('@nightly')
    .tag('@RET-BAT').retry(testConfig.TestRetryScenarios);
