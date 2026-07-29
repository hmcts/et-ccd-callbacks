import { test } from '../../fixtures/common.fixture.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { users } from '../../config/config.dynamic.ts';
import CitizenHubLoginPage from '../../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../../pages/claimantCitizenHub/CitizenHubPage.ts';
import ContactTheTribunalPage from '../../pages/claimantCitizenHub/ContactTheTribunalPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseDetailsPage from '../../pages/caseDetailsPage.ts';
import { ApplicationTabPage } from '../../pages/applicationTabPage.ts';
import Et3LoginPage from '../../pages/respondentCitizenHub/et3LoginPage.ts';
import ResClaimantsApplicationsPage from '../../pages/respondentCitizenHub/resClaimantsApplicationsPage.ts';
import RespondentCaseOverviewPage from '../../pages/respondentCitizenHub/respondentCaseOverviewPage.ts';

test.describe('Citizen applications', () => {
  let caseId: string;
  let caseNumber: string;

  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })

  //RET-5818
  test('Citizen make an application with 1 respondent, legal rep respond to it and caseworker validate documents - England', async ({
    manageCaseDashboardPage,
    loginPage,
    nocPage,
    applicationTabPage,
    caseDetailsPage,
    browserUtils
  }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

    // perform NOC
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

    // Citizen rep make an application
    const citizenBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(citizenBrowserPage);
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    const citizenHubPage = new CitizenHubPage(citizenBrowserPage);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.navigateToContactTheTribunalPage();
    const contactTheTribunalPage = new ContactTheTribunalPage(citizenBrowserPage);
    await contactTheTribunalPage.makeApplicationToTribunal(
      'change personal details',
      'Citizen made an application',
      'Yes',
    );
    await contactTheTribunalPage.clickSubmitButton();
    await contactTheTribunalPage.assertApplicationSentSuccessPageIsDisplayed();
    await contactTheTribunalPage.clickCloseAndReturn();
    await citizenBrowserPage.close();

    // Legal Rep respond to an application
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.navigateToTab('Applications');
    await applicationTabPage.legalRepRespondToAnApplication('Change my personal details');

    // Caseworker validates Document tab
    const caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    const manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    await manageCaseDashboardPageCW.visit();
    const loginPageCW = new LoginPage(caseWorkerBrowserPage);
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    const caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
    await caseDetailsPageCW.navigateToTab('Documents');
    await caseDetailsPageCW.assertTabData([
      {
        tabName: 'Documents',
        tabContent: [
         'Application 1 - Change my personal details - Respondent Response.pdf',
          'Application 1 - Change my personal details - Respondent Response Attachment.pdf'
        ]
      }
    ]);
  });
});

test.describe.serial('Claimant applications with correspondence No - Caseworker checks document', () => {
  let caseId: string;
  let caseNumber: string;

  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })

  test('Respondent Makes an application with Correspondence No',
    async ({
             browserUtils
           }) =>{
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales, true);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

    const respondentBrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
    const et3LoginPage = new Et3LoginPage(respondentBrowserPage);
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.replyToNewClaim(
      caseId,
      caseNumber,
      CaseDetailsValues.respondentName,
      CaseDetailsValues.claimantFirstName,
      CaseDetailsValues.claimantLastName
    );
    const respondentCaseOverviewPage = new RespondentCaseOverviewPage(respondentBrowserPage);
    await respondentCaseOverviewPage.respondentMakeApplication('TypeA', false);
    await respondentBrowserPage.close();
  });

  test('Claimant makes an application with correspondence No',
    async ({
             browserUtils
           }) => {
      // Citizen rep make an application
      const citizenBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
      const citizenHubLoginPage = new CitizenHubLoginPage(citizenBrowserPage);
      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      const citizenHubPage = new CitizenHubPage(citizenBrowserPage);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
      await citizenHubPage.navigateToContactTheTribunalPage();
      const contactTheTribunalPage = new ContactTheTribunalPage(citizenBrowserPage);
      await contactTheTribunalPage.makeApplicationToTribunal(
        'change personal details',
        'Citizen made an application',
        'No',
      );
      await contactTheTribunalPage.clickSubmitButton();
      await contactTheTribunalPage.assertApplicationSentSuccessPageIsDisplayed();
      await contactTheTribunalPage.clickCloseAndReturn();
      await citizenBrowserPage.close();
    })

  test('Caseworker checks document tab for application with Correspondence No',
    async ({
      manageCaseDashboardPage, loginPage, caseDetailsPage, context
           }) => {
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPage.assertTabData([
        {
          tabName: 'Documents',
          tabContent: [
            { tabItem: 'Application 1 - Amend my claim.pdf', value: 'Case Management' },
            { tabItem: 'Application 2 - Change my personal details.pdf', value: 'Case Management'}
          ]
        }
      ]);

      await caseDetailsPage.assertDocumentSnippet(
        'Application 1 - Amend my claim.pdf',
        ['Give details', 'Correspondence No Respondent'],
        context
      );
      await caseDetailsPage.assertDocumentSnippet(
        'Application 2 - Change my personal details.pdf',
        ['Give details', 'This is Correspondence No Claimant'],
        context
      );

  });
});

//RET-6425
test.describe.serial('Citizen with 2 respondent case applications', () => {
  let caseId: string;
  let caseNumber: string;

  test.use({
    storageState: users.etClaimant.sessionFile,
  })

  test('Set up a case with two respondents', async ({ browserUtils }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales, true);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

    const respondent1BrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
    const et3LoginPage1 = new Et3LoginPage(respondent1BrowserPage);
    await et3LoginPage1.processRespondentLogin(users.etRespondent);
    await et3LoginPage1.replyToNewClaim(
      caseId,
      caseNumber,
      CaseDetailsValues.respondentName,
      CaseDetailsValues.claimantFirstName,
      CaseDetailsValues.claimantLastName
    );
    await respondent1BrowserPage.close();

    const respondent2BrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent2.sessionFile);
    const et3LoginPage2 = new Et3LoginPage(respondent2BrowserPage);
    await et3LoginPage2.processRespondentLogin(users.etRespondent2);
    await et3LoginPage2.replyToNewClaim(
      caseId,
      caseNumber,
      CaseDetailsValues.respondentName2,
      CaseDetailsValues.claimantFirstName,
      CaseDetailsValues.claimantLastName
    );
    await respondent2BrowserPage.close();
  });

  test('Citizen submits an application and caseworker responds', async ({
    citizenHubLoginPage,
    contactTheTribunalPage,
    citizenHubPage,
    browserUtils,
  }) => {
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.navigateToContactTheTribunalPage();
    await contactTheTribunalPage.makeApplicationToTribunal(
      'change personal details',
      'Citizen made an application',
      'Yes',
    );
    await contactTheTribunalPage.clickSubmitButton();
    await contactTheTribunalPage.assertApplicationSentSuccessPageIsDisplayed();
    await contactTheTribunalPage.clickCloseAndReturn();

    const caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    const manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    const loginPageCW = new LoginPage(caseWorkerBrowserPage);
    const caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
    const applicationTabPage = new ApplicationTabPage(caseWorkerBrowserPage);

    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Applications');
    await applicationTabPage.caseWorkerRespondToAnApplication('Change my personal details');
    await caseWorkerBrowserPage.close();
  });

  test('Respondent 1 responds to claimant application', async ({ browserUtils }) => {
    const respondent1BrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
    const et3LoginPage1 = new Et3LoginPage(respondent1BrowserPage);
    const resClaimantsApplicationsPage1 = new ResClaimantsApplicationsPage(respondent1BrowserPage);

    await et3LoginPage1.processRespondentLoginForExistingCase(users.etRespondent, caseNumber);
    await resClaimantsApplicationsPage1.navigateToClaimantsApplicationsPage();
    await resClaimantsApplicationsPage1.assertClaimantsApplicationsPageVisibile();
    await resClaimantsApplicationsPage1.navigateToListedClaimantsApplication('Change my personal details');
    await resClaimantsApplicationsPage1.assertClaimantsApplicationInformation(
      'Change my personal details',
      ['Response - Response of an application By Caseworker', 'Sent by - Tribunal', 'Case management order or request? - Case management order']
    );
    await resClaimantsApplicationsPage1.respondToApplication('Change my personal details', false);
    await respondent1BrowserPage.close();
  });

  test('Citizen claimant responds and does not see respondent response', async ({
    citizenHubLoginPage,
    citizenHubPage,
    contactTheTribunalPage,
    citizenApplicationsPage,
  }) => {
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    // Workaround specified in RET-6107.
    await citizenHubPage.navigateToContactTheTribunalPage();
    await contactTheTribunalPage.assertContactTheTribunalPageIsDisplayed();
    await contactTheTribunalPage.selectApplicationTypeTobeMadeToTribunal('withdraw');

    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenApplicationsPage.navigateToYourRequestAndApplicationsPage();
    await citizenApplicationsPage.navigateToApplication('Change my personal details', 'Waiting for the tribunal');
    await citizenApplicationsPage.assertApplicationAndResponseDetails(
      'Change my personal details',
      ['Response - Response of an application By Caseworker', 'Sent by - Tribunal', 'Case management order or request? - Case management order']
    );
    await citizenApplicationsPage.assertTextNotVisibleInPage(['Response from Respondent']);
    await citizenApplicationsPage.clickRespondButton();
    await citizenApplicationsPage.enterDetailsForRespondingToApplication(false);
  });

  test('Caseworker responds to claimant follow-up', async ({ browserUtils }) => {
    const caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    const manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    const loginPageCW = new LoginPage(caseWorkerBrowserPage);
    const caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
    const applicationTabPage = new ApplicationTabPage(caseWorkerBrowserPage);

    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Applications');
    await applicationTabPage.caseWorkerRespondToAnApplication('Change my personal details', 'Request');
    await caseWorkerBrowserPage.close();
  });

  test('Respondent 2 cannot view respondent 1 or claimant responses', async ({ browserUtils }) => {
    const respondent2BrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent2.sessionFile);
    const et3LoginPage2 = new Et3LoginPage(respondent2BrowserPage);
    const resClaimantsApplicationsPage2 = new ResClaimantsApplicationsPage(respondent2BrowserPage);

    await et3LoginPage2.processRespondentLoginForExistingCase(users.etRespondent2, caseNumber);
    await resClaimantsApplicationsPage2.navigateToClaimantsApplicationsPage();
    await resClaimantsApplicationsPage2.navigateToListedClaimantsApplication('Change my personal details');
    await resClaimantsApplicationsPage2.assertTextNotVisibleInPage(['Response from Respondent', 'Response from claimant']);
    await resClaimantsApplicationsPage2.assertClaimantsApplicationInformation(
      'Change my personal details',
      ['Case management order or request? - Case management order', 'Case management order or request? - Request']
    );
    await respondent2BrowserPage.close();
  });

  test('Caseworker could see all responses', async ({ browserUtils }) => {
    const caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    const manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    const loginPageCW = new LoginPage(caseWorkerBrowserPage);
    const caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);

    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.navigateToTab('Applications');
    await caseDetailsPageCW.assertTabData([
      {
        tabName: 'Applications',
        tabContent: [
          { tabItem: 'Change my personal details', value:'Claimant', exact: false, clickable: true},
          { tabItem: 'Response from', value: 'Admin'},
          { tabItem: 'Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?', value: 'Yes'},
          { tabItem: 'Enter response title', value:'Response of an application By Caseworker'},
          { tabItem: 'Is this a case management order or request?', value:'Case management order'},
          { tabItem: 'Response from', value: 'Respondent'},
          { tabItem: `What's your response to the claimant's application`, value: 'Response from Respondent'},
          { tabItem: 'Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?', value: 'No'},
          { tabItem: 'Response from', value: 'Claimant'},
          { tabItem: 'Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?', value: 'No'},
          { tabItem: 'Response from', value: 'Admin'},
          { tabItem: 'Enter response title', value:'Response of an application By Caseworker'},
        ]
      }
    ]);

  });
});
