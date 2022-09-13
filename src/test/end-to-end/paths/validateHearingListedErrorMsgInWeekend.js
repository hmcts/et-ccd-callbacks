const testConfig = require('./../../config');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {eventNames} = require('../pages/common/constants.js');
const {scheduleHearingDuringTheWeekend} = require("../helpers/caseHelper");


Feature('Validation to stop users having the ability to list any type of hearing on a weekend (Saturday or Sunday).');

Scenario('Validate hearing error message if user schedules the hearing date during the weekend', async ({I}) => {
    let caseNumber = await processCaseToAcceptedState();
    console.log("... case id =>" +caseNumber);
    await scheduleHearingDuringTheWeekend(I, eventNames.LIST_HEARING, 'Leeds');

}).tag('@nightly')
    .retry(testConfig.TestRetryScenarios);
