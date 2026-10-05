import { test } from "../fixtures/et.test.fixture.ts";
import letterPageData from '@et-shared-test-library/core/resources/payload/letter-content.json';
import {CaseTypeLocation, CaseworkerCaseFactory, DateUtilComponent, Events, users} from "@et-shared-test-library/core";

let caseNumber: string;
let caseId: string;
test.describe('Generate Letters', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  test('ET2 - Short track letter', {tag: '@demo'}, async({caseListPage, listHearingPage, lettersPage,initialConsiderationPage, caseDetailsPage}) => {

      await caseDetailsPage.selectNextEvent(Events.listHearing);
      await listHearingPage.listCase('EnglandWales', 0,'Leeds ET');

      await caseDetailsPage.selectNextEvent(Events.letters);
      await lettersPage.generateShortTrackLetter();

      await caseListPage.verifyCaseDetailsOnTab(letterPageData.claimLabel, DateUtilComponent.formatTodaysDate(new Date()));
      await caseListPage.verifyCaseDetailsOnTab(letterPageData.et3DueDateLabel, DateUtilComponent.addDaysAndMonths(28));

      await caseDetailsPage.navigateToTab("BF Actions");
      await caseListPage.verifyBFActionsTab('Description', 'Other action');

      // RET-5793 Validate initial consideration hearing details
      await caseDetailsPage.selectNextEvent(Events.initialConsideration);
      await initialConsiderationPage.validateHearingDetails();
      await initialConsiderationPage.completeSubmissionWithHearing();
  });
});
