import { cuiApi, test } from '../../fixtures/common.fixture.ts';
import { CaseTypeLocation } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import DateUtilComponent from '../../data-utils/DateUtilComponent.ts';
import { users } from '../../config/config.dynamic.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseDetailsPage from '../../pages/caseDetailsPage.ts';
import { ApplicationTabPage } from '../../pages/applicationTabPage.ts';
import { CheckYourAnswersPage } from '../../pages/helpers/CheckYourAnswersPage.ts';
import { LegalRepCaseFactory } from '../../data-utils/factory/exui/LegalRepCaseFactory.ts';
import Et3LoginPage from '../../pages/respondentCitizenHub/et3LoginPage.ts';
import ResClaimantsApplicationsPage from '../../pages/respondentCitizenHub/resClaimantsApplicationsPage.ts';

let caseId: string;
let caseNumber: string;

//RET-5083
test.describe('LR Make an application and view Recorded Decision for respondent - Both Online', () => {

  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })
  let et3LoginPage: Et3LoginPage;
  let resClaimantsApplicationPage: ResClaimantsApplicationsPage;
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let loginPageCW: LoginPage;
  let caseDetailsPageCW: CaseDetailsPage;
  let applicationTabPageCW: ApplicationTabPage;
  let checkYourAnswersPageCW: CheckYourAnswersPage;

  test.beforeEach(async ({ manageCaseDashboardPage, browserUtils, loginPage, nocPage }) => {
    ({caseId, caseNumber} = await LegalRepCaseFactory.createAndProgressToSubmitEnglandWalesCase());
    await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId);
    const respondentCcdId = await cuiApi.assignCaseToRespondent(
      users.etRespondent.email,
      users.etRespondent.password,
      caseId,
    );
    await cuiApi.submitET3(
      users.etRespondent.email,
      users.etRespondent.password,
      respondentCcdId,
      caseId,
      CaseTypeLocation.EnglandAndWales,
    );
    await manageCaseDashboardPage.visit();

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
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    // load browser context for Respondent
    const respondentBrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
    et3LoginPage = new Et3LoginPage(respondentBrowserPage);
    resClaimantsApplicationPage = new ResClaimantsApplicationsPage(respondentBrowserPage);
  });

  test('Legal representatives make an application - England - Online', async ({
    applicationTabPage, caseDetailsPage
  }) => {

    //legal rep make an application
    await caseDetailsPage.navigateToTab('Applications')
    await applicationTabPage.enterDetailsForMakingApplication('Amend claim')

    //Case Worker Request for additional information (respond to claimant's response)
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Applications')
    await applicationTabPageCW.caseWorkerRespondToAnApplication('Amend claim');

    // Respondent Citizen responds to application
    await et3LoginPage.processRespondentLoginForExistingCase(users.etRespondent, caseNumber);
    await resClaimantsApplicationPage.navigateToClaimantsApplicationsPage();
    await resClaimantsApplicationPage.assertClaimantsApplicationsPageVisibile();
    await resClaimantsApplicationPage.navigateToListedClaimantsApplication('Amend my claim');
    await resClaimantsApplicationPage.assertClaimantsApplicationInformation('Amend my claim',
      ['Applicant - Claimant Representative', 'Application type - Amend my claim', 'What do you want to tell or ask the tribunal? - Details of application for Amend claim',
      'Response - Response of an application By Caseworker']);
    await resClaimantsApplicationPage.respondToApplication('Amend my claim', true);
  });

  test('Legal representatives make Withdraw application - England - Online', async ({
                                                                                 applicationTabPage, caseDetailsPage
                                                                               }) => {

    //legal rep make an application
    await caseDetailsPage.navigateToTab('Applications')
    await applicationTabPage.enterDetailsForMakingApplication('Withdraw all or part of claim')

    //Case Worker Request for additional information (respond to claimant's response)
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Applications')
    await applicationTabPageCW.caseWorkerRespondToAnApplication('Withdraw all or part of claim');

    // Respondent Citizen responds to application
    await et3LoginPage.processRespondentLoginForExistingCase(users.etRespondent, caseNumber);
    await resClaimantsApplicationPage.navigateToClaimantsApplicationsPage();
    await resClaimantsApplicationPage.assertClaimantsApplicationsPageVisibile();
    await resClaimantsApplicationPage.navigateToListedClaimantsApplication('Withdraw all or part of claim');
    await resClaimantsApplicationPage.assertClaimantsApplicationInformation('Withdraw all or part of claim',
      ['Applicant - Claimant Representative', 'Application type - Withdraw all or part of claim', 'What do you want to tell or ask the tribunal? - Details of application for Withdraw all or part of claim',
        'Response - Response of an application By Caseworker']);
    await resClaimantsApplicationPage.respondToApplication('Withdraw all or part of claim', true);
  });

  test(
    'Legal representatives make and application, caseworker record a decision, LR and citizen view a decision - E/W - online',
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
      await applicationTabPage.enterDetailsForMakingApplication('Amend claim');

      //caseworker records a decision
      await manageCaseDashboardPageCW.visit();
      await loginPageCW.processLogin(
        users.etCaseWorker
      );
      await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPageCW.navigateToTab('Applications');
      await applicationTabPageCW.enterDetailsForRecordADecision(checkYourAnswersPageCW, 'Amend claim');

      await caseDetailsPageCW.navigateToTab('Applications');
      await caseDetailsPageCW.assertTabData([
        {
          tabName: 'Applications',
          tabContent: [
            { tabItem: 'Amend claim', value: 'Claimant Representative', exact: false, clickable: true },
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
      await applicationTabPage.viewApplicationAndAssertDetails('Amend claim', 'Open', [
        `Type of application - Amend claim`,
        'Notification - Amend claim',
        `Date - ${DateUtilComponent.formatToDayMonthYear(new Date())}`,
        `Sent by - Tribunal`
      ]);

      // Respondent Citizen responds to application
      await et3LoginPage.processRespondentLoginForExistingCase(users.etRespondent, caseNumber);
      await resClaimantsApplicationPage.navigateToClaimantsApplicationsPage();
      await resClaimantsApplicationPage.assertClaimantsApplicationsPageVisibile();
      await resClaimantsApplicationPage.navigateToListedClaimantsApplication('Amend my claim');
      await resClaimantsApplicationPage.assertClaimantsApplicationInformation('Amend my claim',
        ['Applicant - Claimant Representative', 'Application type - Amend my claim', 'What do you want to tell or ask the tribunal? - Details of application for Amend claim',
          'Notification - Amend claim', 'Decision - Granted', 'Type of decision - Judgment']);
      await resClaimantsApplicationPage.respondToApplication('Amend my claim', true);
    },
  );
});

test.describe('LR Make an application and view Recorded Decision for respondent - Respondent Offline', () => {

  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let loginPageCW: LoginPage;
  let caseDetailsPageCW: CaseDetailsPage;
  let applicationTabPageCW: ApplicationTabPage;
  let checkYourAnswersPageCW: CheckYourAnswersPage;

  test.beforeEach(async ({ manageCaseDashboardPage, browserUtils, loginPage, nocPage }) => {
    ({caseId, caseNumber} = await LegalRepCaseFactory.createAndProgressToSubmitEnglandWalesCase());
    await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId);
    await manageCaseDashboardPage.visit();

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
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  test('Offline - Legal representatives make and application - England', async ({
                                                                        applicationTabPage, caseDetailsPage
                                                                      }) => {

    //legal rep make an application
    await caseDetailsPage.navigateToTab('Applications')
    await applicationTabPage.enterDetailsForMakingApplication('Postpone a hearing');

    //Case Worker Request for additional information (respond to claimant's response)
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Applications')
    await applicationTabPageCW.caseWorkerRespondToAnApplication('Postpone a hearing');

  });

  test(
    'offline - Legal representatives make and application, caseworker record a decision, LR and citizen view a decision - E/W',
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
      await applicationTabPage.enterDetailsForMakingApplication('Reconsider judgment');

      //caseworker records a decision
      await manageCaseDashboardPageCW.visit();
      await loginPageCW.processLogin(
        users.etCaseWorker
      );
      await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPageCW.navigateToTab('Applications');
      await applicationTabPageCW.enterDetailsForRecordADecision(checkYourAnswersPageCW, 'Reconsider judgment');

      await caseDetailsPageCW.navigateToTab('Applications');
      await caseDetailsPageCW.assertTabData([
        {
          tabName: 'Applications',
          tabContent: [
            { tabItem: 'Reconsider judgment', value: 'Claimant Representative', exact: false, clickable: true },
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
      await applicationTabPage.viewApplicationAndAssertDetails('Reconsider judgment', 'Open', [
        `Type of application - Reconsider judgment`,
        'Notification - Reconsider judgment',
        `Date - ${DateUtilComponent.formatToDayMonthYear(new Date())}`,
        `Sent by - Tribunal`
      ]);

    },
  );
});

