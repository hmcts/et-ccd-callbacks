import { test } from '../fixtures/common.fixture';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import LoginPage from '../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import { Page } from '@playwright/test';
import CreateCaseFlag from '../pages/createCaseFlag.ts';
import ManageSupportPage from '../pages/manageSupportPage.ts';
import ManageCaseFlag from '../pages/manageCaseFlag.ts';
import { NocPage } from '../pages/legalRepresentative/NocPage.ts';
import RequestSupportPage from '../pages/requestSupportPage.ts';

let caseNumber: string;
let caseId: string;

test.describe('Case Flag 2.1', () => {
  test.skip();
  let caseWorkerBrowserPage: Page;
  let loginPageCW: LoginPage;
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let caseDetailsPageCW: CaseDetailsPage;
  let createCaseFlagPageCW: CreateCaseFlag;
  let manageSupportPageCW: ManageSupportPage;
  let manageCaseFlagPageCW: ManageCaseFlag;

  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })
  test.beforeEach(async ({manageCaseDashboardPage, loginPage, browserUtils}) => {
     caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
     ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etLegalRepresentative);

    caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    loginPageCW = new LoginPage(caseWorkerBrowserPage);
    manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
    createCaseFlagPageCW= new CreateCaseFlag(caseWorkerBrowserPage);
    manageSupportPageCW = new ManageSupportPage(caseWorkerBrowserPage);
    manageCaseFlagPageCW = new ManageCaseFlag(caseWorkerBrowserPage);
  })

  //RET-6178,79
  test('Caseworker (Internal Case flags)- Creates claimant and claimant representative case Flag for E/W-Single case', async ({ manageCaseDashboardPage, nocPage,browserUtils, caseDetailsPage, manageSupportPage  }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, `${CaseDetailsValues.claimantFirstName} ${CaseDetailsValues.claimantLastName}`, caseNumber);

    // login as Caseworker check the detail updated
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //Create case flag
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForClaimant();
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForClaimantRep();
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await createCaseFlagPageCW.validateCaseFlagForClaimantAndRep(true, false);

    //judge can view internal flags
    const judgeBrowserPage = await browserUtils.openNewBrowserContext(users.etEnglandJudge.sessionFile);
    const manageCaseDashboardPageJudge = new ManageCaseDashboardPage(judgeBrowserPage);
    await manageCaseDashboardPageJudge.visit();
    const judgeLogin = new LoginPage(judgeBrowserPage);
    await judgeLogin.processLogin(users.etEnglandJudge);
    await manageCaseDashboardPageJudge.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    const judgeCaseDetailsPage = new CaseDetailsPage(judgeBrowserPage);
    await judgeCaseDetailsPage.navigateToTab('Case Flags');
    await createCaseFlagPageCW.validateCaseFlagForClaimantAndRep(true, false);

    //internal Flags not visible to LR
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPage.validateSupportTab();
  });

  //RET-6178,79
  test('Caseworker (Internal Case flags)- Create respondent and respondent representative case Flag for E/W-Single case', async ({ manageCaseDashboardPage, nocPage}) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

    // login as Caseworker check the detail updated
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //Create case flag
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondent('Confidential','Requested');
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondentRep();
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await createCaseFlagPageCW.validateCaseFlagForClaimantAndRep(false, false);
  });

  //RET-6178,79
  test('Caseworker (Internal Case flags)- Creates multiple Case Flags for respondent', async ({ manageCaseDashboardPage, nocPage }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

    // login as Caseworker check the detail updated
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //Create multiple case flags
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondent('Confidential', 'Requested');
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondent('Banning order','Active');
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await createCaseFlagPageCW.validateCaseFlagForClaimantAndRep(true, true);
  });

  //RET-6178,79
  test('Respondent legal representative (external case flags) - Creates a Case Flags for the respondent and respondent`s legal representative', async ({ manageCaseDashboardPage, nocPage, caseDetailsPage, requestSupportPage, manageSupportPage, browserUtils }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //Create case flag as a legal rep
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Respondent', 'forms');
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Representative', 'building access');
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPage.validateSupportFlag(true);

    //caseworker can see support flag
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateSupportFlag(true);

    //judge can view external flags
    const judgeBrowserPage = await browserUtils.openNewBrowserContext(users.etEnglandJudge.sessionFile);
    const manageCaseDashboardPageJudge = new ManageCaseDashboardPage(judgeBrowserPage);
    await manageCaseDashboardPageJudge.visit();
    const judgeLogin = new LoginPage(judgeBrowserPage);
    await judgeLogin.processLogin(users.etEnglandJudge);
    await manageCaseDashboardPageJudge.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    const judgeCaseDetailsPage = new CaseDetailsPage(judgeBrowserPage);
    await judgeCaseDetailsPage.navigateToTab('Case Flags');
    await manageSupportPage.validateSupportFlag(true);
  });

  //RET-6243
  test('Respondent legal representative- Creates a Case Flags for the respondent, and the caseworker updates the case flag status to Active', async ({ manageCaseDashboardPage, nocPage, caseDetailsPage, requestSupportPage, manageSupportPage }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //Create case flag as a legal rep
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Respondent', 'forms');
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPage.validateSupportFlag(false);

    //caseworker can see support flag
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateSupportFlag(false);

    //update case flag staus
    await caseDetailsPageCW.selectNextEvent(Events.manageFlag);
    await manageCaseFlagPageCW.updateCaseFlag('Active');
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateUpdatedSupportFlag('Active');

    //legal rep validates updated status
    //RET-6244
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPageCW.validateUpdatedSupportFlag('Active');
  });

  //RET-6245
  test('Respondent legal representative- Creates a Case Flags for the respondent, and the caseworker not approved/inactive case flags', async ({ manageCaseDashboardPage, nocPage, caseDetailsPage, requestSupportPage, manageSupportPage }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);


   //Create case flag as a legal rep
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Respondent', 'forms');
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Representative', 'building access');
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPage.validateSupportFlag(true);

    //caseworker can see support flag
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateSupportFlag(true);

    //update case flag staus
    await caseDetailsPageCW.selectNextEvent(Events.manageFlag);
    await manageCaseFlagPageCW.updateCaseFlag('Not approved');
    await caseDetailsPageCW.selectNextEvent(Events.manageFlag);
    await manageCaseFlagPageCW.updateCaseFlag('Inactive');
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateUpdatedSupportFlag('Not approved');
    await manageSupportPageCW.validateUpdatedSupportFlag('Inactive');

    //legal rep validates updated status
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPageCW.validateUpdatedSupportFlag('Not approved');
    await manageSupportPageCW.validateUpdatedSupportFlag('Inactive');
  });

  //RET-6229
  test('Respondent legal representative- Creates a Case Flags for the respondent, and inactive case flags as a LR', async ({ manageCaseDashboardPage, nocPage, caseDetailsPage, requestSupportPage, manageSupportPage }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);


    //Create case flag as a legal rep
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Respondent', 'forms');
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Representative', 'building access');
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPage.validateSupportFlag(true);


    //update case flag staus as LR
    await caseDetailsPage.selectNextEvent(Events.manageSupport);
    await manageSupportPage.manageSupportFlag();
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPage.validateInactiveFlag();

    //caseworker validates updated status
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateInactiveFlag();
  });

});



test.describe('Case Flag 2.1- Multiple respondents', () => {
  let caseWorkerBrowserPage: Page;
  let loginPageCW: LoginPage;
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let caseDetailsPageCW: CaseDetailsPage;
  let manageSupportPageCW: ManageSupportPage;

  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage, browserUtils }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales, true);
    ({ caseId, caseNumber } = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

    caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    loginPageCW = new LoginPage(caseWorkerBrowserPage);
    manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
    manageSupportPageCW = new ManageSupportPage(caseWorkerBrowserPage);
  })


  //RET-6178
  test('Caseworker- Creates case Flag for multiple respondents', async ({ manageCaseDashboardPage, browserUtils }) => {
    const legalRep1BrowserPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative.sessionFile);
    const legalRep2BrowserPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative2.sessionFile);
    const loginPageLR1 = new LoginPage(legalRep1BrowserPage);
    const loginPageLR2 = new LoginPage(legalRep2BrowserPage);
    const manageCaseDashboardPageLR1 = new ManageCaseDashboardPage(legalRep1BrowserPage);
    const manageCaseDashboardPageLR2 = new ManageCaseDashboardPage(legalRep2BrowserPage);
    const nocPageLR1 = new NocPage(legalRep1BrowserPage);
    const nocPageLR2 = new NocPage(legalRep2BrowserPage);
    const caseDetailsPageLR1 = new CaseDetailsPage(legalRep1BrowserPage);
    const caseDetailsPageLR2 = new CaseDetailsPage(legalRep1BrowserPage);
    const requestSupportPageLR1 =new RequestSupportPage(legalRep1BrowserPage);
    const requestSupportPageLR2 =new RequestSupportPage(legalRep1BrowserPage);
    const manageSupportPageLR1 = new ManageSupportPage(legalRep1BrowserPage);
    const manageSupportPageLR2 = new ManageSupportPage(legalRep1BrowserPage);


    // LR1 takes the case by NOC
    await manageCaseDashboardPageLR1.visit();
    await loginPageLR1.processLogin(users.etLegalRepresentative);
    await manageCaseDashboardPageLR1.navigateToNoticeOfChange();
    await nocPageLR1.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    await manageCaseDashboardPageLR1.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //Create case flag as a legal rep1
    await caseDetailsPageLR1.selectNextEvent(Events.requestSupport);
    await requestSupportPageLR1.requestSupportFlag('Respondent', 'forms');
    await caseDetailsPageLR1.navigateToTab('Support');
    await manageSupportPageLR1.validateSupportFlag(false);

    //caseworker can see support flag
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateSupportFlag(false);

    // LR2 takes the case by NOC
    await manageCaseDashboardPageLR2.visit();
    await loginPageLR2.processLogin(users.etLegalRepresentative2);
    await manageCaseDashboardPageLR2.navigateToNoticeOfChange();
    await nocPageLR2.processNocRequest(caseId, CaseDetailsValues.respondentName2, caseNumber);
    await manageCaseDashboardPageLR2.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //Create case flag as a legal rep2
    await caseDetailsPageLR2.selectNextEvent(Events.requestSupport);
    await requestSupportPageLR2.requestSupportFlag('Representative', 'building access');
    await caseDetailsPageLR2.navigateToTab('Support');
    await manageSupportPageLR2.validateSupportFlag(true);


    //caseworker can see support flag
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateSupportFlag(true);
  });
});

