const testConfig = require('../config');
const {uploadDocumentEvent} = require("../helpers/caseHelper");
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Validate Upload Document');

Scenario('Verify Upload Document', async ({I}) => {

    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);

    await uploadDocumentEvent(I, eventNames.UPLOAD_DOCUMENT);

}).tag('@RET_BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);
