import { test } from '../../fixtures/common.fixture.ts';
import {Helpers} from "../../pages/helpers/Helper.ts";
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { users } from '../../config/config.dynamic.ts';
import CitizenHubLoginPage from '../../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../../pages/claimantCitizenHub/CitizenHubPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseDetailsPage from '../../pages/caseDetailsPage.ts';

let caseNumber: string;
let caseId: string;

test.describe.serial('ET3/Respondent Applications and verify WA tasks', () => {
  test.use({
    storageState: users.etRespondent.sessionFile
  })

  test('Respondent makes Type A Application, Claimant respond to an application successfully',
    async ({
            browserUtils,
            et3LoginPage,
            respondentCaseOverviewPage,
        }) => {
      caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
      ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

      //Assign a claim to respondent
      await et3LoginPage.processRespondentLogin(users.etRespondent);
      await et3LoginPage.replyToNewClaim(caseId, caseNumber, CaseDetailsValues.respondentName, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);

      //make type A application
      await respondentCaseOverviewPage.respondentMakeApplication('TypeA', true);

      //validate application is visible in respondent application link
      await respondentCaseOverviewPage.validateApplication('TypeA');

      //Citizen & caseworker can view an application
      const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
      const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage);
      const citizenHubPage = new CitizenHubPage(claimantBrowserPage);

      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
      //RET-5754
      //await citizenHubPage.validateApplicationBanner();
      await citizenHubPage.respondToRespondentApplication('TypeA');
  });

  test('Respondent makes Type A Application, review Application and review Application Response task generated',
    async ({
      page, browserUtils
    }) => {
      await page.close();
      const caseworkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
      const manageCaseDashboardPage = new ManageCaseDashboardPage(caseworkerBrowserPage);
      const loginPage = new LoginPage(caseworkerBrowserPage);
      const caseDetailsPage = new CaseDetailsPage(caseworkerBrowserPage);

      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      await caseDetailsPage.navigateToTab('Tasks');
      await Helpers.waitForTask(caseworkerBrowserPage, 'Review Application - Amend response');
      await Helpers.waitForTask(caseworkerBrowserPage, 'Review Application Response - Amend response');
  });
});
