import { test } from '../fixtures/common.fixture';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { config, users } from '../config/config.dynamic.ts';
import userDetailsData from '../resources/payload/user-details.json';
import { createCaseViaCitizenUI, vetAndAcceptCitizenCase } from '../pages/helpers/CuiCaseCreationHelper.ts';

test.describe('non-HMCTS claimant legal representative Case creation in Citizen UI', () => {

  test.skip();
  test(
    'Create a claim as a claimant representative for still working for organisation, submit and process within manage cases',
    {
      tag: [],
    },
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
             submitClaimPage, caseDetailsPage, singleOrMultipleClaimPage, manageCaseDashboardPage
           }) => {




      const submissionReference = await createCaseViaCitizenUI(
        page,
        citizenPreLoginPage,
        citizenPostLoginPage,
        personalDetailsPage,
        employmentAndRespondentDetailsPage,
        claimDetailsPage,
        submitClaimPage,
        singleOrMultipleClaimPage,
        'EnglandWales', 'Claiming for someone else', false,
        async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
        employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.processStillWorkingJourney(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      );

    },
  );


  test(
    'Create a claim as a claimant representative for  working notice period for organisation, submit and process within manage cases',
    {
      tag: [],
    },
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
             submitClaimPage, caseDetailsPage, singleOrMultipleClaimPage, manageCaseDashboardPage
           }) => {




      const submissionReference = await createCaseViaCitizenUI(
        page,
        citizenPreLoginPage,
        citizenPostLoginPage,
        personalDetailsPage,
        employmentAndRespondentDetailsPage,
        claimDetailsPage,
        submitClaimPage,
        singleOrMultipleClaimPage,
        'EnglandWales', 'Claiming for someone else', false,
        async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
        employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.processWorkingNoticePeriodJourney(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      );

    },
  );

  test(
    'Create a claim as a claimant representative for no longer working for organisation, submit and process within manage cases',
    {
      tag: [],
    },
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
             submitClaimPage, caseDetailsPage, singleOrMultipleClaimPage, manageCaseDashboardPage
           }) => {




      const submissionReference = await createCaseViaCitizenUI(
        page,
        citizenPreLoginPage,
        citizenPostLoginPage,
        personalDetailsPage,
        employmentAndRespondentDetailsPage,
        claimDetailsPage,
        submitClaimPage,
        singleOrMultipleClaimPage,
        'EnglandWales', 'Claiming for someone else', false,
        async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
        employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.processNoLongerWorkingForOrgJourney(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      );

    },
  );

  test(
    'Create a claim as a claimant representative DID NOT work for organisation, submit and process within manage cases',
    {
      tag: [],
    },
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
             submitClaimPage, caseDetailsPage, singleOrMultipleClaimPage, manageCaseDashboardPage
           }) => {




      const submissionReference = await createCaseViaCitizenUI(
        page,
        citizenPreLoginPage,
        citizenPostLoginPage,
        personalDetailsPage,
        employmentAndRespondentDetailsPage,
        claimDetailsPage,
        submitClaimPage,
        singleOrMultipleClaimPage,
        'EnglandWales', 'Claiming for someone else', false,
        async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
        employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.processDidNotWorkForOrganisationMakingClaimAgainst(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      );

    },
  );


  test(
    'Create a claim as a claimant representative with multiple respondent and multiple ACAS, submit and process within manage cases',
    {
      tag: [],
    },
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
             submitClaimPage, caseDetailsPage, singleOrMultipleClaimPage, manageCaseDashboardPage
           }) => {




      const submissionReference = await createCaseViaCitizenUI(
        page,
        citizenPreLoginPage,
        citizenPostLoginPage,
        personalDetailsPage,
        employmentAndRespondentDetailsPage,
        claimDetailsPage,
        submitClaimPage,
        singleOrMultipleClaimPage,
        'EnglandWales', 'Claiming for someone else', false,
        async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
        employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.multipleAcasCertificateAndMultipleRespondents(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      );

    },
  );


});


