const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {judgment,} = require("../helpers/caseHelper");
const {processCaseToAcceptedWithAJurisdiction, processCaseToListedStateWithAJursdiction} = require("../helpers/etCaseHepler");

Feature('Office Singles Case & Execute Judgment Event');

Scenario('Verify Case Judgment When a Hearing is not Done on the Case', async ({I}) => {

    let caseId = await processCaseToAcceptedWithAJurisdiction();
    console.log("... case id =>" +caseId);
    await judgment(I, eventNames.JUDGMENT, false);



}).tag('@nightly').tag('@RET-BAT').tag('sc2').retry(testConfig.TestRetryScenarios);

Scenario('Verify Case Judgment when a Hearing is part of the Case', async ({I}) => {

    let caseId = await processCaseToListedStateWithAJursdiction();
    console.log("... case id =>" +caseId);
    await judgment(I, eventNames.JUDGMENT, true);


}).tag('@nightly').tag('@RET-BAT').retry(testConfig.TestRetryScenarios);
