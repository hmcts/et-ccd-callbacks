const testConfig = require('../config');
const commonConfig = require('./../data/commonConfig.json');
const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction, closeCase} = require("../helpers/caseHelper");
const {processCaseToAcceptedWithAJurisdiction} = require("../helpers/etCaseHepler");

Feature('Execute Case Close Scenario');

Scenario('Verify Case Close', async ({I}) => {
    let caseId = await processCaseToAcceptedWithAJurisdiction();
    console.log("... case id =>" +caseId);
    await closeCase(I, eventNames.CLOSE_CASE, commonConfig.clerkResponsible, commonConfig.physicalLocation)

}).tag('@nightly').tag('@RET-BAT').retry(testConfig.TestRetryScenarios);
