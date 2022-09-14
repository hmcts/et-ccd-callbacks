const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {respondentRepresentative} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Leeds Office Individual Case & Execute Respondent Representative');

Scenario('Verify Respondent Representative', async ({I}) => {

    let caseId = await processCaseToAcceptedState();
    await respondentRepresentative(I, eventNames.RESPONDENT_REPRESENTATIVE);

}).tag('@e2e')
    .tag('@nightly')
    .tag('@wip')
    .retry(testConfig.TestRetryScenarios);
