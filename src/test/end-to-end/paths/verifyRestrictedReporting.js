const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {restrictedReporting} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Leeds Singles Case & Execute Restricted Reporting');

Scenario('Verify Restricted Reporting', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await restrictedReporting(I, eventNames.RESTRICTED_REPORTING);

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
