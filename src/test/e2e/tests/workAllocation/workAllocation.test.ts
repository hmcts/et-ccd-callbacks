import { test } from '../../fixtures/common.fixture.ts';
import { Helpers } from '../../pages/helpers/Helper.ts';
import { CaseTypeLocation, Events } from '../../config/case-data.ts';
import { CaseworkerCaseFactory } from '../../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { users } from '../../config/config.dynamic.ts';
import LoginPage from '../../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import CaseDetailsPage from '../../pages/caseDetailsPage.ts';
import ReferralPage from '../../pages/referralPage.ts';
import DraftJudgementPage from '../../pages/events/draftJudgementPage.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import RolesAndAccessPage from '../../pages/rolesAndAccessPage.ts';
import { expect } from '@playwright/test';

let caseNumber: string;
let caseId: string;

test.describe.serial('Work Allocation', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  test('CTSC user assign a task to itself and completes a task', async ({
    page,
    caseDetailsPage,
    et1VettingPage, manageCaseDashboardPage, loginPage
  }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //user completes a task
    await caseDetailsPage.navigateToTab('Tasks');
    await Helpers.assignTaskToMeAndTriggerNextSteps(page, 'Et1 Vetting', 'ET1 Vetting');
    await et1VettingPage.processET1CaseVettingPages();
  });

  test('Caseworker sends Referral to Judge- Referral task generated, Judge assign and completes referral task',
    async ({
    referralPage, caseDetailsPage, browserUtils, manageCaseDashboardPage
  }) => {
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    //send referral
    await caseDetailsPage.navigateToTab('Referrals');
    await caseDetailsPage.verifyAndClickLinkInTab('Send a new referral');
    await referralPage.sendNewReferral('Judge');

    //verify referral details
    await caseDetailsPage.navigateToTab('Referrals');
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Referrals',
        tabContent: [
          { tabItem: 'ET1', value: ' | Judge | Yes', clickable: true, exact: false },
          'Awaiting instructions',
          { tabItem: 'Details of the referral', value: 'This is a test referral' },
        ]
      }
    ]);

    //log in as judge & assign and completes a task
    const judgeBrowserPage = await browserUtils.openNewBrowserContext(users.etWorkAllocationJudge.sessionFile);
    const loginPageJudge= new LoginPage(judgeBrowserPage);
    const manageCaseDashboardPageJudge = new ManageCaseDashboardPage(judgeBrowserPage);
    const caseDetailsPageJudge = new CaseDetailsPage(judgeBrowserPage);
    const referralPageJudge = new ReferralPage(judgeBrowserPage);
    await loginPageJudge.processLogin(
      users.etWorkAllocationJudge
    );
    caseNumber = await manageCaseDashboardPageJudge.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageJudge.navigateToTab('Tasks');

    await Helpers.assignTaskToMeAndTriggerNextSteps(judgeBrowserPage, 'EJ - Review Referral #1 - ET1', 'Reply to the Referral');
    await referralPageJudge.replyToReferral('Judge');

    await caseDetailsPageJudge.navigateToTab('Tasks');
    await Helpers.waitForTask(judgeBrowserPage, 'EJ - Review Referral Response #1 - ET1');
    await judgeBrowserPage.close();
  });

  test('Caseworker sends Referral to Legal Officer- Referral task generated, CS assign and completes referral task',
    async ({
          page, referralPage, caseDetailsPage, browserUtils, manageCaseDashboardPage
        }) => {
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    //send referral
    await caseDetailsPage.navigateToTab('Referrals');
    await caseDetailsPage.verifyAndClickLinkInTab('Send a new referral');
    await referralPage.sendNewReferral('Legal Officer');

    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.navigateToTab('Tasks');
    await Helpers.waitForTask(page, 'LO - Review Referral #2 - ET1');

    //verify referral details
    await caseDetailsPage.navigateToTab('Referrals');
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Referrals',
        tabContent: [
          { tabItem: 'ET1', value: ' | Legal officer | Yes', clickable: true, exact: false, position: 1 },
          'Awaiting instructions',
          { tabItem: 'Details of the referral', value: 'This is a test referral' },
        ]
      }
    ]);

    //log in as Legal Ops & assign and update a task
    const legalOpsPage = await browserUtils.openNewBrowserContext(users.etLegalOpsUser.sessionFile);
    const loginPage= new LoginPage(legalOpsPage);
    const manageCaseDashboardPageLO = new ManageCaseDashboardPage(legalOpsPage);
    const caseDetailsPageLO = new CaseDetailsPage(legalOpsPage);
    const referralPageLO = new ReferralPage(legalOpsPage);
    const rolesAndAccessLO = new RolesAndAccessPage(legalOpsPage);
    await loginPage.processLogin(
      users.etManageCaseUser
    );
    caseNumber = await manageCaseDashboardPageLO.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageLO.navigateToTab('Roles and access');
    await rolesAndAccessLO.assignAccessToLegalOpsRoleToMe();

    await caseDetailsPageLO.navigateToTab('Tasks');
    await Helpers.assignTaskToMeAndTriggerNextSteps(legalOpsPage, 'LO - Review Referral #2 - ET1', 'Update the Referral');
    await referralPageLO.updateTheReferral('2 - ET1','Judge');

    await caseDetailsPageLO.navigateToTab('Tasks');
    await Helpers.waitForTask(legalOpsPage, 'EJ - Review Referral #2 - ET1');

    // RET-6512 since Legal ops already assigned as allocated-tribunal-caseworker,
    // above new task should be assigned automatically, thus showing next steps
    await expect(legalOpsPage.locator(`//p[normalize-space()='EJ - Review Referral #2 - ET1']/following-sibling::dl//div/dt/span[normalize-space()='Next steps']`)).toBeVisible();
    await legalOpsPage.close();
  });
});

test.describe('Work Allocation- Judge completes tasks', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  test('Judge completes Draft and sign document task', async ({
    page,
    listHearingPage,
    hearingDetailsPage,
    caseDetailsPage, browserUtils
  }) => {
    //list past hearing
    await caseDetailsPage.selectNextEvent(Events.listHearing);
    await listHearingPage.listCase('EnglandWales', 0, 'Leeds ET');

    //update hearing
    await caseDetailsPage.selectNextEvent(Events.hearingDetails);
    await hearingDetailsPage.updateHearing();
    //wait for draft and sign document task
    await caseDetailsPage.navigateToTab('Tasks');
    await Helpers.waitForTask(page, 'Draft And Sign Judgment');

    //login as judge and assign a task
    const judgeBrowserPage = await browserUtils.openNewBrowserContext(users.etWorkAllocationJudge.sessionFile);
    const loginPage= new LoginPage(judgeBrowserPage);
    const manageCaseDashboardPage = new ManageCaseDashboardPage(judgeBrowserPage);
    const caseDetailsPageJudge = new CaseDetailsPage(judgeBrowserPage);
    const draftJudgementPage = new DraftJudgementPage(judgeBrowserPage);

    await loginPage.processLogin(
      users.etWorkAllocationJudge
    );
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageJudge.navigateToTab('Tasks');

    await Helpers.assignTaskToMeAndTriggerNextSteps(judgeBrowserPage, 'Draft And Sign Judgment/Order', 'Draft and Sign Judgment');
    await draftJudgementPage.submitDraftJudgement();
  });
});

test.describe.serial('Cancelling Work Allocation tasks', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  test('Caseworker transfers case to ECM and waits for all Work allocation to be closed', async ({
                                                                          page,
                                                                          caseDetailsPage,
                                                                          et1VettingPage, manageCaseDashboardPage, loginPage, caseTransferToEcmPage
                                                                        }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId);

    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //user completes a task
    await caseDetailsPage.navigateToTab('Tasks');
    await Helpers.waitForTask(page, 'Et1 Vetting');

    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.selectNextEvent(Events.caseTransferToEcm);
    await caseTransferToEcmPage.transferCaseToEcm('Newcastle', 'Test case Transfer To ECM');

    await caseDetailsPage.assertTabData([
      {
        tabName: 'Case Details',
        tabContent: [
          'Case Status: Transferred',
          { tabItem: 'Reason for Case Transfer', value: 'Test case Transfer To ECM'},
          'Case Transfer: Transferred to ECM'
        ]
      }
    ]);

    await caseDetailsPage.navigateToTab('Tasks');
    await caseTransferToEcmPage.waitForWorkAllocationTasksToDisappear();
  });
});
