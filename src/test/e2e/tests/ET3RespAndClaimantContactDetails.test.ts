import { test } from '../fixtures/et.test.fixture.ts';
import {
  CaseTypeLocation,
  CitizenClaimantFactory,
  CitizenHubLoginPage, CitizenHubPage, ET3LoginPage,
  Events, RespondentCaseOverviewPage,
  users
} from "@et-shared-test-library/core";

let caseNumber: string;
let subRef: string;

test.describe('ET3/Respondent Journey validates respondent/claimant details', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async ({}) => {
    subRef = await CitizenClaimantFactory.progressCaseFromCreateToEt3(CaseTypeLocation.EnglandAndWales);
  });

  //RET-5516
  test('Citizen user validates respondent contact details', async ({loginPage,browserUtils, respondentDetailsPage, manageCaseDashboardPage, caseDetailsPage }) => {
    //caseworker completes respondent details
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(subRef.toString(), CaseTypeLocation.EnglandAndWales);

    await caseDetailsPage.selectNextEvent(Events.respondentDetails);
    await respondentDetailsPage.processRespondentDetailsET3(true);

    //citizen validates respondent contact details
    const claimantPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantPage);
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);

    const citizenHubPage = new CitizenHubPage(claimantPage);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(subRef);
    await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
    await citizenHubPage.clickRespondentContactDetailsLink();
    await citizenHubPage.verifyRespondentContactDetails();
  });

  //RET-5767
  test('Respondent validates claimant contact details', {tag: '@demo'},
    async ({ browserUtils, loginPage, manageCaseDashboardPage}) => {
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(subRef.toString(), CaseTypeLocation.EnglandAndWales);

    //Assign a claim to respondent
    const respondentBrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
    const et3LoginPage = new ET3LoginPage(respondentBrowserPage);
    await et3LoginPage.processRespondentLoginForExistingCase(users.etRespondent, caseNumber);
    const respondentCaseOverviewPage = new RespondentCaseOverviewPage(respondentBrowserPage);
    await respondentCaseOverviewPage.validateRespondentClaimantContactDetailsPage();
  });
});
