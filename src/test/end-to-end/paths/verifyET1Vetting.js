const testConfig = require('./../../config');
const { eventNames } = require('../pages/common/constants.js');
const {et1Vetting} = require("../helpers/caseHelper");
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
let testUrl = '/cases/case-details/1659533645259302'; //#Case%20Details
const { I } = inject();



//const { pageOneEt1Vetting } = require('../pages/et1Vetting/selectActionET1Vetting');

Feature('ET CCD ET1 Vetting Process');

Scenario('ET1 Case Vetting', async ({I}) => {
    const caseId = await createCaseInCcd()
    //await I.initiateEvent('et1Vetting');
    //await I.executeEvent('et1Vetting');
    await I.authenticateWithIdam();
    await I.amOnPage('/case-details/' + caseId);
    await et1Vetting(I, eventNames.ET1_VETTING);
    //await pageOneEt1Vetting.et1VettingProcessingPageOne
  
}).tag('@RET-BAT');
    //.retry(testConfig.TestRetryScenarios)   
  
  