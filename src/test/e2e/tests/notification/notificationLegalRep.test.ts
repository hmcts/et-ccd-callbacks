import { test } from '../../fixtures/et.test.fixture.ts';
import {
  CaseTypeLocation, CaseWorkerNotificationPage,
  LegalRepCaseFactory,
  LoginPage,
  ManageCaseDashboardPage,
  users
} from "@et-shared-test-library/core";

let caseNumber: string;
let caseId: string;

test.describe('Legal Representative Notifications', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  //RET-5309
  test(
    'Legal Representative creates a claim and tribunal sends notification, Legal Rep view notification',
    { tag: '@demo' },
    async ({ manageCaseDashboardPage, caseWorkerNotificationPage, loginPage, browserUtils }) => {
      ({ caseId, caseNumber } = await LegalRepCaseFactory.createAndProgressToSubmitEnglandWalesCase());
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      //send Notification
      await caseWorkerNotificationPage.navigateToSendANotifications();
      await caseWorkerNotificationPage.sendNotification('ET1 claim');

      //view Notification as Legal rep
      const legalRepBrowserPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative.sessionFile);
      const loginPageLR = new LoginPage(legalRepBrowserPage);
      const manageCaseDashboardPageLR = new ManageCaseDashboardPage(legalRepBrowserPage);
      const caseWorkerNotificationPageLR = new CaseWorkerNotificationPage(legalRepBrowserPage);

      await manageCaseDashboardPageLR.visit();
      await loginPageLR.processLogin(
        users.etLegalRepresentative
      );
      await manageCaseDashboardPageLR.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseWorkerNotificationPageLR.viewNotification();
      await legalRepBrowserPage.close();
    },
  );
});
