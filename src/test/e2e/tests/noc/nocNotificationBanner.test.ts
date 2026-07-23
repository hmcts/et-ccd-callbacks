import { test } from '../../fixtures/common.fixture.ts';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../../config/case-data.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { users } from '../../config/config.dynamic.ts';
import CitizenHubLoginPage from '../../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../../pages/claimantCitizenHub/CitizenHubPage.ts';
import Et3LoginPage from '../../pages/respondentCitizenHub/et3LoginPage.ts';

let caseId: string;
let caseNumber: string;
let firstName: string;
let lastName: string;
let respName: string;

test.describe('NOC Notification Banner', () => {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId.toString()));
    await manageCaseDashboardPage.visit();
    firstName = CaseDetailsValues.claimantFirstName;
    lastName = CaseDetailsValues.claimantLastName;
    respName = CaseDetailsValues.respondentName;
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToNoticeOfChange();
  });

  //RET-5419
  test('Perform NOC and validate the notification banner in CUI', async ({
    nocPage, browserUtils
  }) => {
    await nocPage.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);

    //citizen validates notification banner
    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
    const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
    await citizenHubPage.verifyLegalRepNotificationBanner();
    await citizenHubPage.contactTheTribunalLink();
    await claimantBrowserPage.close();
  });

  //RET-5791
  test('Perform NOC and validate the notification banner in  Respondent UI', async ({
    nocPage, browserUtils
  }) => {
    await nocPage.processNocRequest(caseId, respName, caseNumber);

    //Assign a claim to respondent
    const respondentBrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
    const et3LoginPage = new Et3LoginPage(respondentBrowserPage);
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.replyToNewClaim(caseId, caseNumber, respName, firstName, lastName);

    //citizen validates notification banner in respondent UI
    await et3LoginPage.validateNocNotificationBanner();

    // RET-6007 -change my legal representative- stop representation
    await et3LoginPage.stopLegalRepRepresentation();
    await respondentBrowserPage.close();
  });

  //RET-5416
  test('Perform NOC and Remove claimant legal representative as a citizen, claimant reinstated case',
    async ({
           nocPage, browserUtils
           }) => {

    await nocPage.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);

    //remove claimant legal rep
    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
    const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
    await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
    await citizenHubPage.clickChangeMyLegalRep();
    await citizenHubPage.verifyLegalRepUnassignedNotificationBanner();
      await claimantBrowserPage.close();
  });
});
