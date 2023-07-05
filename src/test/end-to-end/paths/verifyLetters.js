const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {letters} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create Single Case & Execute Letters');

Scenario('Verify Letters', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await letters(I, eventNames.LETTERS);

}).tag('@RET-BAT1').retry(testConfig.TestRetryScenarios);
