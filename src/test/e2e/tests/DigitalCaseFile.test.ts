import { test } from "../fixtures/et.test.fixture.ts";
import {CaseTypeLocation, CaseworkerCaseFactory, Events, users} from "@hmcts/et-shared-test-library";
let caseNumber: string;
let caseId: string;

test.describe('Digital Case File', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  });

  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test('Create a claim, perform DCF event', {tag: '@demo'}, async ({manageCaseDashboardPage, uploadDocumentPage, caseDetailsPage, loginPage}) => {
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etCaseWorker
    );
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(
      caseId,
      CaseTypeLocation.EnglandAndWales,
    );
    await caseDetailsPage.selectNextEvent(Events.uploadDocument);
    await uploadDocumentPage.uploadCaseManagementDocument();
    await caseDetailsPage.navigateToTab('Documents');
    await uploadDocumentPage.createDCF();
    await caseDetailsPage.navigateToTab('Documents');
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Documents',
        tabContent: ['Digital Case File', { tabItem: 'Status', value: 'DCF Updating:', exact: false }],
      }
    ]);
  });
});
