const testConfig = require('./../../config');
const commonConfig = require('./../data/commonConfig.json');
const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction, closeCase} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Execute Case Close Scenario');

Scenario('Verify Case Close', async ({I}) => {
    let caseId = await processCaseToAcceptedState();
    console.log("... case id =>" +caseId);
    await jurisdiction(I, eventNames.JURISDICTION);
    await closeCase(I, eventNames.CLOSE_CASE, commonConfig.clerkResponsible, commonConfig.physicalLocation)

}).tag('@wip')
    .tag('@nightly');
    //.retry(testConfig.TestRetryScenarios);
