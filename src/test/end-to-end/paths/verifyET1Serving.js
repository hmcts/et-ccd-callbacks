const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {et1Serving} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
Feature('ET1 Serving Process');

Scenario('progress application through et1 serving - happy path England and Wales', async ({ I }) => {

   let caseId = await processCaseToAcceptedState();

   console.log("... case id =>" +caseId);

    await et1Serving(I,eventNames.ET1_SERVING);

}).tag('@RET-BAT') ;//.retry(testConfig.TestRetryScenarios)