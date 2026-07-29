import { test } from '../../fixtures/common.fixture.ts';
import { CaseworkerCaseFactory } from '../../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../../config/case-data.ts';
import { users } from '../../config/config.dynamic.ts';

test.describe('IC Upload documents', () => {
  let caseId: string, caseNumber: string;

  test.use({
    storageState: users.etEnglandJudge.sessionFile,
  })

  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test(
    'Judge uploads Internal consideration document',
    { tag: '@demo' },
    async ({ manageCaseDashboardPage, loginPage, icUploadDocPage, caseDetailsPage }) => {
      await manageCaseDashboardPage.visit();
      //judge log in
      await loginPage.processLogin(
        users.etEnglandJudge
      );
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      await caseDetailsPage.selectNextEvent(Events.initialConsideration);

      //judge uploads document
      await icUploadDocPage.judgeUploadsDocument();
      await caseDetailsPage.navigateToTab('Initial Consideration');

      //verify the uploaded document details
      await caseDetailsPage.assertTabData([
        {
          tabName: 'Initial Consideration',
          tabContent: [
            { tabItem: 'Initial Consideration Document', value: 'Initial Consideration.pdf' },
            { tabItem: 'Document', value: 'test-doc.pdf' },
            { tabItem: 'Initial consideration completed by:', value: 'ET Judge1' },
          ],
        },
      ]);
    },
  );
});
