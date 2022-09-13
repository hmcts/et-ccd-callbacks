const testConfig = require('./../../config');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {eventNames} = require('../pages/common/constants.js');
const {letters} = require("../helpers/caseHelper");


Feature('Create a Leeds Single Case & Execute Letters');

Scenario('Verify Letters', async ({I}) => {
    let caseNumber = await processCaseToAcceptedState();
    console.log("... case id =>" +caseNumber);
    await letters(I, eventNames.LETTERS);

}).tag('@wip')
    .tag('@nightly');
    //.retry(testConfig.TestRetryScenarios);
