const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {listHearing, allocateHearing} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Create a Manchester Single Case & Execute Allocate Hearing');

Scenario('Verify Manchester case Allocate Hearing', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await listHearing(I, eventNames.LIST_HEARING, 'Manchester');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Manchester');

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
