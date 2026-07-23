import { test } from '../../fixtures/common.fixture.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { LegalRepCaseFactory } from '../../data-utils/factory/exui/LegalRepCaseFactory.ts';
import DateUtilComponent from '../../data-utils/DateUtilComponent.ts';
import { users } from '../../config/config.dynamic.ts';
import CitizenHubLoginPage from '../../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../../pages/claimantCitizenHub/CitizenHubPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseDetailsPage from '../../pages/caseDetailsPage.ts';
import { ApplicationTabPage } from '../../pages/applicationTabPage.ts';

let caseNumber: string;
let caseId: string;

const respName ='Mrs Test Auto';
const firstName ='Grayson';
const lastName = 'Becker';

test.describe('ET3/Respondent Applications', () => {
  test.use({
    storageState: users.etRespondent.sessionFile,
  })

  test.beforeEach(async () => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} =  await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
 });

  test('Respondent makes Type B Application, Claimant respond to an application successfully',
    async ({ et3LoginPage, respondentCaseOverviewPage, browserUtils}) => {
      //Assign a claim to respondent
      await et3LoginPage.processRespondentLogin(users.etRespondent);
      await et3LoginPage.replyToNewClaim(caseId, caseNumber, respName, firstName, lastName);

      //make type B application
      await respondentCaseOverviewPage.respondentMakeApplication('TypeB', true);

      //validate application is visible in respondent application link
      await respondentCaseOverviewPage.validateApplication('TypeB');

      //Citizen & caseworker can view an application
      const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
      const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
      const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
      await citizenHubPage.respondToRespondentApplication('TypeB');
  });

  //TODO: RET-6573 defect raised for this issue, PAGE NOT FOUND error
  test.fail('Respondent makes Type C Application successfully',
    async ({ et3LoginPage, respondentCaseOverviewPage}) => {
      //Assign a claim to respondent
      await et3LoginPage.processRespondentLogin(users.etRespondent);
      await et3LoginPage.replyToNewClaim(caseId, caseNumber, respName, firstName, lastName);

      //make type B application
      await respondentCaseOverviewPage.respondentMakeApplication('TypeC', false);

      //validate application is visible in respondent application link
      await respondentCaseOverviewPage.validateApplication('TypeC');
  });

});

test.describe('ET3/Respondent Applications', () => {
  test.use({
    storageState: users.etRespondent.sessionFile,
  })
  //too long UI work flow, create a case via API as a legal rep
  test('Legal Representative created a case, Respondent makes Type A Application, LR can see application', async ({
    et3LoginPage, browserUtils,
    respondentCaseOverviewPage,
  }) => {
    const respName = 'Mrs Test Auto';
    const firstName = CaseDetailsValues.claimantFirstName;
    const lastName = CaseDetailsValues.claimantLastName;

    ({ caseId, caseNumber } = await LegalRepCaseFactory.createAndProgressToSubmitEnglandWalesCase());
    await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId);
    console.log('Created ' + caseId + caseNumber);

    // assign case to respondent and make application
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.replyToNewClaim(caseId, caseNumber, respName, firstName, lastName);
    await respondentCaseOverviewPage.respondentMakeApplication('TypeA', true);
    await respondentCaseOverviewPage.signOutButtonSyr();

    // legal rep view application
    const legalRepBrowserPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative.sessionFile);
    const manageCaseDashboardPage = new ManageCaseDashboardPage(legalRepBrowserPage);
    const loginPage = new LoginPage(legalRepBrowserPage);
    const caseDetailsPage = new CaseDetailsPage(legalRepBrowserPage);
    const applicationTabPage = new ApplicationTabPage(legalRepBrowserPage);

    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    // legal rep can see an application
    await caseDetailsPage.navigateToTab('Applications');
    await applicationTabPage.viewApplicationAndAssertDetails('Amend response', 'Open', [
      `Type of application - Amend response`,
      `Applicant - Respondent`,
      `Application date - ${DateUtilComponent.formatToDayMonthYear(new Date())}`,
    ]);
  });

});
