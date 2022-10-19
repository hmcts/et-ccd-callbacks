const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {restrictedReporting} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Singles Case & Execute Restricted Reporting');

Scenario('Verify Manchester case Restricted Reporting', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await restrictedReporting(I, eventNames.RESTRICTED_REPORTING);

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
