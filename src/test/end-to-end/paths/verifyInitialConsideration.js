const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {initialConsideration} = require("../helpers/caseHelper");
let testUrl = '/cases/case-details/1659525858049156';

Feature('Initial Consideration Happy Path - England and Wales');

Scenario('Verify Initial Consideration Journey', async ({I}) => {
    await I.authenticateWithIdam();
    await I.amOnPage(testUrl)
    await initialConsideration(I,eventNames.INITIAL_CONSIDERATION);

}).tag('@bat')
    .retry(testConfig.TestRetryScenarios)