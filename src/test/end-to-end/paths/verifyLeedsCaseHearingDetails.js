const testConfig = require('../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing, hearingDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Leeds Single Case & Execute Hearing details');

Scenario('Verify Leeds case Hearing details', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await listHearing(I, eventNames.LIST_HEARING, 'Leeds');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Leeds');
    await hearingDetails(I, eventNames.HEARING_DETAILS);

}).tag('@biggerrefactoring');
    //.retry(testConfig.TestRetryScenarios);
