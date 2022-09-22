const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction, judgment} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Manchester Office Singles Case & Execute Judgment Event');

Scenario('Verify Manchester Case Judgment', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await jurisdiction(I, eventNames.JURISDICTION);
    await judgment(I, eventNames.JUDGMENT);

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
