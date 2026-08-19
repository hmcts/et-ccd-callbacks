import { test } from '../../fixtures/common.fixture.ts';
import { CaseworkerCaseFactory } from '../../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../../config/case-data.ts';
import { users } from '../../config/config.dynamic.ts';

test.describe('Initial Consideration Tests', () => {
  let caseId: string, caseNumber: string;

  test.use({
    storageState: users.etEnglandJudge.sessionFile,
  })

  test.beforeEach(async () => {
   ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test(
    'Initial Consideration - Preliminary Hearing options ',
    { tag: '@demo' },
    async ({ manageCaseDashboardPage, loginPage, caseDetailsPage, initialConsiderationPage}) => {
      await manageCaseDashboardPage.visit();
      //judge log in
      await loginPage.processLogin(
        users.etEnglandJudge
      );
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      await caseDetailsPage.selectNextEvent(Events.initialConsideration);
      await initialConsiderationPage.preliminaryHearingDetails()
      await caseDetailsPage.navigateToTab('Initial Consideration');
      await caseDetailsPage.assertTabData([
        {
          tabName: 'Initial Consideration',
          tabContent: [
            { tabItem: 'Type of preliminary hearing', value: 'Video' },
            { tabItem: 'Purpose of preliminary hearing', value: 'Preliminary issue' },
            { tabItem: 'Length of hearing', value: '1' },
          ],
        },
      ]);
    },
  );

  test(
    'Initial Consideration - Case Management Hearing Options ',
    { tag: '@demo' },
    async ({ manageCaseDashboardPage, loginPage, caseDetailsPage, initialConsiderationPage,listHearingPage}) => {
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPage.selectNextEvent(Events.listHearing);
      await listHearingPage.listCase('EnglandWales', 0,'Leeds ET','Preliminary Hearing (CM)');
      await caseDetailsPage.selectNextEvent(Events.initialConsideration);
      await initialConsiderationPage.caseManagementHearingOptions()
      await caseDetailsPage.navigateToTab('Initial Consideration');
      await caseDetailsPage.assertTabData([
        {
          tabName: 'Initial Consideration',
          tabContent: [
            { tabItem: 'Is this hearing judge alone or with members?', value: 'JSA' },
            { tabItem: 'Hearing With Jsa', value: 'Case management only' },

          ],
        },
      ]);
    },
  );

  test(
    'Initial Consideration - Final Hearing Options ',
    { tag: '@demo' },
    async ({ manageCaseDashboardPage, loginPage, caseDetailsPage, initialConsiderationPage,listHearingPage}) => {
      await manageCaseDashboardPage.visit();

      await loginPage.processLogin(users.etCaseWorker);

      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPage.selectNextEvent(Events.listHearing);
      await listHearingPage.listCase('EnglandWales', 0,'Leeds ET','Final Hearing');

      await caseDetailsPage.selectNextEvent(Events.initialConsideration);
      await initialConsiderationPage.finalHearingOptions()
      await caseDetailsPage.navigateToTab('Initial Consideration');
      await caseDetailsPage.assertTabData([
        {
          tabName: 'Initial Consideration',
          tabContent: [
            { tabItem: 'Is this hearing judge alone or with members?', value: 'With members' },
            { tabItem: 'Hearing With Members reasons', value: 'Already decided' },
          ],
        },
      ]);
    },
  );
});
