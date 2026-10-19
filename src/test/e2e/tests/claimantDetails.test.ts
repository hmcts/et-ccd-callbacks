import { test } from '../fixtures/et.test.fixture.ts';
import {
  CaseDetailsPage,
  CaseTypeLocation,
  CaseworkerCaseFactory,
  Events, ICUploadDocPage, LoginPage,
  ManageCaseDashboardPage,
  users
} from "@et-shared-test-library/core";

let caseNumber: string;
let caseId: string;

test.describe('Claimant details test', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile
  });

  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test('England - Claimant details', {tag: ['@ccd-callback-tests', '@demo']},
    async ({manageCaseDashboardPage, loginPage, claimantDetailsPage, caseDetailsPage,
      browserUtils
           }) => {
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(
        users.etCaseWorker
      );

      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(
        caseId,
        CaseTypeLocation.EnglandAndWales,
      );
      await caseDetailsPage.selectNextEvent(Events.claimantDetails);
      // Check case file view
      await claimantDetailsPage.processClaimantDetails(true);
      await caseDetailsPage.navigateToTab('Claimant');
      await claimantDetailsPage.verifyClaimantDetails();

      //judge log in
      const judgeBrowserPage = await browserUtils.openNewBrowserContext(users.etEnglandJudge.sessionFile);
      const manageCaseDashboardPageJudge = new ManageCaseDashboardPage(judgeBrowserPage);
      await manageCaseDashboardPageJudge.visit();
      const judgeLogin = new LoginPage(judgeBrowserPage);
      await judgeLogin.processLogin(users.etEnglandJudge);
      await manageCaseDashboardPageJudge.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      const judgeCaseDetailsPage = new CaseDetailsPage(judgeBrowserPage);
      await judgeCaseDetailsPage.selectNextEvent(Events.initialConsideration);
      const judgeIcUploadPage = new ICUploadDocPage(judgeBrowserPage);
      await judgeIcUploadPage.verifyClaimantHearingPanelValues();
  });
});
