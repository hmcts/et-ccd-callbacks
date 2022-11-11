const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Disposal date validation');

Scenario('Ensure disposal date is not present for jurisdiction outcomes allocated and input in error', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await jurisdiction(I, eventNames.JURISDICTION, "Not allocated");

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .tag('RET-BAT');
//.retry(testConfig.TestRetryScenarios);

Scenario('Ensure disposal date is present for jurisdiction outcomes successful hearings', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await jurisdiction(I, eventNames.JURISDICTION, "Successful at hearing");

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .tag('RET-BAT');

Scenario('Ensure date notified field is not present for "withdrawn or private settlement" outcome', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await jurisdiction(I, eventNames.JURISDICTION, "Withdrawn or private settlement");

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .tag('RET-BAT');