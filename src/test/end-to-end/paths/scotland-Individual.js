'use strict';
const testConfig = require('../../config');
const createCaseConfig = require('../pages/createCase/createCaseConfig');
Feature('Scotland individual Journey').retry(testConfig.TestRetryFeatures);

Scenario('01 BO Caveat E2E - Order summons', async ({ I }) =>  {

    I.authenticateWithIdam();
    I.selectNewCase();
    I.selectCaseTypeOptions(createCaseConfig.list1_text, createCaseConfig.list2_text_case_type_scotland, createCaseConfig.list3_text_event);
    I.enterCreateCasePage1();
    I.enterClaimantDetailsPage2();
    I.enterRespondentDetailPage3();
    I.enterClaimantWorkDetailsPage4();
    I.enterClaimantOtherDetailsPage5();
    I.enterBroughtForwardDatesPage6();
    I.enterClaimantRepresentedPage7();
    I.enterUploadDocPage8();
    I.submitPage9();
}).retry(testConfig.TestRetryScenarios);
