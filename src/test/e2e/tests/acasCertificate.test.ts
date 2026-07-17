import { test  } from '../fixtures/common.fixture';
import userDetailsData from '../resources/payload/user-details.json';
import acasCertData from '../resources/payload/acas-content.json';
import { createCaseViaCitizenUI, vetAndAcceptCitizenCase } from '../pages/helpers/CuiCaseCreationHelper.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { config, users } from '../config/config.dynamic.ts';

test.describe('Add & Search ACAS certificate tests', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile
  })
  let caseId: string;
  let caseNumber: string;

  test.beforeEach(
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
      submitClaimPage, manageCaseDashboardPage, caseDetailsPage, citizenHubLoginPage
    }) => {

      ({caseId, caseNumber} = await createCaseViaCitizenUI(
        page,
        citizenPreLoginPage,
        citizenPostLoginPage,
        personalDetailsPage,
        employmentAndRespondentDetailsPage,
        claimDetailsPage,
        submitClaimPage,
        'EnglandWales',
        async () => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl);},
        employmentAndRespondentDetailsPage =>
          employmentAndRespondentDetailsPage.processStillWorkingJourney(
            userDetailsData.workPostcode,
            userDetailsData.selectedWorkAddress,
            userDetailsData.firstLineOfAddress,
          ),
      ));

      await vetAndAcceptCitizenCase(
        loginPage,
        et1VettingPage,
        et1CaseServingPage,
        manageCaseDashboardPage,
        caseDetailsPage,
        CaseTypeLocation.EnglandAndWales,
        caseId,
        users.etCaseWorker
      );
    },
  );

  test('England case - ACAS certificate', {tag: '@demo'}, async ({ caseListPage, searchAcasPage, caseDetailsPage}) => {
      // Verify ACAS certificate
      await caseDetailsPage.navigateToTab('Documents');
      await caseListPage.verifyAcasCertificateDetailsOnTab(acasCertData.docName, acasCertData.docType);

      // const str = acasCertData.docName;
      let acasCertNum = acasCertData.docName.match(/[A-Za-z0-9]+\/[0-9]+\/[0-9]+/);

      // Successfully search for ACAS Certificate
      await caseDetailsPage.selectNextEvent(Events.searchAcasCertificate);
      await searchAcasPage.findAcasCertificateSuccessfully(acasCertNum ? acasCertNum[0].toString() : "No match");
  });
})
