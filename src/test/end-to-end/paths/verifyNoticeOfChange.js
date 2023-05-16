const testConfig = require('./../../config');
const {noticeOfChange} = require("../helpers/caseHelper");
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Notice of Change');

Scenario('Verify Notice of Change by Legal Rep for an unassigned cases', async ({I}) => {

    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);

    await noticeOfChange(I, eventNames.UPLOAD_DOCUMENT);

}).tag('@RET-BAT').tag('@nightly').tag('sc2').retry(testConfig.TestRetryScenarios);