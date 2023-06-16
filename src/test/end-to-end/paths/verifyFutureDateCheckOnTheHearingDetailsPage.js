const testConfig = require('../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing, updateHearingDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Verify future date check on the hearing details page');

Scenario('Validate future date check on the hearing details page', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await listHearing(I, eventNames.LIST_HEARING, 'Leeds');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Leeds');
    await updateHearingDetails(I, eventNames.HEARING_DETAILS);

}).tag('@biggerrefactoring');
    //.retry(testConfig.TestRetryScenarios);
