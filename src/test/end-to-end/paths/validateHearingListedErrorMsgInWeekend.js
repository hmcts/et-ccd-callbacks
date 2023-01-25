const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {scheduleHearingDuringTheWeekend} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Validation to stop users having the ability to list any type of hearing on a weekend (Saturday or Sunday).');

Scenario('Validate hearing error message if user schedules the hearing date during the weekend', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await scheduleHearingDuringTheWeekend(I, eventNames.LIST_HEARING, 'Leeds');

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);
