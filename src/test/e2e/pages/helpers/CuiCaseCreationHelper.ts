import { Page } from '@playwright/test';
import CUIPreLoginPage from '../claimantCitizenHub/CUIPreLoginPage.ts';
import CUIPostLoginPages from '../claimantCitizenHub/CUIPostLoginPages.ts';
import PersonalDetailsPage from '../claimantCitizenHub/personalDetailsPage.ts';
import EmploymentAndRespDetailsPage from '../claimantCitizenHub/employmentAndRespDetailsPage.ts';
import ClaimDetailsPage from '../claimantCitizenHub/claimDetailsPage.ts';
import SubmitClaimPage from '../claimantCitizenHub/submitClaimPage.ts';

import userDetailsData from '../../resources/payload/user-details.json';
import LoginPage from '../loginPage.ts';
import Et1VettingPages from '../et1VettingPages.ts';
import Et1CaseServingPage from '../et1CaseServingPage.ts';
import { ManageCaseDashboardPage } from '../ManageCaseDashboardPage.ts';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../../config/case-data.ts';
import CaseDetailsPage from '../caseDetailsPage.ts';
import DateUtilComponent from '../../data-utils/DateUtilComponent.ts';
import { config, UserCredentials } from '../../config/config.dynamic.ts';
import SingleOrMultipleClaimPage from '../claimantCitizenHub/singleOrMultipleClaimPage.ts';

export async function createCaseViaCitizenUI(
  page: Page,
  citizenPreLoginPage: CUIPreLoginPage,
  citizenPostLoginPage: CUIPostLoginPages,
  personalDetailsPage: PersonalDetailsPage,
  employmentAndRespondentDetailsPage: EmploymentAndRespDetailsPage,
  claimDetailsPage: ClaimDetailsPage,
  submitClaimPage: SubmitClaimPage,
  singleOrMultipleClaimPage:SingleOrMultipleClaimPage,
  region: string,
  typeOfClaim:string,
  groupClaim:boolean,
  loginMethod: () => Promise<void>,
  employmentJourneyMethod?: (page: any) => Promise<void>,
) {
  await page.goto(config.etSyaUiUrl);
  await citizenPreLoginPage.processPreLoginPagesForTheDraftApplication(region, typeOfClaim);
  await loginMethod();
  await citizenPostLoginPage.processPostLoginPagesForTheDraftApplication();
  const location = region === 'EnglandWales' ? 'England' : region;

  if (typeOfClaim == 'Claiming for myself') {
    //Section-1
    await personalDetailsPage.processPersonalDetails(userDetailsData.postcode, location, userDetailsData.addressOption);
    //group claim logic
    //Section-2
    if(groupClaim){
      await singleOrMultipleClaimPage.processClaimingWithOthers();
      await personalDetailsPage.addClaimantsDetailsManually();
    }
    //Section-3
    if (employmentJourneyMethod) await employmentJourneyMethod(employmentAndRespondentDetailsPage);
    await claimDetailsPage.processClaimDetails();
  } else if (typeOfClaim == 'Claiming for someone else') {
    //non-HMCTS- Section1,2,3,4
    await personalDetailsPage.processRepresentativeDetails(userDetailsData.representativePostcode, location, userDetailsData.representativeAddressOption);
    await personalDetailsPage.processClaimantDetails(userDetailsData.postcode, userDetailsData.addressOption);
    if (employmentJourneyMethod) await employmentJourneyMethod(employmentAndRespondentDetailsPage);
    await claimDetailsPage.processClaimDetails();
  }else if (typeOfClaim == 'legal representative representing a claimant') {
    // TODO: Update tests and logic here after introducing feature
  } else {
    throw new Error(`Option not found for who is making the claim question`);
  }
   //Section-5
  const caseId = await submitClaimPage.submitClaim();
  const caseNumber = await submitClaimPage.returnToCaseOverviewAndReturnCaseNumber();
  return {caseId, caseNumber};
}

async function assertTabData(caseDetailsPage: CaseDetailsPage, etiVetting: boolean = false) {
  const tabData = [
    {
      tabName: 'Case Details',
      tabContent: [
        { tabItem: 'Claimant', value: `${CaseDetailsValues.claimantFirstName} ${CaseDetailsValues.claimantLastName}`, position: 0 },
        { tabItem: 'Respondent', value: `${CaseDetailsValues.respondentName}`, position: 0 }
      ]
    },
    {
      tabName: 'Claimant',
      tabContent: [
        'Claimant Details'
      ]
    },
    {
      tabName: 'Respondent',
      tabContent: [
        { tabItem: CaseDetailsValues.respondentName, value: 'No', clickable: true },
      ]
    },
    {
      tabName: 'Jurisdictions',
      tabContent: [
        { tabItem: 'Jurisdiction Code', value: 'DDA' },
        'Suffered a detriment, discrimination, including indirect discrimination, and discrimination based on association or perception, harassment and/or dismissal on grounds of disability or failure of employer to make reasonable adjustments',
        { tabItem: 'Jurisdiction Code', value: 'PID' },
        'Suffered a detriment and/or dismissal due to exercising rights under the Public Interest Disclosure Act',
        { tabItem: 'Jurisdiction Code', value: 'DAG' },
        'Discrimination, including harassment or discrimination based on association or perception on grounds of age'
      ]
    },
    {
      tabName: 'Referrals',
      tabContent: [
        'Send a new referral',
        'Update a referral',
        'Reply to a referral',
        'Close a referral'
      ]
    },
    {
      tabName: 'History',
      tabContent: [
      ]
    },
    {
      tabName: 'Documents',
      tabContent: [
        'Case documentation',
      ]
    }
  ];

  if (etiVetting) {
    tabData.push({
      tabName: 'ET1 Vetting',
      tabContent: [
        {
          tabItem: 'ET1 Vetting Document',
          value: `ET1 Vetting - ${CaseDetailsValues.claimantFirstName} ${CaseDetailsValues.claimantLastName}.pdf`
        },
        { tabItem: 'Date completed:', value: DateUtilComponent.formatToDayMonthYearShort(DateUtilComponent.getCurrentDate()) }
      ]
    })
  }

  await caseDetailsPage.assertTabData(tabData);
}

export async function vetAndAcceptCitizenCase(
  loginPage: LoginPage,
  et1VettingPage: Et1VettingPages,
  et1CaseServingPage: Et1CaseServingPage,
  manageCaseDashboardPage: ManageCaseDashboardPage,
  caseDetailsPage: CaseDetailsPage,
  caseTypeLocation: CaseTypeLocation,
  submissionReference: string,
  loginCredentials: UserCredentials,
) {
  await manageCaseDashboardPage.visit();
  await loginPage.processLogin(loginCredentials);
  let caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(submissionReference,caseTypeLocation)
  await assertTabData(caseDetailsPage);

  await caseDetailsPage.selectNextEvent(Events.et1Vetting);
  await et1VettingPage.processET1CaseVettingPages();
  await assertTabData(caseDetailsPage, true);
  await caseDetailsPage.selectNextEvent(Events.acceptRejectCase);
  await et1CaseServingPage.processET1CaseServingPages();

  return caseNumber;
}

export async function partiallyCreateCaseViaCitizenUI(
  page: Page,
  citizenPreLoginPage: CUIPreLoginPage,
  citizenPostLoginPage: CUIPostLoginPages,
  personalDetailsPage: PersonalDetailsPage,
  region: string,
  typeOfClaim:string,
  loginMethod: () => Promise<void>,
) {
  await page.goto(config.etSyaUiUrl);
  await citizenPreLoginPage.processPreLoginPagesForTheDraftApplication(region, typeOfClaim);
  await loginMethod();
  await citizenPostLoginPage.processPostLoginPagesForTheDraftApplication();
  const location = region === 'EnglandWales' ? 'England' : region;
  await personalDetailsPage.processPersonalDetails(userDetailsData.postcode, location, userDetailsData.addressOption);
  await page.getByRole('button', { name: 'Save as draft' }).click();
}
