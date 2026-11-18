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
  UploadHearingBundlePage, BasePage,
} from '../pages/index.js';
import {AxeUtils} from "@hmcts/playwright-common";

function wireAlly<T extends BasePage>(pageObject: T, axeUtils?: AxeUtils): T {
  if (axeUtils) {
    pageObject.setAccessibilityContext(axeUtils);
  }
  return pageObject;
}

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
type AllyFixture = { axeUtils: AxeUtils };
export const pageFixtures = base.extend<PageFixtures & AllyFixture>({

    applicationTabPage: async ({ page, axeUtils }, use) => {
        await use(wireAlly(new ApplicationTabPage(page), axeUtils));
    },

    manageCaseDashboardPage: async ({page, axeUtils}, use) => {
      await use(wireAlly(new ManageCaseDashboardPage(page), axeUtils));
    },

    caseListPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new CaseListPage(page), axeUtils));
    },

    createCaseFlagPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new CreateCaseFlagPage(page), axeUtils));
    },

    manageCaseFlagPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new ManageCaseFlagPage(page), axeUtils));
    },

    et1CaseServingPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new Et1CaseServingPage(page), axeUtils));
    },

    loginPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new LoginPage(page), axeUtils));
    },

    listHearingPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new ListHearingPage(page), axeUtils));
    },

    legalRepPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new LegalRepPage(page), axeUtils));
    },

    citizenHubPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new CitizenHubPage(page), axeUtils));
    },

    caseLinkPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new CaseLinkPage(page), axeUtils));
    },

    respondentRepPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespondentRepPage(page), axeUtils));
    },

    et3LoginPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new ET3LoginPage(page), axeUtils));
    },

    respondentCaseOverviewPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespondentCaseOverviewPage(page), axeUtils));
    },

    respondentTaskListPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespondentTaskListPage(page), axeUtils));
    },

    responseLandingPage: async ({page, axeUtils}, use) => {
        await use(wireAlly(new ResponseLandingPage(page), axeUtils));
    },

    respContactDetailsPages: async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespContactDetailsPages(page), axeUtils));
    },

    respClaimantDetails:async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespClaimantDetails(page), axeUtils));
    },

    respContestClaim:async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespContestClaim(page), axeUtils));
    },

    respSubmitEt3:async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespSubmitEt3(page), axeUtils));
    },

    bfActionPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new BfActionPage(page), axeUtils));
    },

    jurisdictionPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new JurisdictionPage(page), axeUtils));
    },

    caseTransferPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new CaseTransferPage(page), axeUtils));
    },

    caseTransferToEcmPage:async ({page, axeUtils}, use) => {
      await use(wireAlly(new CaseTransferToEcmPage(page), axeUtils));
    },

    caseWorkerNotificationPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new CaseWorkerNotificationPage(page), axeUtils));
    },

    claimantDetailsPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new ClaimantDetailsPage(page), axeUtils));
    },

    respondentDetailsPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new RespondentDetailsPage(page), axeUtils));
    },

    nocPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new NocPage(page), axeUtils));
    },

    manageOrgPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new ManageOrgPage(page), axeUtils));
    },

    icUploadDocPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new ICUploadDocPage(page), axeUtils));
    },

    restrictedReportingPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new RestrictedReportingPage(page), axeUtils));
    },

    uploadDocumentPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new UploadDocumentPage(page), axeUtils));
    },

    referralPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new ReferralPage(page), axeUtils));
    },

    draftJudgementPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new DraftJudgementPage(page), axeUtils));
    },

    issueJudgementPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new IssueJudgementPage(page), axeUtils));
    },

    searchAcasPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new SearchAcasPage(page), axeUtils));
    },

    lettersPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new LettersPage(page), axeUtils));
    },

    depositOrderPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new DepositOrderPage(page), axeUtils));
    },

    rolesAndAccessPage: async ({page, axeUtils}, use)=>{
        await use(wireAlly(new RolesAndAccessPage(page), axeUtils));
    },

    taskPage: async ({page, axeUtils}, use)=>{
        await use(wireAlly(new TaskPage(page), axeUtils));
    },

    hearingDetailsPage: async ({page, axeUtils}, use)=>{
        await use(wireAlly(new HearingDetailsPage(page), axeUtils));
    },

    adrDocument:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new AdrDocumentPage(page), axeUtils));
    },
    caseDetailsPage:async ({page, axeUtils}, use)=>{
        await use(wireAlly(new CaseDetailsPage(page), axeUtils));
    },
    et3NotificationPage:async ({page, axeUtils}, use)=>{
      await use(wireAlly(new Et3NotificationPage(page), axeUtils));
    },
    uploadHearingBundlePage:async({page, axeUtils}, use)=>{
        await use(wireAlly(new UploadHearingBundlePage(page), axeUtils));
    },
    caseNotesPage:async({page, axeUtils}, use)=>{
      await use(wireAlly(new CaseNotesPage(page), axeUtils));
    },
    manageTelephoneNotePage:async ({page, axeUtils}, use) => {
      await use(wireAlly(new ManageTelephoneNotePage(page), axeUtils));
    },
    claimantRepresentativePage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new ClaimantRepresentativePage(page), axeUtils));
    },
    closeCasePage:async({page, axeUtils}, use)=>{
      await use(wireAlly(new CloseCasePage(page), axeUtils));
    },
    reinstateCasePage:async({page, axeUtils}, use)=>{
      await use(wireAlly(new ReinstateCasePage(page), axeUtils));
    },
    uploadDocumentsForHearingPage:async({page, axeUtils}, use)=>{
      await use(wireAlly(new UploadDocumentsForHearingPage(page), axeUtils));
    },
    checkYourAnswersPage:async({page, axeUtils}, use) => {
      await use(new CheckYourAnswersPage(page));
    },
    baseEventPage:async({page, axeUtils}, use) => {
      await use(wireAlly(new BaseEventPage(page), axeUtils));
    },
    legalRepNotificationPage:async ({page, axeUtils}, use) => {
        await use(wireAlly(new LegalRepNotificationPage(page), axeUtils));
    },
   citizenHubLoginPage:async({page, axeUtils}, use) => {
      await use (wireAlly(new CitizenHubLoginPage (page), axeUtils));
    },
    contactTheTribunalPage:async({page, axeUtils}, use) => {
      await use (wireAlly(new ContactTheTribunalPage (page), axeUtils));
    },
    prepareAbdSubmitDocumentPage:async({page, axeUtils}, use) => {
      await use (wireAlly(new PrepareAndSubmitDocumentPage (page), axeUtils));
    },
  et1VettingPage:async ({page, axeUtils}, use)=>{
    await use(wireAlly(new Et1VettingPages(page), axeUtils));
  },
  citizenPreLoginPage:async({page, axeUtils}, use) => {
    await use (wireAlly(new CUIPreLoginPage (page), axeUtils));
  },
  citizenPostLoginPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new CUIPostLoginPages (page), axeUtils));
  },
  personalDetailsPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new PersonalDetailsPage (page), axeUtils));
  },
  employmentAndRespondentDetailsPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new EmploymentAndRespDetailsPage (page), axeUtils));
  },
  claimDetailsPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new ClaimDetailsPage (page), axeUtils));
  },
  submitClaimPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new SubmitClaimPage (page), axeUtils));
  },
  et1CreateDraftClaim:async({page, axeUtils}, use)=>{
    await use (wireAlly(new Et1CreateDraftClaim (page), axeUtils));
  },
  et3ProcessPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new ET3ProcessPage (page), axeUtils));
  },
  initialConsiderationPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new InitialConsiderationPage (page), axeUtils));
  },
  et3DetailsPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new Et3DetailsPage (page), axeUtils));
  },
  et3RespondentDetailsPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new Et3RespondentDetailsPage (page), axeUtils));
  },
  et3EmploymentDetailsPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new Et3EmploymentDetailsPage (page), axeUtils));
  },
  et3ResponseDetailsPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new Et3ResponseDetailsPage (page), axeUtils));
  },
  allocateHearingPAge:async({page, axeUtils}, use)=>{
    await use (wireAlly(new AllocateHearingPage(page), axeUtils));
  },
  et1ClaimsListPage:async({page, axeUtils}, use)=>{
      await use (wireAlly(new Et1ClaimsListPage (page), axeUtils));
  },
  amendContactDetailsLrPage:async({page, axeUtils}, use)=>{
      await use (wireAlly(new AmendContactDetailsLrPage (page), axeUtils));
  },
  citizenApplicationsPage:async({page, axeUtils}, use)=>{
      await use (wireAlly(new CitizenApplicationsPage (page), axeUtils));
  },
  resClaimantsApplicationsPage:async({page, axeUtils}, use)=>{
      await use (wireAlly(new ResClaimantsApplicationsPage (page), axeUtils));
  },
  singleOrMultipleClaimPage:async({page, axeUtils}, use)=>{
    await use (wireAlly(new SingleOrMultipleClaimPage(page), axeUtils));
  },
  requestSupportPage:async({page, axeUtils}, use) => {
      await use (wireAlly(new RequestSupportPage (page), axeUtils));
  },
  manageSupportPage:async({page, axeUtils}, use) => {
      await use (wireAlly(new ManageSupportPage (page), axeUtils));
  }

});
