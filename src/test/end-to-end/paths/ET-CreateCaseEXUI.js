const testConfig = require('./../../config');
const {ETConstants} = require('../pages/common/constants.js');
const { Logger } = require('@hmcts/nodejs-logging');
const logger = Logger.getLogger('ET-CreateCaseEXUI.js');

Feature('ET Case Creation in EXUI');

Scenario('Create a case in EXUI happy path England and Wales', async ({ I,createCasePages, createApplicationScreen}) => {

    await I.authenticateWithIdam();

    createCasePages.clickCreateCaseLink();
    logger.info('The Create Case Link is clicked and completed');
    //I.wait(7);

    createCasePages.processCreateCaseInputPage();
    logger.info('The processing of  the Create Case Input page is completed...');
    //I.wait(7);

    createCasePages.processCreateCaseDateOfReceiptPage();
    logger.info('The processing of  the Date of Receipt page is completed...');
    //I.wait(7);

    createCasePages.processCreateCaseTypeOfClaimantPage();
    logger.info('The processing of  the Case Type of Claimant page is completed...');
    //I.wait(7);

    createCasePages.processCreateCaseRespondentPage();
    logger.info('The processing of  the Respondent page is completed...');
    //I.wait(7);

    createCasePages.processClaimantWorkAddress();
    logger.info('The processing of  the Claimant Work Address page is completed...');
    //I.wait(7);

    createCasePages.processCreateCaseOtherDetailsPage();
    logger.info('The processing of the Other Details page is completed...');
    //I.wait(7);

    createCasePages.processIsClaimantRepresented();
    logger.info('The processing of the Is Claimant Represented is completed...');
    //I.wait(7);

    createCasePages.processCreateCaseClaimantHearingPreferences();
    logger.info('The processing of the Hearing Preferences is completed...');
    I.wait(7);

    createCasePages.clickSubmitButton();
    logger.info('Create Application is Submitted');
    I.wait(7);

    //await createApplicationScreen.getCaseNumberValue();
    createApplicationScreen.verifyGeneralApplicationScreenValues();
    logger.info('General Application Screen Values are verified....');
    createApplicationScreen.verifyCaseDetailsTab();
    logger.info('General Details Tab in Application Values are verified....');
    createApplicationScreen.verifyClaimantDetailsTab();
    logger.info('Claimant Details Tab in Application Values are verified....');
    createApplicationScreen.verifyRespondentsTab();
    logger.info('Respondents Tab in Application Values are verified....');
    createApplicationScreen.verifyReferralsTab();
    logger.info('Referrals Tab in Application Values are verified....');
    createApplicationScreen.verifyHistoryTab();
    logger.info('History Tab in Application Values are verified....');

}).tag('@RET-BAT')
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
