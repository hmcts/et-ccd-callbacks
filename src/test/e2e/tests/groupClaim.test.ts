import { test } from '../fixtures/et.test.fixture.ts';
import userDetailsData from '@hmcts/et-shared-test-library/resources/payload/user-details.json';
import {config, createCaseViaCitizenUI, users} from "@hmcts/et-shared-test-library";

test.describe('Group Claim Case creation in Citizen UI', () => {

  test.skip(
    'Create a group claim for still working for organisation with two claimants as a group',
    {
      tag: [],
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
             submitClaimPage, singleOrMultipleClaimPage, manageCaseDashboardPage
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
        'EnglandWales',
        'Claiming for myself', true,
        async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl)},  employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.processStillWorkingJourney(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      );


    },
  );


});
