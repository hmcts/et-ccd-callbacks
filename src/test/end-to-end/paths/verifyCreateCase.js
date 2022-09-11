const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {clickCreateCase} = require("../helpers/caseHelper");
Feature('ET Case Creation on the Manage Case Portal');

Scenario('Case Creation Test', async ({ I }) => {

   await I.authenticateWithIdam();
   I.wait(2);
   await clickCreateCase(I);
   I.click('Submit');

}).tag('@CreateCase') ;//.retry(testConfig.TestRetryScenarios)
