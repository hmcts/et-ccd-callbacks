import { test } from '../fixtures/et.test.fixture.ts';
import {
  CaseTypeLocation,
  CaseworkerCaseFactory,
  Events,
  resolveResourceJson,
  users
} from "@hmcts/et-shared-test-library";

let caseNumber: string;
let caseId: string;
let fileUploadData: any;

test.describe('Upload Document with multiple file extensions', () => {
    test.use({
      storageState: users.etCaseWorker.sessionFile,
    })

    test.beforeAll(async () => {
      fileUploadData = resolveResourceJson('resources/payload/file-upload-content.json');
    });
    test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
      ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);

      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    });

    test('Multiple file extension test', {tag: '@demo'}, async ({ uploadDocumentPage, caseDetailsPage }) => {

        const fileNames: string[] = [fileUploadData.textFile, fileUploadData.imageFile, fileUploadData.audioFile, fileUploadData.rtfFile];
        let i =1;

        for(const fileName of fileNames) {
            await caseDetailsPage.selectNextEvent(Events.uploadDocument);
            await uploadDocumentPage.uploadFile(fileName, i);
            await caseDetailsPage.navigateToTab('Documents');
            await uploadDocumentPage.verifyUploadDocuments(fileName);
            i++;
        }
    });
});
