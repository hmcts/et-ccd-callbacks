const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing} = require("../helpers/caseHelper");
const {processCaseToListedState} = require("../helpers/etCaseHepler");

Feature('Create a Leeds Single Case & Execute Allocate Hearing');

Scenario('Verify Leeds case Allocate Hearing', async ({I}) => {

    let caseId = await processCaseToListedState();
    console.log("... case id =>" +caseId);

    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Leeds');

}).tag('@nightly').tag('@RET-BAT').retry(testConfig.TestRetryScenarios);
