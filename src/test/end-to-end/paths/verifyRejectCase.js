const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {rejectCaseEvent} = require("../helpers/caseHelper");
const {processCaseToET1VettedState} = require("../helpers/etCaseHepler");
let caseNumber;

Feature('Create Singles Case and move to Rejected state');

Scenario('Verify Reject Case', async ({I}) => {
    caseNumber = await processCaseToET1VettedState();
    await rejectCaseEvent(I, caseNumber, eventNames.REJECT_CASE);

}).tag('@nightly').tag('@RET-BAT-DISABLED').retry(testConfig.TestRetryScenarios);
