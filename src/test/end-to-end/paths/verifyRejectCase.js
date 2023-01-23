const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {rejectCaseEvent} = require("../helpers/caseHelper");
const {processCaseToSubmittedState} = require("../helpers/etCaseHepler");
let caseNumber;

Feature('Create a Leeds Singles Case and move to Rejected state');

Scenario('Verify Reject Case', async ({I}) => {
    caseNumber = await processCaseToSubmittedState();
    await rejectCaseEvent(I, caseNumber, eventNames.REJECT_CASE);

}).tag('@nightly').tag('@RET-BAT').retry(testConfig.TestRetryScenarios);
