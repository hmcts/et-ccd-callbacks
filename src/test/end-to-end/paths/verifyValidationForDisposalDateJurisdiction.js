const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction, listHearing, allocateHearing, hearingDetails, enterDisposalDateJurisdiction} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const testConfig = require("../../config");

Feature('Disposal date validation');

Scenario('Ensure disposal date is not present for jurisdiction outcomes allocated and input in error', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await jurisdiction(I, eventNames.JURISDICTION, "Not allocated");

}).tag('@nightly')
    .tag('@RET-BAT-DISABLED')
    .retry(testConfig.TestRetryScenarios);

Scenario('Ensure disposal date is present for jurisdiction outcomes successful hearings', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await jurisdiction(I, eventNames.JURISDICTION, "Successful at hearing");

}).tag('@RET-BAT-DISABLED')
    .tag('@nightly')
    .retry(testConfig.TestRetryScenarios);


Scenario('Ensure date notified field is not present for "withdrawn or private settlement" outcome', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await jurisdiction(I, eventNames.JURISDICTION, "Withdrawn or private settlement");

}).tag('@RET-BAT-DISABLED')
    .tag('@nightly')
.retry(testConfig.TestRetryScenarios);


Scenario('User enters a disposal date - check error scenarios and completes jurisdiction event', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await listHearing(I, eventNames.LIST_HEARING, 'Leeds');
    await allocateHearing(I, eventNames.ALLOCATE_HEARING, 'Leeds');
    await hearingDetails(I, eventNames.HEARING_DETAILS, 'Yes');
    await jurisdiction(I, eventNames.JURISDICTION, "Successful at hearing");
    await enterDisposalDateJurisdiction(I,'Date NOT contained in hearing collection');
    await enterDisposalDateJurisdiction(I, 'Date in the future');
    await enterDisposalDateJurisdiction(I,'Date contained in hearing collection');

}).tag('@nightly')
    .retry(testConfig.TestRetryScenarios);
