import { test as base } from '@playwright/test';
import { ApplicationTabPage } from '../pages/applicationTabPage';
import CaseListPage from '../pages/caseListPage';
import CreateCaseFlagPage from '../pages/createCaseFlag';
import ManageCaseFlagPage from '../pages/manageCaseFlag';
import Et1CaseServingPage from '../pages/et1CaseServingPage';
import LoginPage from '../pages/loginPage';
import { ListHearingPage } from '../pages/events/listHearingPage';
import { LegalRepPage } from '../pages/legalRepPage';
import CitizenHubPage from '../pages/claimantCitizenHub/CitizenHubPage.ts';
import { CaseLinkPage } from '../pages/caseLinkPage';
import RespondentRepPage from '../pages/respondentCitizenHub/respondentRepPage.ts';
import ET3LoginPage from '../pages/respondentCitizenHub/et3LoginPage.ts';
import RespondentCaseOverviewPage from '../pages/respondentCitizenHub/respondentCaseOverviewPage.ts';
import RespondentTaskListPage from '../pages/respondentCitizenHub/respondentTaskListPage.ts';
import ResponseLandingPage from '../pages/respondentCitizenHub/responseLandingPage.ts';
import RespContactDetailsPages from '../pages/respondentCitizenHub/respContactDetailsPages.ts';
import RespClaimantDetails from '../pages/respondentCitizenHub/respClaimantDetails.ts';
import RespContestClaim from '../pages/respondentCitizenHub/respContestClaim.ts';
import RespSubmitEt3 from '../pages/respondentCitizenHub/respSubmitEt3.ts';
import BfActionPage from '../pages/bfActionPage';
import JurisdictionPage from '../pages/jurisdictionPage';
import CaseTransferPage from '../pages/caseTransferPage';
import CaseWorkerNotificationPage from '../pages/notifications/CaseWorkerNotificationPage.ts';
import ClaimantDetailsPage from '../pages/claimantDetailsPage';
import RespondentDetailsPage from '../pages/respondentCitizenHub/respondentDetailsPage.ts';
import { ManageOrgPage } from '../pages/manageOrgPage';
import ICUploadDocPage from '../pages/icUploadDocPage';
import { RestrictedReportingPage } from '../pages/restrictedReportingPage';
import UploadDocumentPage from "../pages/uploadDocumentPage";
import ReferralPage from '../pages/referralPage';
import DraftJudgementPage from '../pages/events/draftJudgementPage.ts';
import IssueJudgementPage from '../pages/events/issueJudgementPage.ts';
import SearchAcasPage from '../pages/searchAcasPage';
import LettersPage from '../pages/lettersPage';
import DepositOrderPage from '../pages/depositOrderPage';
import RolesAndAccessPage from "../pages/rolesAndAccessPage";
import TaskPage from "../pages/taskPage";
import HearingDetailsPage from "../pages/hearingDetailsPage";
import AdrDocumentPage from "../pages/adrDocumentPage";
import CaseDetailsPage from "../pages/caseDetailsPage";
import Et3NotificationPage from '../pages/et3NotificationPage';
import UploadHearingBundlePage from '../pages/uploadHearingBundlePage';
import CaseNotesPage from '../pages/caseNotesPage';
import CloseCasePage from '../pages/closeCasePage';
import ReinstateCasePage from '../pages/reinstateCasePage';
import ClaimantRepresentativePage from '../pages/events/claimantRepresentativePage.ts';
import { UploadDocumentsForHearingPage } from '../pages/events/UploadDocumentsForHearingPage';
import { CheckYourAnswersPage } from '../pages/helpers/CheckYourAnswersPage';
import { BaseEventPage } from '../pages/events/BaseEventPage.ts';
import LegalRepNotificationPage from '../pages/notifications/LegalRepNotificationPage.ts';
import CitizenHubLoginPage from '../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import ContactTheTribunalPage from '../pages/claimantCitizenHub/ContactTheTribunalPage.ts';
import PrepareAndSubmitDocumentPage from '../pages/claimantCitizenHub/PrepareAndSubmitDocumentPage.ts';
import Et1VettingPages from '../pages/et1VettingPages.ts';
import CUIPreLoginPage from '../pages/claimantCitizenHub/CUIPreLoginPage.ts';
import CUIPostLoginPages from '../pages/claimantCitizenHub/CUIPostLoginPages.ts';
import PersonalDetailsPage from '../pages/claimantCitizenHub/personalDetailsPage.ts';
import EmploymentAndRespDetailsPage from '../pages/claimantCitizenHub/employmentAndRespDetailsPage.ts';
import ClaimDetailsPage from '../pages/claimantCitizenHub/claimDetailsPage.ts';
import SubmitClaimPage from '../pages/claimantCitizenHub/submitClaimPage.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import Et1CreateDraftClaim from '../pages/et1CreateDraftClaim.ts';
import { NocPage } from '../pages/legalRepresentative/NocPage.ts';
import ET3ProcessPage from '../pages/et3ProcessPage.ts';
import { Et3DetailsPage } from '../pages/legalRepresentative/Et3DetailsPage.ts';
import Et3RespondentDetailsPage from '../pages/legalRepresentative/Et3RespondentDetailsPage.ts';
import { Et3EmploymentDetailsPage } from '../pages/legalRepresentative/Et3EmploymentDetailsPage.ts';
import { Et3ResponseDetailsPage } from '../pages/legalRepresentative/Et3ResponseDetailsPage.ts';
import InitialConsiderationPage from '../pages/initialConsiderationPage.ts';
import AllocateHearingPage from '../pages/allocateHearingPage.ts';
import ManageTelephoneNotePage from '../pages/manageTelephoneNotePage.ts';
import { Et1ClaimsListPage } from '../pages/claimantCitizenHub/et1ClaimsListPage.ts';
import AmendContactDetailsLrPage from '../pages/events/AmendContactDetailsLrPage.ts';
import CitizenApplicationsPage from '../pages/claimantCitizenHub/citizenApplicationsPage.ts';
import ResClaimantsApplicationsPage from '../pages/respondentCitizenHub/resClaimantsApplicationsPage.ts';

export type PageFixtures = {
  applicationTabPage: ApplicationTabPage;
  manageCaseDashboardPage: ManageCaseDashboardPage;
  caseListPage: CaseListPage;
  createCaseFlagPage: CreateCaseFlagPage;
  manageCaseFlagPage: ManageCaseFlagPage;
  et1CaseServingPage: Et1CaseServingPage;
  et1VettingPage: Et1VettingPages;
  loginPage: LoginPage;
  listHearingPage: ListHearingPage;
  legalRepPage: LegalRepPage;
  citizenHubLoginPage: CitizenHubLoginPage;
  citizenHubPage: CitizenHubPage;
  caseLinkPage: CaseLinkPage;
  respondentRepPage: RespondentRepPage;
  et3LoginPage: ET3LoginPage;
  respondentCaseOverviewPage: RespondentCaseOverviewPage;
  respondentTaskListPage: RespondentTaskListPage;
  responseLandingPage: ResponseLandingPage;
  respContactDetailsPages: RespContactDetailsPages;
  respClaimantDetails: RespClaimantDetails;
  respContestClaim: RespContestClaim;
  respSubmitEt3: RespSubmitEt3;
  bfActionPage: BfActionPage;
  jurisdictionPage: JurisdictionPage;
  caseTransferPage: CaseTransferPage;
  caseWorkerNotificationPage: CaseWorkerNotificationPage;
  legalRepNotificationPage: LegalRepNotificationPage;
  claimantDetailsPage: ClaimantDetailsPage;
  respondentDetailsPage: RespondentDetailsPage;
  nocPage: NocPage;
  manageOrgPage: ManageOrgPage;
  icUploadDocPage: ICUploadDocPage;
  restrictedReportingPage: RestrictedReportingPage;
  uploadDocumentPage: UploadDocumentPage;
  referralPage: ReferralPage;
  draftJudgementPage: DraftJudgementPage;
  issueJudgementPage: IssueJudgementPage;
  searchAcasPage: SearchAcasPage;
  lettersPage: LettersPage;
  depositOrderPage: DepositOrderPage;
  rolesAndAccessPage: RolesAndAccessPage;
  taskPage: TaskPage;
  hearingDetailsPage: HearingDetailsPage;
  adrDocument: AdrDocumentPage;
  caseDetailsPage: CaseDetailsPage;
  et3NotificationPage: Et3NotificationPage;
  uploadHearingBundlePage: UploadHearingBundlePage;
  caseNotesPage: CaseNotesPage;
  manageTelephoneNotePage: ManageTelephoneNotePage;
  claimantRepresentativePage: ClaimantRepresentativePage;
  closeCasePage: CloseCasePage;
  reinstateCasePage: ReinstateCasePage;
  uploadDocumentsForHearingPage: UploadDocumentsForHearingPage;
  checkYourAnswersPage: CheckYourAnswersPage;
  baseEventPage: BaseEventPage;
  contactTheTribunalPage: ContactTheTribunalPage;
  prepareAbdSubmitDocumentPage: PrepareAndSubmitDocumentPage;
  citizenPreLoginPage: CUIPreLoginPage;
  citizenPostLoginPage: CUIPostLoginPages;
  personalDetailsPage: PersonalDetailsPage;
  employmentAndRespondentDetailsPage: EmploymentAndRespDetailsPage;
  claimDetailsPage: ClaimDetailsPage;
  submitClaimPage: SubmitClaimPage;
  et1CreateDraftClaim: Et1CreateDraftClaim;
  et3ProcessPage: ET3ProcessPage;
  et3DetailsPage: Et3DetailsPage;
  et3RespondentDetailsPage: Et3RespondentDetailsPage;
  et3EmploymentDetailsPage: Et3EmploymentDetailsPage;
  et3ResponseDetailsPage: Et3ResponseDetailsPage;
  initialConsiderationPage: InitialConsiderationPage;
  allocateHearingPAge:AllocateHearingPage;
  et1ClaimsListPage: Et1ClaimsListPage;
  amendContactDetailsLrPage: AmendContactDetailsLrPage;
  citizenApplicationsPage: CitizenApplicationsPage;
  resClaimantsApplicationsPage :ResClaimantsApplicationsPage;
};

export const pageFixtures = base.extend<PageFixtures>({

    applicationTabPage: async ({ page }, use) => {
        await use(new ApplicationTabPage(page));
    },

    manageCaseDashboardPage: async ({page}, use) => {
      await use(new ManageCaseDashboardPage(page));
    },

    caseListPage: async ({page}, use) => {
        await use(new CaseListPage(page));
    },

    createCaseFlagPage: async ({page}, use) => {
        await use(new CreateCaseFlagPage(page));
    },

    manageCaseFlagPage: async ({page}, use) => {
        await use(new ManageCaseFlagPage(page));
    },

    et1CaseServingPage: async ({page}, use) => {
        await use(new Et1CaseServingPage(page));
    },

    loginPage: async ({page}, use) => {
        await use(new LoginPage(page));
    },

    listHearingPage: async ({page}, use) => {
        await use(new ListHearingPage(page));
    },

    legalRepPage: async ({page}, use) => {
        await use(new LegalRepPage(page));
    },

    citizenHubPage: async ({page}, use) => {
        await use(new CitizenHubPage(page));
    },

    caseLinkPage: async ({page}, use) => {
        await use(new CaseLinkPage(page));
    },

    respondentRepPage: async ({page}, use) => {
        await use(new RespondentRepPage(page));
    },

    et3LoginPage: async ({page}, use) => {
        await use(new ET3LoginPage(page));
    },

    respondentCaseOverviewPage: async ({page}, use) => {
        await use(new RespondentCaseOverviewPage(page));
    },

    respondentTaskListPage: async ({page}, use) => {
        await use(new RespondentTaskListPage(page));
    },

    responseLandingPage: async ({page}, use) => {
        await use(new ResponseLandingPage(page));
    },

    respContactDetailsPages: async ({page}, use) => {
        await use(new RespContactDetailsPages(page));
    },

    respClaimantDetails:async ({page}, use) => {
        await use(new RespClaimantDetails(page));
    },

    respContestClaim:async ({page}, use) => {
        await use(new RespContestClaim(page));
    },

    respSubmitEt3:async ({page}, use) => {
        await use(new RespSubmitEt3(page));
    },

    bfActionPage:async ({page}, use) => {
        await use(new BfActionPage(page));
    },

    jurisdictionPage:async ({page}, use) => {
        await use(new JurisdictionPage(page));
    },

    caseTransferPage:async ({page}, use) => {
        await use(new CaseTransferPage(page));
    },

    caseWorkerNotificationPage:async ({page}, use) => {
        await use(new CaseWorkerNotificationPage(page));
    },

    claimantDetailsPage:async ({page}, use) => {
        await use(new ClaimantDetailsPage(page));
    },

    respondentDetailsPage:async ({page}, use) => {
        await use(new RespondentDetailsPage(page));
    },

    nocPage:async ({page}, use) => {
        await use(new NocPage(page));
    },

    manageOrgPage:async ({page}, use) => {
        await use(new ManageOrgPage(page));
    },

    icUploadDocPage:async ({page}, use) => {
        await use(new ICUploadDocPage(page));
    },

    restrictedReportingPage:async ({page}, use) => {
        await use(new RestrictedReportingPage(page));
    },

    uploadDocumentPage:async ({page}, use)=>{
        await use(new UploadDocumentPage(page));
    },

    referralPage:async ({page}, use)=>{
        await use(new ReferralPage(page));
    },

    draftJudgementPage:async ({page}, use)=>{
        await use(new DraftJudgementPage(page));
    },

    issueJudgementPage:async ({page}, use)=>{
        await use(new IssueJudgementPage(page));
    },

    searchAcasPage:async ({page}, use)=>{
        await use(new SearchAcasPage(page));
    },

    lettersPage:async ({page}, use)=>{
        await use(new LettersPage(page));
    },

    depositOrderPage:async ({page}, use)=>{
        await use(new DepositOrderPage(page));
    },

    rolesAndAccessPage: async ({page}, use)=>{
        await use(new RolesAndAccessPage(page));
    },

    taskPage: async ({page}, use)=>{
        await use(new TaskPage(page));
    },

    hearingDetailsPage: async ({page}, use)=>{
        await use(new HearingDetailsPage(page));
    },

    adrDocument:async ({page}, use)=>{
        await use(new AdrDocumentPage(page));
    },
    caseDetailsPage:async ({page}, use)=>{
        await use(new CaseDetailsPage(page));
    },
    et3NotificationPage:async ({page}, use)=>{
      await use(new Et3NotificationPage(page));
    },
    uploadHearingBundlePage:async({page}, use)=>{
        await use(new UploadHearingBundlePage(page));
    },
    caseNotesPage:async({page}, use)=>{
      await use(new CaseNotesPage(page));
    },
    manageTelephoneNotePage:async ({page}, use) => {
      await use(new ManageTelephoneNotePage(page));
    },
    claimantRepresentativePage:async ({page}, use) => {
        await use(new ClaimantRepresentativePage(page));
    },
    closeCasePage:async({page}, use)=>{
      await use(new CloseCasePage(page));
    },
    reinstateCasePage:async({page}, use)=>{
      await use(new ReinstateCasePage(page));
    },
    uploadDocumentsForHearingPage:async({page}, use)=>{
      await use(new UploadDocumentsForHearingPage(page));
    },
    checkYourAnswersPage:async({page}, use) => {
      await use(new CheckYourAnswersPage(page));
    },
    baseEventPage:async({page}, use) => {
      await use(new BaseEventPage(page));
    },
    legalRepNotificationPage:async ({page}, use) => {
        await use(new LegalRepNotificationPage(page));
    },
   citizenHubLoginPage:async({page}, use) => {
      await use (new CitizenHubLoginPage (page));
    },
    contactTheTribunalPage:async({page}, use) => {
      await use (new ContactTheTribunalPage (page));
    },
    prepareAbdSubmitDocumentPage:async({page}, use) => {
      await use (new PrepareAndSubmitDocumentPage (page));
    },
  et1VettingPage:async ({page}, use)=>{
    await use(new Et1VettingPages(page));
  },
  citizenPreLoginPage:async({page}, use) => {
    await use (new CUIPreLoginPage (page));
  },
  citizenPostLoginPage:async({page}, use)=>{
    await use (new CUIPostLoginPages (page));
  },
  personalDetailsPage:async({page}, use)=>{
    await use (new PersonalDetailsPage (page));
  },
  employmentAndRespondentDetailsPage:async({page}, use)=>{
    await use (new EmploymentAndRespDetailsPage (page));
  },
  claimDetailsPage:async({page}, use)=>{
    await use (new ClaimDetailsPage (page));
  },
  submitClaimPage:async({page}, use)=>{
    await use (new SubmitClaimPage (page));
  },
  et1CreateDraftClaim:async({page}, use)=>{
    await use (new Et1CreateDraftClaim (page));
  },
  et3ProcessPage:async({page}, use)=>{
    await use (new ET3ProcessPage (page));
  },
  initialConsiderationPage:async({page}, use)=>{
    await use (new InitialConsiderationPage (page));
  },
  et3DetailsPage:async({page}, use)=>{
    await use (new Et3DetailsPage (page));
  },
  et3RespondentDetailsPage:async({page}, use)=>{
    await use (new Et3RespondentDetailsPage (page));
  },
  et3EmploymentDetailsPage:async({page}, use)=>{
    await use (new Et3EmploymentDetailsPage (page));
  },
  et3ResponseDetailsPage:async({page}, use)=>{
    await use (new Et3ResponseDetailsPage (page));
  },
  allocateHearingPAge:async({page}, use)=>{
    await use (new AllocateHearingPage(page));
  },
  et1ClaimsListPage:async({page}, use)=>{
      await use (new Et1ClaimsListPage (page));
  },
  amendContactDetailsLrPage:async({page}, use)=>{
      await use (new AmendContactDetailsLrPage (page));
  },
  citizenApplicationsPage:async({page}, use)=>{
      await use (new CitizenApplicationsPage (page));
  },
  resClaimantsApplicationsPage:async({page}, use)=>{
      await use (new ResClaimantsApplicationsPage (page));
  }
});
