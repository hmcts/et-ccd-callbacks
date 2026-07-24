import { test } from '../../fixtures/common.fixture.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { users } from '../../config/config.dynamic.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseWorkerNotificationPage from '../../pages/notifications/CaseWorkerNotificationPage.ts';

let caseId: string;
let caseNumber: string;

test.describe('Legal Rep Respond to an application made by caseworker', () => {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let loginPageCW: LoginPage;
  let caseWorkerNotificationPageCW: CaseWorkerNotificationPage;

  test.beforeEach(async ({browserUtils}) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

    const caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    loginPageCW = new LoginPage(caseWorkerBrowserPage);
    manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    caseWorkerNotificationPageCW = new CaseWorkerNotificationPage(caseWorkerBrowserPage);
  });

  //RET-5921
  test('Perform NOC using claimant details, caseworker sends notification and (claimant)legal rep respond to notification', async ({
    manageCaseDashboardPage,
    loginPage,
    legalRepNotificationPage,
    nocPage, caseDetailsPage
  }) => {
    const firstName = CaseDetailsValues.claimantFirstName;
    const lastName = CaseDetailsValues.claimantLastName;

    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);


    //caseworker send notification
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseWorkerNotificationPageCW.navigateToSendANotifications();
    const notificationTitle = await caseWorkerNotificationPageCW.sendNotification('ET1 claim', 'Yes', 'Both parties');

    //respond as a (claimant) Legal rep
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //respond to an application
    await caseDetailsPage.navigateToTab('Judgments, orders & notifications');
    await legalRepNotificationPage.respondToTribunal({
      notificationName: notificationTitle,
      responseText: 'Responding to notification from tribunal - claimant legal rep',
      supportingMaterialFiles: [],
      notifyOtherParty: 'Yes',
    });
  });

  //RET-5924
  // RET-6476
  test.skip('Perform NOC using respondent details, caseworker sends notification and (respondent)legal rep respond to notification', async ({
    manageCaseDashboardPage,
    loginPage,
    caseWorkerNotificationPage,
    legalRepNotificationPage,
    nocPage, caseDetailsPage
  }) => {
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

    //caseworker send notification
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseWorkerNotificationPageCW.navigateToSendANotifications();
    const notificationTitle = await caseWorkerNotificationPageCW.sendNotification('CMO', 'Yes', 'Both parties');

    //respond as a (respondent) Legal rep
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //respond to an application
    await caseDetailsPage.navigateToTab('Judgments, orders & notifications');
    await legalRepNotificationPage.respondToTribunal({
      notificationName: notificationTitle,
      responseText: 'Responding to notification from tribunal - Respondent legal rep',
      supportingMaterialFiles: [],
      notifyOtherParty: 'Yes',
    });
  });

});
