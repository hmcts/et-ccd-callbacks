import { test } from '../../fixtures/et.test.fixture.ts';
import {Page} from "@playwright/test";
import {
  CaseDetailsPage,
  CaseDetailsValues, CaseEventApi, CaseTypeLocation, CitizenClaimantFactory, Events,
  LoginPage,
  ManageCaseDashboardPage,
  RespondentRepPage, users
} from "@et-shared-test-library/core";

let caseId: string;
let caseNumber: string;
let firstName: string;
let lastName: string;

test.describe('Legalrep/Caseworker add or remove Representative via NoC or manage case event', () => {
  let caseWorkerBrowserPage: Page;
  let loginPageCW: LoginPage;
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let caseDetailsPageCW: CaseDetailsPage;
  let respondentRepPageCW: RespondentRepPage;
  firstName = CaseDetailsValues.claimantFirstName;
  lastName = CaseDetailsValues.claimantLastName;
  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  });

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage, browserUtils }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({ caseId, caseNumber } = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etLegalRepresentative);

    caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    loginPageCW = new LoginPage(caseWorkerBrowserPage);
    manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
    respondentRepPageCW = new RespondentRepPage(caseWorkerBrowserPage);

  });

  //RET-6074
  test('validate removal of respondent representatives when self-assigning(via noc) as claimant  representative with same organisation', async ({
                                                                              manageCaseDashboardPage,
                                                                              nocPage,
                                                                            }) => {
    //perform noc for respondent representative
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales, true);


    //Caseworker can see a respondent representative
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Respondent Representative');
    await caseDetailsPageCW.assertTabData([
      {
        tabName: 'Respondent Representative',
        tabContent: [
          { tabItem: 'Name:', value: 'ET Test Factory Solicitor' },
          { tabItem: 'Email address', value: users.etLegalRepresentative.email },
        ],
      },
    ]);

    //perform noc for claimant representative
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales, true);

    //caseworker can see a claimant representative
    await caseDetailsPageCW.page.reload();
    await caseDetailsPageCW.navigateToTab('Claimant Representative');
    await caseDetailsPageCW.assertTabData([
      {
        tabName: 'Claimant Representative',
        tabContent: [
          { tabItem: 'Name:', value: 'ET Test Factory Solicitor' },
          { tabItem: 'Email address', value: users.etLegalRepresentative.email },
        ],
      },
    ]);

  });


  //RET-6074
  test('validate removal of claimant representatives when caseworker assign respondent representative via manage case event', async ({
                                                                                                                                                  manageCaseDashboardPage,
                                                                                                                                                  nocPage,
                                                                                                                                                }) => {
    //perform noc for claimant representative
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales, true);


    //Caseworker can see a claimant representative
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Claimant Representative');
    await caseDetailsPageCW.assertTabData([
      {
        tabName: 'Claimant Representative',
        tabContent: [
          { tabItem: 'Name:', value: 'ET Test Factory Solicitor' },
          { tabItem: 'Email address', value: users.etLegalRepresentative.email },
        ],
      },
    ]);

    //caseworker assign respondent representative via manage case event
    await caseDetailsPageCW.selectNextEvent(Events.respondentRepresentative);
    await respondentRepPageCW.addRespondentRepresentative('registered', 'ET Test Factory Solicitor');

    //caseworker can see a Respondent representative
    await caseDetailsPageCW.navigateToTab('Respondent Representative');
    await caseDetailsPageCW.assertTabData([
      {
        tabName: 'Respondent Representative',
        tabContent: [
          { tabItem: 'Name:', value: 'ET Test Factory Solicitor' },
          { tabItem: 'Email address', value: users.etLegalRepresentative.email },
        ],
      },
    ]);

  });

});
