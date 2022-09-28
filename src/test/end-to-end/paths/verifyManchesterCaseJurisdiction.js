const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Manchester Office Single Case & Execute Jurisdiction Event');

Scenario('Verify Jurisdiction', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await jurisdiction(I, eventNames.JURISDICTION);

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
