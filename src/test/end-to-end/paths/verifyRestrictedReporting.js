const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {restrictedReporting} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Case & Execute Restricted Reporting');

Scenario('Verify Restricted Reporting', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await restrictedReporting(I, eventNames.RESTRICTED_REPORTING);

}).tag('@RET-BAT-DISABLED').tag('@nightly').retry(testConfig.TestRetryScenarios);