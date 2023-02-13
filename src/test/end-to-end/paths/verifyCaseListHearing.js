const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");


Feature('Create a Leeds Single Case & Execute List Hearing');

Scenario('Verify Leeds case List Hearing', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await listHearing(I, eventNames.LIST_HEARING, 'Leeds');

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);
