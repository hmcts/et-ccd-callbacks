const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction, judgment} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Leeds Office Singles Case & Execute Judgment Event');

Scenario('Verify Leeds Case Judgment', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await jurisdiction(I, eventNames.JURISDICTION);
    await judgment(I, eventNames.JUDGMENT);

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
