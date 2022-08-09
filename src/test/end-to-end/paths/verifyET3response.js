const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {et3Response} = require("../helpers/caseHelper");
//demo envt case
let testUrl = '/cases/case-details/1656658024203945';

Feature('ET3 Response Form Happy Path - England and Wales');

Scenario('Verify Initial Consideration Journey', async ({I}) => {
    await I.authenticateWithIdam();
    await I.amOnPage(testUrl)
    await et3Response(I,eventNames.ET3_RESPONSE);

}).tag('@wip');
    //.retry(testConfig.TestRetryScenarios)