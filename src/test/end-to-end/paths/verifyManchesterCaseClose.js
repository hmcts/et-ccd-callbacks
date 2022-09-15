const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction, closeCase} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Execute Manchester Case Close Scenario');

Scenario('Verify Manchester Case Close', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await jurisdiction(I, eventNames.JURISDICTION);
    await closeCase(I, eventNames.CLOSE_CASE, 'A Clerk', 'Casework Dropback Shelf');

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
