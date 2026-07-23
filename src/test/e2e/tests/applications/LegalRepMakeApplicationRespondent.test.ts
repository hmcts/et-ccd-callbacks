import { test } from '../../fixtures/common.fixture.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import DateUtilComponent from '../../data-utils/DateUtilComponent.ts';
import { users } from '../../config/config.dynamic.ts';
import CitizenHubLoginPage from '../../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../../pages/claimantCitizenHub/CitizenHubPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseDetailsPage from '../../pages/caseDetailsPage.ts';
import { ApplicationTabPage } from '../../pages/applicationTabPage.ts';
import { CheckYourAnswersPage } from '../../pages/helpers/CheckYourAnswersPage.ts';

let caseId: string;
let caseNumber: string;

test.describe('LR Make an application and view Recorded Decision for respondent', () => {

  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })
  let citizenHubLoginPage: CitizenHubLoginPage;
  let citizenHubPage: CitizenHubPage;
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let loginPageCW: LoginPage;
  let caseDetailsPageCW: CaseDetailsPage;
  let applicationTabPageCW: ApplicationTabPage;
  let checkYourAnswersPageCW: CheckYourAnswersPage;

  test.beforeEach(async ({ manageCaseDashboardPage, browserUtils, loginPage, nocPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    // load browser context for claimant
    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
    citizenHubPage = new CitizenHubPage(claimantBrowserPage);

    // caseworker context
    const caseworkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseworkerBrowserPage);
    loginPageCW = new LoginPage(caseworkerBrowserPage);
    caseDetailsPageCW = new CaseDetailsPage(caseworkerBrowserPage);
    applicationTabPageCW = new ApplicationTabPage(caseworkerBrowserPage);
    checkYourAnswersPageCW = new CheckYourAnswersPage(caseworkerBrowserPage)

    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

  });

  //RET-5787
  // Test is failing at submission of application or responding to application in citizen UI, DEFECT RET-6573 needs fixing
  test.fail('Legal representatives make and application - England', async ({
    applicationTabPage, caseDetailsPage
  }) => {

    //legal rep make an application
    await caseDetailsPage.navigateToTab('Applications')
    await applicationTabPage.enterDetailsForMakingApplication('Amend response')

    // Claimant Reply to Application from Legal Rep
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.respondToAnApplication();

    //Case Worker Request for additional information (respond to claimant's response)
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Applications')
    await applicationTabPageCW.caseWorkerRespondToAnApplication('Amend response');

    //claimant see response of respond
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.validateResponseOfResponse();
  });

  //RET-5787
  test(
    'Legal representatives make and application, caseworker record a decision, LR and citizen view a decision - E/W',
    { tag: '@demo' },
    async ({
      manageCaseDashboardPage,
      loginPage,
      applicationTabPage,
      caseDetailsPage
    }) => {
      await loginPage.processLogin(
        users.etLegalRepresentative
      );

      //legal rep make an application
      await caseDetailsPage.navigateToTab('Applications');
      await applicationTabPage.enterDetailsForMakingApplication('Amend response');

      //caseworker records a decision
      await manageCaseDashboardPageCW.visit();
      await loginPageCW.processLogin(
        users.etCaseWorker
      );
      await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPageCW.navigateToTab('Applications');
      await applicationTabPageCW.enterDetailsForRecordADecision(checkYourAnswersPageCW, 'Amend response');

      await caseDetailsPageCW.navigateToTab('Applications');
      await caseDetailsPageCW.assertTabData([
        {
          tabName: 'Applications',
          tabContent: [
            { tabItem: 'Amend response', value: 'Respondent Representative', exact: false, clickable: true },
            {tabItem: 'Decision date', value: DateUtilComponent.formatToDayMonthYear(new Date())},
            { tabItem: 'Decision', value: 'Granted'}
          ]
        }
      ]);

      //Legal rep view decision in an application tab
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(
        users.etLegalRepresentative
      );
      await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPage.navigateToTab('Applications');
      await applicationTabPage.viewApplicationAndAssertDetails('Amend response', 'Open', [
        `Type of application - Amend response`,
        'Notification - Amend response',
        `Date - ${DateUtilComponent.formatToDayMonthYear(new Date())}`,
        `Sent by - Tribunal`
      ]);

      //citizen view notification about decision
      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
      //await citizenHubPage.validateRecordDecisionBanner(); RET-5707 bug ticket raised for the failing step
    },
  );

  test('England - submit ET3 as a legal Representative',
    async ({
             caseDetailsPage,
             checkYourAnswersPage,
             et3DetailsPage, et3RespondentDetailsPage,
             et3EmploymentDetailsPage, et3ResponseDetailsPage}) => {

    //perform all ET3 events as a LR
    await caseDetailsPage.selectNextEvent(Events.et3RespondentDetails);
    await et3RespondentDetailsPage.enterEt3RespondentDetails(checkYourAnswersPage);

    await et3DetailsPage.assertSubmitEt3ButtonNotVisible();

    await et3DetailsPage.navigateToEt3EmploymentDetailsPage();
    await et3EmploymentDetailsPage.enterEt3EmploymentDetails(checkYourAnswersPage);
    await et3DetailsPage.assertSubmitEt3ButtonNotVisible();

    await et3DetailsPage.navigateToEt3ResponseDetailsPage();
    await et3ResponseDetailsPage.enterEt3ResponseDetails(checkYourAnswersPage);

    await caseDetailsPage.selectNextEvent(Events.submitEt3Form);
    await et3DetailsPage.submitEt3Form(checkYourAnswersPage);
  });
});

