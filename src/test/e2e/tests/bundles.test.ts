import { test } from '../fixtures/common.fixture';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../config/case-data';
import DateUtilComponent from '../data-utils/DateUtilComponent';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import { users } from '../config/config.dynamic.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../pages/loginPage.ts';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import CitizenHubLoginPage from '../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../pages/claimantCitizenHub/CitizenHubPage.ts';
import PrepareAndSubmitDocumentPage from '../pages/claimantCitizenHub/PrepareAndSubmitDocumentPage.ts';
import { ListHearingPage } from '../pages/events/listHearingPage.ts';

let caseId: string;
let caseNumber: string;
//TODO: use browser context once List hearing api is done
test.describe('England - Caseworker Bundles test', () => {
    test.use({
      storageState: users.etLegalRepresentative.sessionFile,
    })

    test.beforeEach(async ({browserUtils}) => {
      ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());

      const csBroweserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
      const manageCaseDashboardPage = new ManageCaseDashboardPage(csBroweserPage);
      const loginPage = new LoginPage(csBroweserPage);
      const caseDetailsPage = new CaseDetailsPage(csBroweserPage);
      const listHearingPage = new ListHearingPage(csBroweserPage);

      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);

      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      let region = 'EnglandWales';
      await caseDetailsPage.selectNextEvent(Events.listHearing);
      await listHearingPage.listCase(region, 0, 'Leeds ET');
      await csBroweserPage.close();
    });

    test('Bundles - Legal rep submit hearing preparation document - England & Wales', async ({
       loginPage, manageCaseDashboardPage, nocPage, uploadDocumentsForHearingPage, caseDetailsPage,
      checkYourAnswersPage
    }) => {

      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(
        users.etLegalRepresentative
      );
      await manageCaseDashboardPage.navigateToNoticeOfChange();
      await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
      await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      await caseDetailsPage.selectNextEvent(Events.uploadDocumentsForHearing);
      const date = DateUtilComponent.formatToDayMonthYear(DateUtilComponent.addWeekdays(new Date(), 21));
      // //Verify only future hearings are shown in the options
      await uploadDocumentsForHearingPage.submitDocumentForHearing(
        {
          agreementOption: 'Yes',
          hearingOption: `1 Costs Hearing - Leeds ET - ${date}`,
          hearingList: [`1 Costs Hearing - Leeds ET - ${date}`],
          whoseDocuments: 'Both Parties',
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
            {
              tabItem: `Whose hearing documents are you uploading?`,
              value: `Both parties' hearing documents combined`,
            },
          ],
        },
      ]);
    });
});

test.describe('Scotland - Caseworker Bundles test', () => {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })

  test.beforeEach(async ({ browserUtils }) => {
      ({ caseId, caseNumber } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
      const csBroweserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
      const manageCaseDashboardPage = new ManageCaseDashboardPage(csBroweserPage);
      const loginPage = new LoginPage(csBroweserPage);
      const caseDetailsPage = new CaseDetailsPage(csBroweserPage);
      const listHearingPage = new ListHearingPage(csBroweserPage);

      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);
      let region = 'Scotland';
      await caseDetailsPage.selectNextEvent(Events.listHearing);
      await listHearingPage.listCase(region, 0, 'Glasgow', 'Expenses/Wasted Costs Hearing',"Sit Alone",'Scotland');
      await csBroweserPage.close();
    });

    //Flaky xui- claimant tab issue
    test(
      'Bundles - Legal rep submit hearing preparation document - Scotland',
      { tag: '@demo' },
      async ({ loginPage, manageCaseDashboardPage, nocPage, caseDetailsPage, legalRepPage }) => {
        // await bundleSteps.submitHearingPreparationDocument(page, 'Scotland', subRef, respondentName, firstName, lastName);
        await manageCaseDashboardPage.visit();

        await loginPage.processLogin(
          users.etLegalRepresentative
        );
        await manageCaseDashboardPage.navigateToNoticeOfChange();
        await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
        await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);

        await caseDetailsPage.selectNextEvent(Events.uploadDocumentsForHearing);
        await legalRepPage.submitDocumentForHearingRespondent('Yes', 'Both Parties', 'Witness statement only');
        await caseDetailsPage.navigateToTab('Hearing Documents');
        const date = DateUtilComponent.formatToDayMonthYear(DateUtilComponent.addWeekdays(new Date(), 21));
        await caseDetailsPage.assertTabData([
          {
            tabName: 'Hearing Documents',
            tabContent: [
              `Respondent Hearing Documents`,
              { tabItem: `Hearing these documents are for`, value: `Uploaded date | Document` },
              {
                tabItem: `1 Expenses/Wasted Costs Hearing - Glasgow - ${date}`,
                value: `${DateUtilComponent.getUtcDateTimeFormatted()} | welshTest.pdf`,
                clickable: true,
                exact: false,
              },
              { tabItem: `Have you agreed these documents with the other party?`, value: `Yes` },
              { tabItem: `Type`, value: `Hearing documents, including witness statements` },
              {
                tabItem: `Whose hearing documents are you uploading?`,
                value: `Both parties' hearing documents combined`,
              },
            ],
          },
        ]);
      },
    );
});

test.describe('England - Claimant Bundles test', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  test(
    'Bundles - Claimant Submitting hearing preparation document - England',
    { tag: '@demo' },
    async ({
      caseDetailsPage, browserUtils,
      listHearingPage,
    }) => {
      await caseDetailsPage.selectNextEvent(Events.listHearing);
      await listHearingPage.listCase('EnglandWales', 0, 'Amersham');

      const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
      const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
      const citizenHubPage = new CitizenHubPage(claimantBrowserPage);
      const prepareAbdSubmitDocumentPage = new PrepareAndSubmitDocumentPage(claimantBrowserPage);

      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
      await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
      await citizenHubPage.navigateToContactTheTribunalPage();
      await prepareAbdSubmitDocumentPage.submitDocumentsForHearing();
      await claimantBrowserPage.close();
    },
  );
});
