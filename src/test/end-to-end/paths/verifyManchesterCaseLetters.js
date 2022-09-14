const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {letters} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Single Case & Execute Letters');

Scenario('Verify Manchester case Letters', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await letters(I, eventNames.LETTERS);

}).tag('@nightly')
    .tag('@e2e')
    .retry(testConfig.TestRetryScenarios);
