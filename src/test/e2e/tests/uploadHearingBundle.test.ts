import { test } from '../fixtures/common.fixture';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { users } from '../config/config.dynamic.ts';

let caseId: string;
let caseNumber: string;

test.describe('Upload Hearing Bundle as a Caseworker', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  //RET-5927
  test('Caseworker uploads hearing bundle', async ({
    uploadHearingBundlePage,
    listHearingPage, caseDetailsPage
  }) => {
    //List 2 hearings for the case
    const hearingNumbers: number[] = [0, 1];
    for (const number of hearingNumbers) {
      await caseDetailsPage.selectNextEvent(Events.listHearing);
      await listHearingPage.listCase('EnglandWales', number, 'Leeds ET');
    }
    await caseDetailsPage.selectNextEvent(Events.uploadHearingDocuments);
    await uploadHearingBundlePage.uploadHearingBundleDocuments();
    await caseDetailsPage.navigateToTab('Hearing Documents');
    await uploadHearingBundlePage.validateHearingDocument();
  });
});
