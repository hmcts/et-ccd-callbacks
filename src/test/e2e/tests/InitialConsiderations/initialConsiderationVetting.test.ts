import { test } from '../../fixtures/common.fixture.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { users } from '../../config/config.dynamic.ts';
import LoginPage from '../../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import CaseDetailsPage from '../../pages/caseDetailsPage.ts';
import ClaimantDetailsPage from '../../pages/claimantDetailsPage.ts';
import RespondentRepPage from '../../pages/respondentCitizenHub/respondentRepPage.ts';
import InitialConsiderationPage from '../../pages/initialConsiderationPage.ts';

let caseNumber: string;
let caseId: string;

test.describe('Initial Consideration Enhanced party details and ET1Vetting issues', () => {
  test.use({
    storageState: users.etRespondent.sessionFile,
  })

  test.beforeEach(async () => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
  });

  test(
    'Enhanced party details and ET1Vetting issues',
    { tag: '@demo' },
    async ({
      et3LoginPage,
      responseLandingPage,
      respContactDetailsPages,
      respClaimantDetails,
      respContestClaim,
      respSubmitEt3, browserUtils
    }) => {
      //Assign a claim to respondent
      await et3LoginPage.processRespondentLogin(users.etRespondent);
      await et3LoginPage.replyToNewClaim(caseId, caseNumber, CaseDetailsValues.respondentName, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);
      await responseLandingPage.startEt3();
      await respContactDetailsPages.et3Section1();
      await respClaimantDetails.et3Section2();
      await respContestClaim.et3Section3();
      await respSubmitEt3.checkYourAnswers();

      const caseworkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
      const loginPage = new LoginPage(caseworkerBrowserPage);
      const manageCaseDashboardPage = new ManageCaseDashboardPage(caseworkerBrowserPage);
      const caseDetailsPage = new CaseDetailsPage(caseworkerBrowserPage);
      const claimantDetailsPage = new ClaimantDetailsPage(caseworkerBrowserPage);
      const respondentRepPage = new RespondentRepPage(caseworkerBrowserPage);
      const initialConsiderationPage = new InitialConsiderationPage(caseworkerBrowserPage);

      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPage.selectNextEvent(Events.claimantDetails);
      await claimantDetailsPage.processClaimantDetailsForIC(true);
      await caseDetailsPage.selectNextEvent(Events.respondentDetails);
      await respondentRepPage.enterRespTypeforIC();
      await caseDetailsPage.selectNextEvent(Events.initialConsideration);
      await initialConsiderationPage.validateEnhancedAllPartyDetails();
      await initialConsiderationPage.validateET1VettingIssues();
    },
  );
});
