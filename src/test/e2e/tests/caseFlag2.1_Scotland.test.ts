import { test } from '../fixtures/common.fixture';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';
import LoginPage from '../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import { Page } from '@playwright/test';
import CreateCaseFlag from '../pages/createCaseFlag.ts';
import ManageSupportPage from '../pages/manageSupportPage.ts';
import ManageCaseFlag from '../pages/manageCaseFlag.ts';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';

let caseNumber: string;
let caseId: string;

test.describe('Case Flag 2.1 Scotland cases', () => {
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

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage,browserUtils }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etCaseWorker
    );

    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etLegalRepresentative);

    caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    loginPageCW = new LoginPage(caseWorkerBrowserPage);
    manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
    createCaseFlagPageCW = new CreateCaseFlag(caseWorkerBrowserPage);
    manageSupportPageCW = new ManageSupportPage(caseWorkerBrowserPage);
    manageCaseFlagPageCW = new ManageCaseFlag(caseWorkerBrowserPage);
  });


  //RET-6178,79
  test('Caseworker (Internal Case flags)- Create respondent and respondent representative case Flag for Scotland case', async ({
                                                                                                                                 manageCaseDashboardPage,
                                                                                                                                 nocPage
                                                                                                                               }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

    // login as Caseworker check the detail updated
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);

    //Create case flag
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondent('Confidential', 'Requested');
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondentRep();
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await createCaseFlagPageCW.validateCaseFlagForClaimantAndRep(false, false);
  });

  //RET-6178,79
  test('Caseworker (Internal Case flags)- Creates multiple Case Flags for respondent', async ({
                                                                                                manageCaseDashboardPage,
                                                                                                nocPage
                                                                                              }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

    // login as Caseworker check the detail updated
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);

    //Create multiple case flags
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondent('Confidential', 'Requested');
    await caseDetailsPageCW.selectNextEvent(Events.createFlag);
    await createCaseFlagPageCW.createCaseFlagForRespondent('Banning order', 'Active');
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await createCaseFlagPageCW.validateCaseFlagForClaimantAndRep(true, true);
  });

  //RET-6178,79
  test('Respondent legal representative (external case flags) - Creates a Case Flags for the respondent and respondent`s legal representative', async ({
                                                                                                                                                         manageCaseDashboardPage,
                                                                                                                                                         nocPage,
                                                                                                                                                         caseDetailsPage,
                                                                                                                                                         requestSupportPage,
                                                                                                                                                         manageSupportPage
                                                                                                                                                       }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);

    //Create case flag as a legal rep
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Respondent', 'forms');
    await caseDetailsPage.selectNextEvent(Events.requestSupport);
    await requestSupportPage.requestSupportFlag('Representative', 'building access');
    await caseDetailsPage.navigateToTab('Support');
    await manageSupportPage.validateSupportFlag(true);

    //caseworker can see support flag
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);
    await caseDetailsPageCW.navigateToTab('Case Flags');
    await manageSupportPageCW.validateSupportFlag(true);

  });
});
