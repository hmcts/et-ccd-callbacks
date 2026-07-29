import { test } from '../fixtures/common.fixture';
import userDetailsData from '../resources/payload/user-details.json';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../pages/loginPage.ts';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import ClaimantDetailsPage from '../pages/claimantDetailsPage.ts';
import RespondentDetailsPage from '../pages/respondentCitizenHub/respondentDetailsPage.ts';
import { NocPage } from '../pages/legalRepresentative/NocPage.ts';

let caseNumber: string;
let caseId: string;

test.describe('Legal Representative submits a case and perform various events', () => {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })
  test(
    'LR creates a claim, amend claimant/respondent names and persist NOC with original claimant/respondent names',
    { tag: '@demo' },
    async ({
      manageCaseDashboardPage,
      caseListPage,
      loginPage,
      et1CreateDraftClaim,
      browserUtils
    }) => {
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(
        users.etLegalRepresentative
      );
      await caseListPage.claimantRepCreateCase('Employment', 'Eng/Wales - Singles', 'LS1 2AJ');

      await et1CreateDraftClaim.et1Section1(userDetailsData.claimantsFirstName, userDetailsData.claimantsLastName);
      await et1CreateDraftClaim.et1Section2(userDetailsData.respondentsFirstName, userDetailsData.respondentsLastName);
      await et1CreateDraftClaim.et1Section3();
      let submissionReference = await et1CreateDraftClaim.et1SubmitClaim();
      caseId = submissionReference.toString();
      console.log('Case Submission Reference ' + submissionReference);

      ({ caseId, caseNumber } = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

      const caseworkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
      const manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseworkerBrowserPage);
      const loginPageCW = new LoginPage(caseworkerBrowserPage);
      const caseDetailsPageCW = new CaseDetailsPage(caseworkerBrowserPage);
      const claimantDetailsPageCW = new ClaimantDetailsPage(caseworkerBrowserPage);
      const respondentDetailsPageCW = new RespondentDetailsPage(caseworkerBrowserPage);

      await manageCaseDashboardPageCW.visit();
      await loginPageCW.processLogin(users.etCaseWorker);
      await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      // Amend Claimant and Respondent names
      await caseDetailsPageCW.selectNextEvent(Events.claimantDetails);
      await claimantDetailsPageCW.processClaimantDetails();
      await caseDetailsPageCW.selectNextEvent(Events.respondentDetails);
      await respondentDetailsPageCW.processRespondentDetails();

      // Perform NOC using original Claimant and Respondent names (different org)
      const legalRep2BrowserPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative2.sessionFile);
      const manageCaseDashboardPageLR = new ManageCaseDashboardPage(legalRep2BrowserPage);
      const loginPageLR = new LoginPage(legalRep2BrowserPage);
      const nocLR = new NocPage(legalRep2BrowserPage);
      await manageCaseDashboardPageLR.visit();
      await loginPageLR.processLogin(
        users.etLegalRepresentative2
      );
      await manageCaseDashboardPageLR.navigateToNoticeOfChange();
      await nocLR.processNocRequest(
        caseId,
        `${userDetailsData.respondentsFirstName} ${userDetailsData.respondentsLastName}`,
        caseNumber,
      );
    },
  );
});
