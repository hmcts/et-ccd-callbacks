import { test } from '../../fixtures/common.fixture.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseTypeLocation, Events } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { users } from '../../config/config.dynamic.ts';
import CitizenHubLoginPage from '../../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../../pages/claimantCitizenHub/CitizenHubPage.ts';

let caseNumber: any;
let caseId: string;

test.describe('Notification', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  //RET-5718
  test('Tribunal/caseworker sends ET1 claim notification to claimant', async ({
    browserUtils,
    caseWorkerNotificationPage
  }) => {

    //Caseworker send notification
    await caseWorkerNotificationPage.navigateToSendANotifications();
    await caseWorkerNotificationPage.sendNotification('ET1 claim');

    //claimant verify notification
    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
    const citizenHubPage = new CitizenHubPage(claimantBrowserPage);
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
    //claimant validates notification banner
    await citizenHubPage.verifyNotificationBanner('ET1 claim');
    await claimantBrowserPage.close();
  });

  test('Tribunal/caseworker sends CMO notification to claimant', async ({
    browserUtils,
    caseWorkerNotificationPage,
  }) => {
    //Caseworker send notification
    await caseWorkerNotificationPage.navigateToSendANotifications();
    await caseWorkerNotificationPage.sendNotification('CMO');

    //claimant verify notification
    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
    const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
    //claimant validates notification banner
    await citizenHubPage.verifyNotificationBanner('CMO');
    await claimantBrowserPage.close();
  });

  //RET-4646
  test('Tribunal/caseworker sends Hearing notification to claimant', async ({
    browserUtils, caseDetailsPage,
    listHearingPage,
    caseWorkerNotificationPage,
  }) => {
    //list hearing
    await caseDetailsPage.selectNextEvent(Events.listHearing);
    await listHearingPage.listCase('EnglandWales', 0, 'Amersham');

    //Caseworker send notification
    await caseWorkerNotificationPage.navigateToSendANotifications();
    await caseWorkerNotificationPage.sendNotification('Hearing');

    //claimant verify notification
    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
    const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);

    //claimant validates notification banner
    await citizenHubPage.verifyNotificationBanner('Hearing');
    await claimantBrowserPage.close();
  });

  test('Tribunal/caseworker perform ET1 serving event with 7.7 type document',
    async ({
    caseDetailsPage,
    et1CaseServingPage,
  }) => {
    //Caseworker perform ET1 serving notification
    await caseDetailsPage.selectNextEvent(Events.et1Serving);
    await et1CaseServingPage.et1ServingEvent(false);
    await et1CaseServingPage.validateEt1ErrorMessage();
  });

  //RET-5850, 5627
  test('Tribunal/caseworker perform ET1 serving, claimant validates notification',
    async ({browserUtils,
    caseDetailsPage,
    et1CaseServingPage,
  }) => {
    //Caseworker perform ET1 serving notification
    await caseDetailsPage.selectNextEvent(Events.et1Serving);
    await et1CaseServingPage.et1ServingEventNoticeOfClaim();

    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
    const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);

    //claimant validates notification banner
    await citizenHubPage.verifyNotificationBannerForNoticeOfClaim();
    await claimantBrowserPage.close();
  });
});
