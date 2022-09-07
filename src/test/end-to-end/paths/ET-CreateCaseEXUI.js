const testConfig = require('./../../config');
const {ETConstants} = require('../pages/common/constants.js');
Feature('ET Case Creation in EXUI');

Scenario('Create a case in EXUI happy path England and Wales', async ({ I,createCasePages, createApplicationScreen}) => {

    await I.authenticateWithIdam();

    createCasePages.clickCreateCaseLink();
    I.wait(5);

    createCasePages.processCreateCaseInputPage();
    I.wait(5);

    createCasePages.processCreateCaseDateOfReceiptPage();
    I.wait(5);

    createCasePages.processCreateCaseTypeOfClaimantPage();
    I.wait(5);

    createCasePages.processCreateCaseRespondentPage();
    I.wait(5);

    createCasePages.processClaimantWorkAddress();
    I.wait(5);

    createCasePages.processCreateCaseOtherDetailsPage();
    I.wait(5);

    createCasePages.processIsClaimantRepresented();
    I.wait(5);

    createCasePages.processCreateCaseClaimantHearingPreferences();
    I.wait(5);

    createCasePages.clickSubmitButton();
    I.wait(5);

    await createApplicationScreen.getCaseNumberValue();
    createApplicationScreen.verifyGeneralApplicationScreenValues();
    createApplicationScreen.verifyCaseDetailsTab();




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
