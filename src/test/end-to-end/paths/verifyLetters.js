const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {letters} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Leeds Single Case & Execute Letters');

Scenario('Verify Letters', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await letters(I, eventNames.LETTERS);

}).tag('@nightly').retry(testConfig.TestRetryScenarios);