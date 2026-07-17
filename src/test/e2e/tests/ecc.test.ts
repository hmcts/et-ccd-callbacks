import { test } from '../fixtures/common.fixture';
import { expect } from '@playwright/test';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { config, users } from '../config/config.dynamic.ts';
import CitizenHubPage from '../pages/claimantCitizenHub/CitizenHubPage.ts';
import CitizenHubLoginPage from '../pages/claimantCitizenHub/CitizenHubLoginPage.ts';

let subRef: any;
let caseNumber: any;

test.describe('ECC', () => {

  test.use({
  storageState: users.etCaseWorker.sessionFile,
  });

  test.describe.configure({ mode: 'default' });
  test.beforeEach(async ({ loginPage, manageCaseDashboardPage }) => {
    subRef = await CitizenClaimantFactory.progressCaseFromCreateToEt3(CaseTypeLocation.EnglandAndWales);
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker, config.manageCaseBaseUrl);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(subRef.toString(), CaseTypeLocation.EnglandAndWales);
  });

  test('Validate ECC flag is set on case', { tag: '@ecc' }, async ({ caseDetailsPage, jurisdictionPage }) => {
    await caseDetailsPage.selectNextEvent(Events.jurisdiction);
    await jurisdictionPage.addJurisdiction('BOC', 'Not allocated', 1);
    await jurisdictionPage.addJurisdiction('ECC', 'Not allocated', 2);
    await jurisdictionPage.clickSubmitButton();

    await expect(jurisdictionPage.page.getByRole('tab', { name: 'Case Details' }).locator('div')).toContainText(
      'Case Details',
    );
    await expect(jurisdictionPage.page.getByText('ECC', { exact: true })).toBeVisible();
  });

  test('Validate ECC error message', { tag: '@ecc' }, async ({ caseDetailsPage, jurisdictionPage }) => {
    await caseDetailsPage.selectNextEvent(Events.jurisdiction);
    await jurisdictionPage.addJurisdiction('ECC', 'Not allocated', 1);
    await jurisdictionPage.clickSubmitButton(false);
    await expect(
      jurisdictionPage.page.getByText(
        "ECC jurisdiction code can only be added if there's a corresponding BOC jurisdiction code",
        { exact: true },
      ),
    ).toBeVisible();
  });

  test(
    'ECC Notification - should create BF Action and show notification banner to claimant',
    { tag: '@ecc' },
    async ({ caseWorkerNotificationPage, caseListPage, caseDetailsPage, browserUtils }) => {
      await caseWorkerNotificationPage.navigateToSendANotifications();
      await caseWorkerNotificationPage.sendNotification('ECC');
      await expect(caseListPage.page.getByRole('tab', { name: 'BF Actions' })).toBeVisible();
      await caseDetailsPage.navigateToTab('BF Actions');
      await caseListPage.verifyBFActionsTab('Action', 'ECC served');

      // switch page context to Claimant user context
      const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
      const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      const citizenHubPage = new CitizenHubPage(claimantBrowserPage);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(subRef);
      await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
      await citizenHubPage.verifyNotificationBanner('ECC');
    },
  );

  test(
    'Should show ECC in table once set on respondent',
    { tag: '@ecc' },
    async ({ caseListPage, respondentDetailsPage, caseDetailsPage }) => {
      await caseDetailsPage.selectNextEvent(Events.respondentDetails);
      await respondentDetailsPage.processRespondentDetailsET3(true);
      await caseDetailsPage.selectNextEvent(Events.respondentDetails);
      await respondentDetailsPage.page.getByRole('group', { name: 'Is there an ECC?' }).getByLabel('Yes').check();
      await respondentDetailsPage.clickSubmitButton();
      await expect(caseListPage.page.getByRole('tab', { name: 'Respondents' })).toBeVisible();
      await caseDetailsPage.navigateToTab('Respondents');
      await expect(caseListPage.page.getByText('Is there an ECC?', { exact: true })).toBeVisible();
    },
  );
});
