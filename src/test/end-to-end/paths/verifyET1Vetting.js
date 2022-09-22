const { eventNames } = require('../pages/common/constants.js');
const {et1Vetting} = require("../helpers/caseHelper");
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
const testConfig = require('../../config');
let testUrl = '/cases/case-details/1659533645259302'; //#Case%20Details
const { I } = inject();


Feature('ET CCD ET1 Vetting Process');

Scenario('ET1 Case Vetting', async ({I}) => {
    const caseId = await createCaseInCcd()
    await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);
    await I.amOnPage('/case-details/' + caseId);
    await et1Vetting(I, eventNames.ET1_VETTING);
  
}).tag('@wip');
    //.retry(testConfig.TestRetryScenarios)   
  
