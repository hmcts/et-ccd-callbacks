const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing} = require("../helpers/caseHelper");
const {processCaseToListedState} = require("../helpers/etCaseHepler");

Feature('Create a Single Case & Execute Allocate Hearing');

Scenario('Verify Case Allocate Hearing', async ({I}) => {

    let caseId = await processCaseToListedState();
    console.log("... case id =>" +caseId);

    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Leeds');

}).tag('@nightly').tag('@RET-BAT-DISABLED').retry(testConfig.TestRetryScenarios);
