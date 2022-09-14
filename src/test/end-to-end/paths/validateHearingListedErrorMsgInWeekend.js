const testConfig = require('./../../config');
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
const {eventNames} = require('../pages/common/constants.js');
const {scheduleHearingDuringTheWeekend} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Validation to stop users having the ability to list any type of hearing on a weekend (Saturday or Sunday).');

Scenario('Validate hearing error message if user schedules the hearing date during the weekend', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await scheduleHearingDuringTheWeekend(I, eventNames.LIST_HEARING, 'Leeds');

}).tag('@nightly').tag('@wip')
    .retry(testConfig.TestRetryScenarios);
