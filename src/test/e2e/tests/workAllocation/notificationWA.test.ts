import { test } from '../../fixtures/common.fixture.ts';
import { users } from '../../config/config.dynamic.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import CitizenHubLoginPage from '../../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../../pages/claimantCitizenHub/CitizenHubPage.ts';
import { Helpers } from '../../pages/helpers/Helper.ts';
import { expect } from '@playwright/test';
import Et3LoginPage from '../../pages/respondentCitizenHub/et3LoginPage.ts';
import RespondentCaseOverviewPage from '../../pages/respondentCitizenHub/respondentCaseOverviewPage.ts';

let caseId: string;
let caseNumber: string;
let notificationTitle: string;

test.describe.serial('Notification Responses Work Allocation', () => {

  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  // waiting for RET-6610 to be finished
  test('Caseworker sends Notification, Respondent Citizen responds  and WA created for Response with correct number',
    async ({ manageCaseDashboardPage, loginPage, browserUtils, caseDetailsPage, page, caseWorkerNotificationPage }) => {

    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

    await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseWorkerNotificationPage.navigateToSendANotifications();
      notificationTitle = await caseWorkerNotificationPage.sendNotification('CMO', 'Yes', 'Both parties');

      const respondentBrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
      const et3LoginPage = new Et3LoginPage(respondentBrowserPage);
      const respondentCaseOverviewPage = new RespondentCaseOverviewPage(respondentBrowserPage);

      await et3LoginPage.processRespondentLogin(users.etRespondent);
      await et3LoginPage.replyToNewClaim(caseId, caseNumber, CaseDetailsValues.respondentName, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);
      await respondentCaseOverviewPage.respondToTribunalsNotification(notificationTitle);
      await respondentBrowserPage.close();

      await caseDetailsPage.navigateToTab('Tasks');
      await Helpers.waitForTask(page, 'Review notification response', 0);
      // remove NOT assertion once RET-6601 & 6610 is released
      await expect(page.locator(`//dt[normalize-space()='Next steps']/following-sibling::dd/a[normalize-space()='Respond to the notification response']`))
        .not.toBeVisible();
    });

  test('Caseworker sends Notification, Claimant Citizen responds and WA created for Response with correct number',
    async ({ manageCaseDashboardPage, loginPage, browserUtils, caseDetailsPage, page }) => {
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
      const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
      const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
      await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
      await citizenHubPage.verifyRespondToTheTribunalNotificationBanner('CMO');
      await citizenHubPage.respondToTheTribunalsNotification();
      await claimantBrowserPage.close();

      await caseDetailsPage.navigateToTab('Tasks');
      await Helpers.waitForTask(page, 'Review notification 1 response', 0);
      // remove NOT assertion once RET-6601 & 6610 is released
      await expect(page.locator(`//dt[normalize-space()='Next steps']/following-sibling::dd/a[normalize-space()='Respond to the notification response']`))
        .not.toBeVisible();
    });

});
