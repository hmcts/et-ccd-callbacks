import { test } from '../../fixtures/common.fixture.ts';
import { CaseworkerCaseFactory } from '../../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../../config/case-data.ts';
import fileUploadData from '../../resources/payload/file-upload-content.json';
import { users } from '../../config/config.dynamic.ts';

let caseNumber: string;
let caseId: string;

test.describe('Scotland Case Initial Consideration', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etCaseWorker
    );
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);
  });

  test('Tribunal Case file link on IC - Scotland case', {tag: '@demo'}, async ({ caseDetailsPage,uploadDocumentPage,initialConsiderationPage }) => {

    const fileName = fileUploadData.rtfFile
    await caseDetailsPage.selectNextEvent(Events.uploadDocument);
    await uploadDocumentPage.uploadFile(fileName, 1);
    await caseDetailsPage.navigateToTab('Documents');
    await uploadDocumentPage.verifyUploadDocuments(fileName);
    await uploadDocumentPage.createDCF();
    await initialConsiderationPage.waitForTribunalCaseFileLink();
    await caseDetailsPage.selectNextEvent(Events.initialConsideration);
    await initialConsiderationPage.validateTribunalCaseFileLink();
  });
});
