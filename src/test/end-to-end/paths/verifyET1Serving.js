const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {et1Serving} = require("../helpers/caseHelper");

Feature('ET1 Serving Process');

const case_detail_url = '/cases/case-details/1659609053222055'

Scenario('progress application through et1 serving - happy path England and Wales', async ({ I }) => {
    await I.authenticateWithIdam();
    await I.amOnPage(case_detail_url)
    await et1Serving(I,eventNames.ET1_SERVING);

}).tag('@bat') .retry(testConfig.TestRetryScenarios)