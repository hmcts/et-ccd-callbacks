import { expect, Locator, Page } from '@playwright/test';
import CitizenHubPage from './CitizenHubPage.ts';
import DateUtilComponent from '../../data-utils/DateUtilComponent.ts';
import dateUtilComponent from '../../data-utils/DateUtilComponent.ts';
import { CaseDetailsValues } from '../../config/case-data.ts';

const today = new Date();
let inNoticePeriod: boolean = true;

export default class EmploymentAndRespDetailsPage extends CitizenHubPage {
  private readonly employmentStatusLink: Locator;
  private readonly workedForOrganisationGroup: Locator;
  private readonly stillWorkingFOrOrgGroup: Locator;
  private readonly stillWorkingOption: Locator;
  private readonly workingNoticePeriodOption: Locator;
  private readonly noLongerWorkingOption: Locator;
  private readonly employmentDetailsHeading: Locator;
  private readonly jobTitleInput: Locator;
  private readonly employmentStartDateHeading: Locator;
  private readonly employmentStartDateInputGroup: Locator;
  private readonly writtenContractNoticePeriodGroup: Locator;
  private readonly noticePeriodTypeGroup: Locator;
  private readonly noticePeriodWeeksOption: Locator;
  private readonly noticePeriodTime:Locator;
  private readonly noticePeriodInput: Locator;
  private readonly averageWeeklyHoursHeading: Locator;
  private readonly averageWeeklyHoursPastHeading: Locator;
  private readonly averageWeeklyHoursInput: Locator;
  private readonly payHeading: Locator;
  private readonly payBeforeTaxInput: Locator;
  private readonly payAfterTaxInput: Locator;
  private readonly payWeeklyOption: Locator;
  private readonly pensionContributionHeading: Locator;
  private readonly pensionYesOption: Locator;
  private readonly pensionContributionInput: Locator;
  private readonly employeeBenefitsHeading: Locator;
  private readonly employeeBenefitsInputGroup: Locator;
  private readonly employeeBenefitsYesOption: Locator;
  // working notice period properties
  private readonly endOfNoticePeriodHeading: Locator;
  private readonly endOfNoticePeriodQuestion: Locator;
  private readonly endOfNoticePeriodInputGroup: Locator;
  // No longer working properties
  private readonly employmentEndDateHeading: Locator;
  private readonly employmentEndDateQuestion: Locator;
  private readonly employmentEndDateInputGroup: Locator;
  private readonly noticePeriodTypeNoLongerWorkingGroup: Locator;
  private readonly noticePeriodNoLongerWorkingGroup: Locator;
  private readonly newJobHeading: Locator;
  private readonly startOfNewJobHeading: Locator;
  private readonly startOfNewJobQuestion: Locator;
  private readonly startOfNewJobInputGroup: Locator;
  private readonly newJobPayHeading: Locator;
  private readonly newJobPayBeforeTaxInput: Locator;
  private readonly newJobAnnualPayRadioOption: Locator;
  private readonly respondentNameHeading: Locator;
  private readonly respondentNameInput: Locator;
  private readonly respondentPostCodeInput: Locator;
  private readonly selectAnAddressHeading: Locator;
  private readonly respondentAddressSelect: Locator;
  private readonly acasCertificateHeading: Locator;
  private readonly acasCertificateYesOption: Locator;
  private readonly acasCertificateInput: Locator;
  private readonly acasCertificateNoOption: Locator;
  private readonly reasonForNoAcasCertificateHeading: Locator;
  private readonly reasonForNoAcasQuestion: Locator;
  private readonly reasonForNoAcasCertificateFirstOption: Locator;
  private readonly didYouWorkAtHeading: Locator;
  private readonly didYouWorkAtYesOption: Locator;
  private readonly checkRespondentDetailsHeading: Locator;
  private readonly noticePeriodHeading: Locator;
  private readonly newJobInput: Locator;

  constructor(page: Page) {
    super(page);
    this.employmentStatusLink = this.page.locator(
      'a[href="/past-employer?lng=en"], a[href="/did-claimant-work-for-employer?lng=en"], a[href="/past-employer"]');
    this.workedForOrganisationGroup = this.page.locator(
      'fieldset:has(h1:text-matches("work for the organisation or person", "i"))'
    );
    this.stillWorkingFOrOrgGroup = this.page.locator(
      'fieldset:has(h1:text-matches("still working for the organisation or person", "i"))'
    );
    this.stillWorkingOption = this.page.locator(`#still-working, #is-claimant-still-working`);
    this.workingNoticePeriodOption = this.page.locator(`#still-working-2, #is-claimant-still-working-2`);
    this.noLongerWorkingOption = this.page.locator(`#still-working-3, #is-claimant-still-working-3`);
    this.employmentDetailsHeading = this.page.getByRole('heading', { name: 'Employment details' });
    this.jobTitleInput = this.page.locator(`#jobTitle`);
    this.employmentStartDateHeading = this.page.getByRole('heading', { name: 'Employment start date' });
    this.employmentStartDateInputGroup = this.page.locator(`#startDate`);
    this.writtenContractNoticePeriodGroup = this.page.locator(
      'fieldset:has-text("written contract with a notice period")'
    );
    this.noticePeriodTypeGroup = this.page.locator(
      'fieldset:has(h1:text-matches("notice period in weeks or months", "i"))'
    );
    this.noticePeriodHeading = this.page.locator(
      'h1:text-matches("How many weeks.*notice period", "i")'
    );
    this.noticePeriodWeeksOption = this.page.locator(`#notice-type, #claimant-notice-type`);
    this.noticePeriodInput = this.page.locator(`#notice-length, #claimant-notice-length`);
    this.averageWeeklyHoursHeading = this.page.getByRole('heading', {
      name: /^(What are your average weekly hours\? \(optional\)|Claimant's average weekly hours \(optional\))$/,
    });
    this.averageWeeklyHoursPastHeading = this.page.getByRole('heading', {
      name: /average weekly hours.*optional/i,
    });
    this.averageWeeklyHoursInput = this.page.locator('#avg-weekly-hrs');
    this.payHeading = this.page.getByRole('heading', {
      name: /^(Your pay \(optional\)|Claimant's pay \(optional\))$/,
    });
    this.payBeforeTaxInput = this.page.locator('#pay-before-tax');
    this.payAfterTaxInput = this.page.locator('#pay-after-tax');
    this.payWeeklyOption = this.page.locator('#pay-interval');
    this.pensionContributionHeading = this.page.getByRole('heading', {
      name: /^(Did the respondent make any contributions to your pension\? \(optional\)|Does the respondent make contributions to the claimant's pension\? \(optional\))$/,
    });
    this.pensionYesOption = this.page.locator('#pension, #claimant-pension');
    this.pensionContributionInput = this.page.locator('#pension-contributions');
    this.employeeBenefitsHeading = this.page.getByRole('heading', { name: 'Employee benefits' });
    this.employeeBenefitsInputGroup = this.page.locator(
      'fieldset:has(legend:text-matches("employee benefits", "i"))'
    );
    this.employeeBenefitsYesOption = this.page.locator('#employee-benefits');
    this.endOfNoticePeriodHeading = this.page.getByRole('heading', { name: 'End of notice period' });
    this.endOfNoticePeriodQuestion = this.page.locator(
      `fieldset:has(legend:text("When does your notice period end?"))`,
    );
    this.endOfNoticePeriodInputGroup = this.page.locator(`#notice-dates`);
    this.employmentEndDateHeading = this.page.getByRole('heading', { name: 'Employment end date' });
    this.employmentEndDateQuestion = this.page.locator(`fieldset:has(legend:text("When did your employment end?"))`);
    this.employmentEndDateInputGroup = this.page.locator(`#end-date`);
    this.noticePeriodTypeNoLongerWorkingGroup = this.page.locator(
      'fieldset:has(legend:text-matches("have or work a notice period", "i"))'
    );
    this.noticePeriodNoLongerWorkingGroup = this.page.locator(
      'fieldset:has(h1:text-matches("notice period in weeks or months", "i"))'
    );
    this.noticePeriodTime = this.page.getByRole('heading', {
      name: /how many weeks in .* notice period/i,
    });
    this.newJobHeading = this.page.locator(
      `fieldset:has(h1:text("Has the claimant got a new job? (optional)")),
   fieldset:has(legend:text("Have you got a new job? (optional)"))`
    );
    this.newJobInput = this.page.locator(`#new-job, #claimant-new-job`);
    this.startOfNewJobHeading = this.page.getByRole('heading', { name: 'Start of new job' });
    this.startOfNewJobQuestion = this.page.locator(
      'fieldset:has(legend:text-matches("start.*new job", "i"))'
    );
    this.startOfNewJobInputGroup = this.page.locator(`#new-job-start-date`);
    this.newJobPayHeading = this.page.getByRole('heading', {
      name: /new job pay.*optional/i,
    });
    this.newJobPayBeforeTaxInput = this.page.locator(`#new-pay-before-tax`);
    this.newJobAnnualPayRadioOption = this.page.locator(`#new-job-pay-interval-3`);

    this.respondentNameHeading = this.page.getByRole('heading', {
      name: `What is the name of the respondent you're making the claim against?`,
    });
    this.respondentNameInput = this.page.locator('#respondentName');
    this.respondentPostCodeInput = this.page.locator('#respondentEnterPostcode');
    this.selectAnAddressHeading = this.page.getByRole('heading', { name: 'Select an address' });
    this.respondentAddressSelect = this.page.locator('#respondentAddressTypes');
    this.acasCertificateHeading = this.page.getByRole('heading', { name: 'Acas certificate for the respondent' });
    this.acasCertificateYesOption = this.page.locator('#acasCert');
    this.acasCertificateInput = this.page.locator('#acasCertNum');
    this.acasCertificateNoOption = this.page.locator('#acasCert-2');
    this.reasonForNoAcasCertificateHeading = this.page.getByRole('heading', {
      name: 'Reason for not having a certificate number',
    });
    this.reasonForNoAcasQuestion = this.page.locator(
      `fieldset:has(legend:text("Why do you not have an Acas number?"))`,
    );
    this.reasonForNoAcasCertificateFirstOption = this.page.locator('#no-acas-reason');
    this.didYouWorkAtHeading = this.page.getByRole('heading', { name: /^Did (you|the claimant) work at/i });
    this.didYouWorkAtYesOption = this.page.locator('#work-address, #claimant-work-address');
    this.checkRespondentDetailsHeading = this.page.getByRole('heading', { name: 'Check the respondent details' });
  }

  //clicks employment status link
  async clickEmploymentStatusLink() {
    await this.page.waitForLoadState('load');
    await expect(this.employmentStatusLink).toBeVisible();
    await this.employmentStatusLink.click();
  }

  //function to click yes worked for organisation on /past-employer page
  async selectWorkedForOrganisation(workedForOrg: string) {
    await this.page.waitForLoadState('load');
    await expect(this.workedForOrganisationGroup).toBeVisible();

    if (workedForOrg === 'Yes') {
      await this.workedForOrganisationGroup
        .locator('#past-employer, #did-claimant-work-for-employer')
        .click();
    } else if (workedForOrg === 'No') {
      await this.workedForOrganisationGroup.locator('#past-employer-2, #did-claimant-work-for-employer-2').click();
    }
    await this.saveAndContinueButton();
  }

  //selects still working for respondent on /are-you-still-working page
  async stillWorkingForOrganisation() {
    await this.page.waitForLoadState('load');
    await expect(this.stillWorkingFOrOrgGroup).toBeVisible();
    await this.stillWorkingOption.check();
    await this.saveAndContinueButton();
  }

  //selects working notice period for respondent on /are-you-still-working page
  async workingNoticePeriodForOrganisation() {
    await this.page.waitForLoadState('load');
    await expect(this.stillWorkingFOrOrgGroup).toBeVisible();
    await this.workingNoticePeriodOption.check();
    await this.saveAndContinueButton();
  }

  // //selects i'm no longer working for respondent on /are-you-still-working page
  async noLongerWorkingForOrganisation() {
    await this.page.waitForLoadState('load');
    await expect(this.stillWorkingFOrOrgGroup).toBeVisible();
    await this.noLongerWorkingOption.check();
    await this.saveAndContinueButton();
  }

  //check page title and enter job title
  async enterEmploymentJobTitle() {
    await this.page.waitForLoadState('load');
    await expect(this.employmentDetailsHeading).toBeVisible();
    await this.jobTitleInput.fill('Tester');
    await this.saveAndContinueButton();
  }

  //employment start date page
  async enterEmploymentStartDate() {
    await this.page.waitForLoadState('load');
    await expect(this.employmentStartDateHeading).toBeVisible();
    await this.employmentStartDateInputGroup.getByLabel('Day').fill('20');
    await this.employmentStartDateInputGroup.getByLabel('Month').fill('04');
    await this.employmentStartDateInputGroup.getByLabel('Year').fill('2014');
    await this.saveAndContinueButton();
  }

  //select yes to notice period on /got-a-notice-period page
  async selectNoticePeriodWhilstStillWorkingFlow(option: string = 'yes') {
    await this.page.waitForLoadState('load');
    await expect(this.writtenContractNoticePeriodGroup).toBeVisible();

    if (option === 'yes') {
      await this.writtenContractNoticePeriodGroup.getByLabel('Yes').check();
    } else {
      await this.writtenContractNoticePeriodGroup.getByLabel('No').check();
    }

    await this.saveAndContinueButton();
  }

  //select weeks for notice type on /notice-type page
  async selectNoticeType() {
    await this.page.waitForLoadState('load');
    await expect(this.noticePeriodTypeGroup).toBeVisible();
    await this.noticePeriodWeeksOption.check();
    await this.saveAndContinueButton();
  }

  //enter notice length on /notice-length page
  async enterNoticePeriodLength() {
    await expect(this.noticePeriodHeading).toBeVisible();
    await this.noticePeriodInput.fill('4');
    await this.saveAndContinueButton();
  }

  //enters notice period end date
  async noticePeriodEndDate() {
    let [year, month, day] = dateUtilComponent.addWeekdays(new Date(), 21).toISOString().split('T')[0].split('-');
    await this.page.waitForLoadState('load');
    await expect(this.endOfNoticePeriodHeading).toBeVisible();
    await expect(this.endOfNoticePeriodQuestion).toBeVisible();

    await this.endOfNoticePeriodInputGroup.getByLabel('Day').fill(day);
    await this.endOfNoticePeriodInputGroup.getByLabel('Month').fill(month);
    await this.endOfNoticePeriodInputGroup.getByLabel('Year').fill(year);

    await this.saveAndContinueButton();
  }

  // //Enters employment end date dates
  async enterEmploymentEndDate() {
    let [year, month, day] = dateUtilComponent.addWeekdays(new Date(), -10).toISOString().split('T')[0].split('-');
    await this.page.waitForLoadState('load');
    await expect(this.employmentEndDateHeading).toBeVisible();
    await expect(this.employmentEndDateQuestion).toBeVisible();
    await expect(this.employmentEndDateInputGroup).toBeVisible();

    await this.employmentEndDateInputGroup.getByLabel('Year').fill(year);
    await this.employmentEndDateInputGroup.getByLabel('Month').fill(month);
    await this.employmentEndDateInputGroup.getByLabel('Day').fill(day);

    await this.saveAndContinueButton();
  }

  //selects yes to did you have or work a notice period on /got-a-notice-period page
  async selectYesNoticePeriodNoLongerWorking() {
    await this.page.waitForLoadState('load');
    await expect(this.noticePeriodTypeNoLongerWorkingGroup).toBeVisible();
    await this.page.locator('#claimant-notice-period, #notice-period' ).check();
    await this.saveAndContinueButton();
  }

  //select yes for did you have or work a notice period question
  async selectNoticeTypeNoLongerWorking() {
    await this.page.waitForLoadState('load');
    await expect(this.noticePeriodNoLongerWorkingGroup).toBeVisible();
    await this.noticePeriodNoLongerWorkingGroup.locator('#notice-type, #claimant-past-notice-type').check();
    await this.saveAndContinueButton();
  }

  //enter notice length on /notice-length page
  async enterNoticePeriodLengthNoLongerWorking() {
    await expect(this.noticePeriodTime).toBeVisible();
    await this.noticePeriodInput.fill('4');
    await this.saveAndContinueButton();
  }

  //enter average weekly hours
  async enterAverageWeeklyHours(isPastEmployment: boolean = false) {
    await this.page.waitForLoadState('load');
    if(!isPastEmployment) {
      await expect(this.averageWeeklyHoursHeading).toBeVisible();
    } else {
      await expect(this.averageWeeklyHoursPastHeading).toBeVisible();
    }
    await this.averageWeeklyHoursInput.fill('20');
    await this.saveAndContinueButton();
  }

  //enters pay on the /pay page
  async enterPay() {
    await this.page.waitForLoadState('load');
    await expect(this.payHeading).toBeVisible();
    await this.payBeforeTaxInput.fill('40000');
    await this.payAfterTaxInput.fill('35000');
    await this.payWeeklyOption.check();
    await this.saveAndContinueButton();
  }

  //enter Pension contribution on /pension page
  async enterPensionContribution() {
    await this.page.waitForLoadState('load');
    await expect(this.pensionContributionHeading).toBeVisible();
    await this.pensionYesOption.check();
    await this.pensionContributionInput.fill('200');
    await this.saveAndContinueButton();
  }

  //enter employee benefits on /benefits page
  async enterEmployeeBenefits() {
    await this.page.waitForLoadState('load');
    await expect(this.employeeBenefitsHeading).toBeVisible();
    await expect(this.employeeBenefitsInputGroup).toBeVisible();
    await this.employeeBenefitsYesOption.check();
    await this.saveAndContinueButton();
  }

  //enter employment benefir for no longer working different to flow on R1.1.2
  async enterEmployeeBenefitsForNoLongerWorking() {
    await this.page.waitForLoadState('load');
    await expect(this.employeeBenefitsHeading).toBeVisible();
    await expect(this.employeeBenefitsInputGroup).toBeVisible();
    await this.employeeBenefitsYesOption.check();
    await this.saveAndContinueButton();
  }

  //Selects yes to new job on /new-job page
  async newJobDetails() {
    await this.page.waitForLoadState('load');
    await expect(this.newJobHeading).toBeVisible();
    await this.newJobInput.check();
    await this.saveAndContinueButton();
  }

  // enter start date for new job
  async enterNewJobStartDates() {
    let [year, month, day] = dateUtilComponent.addWeekdays(new Date(), -10).toISOString().split('T')[0].split('-');
    await this.page.waitForLoadState('load');
    await expect(this.startOfNewJobHeading).toBeVisible();
    await expect(this.startOfNewJobQuestion).toBeVisible();
    await expect(this.startOfNewJobInputGroup).toBeVisible();

    await this.startOfNewJobInputGroup.getByLabel('Day').fill(day);
    await this.startOfNewJobInputGroup.getByLabel('Month').fill(month);
    await this.startOfNewJobInputGroup.getByLabel('Year').fill(year);

    await this.saveAndContinueButton();
  }

  // enter new job pay
  async enterNewJobPay() {
    await this.page.waitForLoadState('load');
    await expect(this.newJobPayHeading).toBeVisible();
    await expect(this.newJobPayBeforeTaxInput).toBeVisible();
    await this.newJobPayBeforeTaxInput.fill('50000');
    await this.newJobAnnualPayRadioOption.check();
    await this.saveAndContinueButton();
  }

  //verify user is on respondent-name page and then enters a respondent name
  async enterRespondentName(name: string = CaseDetailsValues.respondentName) {
    await this.page.waitForLoadState('load');
    await expect(this.respondentNameHeading).toBeVisible();

    await this.respondentNameInput.fill(name);
    await this.saveAndContinueButton();
  }

  //enters address for respondent
  async enterRespondentAddress(workPostcode: string, selectedWorkAddress: string) {
    await this.page.waitForLoadState('load');
    await expect(this.respondentPostCodeInput).toBeVisible();
    await this.respondentPostCodeInput.fill(workPostcode);
    await this.saveAndContinueButton();

    await expect(this.selectAnAddressHeading).toBeVisible();
    await expect(this.respondentAddressSelect).toBeVisible();

    await this.respondentAddressSelect.selectOption(selectedWorkAddress);
    await this.saveAndContinueButton();

    await expect(this.page.locator('#main-content')).toContainText(
      'This should be the same respondent address given to Acas.',
    );
    await this.saveAndContinueButton();
  }

  //selects no option for ACAS cerificate question on /acas-cer-num page
  async selectNoToAcas() {
    await this.page.waitForLoadState('load');
    await expect(this.acasCertificateHeading).toBeVisible();

    await this.acasCertificateNoOption.check();
    await this.saveAndContinueButton();

    await expect(this.reasonForNoAcasCertificateHeading).toBeVisible();
    await expect(this.reasonForNoAcasQuestion).toBeVisible();
    await this.reasonForNoAcasCertificateFirstOption.check();
    await this.saveAndContinueButton();
  }

  async selectYesToAcas() {
    await this.page.waitForLoadState('load');
    await expect(this.acasCertificateHeading).toBeVisible();

    await this.acasCertificateYesOption.check();
    await this.acasCertificateInput.fill('R444444/89/74');
    await this.delay(2000);
    await this.saveAndContinueButton();
  }

  async addMultipleAcasCertificate() {
    await this.page.waitForLoadState('load');
    await expect(this.checkRespondentDetailsHeading).toBeVisible();
    await this.addRespondentButton();
  }

  //selects yes to working at respondent address
  async selectYesToWorkingAtRespondentAddress() {
    await this.page.waitForLoadState('load');
    await expect(this.didYouWorkAtHeading).toBeVisible();
    await this.didYouWorkAtYesOption.check();
    await this.saveAndContinueButton();
  }

  async addSecondRespondentDetails(workPostcode: string, selectedWorkAddress: string) {
    await this.enterRespondentName(CaseDetailsValues.respondentName2);
    await this.enterRespondentAddress(workPostcode, selectedWorkAddress);
    await this.selectYesToAcas();
  }

  //check respondent details page
  async assertCheckRespondentDetailsPage() {
    await expect(this.checkRespondentDetailsHeading).toBeVisible();
    await this.saveAndContinueButton();
  }

  //still working for organisation/person scenario
  async processStillWorkingJourney(workPostcode: string, selectedWorkAddress: string, firstLineOfAddress: string) {
    await this.clickEmploymentStatusLink();
    await this.selectWorkedForOrganisation('Yes');
    await this.stillWorkingForOrganisation();
    await this.enterEmploymentJobTitle();
    await this.enterEmploymentStartDate();
    await this.selectNoticePeriodWhilstStillWorkingFlow();
    await this.selectNoticeType();
    await this.enterNoticePeriodLength();
    await this.enterAverageWeeklyHours();
    await this.enterPay();
    await this.enterPensionContribution();
    await this.enterEmployeeBenefits();
    await this.enterRespondentName();
    await this.enterRespondentAddress(workPostcode, selectedWorkAddress);
    await this.selectYesToWorkingAtRespondentAddress();
    //this.selectNoToAcas();
    await this.selectYesToAcas();
    await this.assertCheckRespondentDetailsPage();
    await this.confirmHaveYouCompletedThisSection();
  }

  //working notice period for organisation/person scenario
  async processWorkingNoticePeriodJourney(
    workPostcode: string,
    selectedWorkAddress: string,
    firstLineOfAddress: string,
  ) {
    await this.clickEmploymentStatusLink();
    await this.selectWorkedForOrganisation('Yes');
    await this.workingNoticePeriodForOrganisation();
    await this.enterEmploymentJobTitle();
    await this.enterEmploymentStartDate();
    await this.noticePeriodEndDate();
    await this.selectNoticeType();
    await this.enterNoticePeriodLength();
    await this.enterAverageWeeklyHours();
    await this.enterPay();
    await this.enterPensionContribution();
    await this.enterEmployeeBenefits();
    await this.enterRespondentName();
    await this.enterRespondentAddress(workPostcode, selectedWorkAddress);
    await this.selectYesToWorkingAtRespondentAddress();
    await this.selectYesToAcas();
    await this.assertCheckRespondentDetailsPage();
    await this.confirmHaveYouCompletedThisSection();
  }

  // //No longer working for organisation/person scenario
  async processNoLongerWorkingForOrgJourney(
    workPostcode: string,
    selectedWorkAddress: string,
    firstLineOfAddress: string,
  ) {
    await this.clickEmploymentStatusLink();
    await this.selectWorkedForOrganisation('Yes');
    await this.noLongerWorkingForOrganisation();
    await this.enterEmploymentJobTitle();
    await this.enterEmploymentStartDate();
    await this.enterEmploymentEndDate();
    await this.selectYesNoticePeriodNoLongerWorking();
    await this.selectNoticeTypeNoLongerWorking();
    await this.enterNoticePeriodLengthNoLongerWorking();
    await this.enterAverageWeeklyHours(true);
    await this.enterPay();
    await this.enterPensionContribution();
    await this.enterEmployeeBenefitsForNoLongerWorking();
    await this.newJobDetails();
    await this.enterNewJobStartDates();
    await this.enterNewJobPay();
    await this.enterRespondentName();
    await this.enterRespondentAddress(workPostcode, selectedWorkAddress);
    await this.selectYesToWorkingAtRespondentAddress();
    await this.selectNoToAcas();
    await this.assertCheckRespondentDetailsPage();
    await this.confirmHaveYouCompletedThisSection();
  }

  // //Did not work for organisation scenario
  async processDidNotWorkForOrganisationMakingClaimAgainst(workPostcode: string, selectedWorkAddress: string) {
    await this.clickEmploymentStatusLink();
    await this.selectWorkedForOrganisation('No');
    await this.enterRespondentName();
    await this.enterRespondentAddress(workPostcode, selectedWorkAddress);
    await this.selectNoToAcas();
    await this.assertCheckRespondentDetailsPage();
    await this.confirmHaveYouCompletedThisSection();
  }

  async multipleAcasCertificateAndMultipleRespondents(workPostcode: string, selectedWorkAddress: string, firstLineOfAddress: string) {
    await this.clickEmploymentStatusLink();
    await this.selectWorkedForOrganisation('Yes');
    await this.workingNoticePeriodForOrganisation();
    await this.enterEmploymentJobTitle();
    await this.enterEmploymentStartDate();
    await this.noticePeriodEndDate();
    await this.selectNoticeType();
    await this.enterNoticePeriodLength();
    await this.enterAverageWeeklyHours();
    await this.enterPay();
    await this.enterPensionContribution();
    await this.enterEmployeeBenefits();
    await this.enterRespondentName();
    await this.enterRespondentAddress(workPostcode, selectedWorkAddress);
    await this.selectYesToWorkingAtRespondentAddress();
    await this.selectYesToAcas();
    await this.addMultipleAcasCertificate();
    await this.addSecondRespondentDetails(workPostcode, selectedWorkAddress);
    await this.assertCheckRespondentDetailsPage();
    await this.confirmHaveYouCompletedThisSection();
  }
}
