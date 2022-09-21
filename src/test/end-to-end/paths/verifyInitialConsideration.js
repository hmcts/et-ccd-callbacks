const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {initialConsideration} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Initial Consideration Happy Path - England and Wales');

Scenario('Verify Initial Consideration Journey', async ({I}) => {
    await processCaseToAcceptedState();
    await initialConsideration(I,eventNames.INITIAL_CONSIDERATION);

}).tag('@RET-WIP');
    //.retry(testConfig.TestRetryScenarios)