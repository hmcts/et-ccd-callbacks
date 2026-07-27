import { test } from '../../fixtures/common.fixture.ts';
import { LegalRepCaseFactory } from '../../data-utils/factory/exui/LegalRepCaseFactory.ts';
import { CaseTypeLocation } from '../../config/case-data.ts';
import { users } from '../../config/config.dynamic.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseWorkerNotificationPage from '../../pages/notifications/CaseWorkerNotificationPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';

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
