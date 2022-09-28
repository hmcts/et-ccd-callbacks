const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing, printHearingLists} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Single Case & Execute Print Hearing Lists');

Scenario('Verify Manchester Print Hearing Lists', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);

    await listHearing(I, eventNames.LIST_HEARING, 'Manchester');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Manchester');
    await printHearingLists(I, eventNames.PRINT_HEARING_LISTS, 'Manchester');

}).tag('@printHearing').tag('@wip');
    //.retry(testConfig.TestRetryScenarios);
