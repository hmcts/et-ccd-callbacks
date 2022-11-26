const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {clickCreateCase} = require("../helpers/caseHelper");
const {verifyApplicationTabs} = require("../helpers/caseHelper");
const commonConfig = require('./../data/commonConfig.json');
Feature('ET Case Creation on the Manage Case Portal');

Scenario('Case Creation Test', async ({ I }) => {

   await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);
   I.wait(commonConfig.time_interval_1_second);
   await clickCreateCase(I);
   I.wait(commonConfig.time_interval_2_seconds);
   I.click('Submit');
   I.wait(4);
   await verifyApplicationTabs(I);

}).tag('@RET-BAT').tag('@pats').tag('@nightly').tag('@crossbrowser').retry(testConfig.TestRetryScenarios);
