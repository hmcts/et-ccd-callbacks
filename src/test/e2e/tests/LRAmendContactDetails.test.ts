import { test } from '../fixtures/common.fixture.ts';
import { config, users } from '../config/config.dynamic.ts';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../config/case-data.ts';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import { AddressDetails } from '../types/address.ts';
import LoginPage from '../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../pages/ManageCaseDashboardPage.ts';
import { Page } from '@playwright/test';
import CaseDetailsPage from '../pages/caseDetailsPage.ts';
import { createCaseViaCitizenUI } from '../pages/helpers/CuiCaseCreationHelper.ts';
import userDetailsData from '../resources/payload/user-details.json';
import { NocPage } from '../pages/legalRepresentative/NocPage.ts';
import RespondentRepPage from '../pages/respondentCitizenHub/respondentRepPage.ts';
import AmendContactDetailsLrPage from '../pages/events/AmendContactDetailsLrPage.ts';
import { CheckYourAnswersPage } from '../pages/helpers/CheckYourAnswersPage.ts';

let caseId: string;
let caseNumber: string;

test.describe( 'Legal Rep updates update contact to colleagues in his org', () => {
  let caseWorkerBrowserPage: Page;
  let loginPageCW: LoginPage;
  let manageCaseDashboardPageCW: ManageCaseDashboardPage;
  let caseDetailsPageCW: CaseDetailsPage;

  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })

  test.beforeEach(async ({manageCaseDashboardPage, loginPage, browserUtils}) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etLegalRepresentative);

    caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    loginPageCW = new LoginPage(caseWorkerBrowserPage);
    manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    caseDetailsPageCW = new CaseDetailsPage(caseWorkerBrowserPage);
  })

  test('Claimant Legal Rep updates contact to colleagues in his org',
    async ({
      nocPage, manageCaseDashboardPage, caseDetailsPage, amendContactDetailsLrPage, checkYourAnswersPage,
      browserUtils
   }) => {
    // LR takes the case by NOC
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, `${CaseDetailsValues.claimantFirstName} ${CaseDetailsValues.claimantLastName}`, caseNumber);

    // LR amends the contact details by 'Amend contact details'
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.selectNextEvent(Events.amendContactDetailsClaimant);
    let addressDetails: AddressDetails;
    let phNumber: string;
    ({ addressDetails, phNumber} = await amendContactDetailsLrPage.amendLegalRepContactDetails('Amend contact details', checkYourAnswersPage));
    const tabData = [
      {
        tabName: 'Claimant Representative',
        tabContent: [
          { tabItem: 'Phone number', value: phNumber },
          { tabItem: 'Building and Street', value: addressDetails.addressLine1 },
          { tabItem: 'Town or City', value: addressDetails.townOrCity },
          { tabItem: 'Postcode/Zipcode', value: addressDetails.postcode },
        ]
      }
    ];
    await caseDetailsPage.assertTabData(tabData);

    // login as Caseworker check the detail updated
    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.assertTabData(tabData);

// LR updates contact details by 'Use MyHMCTS details'
    await caseDetailsPage.selectNextEvent(Events.amendContactDetailsClaimant);
    ({ addressDetails,  phNumber} = await amendContactDetailsLrPage.amendLegalRepContactDetails('Use MyHMCTS details', checkYourAnswersPage));
    const updatedTabData = [
      {
        tabName: 'Claimant Representative',
        tabContent: [
          { tabItem: 'Building and Street', value: addressDetails.addressLine1 },
          { tabItem: 'Town or City', value: addressDetails.townOrCity },
          { tabItem: 'Postcode/Zipcode', value: addressDetails.postcode },
        ]
      }
    ];
    await caseDetailsPage.assertTabData(updatedTabData);

    // login as Caseworker check the detail updated
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.assertTabData(updatedTabData);
    await caseWorkerBrowserPage.close();
  });

  //RET-6237 to be released before testing this for Respondent LR
  test.skip('Respondent Legal Rep updates contact to colleagues in his org',
    async ({
             nocPage, manageCaseDashboardPage, caseDetailsPage, amendContactDetailsLrPage, checkYourAnswersPage
           }) => {
      await manageCaseDashboardPage.navigateToNoticeOfChange();
      await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

      await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPage.selectNextEvent(Events.amendContactDetails);
      let addressDetails: AddressDetails;
      let phNumber: string;
      ({ addressDetails, phNumber} = await amendContactDetailsLrPage.amendLegalRepContactDetails('Amend contact details', checkYourAnswersPage));
      const tabData = [
        {
          tabName: 'Respondent Representative',
          tabContent: [
            { tabItem: 'Phone number', value: phNumber },
            { tabItem: 'Building and Street', value: addressDetails.addressLine1 },
            { tabItem: 'Town or City', value: addressDetails?.townOrCity },
            { tabItem: 'Postcode/Zipcode', value: addressDetails?.postcode },
          ]
        }
      ];
      await caseDetailsPage.assertTabData(tabData);

      // login as Caseworker check the detail updated
      await manageCaseDashboardPageCW.visit();
      await loginPageCW.processLogin(users.etCaseWorker);
      await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPageCW.assertTabData(tabData);

      await caseDetailsPage.selectNextEvent(Events.amendContactDetails);
      ({ addressDetails,  phNumber} = await amendContactDetailsLrPage.amendLegalRepContactDetails('Use MyHMCTS details', checkYourAnswersPage));
      const updatedTabData = [
        {
          tabName: 'Respondent Representative',
          tabContent: [
            { tabItem: 'Building and Street', value: addressDetails.addressLine1 },
            { tabItem: 'Town or City', value: addressDetails?.townOrCity },
            { tabItem: 'Postcode/Zipcode', value: addressDetails?.postcode },
          ]
        }
      ];
      await caseDetailsPage.assertTabData(updatedTabData);

      // login as Caseworker check the detail updated
      await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
      await caseDetailsPageCW.assertTabData(updatedTabData);
      await caseWorkerBrowserPage.close();
    });
});

//Ret-6041 skipped till dev ticket is released.
test.describe("Legal Rep Amend contact details for multiple respondents", () => {
  test.skip('Create a claim with multiple ACAS certificates, submit and process within manage cases, LR assigns each respondents,' +
    'Caseworker removes LR2 and LR1 tries to amend contact details',
    async ({
            page,
            loginPage,
            citizenPreLoginPage,
            citizenPostLoginPage,
            personalDetailsPage,
            employmentAndRespondentDetailsPage,
            claimDetailsPage,
            submitClaimPage, browserUtils
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
      async() => { await loginPage.processLogin(users.etClaimant, config.etSyaUiUrl) },
      employmentAndRespondentDetailsPage =>
        employmentAndRespondentDetailsPage.multipleAcasCertificate(
          userDetailsData.workPostcode,
          userDetailsData.selectedWorkAddress,
          userDetailsData.firstLineOfAddress,
        ),
    ));
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));

    const legalRep1BrowserPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative.sessionFile);
    const legalRep2BrowserPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative2.sessionFile);
    const loginPageLR1 = new LoginPage(legalRep1BrowserPage);
    const loginPageLR2 = new LoginPage(legalRep2BrowserPage);
    const manageCaseDashboardPageLR1 = new ManageCaseDashboardPage(legalRep1BrowserPage);
    const manageCaseDashboardPageLR2 = new ManageCaseDashboardPage(legalRep2BrowserPage);
    const nocPageLR1 = new NocPage(legalRep1BrowserPage);
    const nocPageLR2 = new NocPage(legalRep2BrowserPage);
    const caseDetailsPageLR1 = new CaseDetailsPage(legalRep1BrowserPage);
    const amendContactDetailsLrPageLr1 = new AmendContactDetailsLrPage(legalRep1BrowserPage);
    const checkYourAnswersPageLr1 = new CheckYourAnswersPage(legalRep1BrowserPage);

    await manageCaseDashboardPageLR1.visit();
    await loginPageLR1.processLogin(users.etLegalRepresentative);
    await manageCaseDashboardPageLR1.navigateToNoticeOfChange();
    await nocPageLR1.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber);

    await manageCaseDashboardPageLR2.visit();
    await loginPageLR2.processLogin(users.etLegalRepresentative2);
    await manageCaseDashboardPageLR2.navigateToNoticeOfChange();
    await nocPageLR2.processNocRequest(caseId, CaseDetailsValues.respondentName2, caseNumber);

    const caseWorkerPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    const loginPageCW = new LoginPage(caseWorkerPage);
    const manageCaseDashboardPageCW = new ManageCaseDashboardPage(caseWorkerPage);
    const caseDetailsPageCW = new CaseDetailsPage(caseWorkerPage);
    const respondentRepPageCW = new RespondentRepPage(caseWorkerPage);

    await manageCaseDashboardPageCW.visit();
    await loginPageCW.processLogin(users.etCaseWorker);
    await manageCaseDashboardPageCW.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageCW.selectNextEvent(Events.respondentRepresentative);
    await respondentRepPageCW.clickRemoveRespondentRepresentative(1);
    await respondentRepPageCW.clickSubmitButton();

    await manageCaseDashboardPageLR1.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPageLR1.selectNextEvent(Events.amendContactDetails);
    let addressDetails: AddressDetails;
    let phNumber: string;
    ({ addressDetails, phNumber} = await amendContactDetailsLrPageLr1.amendLegalRepContactDetails('Amend contact details', checkYourAnswersPageLr1));
    const tabData = [
      {
        tabName: 'Claimant Representative',
        tabContent: [
          { tabItem: 'Phone number', value: phNumber },
          { tabItem: 'Building and Street', value: addressDetails.addressLine1 },
          { tabItem: 'Town or City', value: addressDetails.townOrCity },
          { tabItem: 'Postcode/Zipcode', value: addressDetails.postcode },
        ]
      }
    ];
    await caseDetailsPageLR1.assertTabData(tabData);

    await caseWorkerPage.close();
    await legalRep1BrowserPage.close();
    await legalRep2BrowserPage.close();
  });
});
