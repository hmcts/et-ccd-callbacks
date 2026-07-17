import { test } from '../fixtures/common.fixture';
import userDetailsData from '../resources/payload/user-details.json';
import { createCaseViaCitizenUI, vetAndAcceptCitizenCase, partiallyCreateCaseViaCitizenUI } from '../pages/helpers/CuiCaseCreationHelper.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { config, users } from '../config/config.dynamic.ts';

let caseId: string;
let caseNumber: string;

test.describe('Case creation in Citizen UI', () => {

  test(
    'Create a claim for still working for organisation, submit and process within manage cases',
    {
      tag: ['@cx', '@smoke', '@ccd-callback-tests'],
    },
    async ({
      page,
      respondentRepPage,
      citizenHubLoginPage,
      citizenHubPage,
      contactTheTribunalPage,
      loginPage,
      et1VettingPage,
      et1CaseServingPage,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      employmentAndRespondentDetailsPage,
      claimDetailsPage,
      submitClaimPage, caseDetailsPage, manageCaseDashboardPage
    }) => {
      ({caseId, caseNumber}  = await createCaseViaCitizenUI(
        page,
        citizenPreLoginPage,
        citizenPostLoginPage,
        personalDetailsPage,
        employmentAndRespondentDetailsPage,
        claimDetailsPage,
        submitClaimPage,
        'EnglandWales',
        async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
        employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.processStillWorkingJourney(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      ));

      caseNumber = await vetAndAcceptCitizenCase(
        loginPage,
        et1VettingPage,
        et1CaseServingPage, manageCaseDashboardPage, caseDetailsPage,
        CaseTypeLocation.EnglandAndWales,
        caseId,
        users.etCaseWorker
      );

      await caseDetailsPage.selectNextEvent(Events.respondentRepresentative);
      await respondentRepPage.addRespondentRepresentative('registered', 'ET Organisation');

      await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
      await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(caseId);
      await citizenHubPage.citizenHubCaseOverviewPage(caseNumber);
      await citizenHubPage.navigateToContactTheTribunalPage();
      await contactTheTribunalPage.makeApplicationToTribunal('withdraw', 'Citizen withdrawing an application', 'Yes');
      await contactTheTribunalPage.clickStoreApplication();
      await contactTheTribunalPage.assertApplicationStoredSuccessPageIsDisplayed();
      await contactTheTribunalPage.clickCloseAndReturn();
    },
  );

  test('Create a claim for working notice period for organisation, submit and process within manage cases',
    async ({
      page,
      loginPage,
      et1VettingPage,
      et1CaseServingPage,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      employmentAndRespondentDetailsPage,
      claimDetailsPage,
      submitClaimPage, manageCaseDashboardPage, caseDetailsPage,
  }) => {
      ({caseId, caseNumber}  = await createCaseViaCitizenUI(
      page,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      employmentAndRespondentDetailsPage,
      claimDetailsPage,
      submitClaimPage,
      'EnglandWales',
      async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
      employmentAndRespondentDetailsPage =>
        employmentAndRespondentDetailsPage.processWorkingNoticePeriodJourney(
          userDetailsData.workPostcode,
          userDetailsData.selectedWorkAddress,
          userDetailsData.firstLineOfAddress,
        ),
    ));

      await vetAndAcceptCitizenCase(
        loginPage,
        et1VettingPage,
        et1CaseServingPage, manageCaseDashboardPage, caseDetailsPage,
        CaseTypeLocation.EnglandAndWales,
        caseId,
        users.etManageCaseUser
      );
  });

  test('Create a claim for no longer working for organisation, submit and process within manage cases',
    async ({
      page,
      loginPage,
      et1VettingPage,
      et1CaseServingPage,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      employmentAndRespondentDetailsPage,
      claimDetailsPage,
      submitClaimPage, manageCaseDashboardPage, caseDetailsPage,
  }) => {
      ({caseId, caseNumber}  = await createCaseViaCitizenUI(
      page,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      employmentAndRespondentDetailsPage,
      claimDetailsPage,
      submitClaimPage,
      'EnglandWales',
      async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
      employmentAndRespondentDetailsPage =>
        employmentAndRespondentDetailsPage.processNoLongerWorkingForOrgJourney(
          userDetailsData.workPostcode,
          userDetailsData.selectedWorkAddress,
          userDetailsData.firstLineOfAddress,
        ),
    ));

      await vetAndAcceptCitizenCase(
        loginPage,
        et1VettingPage,
        et1CaseServingPage, manageCaseDashboardPage, caseDetailsPage,
        CaseTypeLocation.EnglandAndWales,
        caseId,
        users.etManageCaseUser
      );
  });

  test('Create a claim for DID NOT work for organisation, submit and process within manage cases', async ({
    page,
    loginPage,
    et1VettingPage,
    et1CaseServingPage,
    citizenPreLoginPage,
    citizenPostLoginPage,
    personalDetailsPage,
    employmentAndRespondentDetailsPage,
    claimDetailsPage,
    submitClaimPage, manageCaseDashboardPage, caseDetailsPage,
  }) => {
    ({caseId, caseNumber}  = await createCaseViaCitizenUI(
      page,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      employmentAndRespondentDetailsPage,
      claimDetailsPage,
      submitClaimPage,
      'EnglandWales',
      async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
      employmentAndRespondentDetailsPage =>
        employmentAndRespondentDetailsPage.processDidNotWorkForOrganisationMakingClaimAgainst(
          userDetailsData.workPostcode,
          userDetailsData.selectedWorkAddress,
        ),
    ));

    await vetAndAcceptCitizenCase(
      loginPage,
      et1VettingPage,
      et1CaseServingPage, manageCaseDashboardPage, caseDetailsPage,
      CaseTypeLocation.EnglandAndWales,
      caseId,
      users.etManageCaseUser
    );
  });

  test('Submit a case from Scotland - Case Progressing Claimant Submit application - record a decision as ECM', async ({
    page,
    loginPage,
    et1VettingPage,
    et1CaseServingPage,
    citizenPreLoginPage,
    citizenPostLoginPage,
    personalDetailsPage,
    employmentAndRespondentDetailsPage,
    claimDetailsPage,
    submitClaimPage, manageCaseDashboardPage, caseDetailsPage,
  }) => {
    ({caseId, caseNumber}  = await createCaseViaCitizenUI(
      page,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      employmentAndRespondentDetailsPage,
      claimDetailsPage,
      submitClaimPage,
      'Scotland',
      async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
      employmentAndRespondentDetailsPage =>
        employmentAndRespondentDetailsPage.processNoLongerWorkingForOrgJourney(
          userDetailsData.scotWorkPostcode,
          userDetailsData.scotSelectedWorkAddress,
          userDetailsData.scotFirstLineOfAddress,
        ),
    ));

    await vetAndAcceptCitizenCase(
      loginPage,
      et1VettingPage,
      et1CaseServingPage, manageCaseDashboardPage, caseDetailsPage,
      CaseTypeLocation.Scotland,
      caseId,
      users.etManageCaseUser
    );
  });

  test('Partially create a claim and return to viewing your et1 claims', async ({
 page,
    loginPage,
    citizenPreLoginPage,
    citizenPostLoginPage,
    personalDetailsPage,
    et1ClaimsListPage,
  }) => {
    await partiallyCreateCaseViaCitizenUI(
      page,
      citizenPreLoginPage,
      citizenPostLoginPage,
      personalDetailsPage,
      'EnglandWales',
      async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) }
        )
    await et1ClaimsListPage.viewYourET1Claims()
    });
});
