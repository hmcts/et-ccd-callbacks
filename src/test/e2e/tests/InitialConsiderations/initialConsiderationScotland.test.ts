import { test } from '../../fixtures/et.test.fixture.ts';
import {CaseTypeLocation, CaseworkerCaseFactory, Events, users} from "@et-shared-test-library/core";
import fileUploadData from '@et-shared-test-library/core/resources/payload/file-upload-content.json';

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
