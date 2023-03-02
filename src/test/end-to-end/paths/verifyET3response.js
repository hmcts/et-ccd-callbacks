const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {et3Response} = require("../helpers/caseHelper");


Feature('ET3 Response Form Happy Path - England and Wales');

Scenario('Verify Initial Consideration Journey', async ({I}) => {
    await processCaseToAcceptedState();
    await et3Response(I,eventNames.ET3_RESPONSE);

}).tag('@wip').tag('@demo').tag('@biggerrefactoring');
    //.retry(testConfig.TestRetryScenarios)
