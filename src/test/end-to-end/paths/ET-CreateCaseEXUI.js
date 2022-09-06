const testConfig = require('./../../config');
const {ETConstants} = require('../pages/common/constants.js');
Feature('ET Case Creation in EXUI');

Scenario('Create a case in EXUI happy path England and Wales', async ({ I,createCasePages }) => {

    await I.authenticateWithIdam();

    createCasePages.clickCreateCaseLink();
    I.wait(5);

    createCasePages.verifyCreateCaseInputPage();
    createCasePages.inputCreateCaseDetailsPage();
    createCasePages.clickStartButton();
    I.wait(5);

    createCasePages.verifyCreateCaseDateOfReceiptInputPage();
    createCasePages.inputCreateCaseDateOfReceiptInputPage();
    createCasePages.clickContinueButton();
    I.wait(5);

    createCasePages.verifyCreateCaseTypeOfClaimantPage();
    createCasePages.inputCreateCaseTypeOfClaimantPage();
    createCasePages.clickContinueButton();
    I.wait(5);

    createCasePages.verifyCreateCaseRespondentsPage();
    createCasePages.inputCreateCaseRespondentsPage();
    createCasePages.clickContinueButton();
    I.wait(5);

    createCasePages.verifyCreateCasePremisesPage();
    createCasePages.inputCreateCasePremisesPage();
    I.wait(5);
    pause();


}).tag('@CreateCase')
    //.retry(testConfig.TestRetryScenarios);

/*async submitNewCase(user, name) {
    const caseName = name || `Test case (${moment().format('YYYY-MM-DD HH:MM')})`;
    const creator = user || config.swanseaLocalAuthorityUserOne;
    const caseData = await apiHelper.createCase(creator, caseName);
    const caseId = caseData.id;
    output.print(`Case #${caseId} has been created`);

    return caseId;
}*/

/*
// done
login(email, password) {
    this.amOnPage('/');
    this.wait(CCPBConstants.twoSecondWaitTime);
    if (testConfig.e2e.testForCrossbrowser !== 'true') {
        this.resizeWindow(CCPBConstants.windowsSizeX, CCPBConstants.windowsSizeY);
        this.wait(CCPBConstants.twoSecondWaitTime);
    }
    this.fillField('Email address', email);
    this.fillField('Password', password);
    this.wait(CCPBConstants.twoSecondWaitTime);
    this.click({ css: '[type="submit"]' });
    this.wait(CCPBConstants.fiveSecondWaitTime);
},

Logout() {
    this.wait(CCPBConstants.fiveSecondWaitTime);
    this.click('Logout');
    this.wait(CCPBConstants.fiveSecondWaitTime);
},*/
