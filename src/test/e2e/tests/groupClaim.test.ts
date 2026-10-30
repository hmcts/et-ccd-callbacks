import { test } from '../fixtures/et.test.fixture.ts';
import {config, createCaseViaCitizenUI, resolveResourceJson, users} from "@hmcts/et-shared-test-library";

const userDetailsData = resolveResourceJson('resources/payload/user-details.json');
test.describe('Group Claim Case creation in Citizen UI', () => {

  test.skip(
    'Create a group claim for still working for organisation with two claimants as a group',
    {
      tag: [],
    },
    async ({
             page,
             loginPage,
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
