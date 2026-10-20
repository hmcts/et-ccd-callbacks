import { test as base } from '@playwright/test';
import {
  AdrDocumentPage,
  AllocateHearingPage,
  AmendContactDetailsLrPage,
  ApplicationTabPage,
  BaseEventPage,
  BfActionPage,
  CUIPostLoginPages,
  CUIPreLoginPage,
  CaseDetailsPage,
  CaseLinkPage,
  CaseListPage,
  CaseNotesPage,
  CaseTransferPage,
  CaseTransferToEcmPage,
  CaseWorkerNotificationPage,
  CitizenApplicationsPage,
  CitizenHubLoginPage,
  CitizenHubPage,
  ClaimDetailsPage,
  ClaimantDetailsPage,
  ClaimantRepresentativePage,
  CheckYourAnswersPage,
  CloseCasePage,
  ContactTheTribunalPage,
  CreateCaseFlagPage,
  DepositOrderPage,
  DraftJudgementPage,
  ET3LoginPage,
  ET3ProcessPage,
  EmploymentAndRespDetailsPage,
  Et1CaseServingPage,
  Et1ClaimsListPage,
  Et1CreateDraftClaim,
  Et1VettingPages,
  Et3DetailsPage,
  Et3EmploymentDetailsPage,
  Et3NotificationPage,
  Et3RespondentDetailsPage,
  Et3ResponseDetailsPage,
  Et3ResponsesDashboardPage,
  HearingDetailsPage,
  ICUploadDocPage,
  InitialConsiderationPage,
  IssueJudgementPage,
  JurisdictionPage,
  LegalRepNotificationPage,
  LegalRepPage,
  LettersPage,
  ListHearingPage,
  LoginPage,
  ManageCaseDashboardPage,
  ManageCaseFlagPage,
  ManageOrgPage,
  ManageSupportPage,
  ManageTelephoneNotePage,
  NocPage,
  PersonalDetailsPage,
  PrepareAndSubmitDocumentPage,
  ReferralPage,
  ReinstateCasePage,
  RequestSupportPage,
  ResClaimantsApplicationsPage,
  RespClaimantDetails,
  RespContactDetailsPages,
  RespContestClaim,
  RespSubmitEt3,
  RespondentCaseOverviewPage,
  RespondentDetailsPage,
  RespondentRepPage,
  RespondentTaskListPage,
  ResponseLandingPage,
  RestrictedReportingPage,
  RolesAndAccessPage,
  SearchAcasPage,
  SingleOrMultipleClaimPage,
  SubmitClaimPage,
  TaskPage,
  UploadDocumentPage,
  UploadDocumentsForHearingPage,
  UploadHearingBundlePage,
} from '../pages';

type PageFixtures = {
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
  caseTransferToEcmPage: CaseTransferToEcmPage;
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
  singleOrMultipleClaimPage: SingleOrMultipleClaimPage;
  requestSupportPage: RequestSupportPage;
  manageSupportPage: ManageSupportPage;
  et3ResponseDashboardPage: Et3ResponsesDashboardPage;
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

    caseTransferToEcmPage:async ({page}, use) => {
      await use(new CaseTransferToEcmPage(page));
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
  },
  singleOrMultipleClaimPage:async({page}, use)=>{
    await use (new SingleOrMultipleClaimPage(page));
  },
  requestSupportPage:async({page}, use) => {
      await use (new RequestSupportPage (page));
  },
  manageSupportPage:async({page}, use) => {
      await use (new ManageSupportPage (page));
  }

});
