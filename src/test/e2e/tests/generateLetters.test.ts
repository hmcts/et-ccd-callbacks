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

      await caseDetailsPage.assertTabData([
        {
          tabName: 'Case Details',
          tabContent:[
            { tabItem: letterPageData.claimLabel, value: DateUtilComponent.formatTodaysDate(new Date()) },
            { tabItem: letterPageData.et3DueDateLabel, value: DateUtilComponent.addDaysAndMonths(28) }
          ]
        },
        {
          tabName: 'BF Actions',
          tabContent: [
            { tabItem: DateUtilComponent.addDaysAndMonths(29), value: '', clickable: true },
            { tabItem: 'Description', value: 'Other action' },
          ]
        }
      ])

      // RET-5793 Validate initial consideration hearing details
      await caseDetailsPage.selectNextEvent(Events.initialConsideration);
      await initialConsiderationPage.validateHearingDetails();
      await initialConsiderationPage.completeSubmissionWithHearing();
  });
});
