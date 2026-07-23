import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';
import LoginPage from '../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import ICUploadDocPage from '../pages/icUploadDocPage.ts';

let caseId: string;
let caseNumber: string;

test.describe('Respondent details test', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  // skipped as processPanelPreference functionality is changed
  test.skip(
    'England - Respondent details',
    { tag: '@demo' },
    async ({ respondentDetailsPage, caseDetailsPage, browserUtils }) => {
      await caseDetailsPage.selectNextEvent(Events.respondentDetails);
      // Check case file view
      await respondentDetailsPage.processPanelPreference();
      await caseDetailsPage.navigateToTab('Respondent');
      await respondentDetailsPage.verifyRespondentDetails();

      //judge log in
      const judgeBrowserPage = await browserUtils.openNewBrowserContext(users.etEnglandJudge.sessionFile);
      const loginPage = new LoginPage(judgeBrowserPage);
      const manageCaseDashboardPage = new ManageCaseDashboardPage(judgeBrowserPage);
      const caseDetailsPageJudge = new CaseDetailsPage(judgeBrowserPage);
      const icUploadDocPage = new ICUploadDocPage(judgeBrowserPage);

      await loginPage.processLogin(
        users.etEnglandJudge
      );
      await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPageJudge.selectNextEvent(Events.initialConsideration);
      await icUploadDocPage.verifyRespondentHearingPanelValues();
    });
});
