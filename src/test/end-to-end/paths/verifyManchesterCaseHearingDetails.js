const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing, hearingDetails} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Single Case & Execute Hearing details');

Scenario('Verify Manchester case Hearing details', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await listHearing(I, eventNames.LIST_HEARING, 'Manchester');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Manchester');
    await hearingDetails(I, eventNames.HEARING_DETAILS);

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
