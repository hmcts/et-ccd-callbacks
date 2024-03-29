const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing, printHearingLists} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create Single Case & Execute Print Hearing Lists');

Scenario('Verify Print Hearing Lists', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await listHearing(I, eventNames.LIST_HEARING, 'Leeds');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Leeds');
    await printHearingLists(I, eventNames.PRINT_HEARING_LISTS, 'Leeds');

}).tag('@biggerrefactoring');
    //.retry(testConfig.TestRetryScenarios);
