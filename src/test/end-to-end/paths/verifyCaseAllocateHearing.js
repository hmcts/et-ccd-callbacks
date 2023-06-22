const testConfig = require('../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing,hearingDetails} = require("../helpers/caseHelper");
const {processCaseToListedState} = require("../helpers/etCaseHepler");

Feature('Create a Single Case & Execute Allocate Hearing');

Scenario('Verify Case Allocate Hearing', async ({I}) => {

    let caseId = await processCaseToListedState();
    console.log("... case id =>" +caseId);
    await listHearing(I, eventNames.LIST_HEARING, 'Leeds');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Leeds');
    await hearingDetails(I, eventNames.HEARING_DETAILS, 'Yes');

}).tag('@nightly').tag('@RET-BAT').retry(testConfig.TestRetryScenarios);
