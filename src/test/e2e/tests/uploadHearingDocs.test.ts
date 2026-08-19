import { test } from '../fixtures/common.fixture';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../config/case-data';
import DateUtilComponent from '../data-utils/DateUtilComponent';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { users } from '../config/config.dynamic.ts';
import LoginPage from '../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import { ListHearingPage } from '../pages/events/listHearingPage.ts';

let caseId: string;
let caseNumber: string;

test.describe.serial('Upload hearing docs test', () =>  {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile
  })

    test.beforeEach('Data setup - Caseworker lists hearing',async ({ browserUtils }) => {
      ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
      const caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
      const loginPage = new LoginPage(caseWorkerBrowserPage);
      const manageCaseDashboardPage = new ManageCaseDashboardPage(caseWorkerBrowserPage);
      const caseDetailsPage = new CaseDetailsPage(caseWorkerBrowserPage);
      const listHearingPage = new ListHearingPage(caseWorkerBrowserPage);

      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      //List 2 hearings for the case
      const hearingNumbers: number[] = [0, 1];
      for (const number of hearingNumbers) {
        await caseDetailsPage.selectNextEvent(Events.listHearing);
        await listHearingPage.listCase('EnglandWales', number, 'Leeds ET');
        await caseDetailsPage.checkHasBeenCreated(Events.listHearing);
      }
      await caseWorkerBrowserPage.close();
    });

  //RET-5787
    test(
      'for respondent - verify only future hearings are shown in options',
      { tag: '@demo' },
      async ({
        loginPage,
        caseDetailsPage,
        uploadDocumentsForHearingPage,
        checkYourAnswersPage,
        nocPage,
        manageCaseDashboardPage,
      }) => {
        const date = DateUtilComponent.formatToDayMonthYear(DateUtilComponent.addWeekdays(new Date(), 21));

        await manageCaseDashboardPage.visit();
        await loginPage.processLogin(
          users.etLegalRepresentative
        );
        await manageCaseDashboardPage.navigateToNoticeOfChange();
        await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
        await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

        await caseDetailsPage.selectNextEvent(Events.uploadDocumentsForHearing);

        //Verify only future hearings are shown in the options
        await uploadDocumentsForHearingPage.submitDocumentForHearing(
          {
            agreementOption: 'Yes',
            hearingOption: `1 Costs Hearing - Leeds ET - ${date}`,
            hearingList: [`1 Costs Hearing - Leeds ET - ${date}`],
            whoseDocuments: 'Respondent',
            documentType: 'Witness statement only',
          },
          checkYourAnswersPage,
        );
        await caseDetailsPage.checkHasBeenCreated(Events.uploadDocumentsForHearing);

        await caseDetailsPage.assertTabData([
          {
            tabName: 'Hearing Documents',
            tabContent: [
              `Respondent Hearing Documents`,
              { tabItem: `Hearing`, value: `Uploaded date | Document` },
              {
                tabItem: `1 Costs Hearing - Leeds ET - ${date}`,
                value: `${DateUtilComponent.getUtcDateTimeFormatted()} | welshTest.pdf`,
                clickable: true,
                exact: false,
              },
              { tabItem: `Have you agreed these documents with the other party?`, value: `Yes` },
              { tabItem: `Type`, value: `Witness statements only` },
              { tabItem: `Whose hearing documents are you uploading?`, value: `Respondent's documents only` },
            ],
          },
        ]);
      },
    );
});
