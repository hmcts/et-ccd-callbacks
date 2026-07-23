import { test  } from '../fixtures/common.fixture';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { users } from '../config/config.dynamic.ts';
import LoginPage from '../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import IssueJudgementPage from '../pages/events/issueJudgementPage.ts';

test.describe('Judgement tests', () => {
  test.use({
    storageState: users.etEnglandJudge.sessionFile,
  })

  let caseId: string;
  let caseNumber: string;

  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test(
    'Submit and Issue Judgement',
    { tag: '@demo' },
    async ({
      manageCaseDashboardPage,
      loginPage,
      caseDetailsPage,
      draftJudgementPage,
      browserUtils
    }) => {

      //Judge creates a draft judgement
      await manageCaseDashboardPage.visit();
      //judge logs in
      await loginPage.processLogin(
        users.etEnglandJudge
      );
      await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      //Create a draft judgment
      await caseDetailsPage.navigateToTab('Judgments');
      await caseDetailsPage.verifyAndClickLinkInTab('Draft and sign judgment/order');
      await draftJudgementPage.submitDraftJudgement();

      await caseDetailsPage.navigateToTab('Judgments');
      await caseDetailsPage.assertTabData([
        {
          tabName: 'Judgments',
          tabContent: [
            { tabItem: 'Is this a Judgment', value: 'Yes' },
            { tabItem: 'Document', value: 'test.txt' },
            { tabItem: 'Any further directions', value: 'Test Draft Judgement' },
          ],
        },
      ]);

      //Case worker issues the judgement
      const caseworkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
      const loginPageCW = new LoginPage(caseworkerBrowserPage);
      const manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseworkerBrowserPage);
      const caseDetailsPageCW = new CaseDetailsPage(caseworkerBrowserPage);
      const issueJudgementPageCW = new IssueJudgementPage(caseworkerBrowserPage);
      await manageCaseDashboardPageCW.visit();
      await loginPageCW.processLogin(
        users.etCaseWorker
      );
      await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      await caseDetailsPageCW.selectNextEvent(Events.judgment);
      await issueJudgementPageCW.issueJudgement();

      await caseDetailsPageCW.navigateToTab('Judgments');
      await caseDetailsPageCW.assertTabData([
        {
          tabName: 'Judgments',
          tabContent: [
            { tabItem: 'Jurisdiction Code', value: 'DAG' },
            { tabItem: 'Non Hearing Judgment?', value: 'Yes' },
            { tabItem: 'Judgment Type', value: 'Case Management' },
            { tabItem: 'Date Judgment made', value: '1 Jan 2025' },
            { tabItem: 'Date Judgment sent', value: '2 Jan 2025' },
          ],
        },
      ]);
    },
  );
})
