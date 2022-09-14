const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {respondentRepresentative} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Manchester Office Individual Single Case & Execute Respondent Representative');

Scenario('Verify Respondent Representative', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await respondentRepresentative(I, eventNames.RESPONDENT_REPRESENTATIVE);

}).tag('@nightly')
    .tag('@e2e')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
