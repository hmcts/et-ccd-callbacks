import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";
import { Events } from '../config/case-data.ts';


export default class Et1CreateDraftClaim extends BasePage {
  private readonly et1Postcode: Locator;
  private readonly et1Section1Link: Locator;
  private readonly claimantFirstName: Locator;
  private readonly claimantLastname: Locator;
  private readonly date: Locator;
  private readonly month: Locator;
  private readonly year: Locator;
  private readonly et1Section2Link: Locator;
  private readonly et1Section3Link: Locator;
  private readonly submitClaimLink: Locator;
  private readonly caseDetailsTab: Locator;
  private readonly representativeAttendHearing: Locator;
  private readonly hearingContactLanguage: Locator;
  private readonly claimantAttendHearing: Locator;
  private readonly claimantHearingContactLanguage: Locator;
  private readonly claimantSupportQuestion: Locator;
  private readonly representativeContactPreference: Locator;
  private readonly contactLanguageQuestion: Locator;
  private readonly representativePhoneNumber: Locator;
  private readonly representativeReferenceNumber: Locator;
  private readonly didClaimantWorkForOrg: Locator;
  private readonly claimantStillWorking: Locator;
  private readonly claimantJobTitle: Locator;
  private readonly claimantStartDate: Locator;
  private readonly claimantStartDateMonth: Locator;
  private readonly claimantStartDateYear: Locator;
  private readonly claimantStillWorkingNoticePeriodMonths: Locator;
  private readonly claimantStillWorkingNoticePeriodMonthsText: Locator;
  private readonly claimantAverageWeeklyWorkHours: Locator;

  constructor(page: Page) {
    super(page);
    this.et1Postcode = page.locator("//input[@id='et1ReppedTriageAddress_et1ReppedTriageAddress_postcodeInput']");
    this.et1Section1Link = page.locator('//a[contains(.,"ET1 Section 1 - Claimant details")]');
    this.claimantFirstName = page.locator('#claimantFirstName');
    this.claimantLastname = page.locator('#claimantLastName');
    this.date = page.locator('#claimantDateOfBirth-day');
    this.month = page.locator('#claimantDateOfBirth-month');
    this.year = page.locator('#claimantDateOfBirth-year');
    this.et1Section2Link = page.locator('//a[contains(.,"ET1 Section 2 - Employment & respondent details")]');
    this.et1Section3Link = page.locator('//a[contains(.,"ET1 Section 3 - Details of the claim")]');
    this.submitClaimLink = page.locator('//a[contains(.,"Submit claim")]');
    this.caseDetailsTab = page.locator('#mat-tab-label-0-0');
    this.representativeAttendHearing = page.locator('#representativeAttendHearing-Phone');
    this.hearingContactLanguage = page.locator('#hearingContactLanguage-English');
    this.claimantAttendHearing = page.locator('#claimantAttendHearing-Phone');
    this.claimantHearingContactLanguage = page.locator('#claimantHearingContactLanguage-English');
    this.claimantSupportQuestion = page.locator('#claimantSupportQuestion-Yes');
    this.representativeContactPreference = page.locator('#representativeContactPreference-Email');
    this.contactLanguageQuestion = page.locator('#contactLanguageQuestion-English');
    this.representativePhoneNumber = page.locator('#representativePhoneNumber');
    this.representativeReferenceNumber = page.locator('#representativeReferenceNumber');
    this.didClaimantWorkForOrg = page.locator('#didClaimantWorkForOrg-Yes');
    this.claimantStillWorking = page.locator('#claimantStillWorking-Working');
    this.claimantJobTitle = page.locator('#claimantJobTitle');
    this.claimantStartDate = page.locator('#claimantStartDate-day');
    this.claimantStartDateMonth = page.locator('#claimantStartDate-month');
    this.claimantStartDateYear = page.locator('#claimantStartDate-year');
    this.claimantStillWorkingNoticePeriodMonths = page.locator('#claimantStillWorkingNoticePeriod-Months');
    this.claimantStillWorkingNoticePeriodMonthsText = page.locator('#claimantStillWorkingNoticePeriodMonths');
    this.claimantAverageWeeklyWorkHours = page.locator('#claimantAverageWeeklyWorkHours');
  }

  async claimantWorkLocation() {
    await this.et1Postcode.click();
  }

  async et1Section1(claimantsFirstName: string, claimantLastname: string) {
    await expect(this.page.getByLabel('case viewer table').getByRole('table')).toContainText(
      'ET1 Section 1 - Claimant details',
    );
    await expect(this.page.getByLabel('case viewer table').getByRole('table')).toContainText(
      'ET1 Section 2 - Employment & respondent details',
    );
    await expect(this.page.getByLabel('case viewer table').getByRole('table')).toContainText(
      'ET1 Section 3 - Details of the claim',
    );
    const url = Events.et1SectionOneClaimantDetails.ccdCallback;
    await expect(this.page.getByText('ET1 Claim', { exact: true })).toBeVisible();
    await this.delay(2000);
    await this.et1Section1Link.click({ clickCount: 2, force: true });
    await this.waitForSpinner();
    await this.delay(2000);
    await expect(this.page.locator('ccd-case-edit-page')).toContainText('Make a claim to an employment tribunal');
    await this.clickContinue(url, 2);

    await expect(this.page.locator('ccd-case-edit-page')).toContainText('Claimant details');
    await this.claimantFirstName.fill(claimantsFirstName);
    await this.claimantLastname.fill(claimantLastname);
    await this.date.fill('1');
    await this.month.fill('11');
    await this.year.fill('2000');
    await this.clickContinue(url, 3);

    await expect(this.page.locator('#claimantSex')).toContainText("Select the claimant's sex (Optional)");

    await this.page.getByLabel('Female').check();
    await this.page.getByLabel('What is the claimant’s').click();
    await this.page.getByLabel('What is the claimant’s').fill('miss');
    await this.clickContinue(url, 4);
    //can check fields are optional

    await expect(this.page.locator('ccd-case-edit-page')).toContainText('Claimant contact address');
    await this.enterPostCode('LS121AA');
    await this.clickContinue(url, 5);

    await expect(this.page.locator('ccd-case-edit-page')).toContainText('Hearing format');
    await this.representativeAttendHearing.check();
    await this.hearingContactLanguage.check();
    await this.claimantAttendHearing.check();
    await this.claimantHearingContactLanguage.check();
    await this.clickContinue(url, 6);

    await this.claimantSupportQuestion.check();
    await this.page.locator('#claimantSupportQuestionReason').fill('disability access');
    await this.clickContinue(url, 7);

    //await expect(this.page.locator('ccd-case-edit-page')).toContainText('Your information (as the representative)');
    await this.representativeContactPreference.isVisible();
    await this.representativeContactPreference.check();
    await this.contactLanguageQuestion.check();

    //TODO-RET-5421- optional contact address
    //await this.enterPostCode('LS121AA');
    await this.representativePhoneNumber.fill('01234567890');
    await this.representativeReferenceNumber.fill('reference no: 1');
    await this.clickContinue(url + '/submit');

    //TODO Use CheckYourAnswersPage function
    // ET1 section 1- CYA page
    await this.page.locator('form').isVisible();
    await expect(this.page.locator('form')).toContainText("Claimant's First Name");
    await expect(this.page.locator('form')).toContainText("Claimant's Last Name");
    await expect(this.page.locator('form')).toContainText(
      'Which types of hearing can you, as the representative, attend?',
    );
    await expect(this.page.locator('form')).toContainText(
      'If a hearing is required, what language does the claimant want to speak at a hearing?',
    );
    await expect(this.page.locator('form')).toContainText('Are there any support requirements?');
    await expect(this.page.locator('form')).toContainText('What is your representative reference number?');
    await expect(this.page.locator('form')).toContainText('What is you contact phone number?');
    await this.saveAsDraft();

    await expect(this.page.locator('#confirmation-body')).toContainText('Your answers have been saved.');
    await this.clickCloseAndReturn();
    await this.delay(2000);
  }

  async et1Section2(respondentFirstName: string, respondentLastName: string) {
    const url = Events.et1SectionTwoRespondentDetails.ccdCallback;
    await expect(this.page.getByText('ET1 Claim', { exact: true })).toBeVisible();

    await this.et1Section2Link.click();

    //await expect(this.page.getByRole('term')).toContainText('employment status');
    await expect(this.page.locator('ccd-case-edit-page')).toContainText(
      'Section 2 - Employment and respondent details',
    );
    await this.clickContinue(url, 2);

    await expect(this.page.locator('ccd-case-edit-page')).toContainText(
      'Did the claimant work for the respondent the claim is being made against? (Optional)',
    );
    await this.didClaimantWorkForOrg.check();
    await this.clickContinue(url, 3);

    await expect(this.page.locator('ccd-case-edit-page')).toContainText(
      'Is the claimant still working for the respondent the claim is being made against? (Optional)',
    );
    await this.claimantStillWorking.check();
    await this.clickContinue(url, 4);

    await expect(this.page.locator('ccd-case-edit-page')).toContainText('Claimant employment details');
    await this.claimantJobTitle.fill('Developer');
    await this.claimantStartDate.fill('1');
    await this.claimantStartDateMonth.fill('2');
    await this.claimantStartDateYear.fill('2022');
    await this.clickContinue(url, 5);

    await expect(this.page.locator('#claimantStillWorkingNoticePeriod')).toContainText(
      'Is there a notice period? (Optional)',
    );
    await this.claimantStillWorkingNoticePeriodMonths.check();
    await this.claimantStillWorkingNoticePeriodMonthsText.fill('1');
    await this.clickContinue(url, 8);

    await this.claimantAverageWeeklyWorkHours.fill('40');
    await this.clickContinue(url, 9);

    await this.page.locator('#claimantPayBeforeTax').fill('44000');
    await this.page.locator('#claimantPayType-Weekly').check();
    await this.clickContinue(url, 10);

    await this.page.locator('#claimantPensionContribution-Yes').check();
    await this.page.locator('#claimantWeeklyPension').fill('20');
    await this.page.locator('#claimantEmployeeBenefits-Yes').check();
    await this.page.locator('#claimantBenefits').fill('child benefit');
    await this.clickContinue(url, 13);

    await this.page.locator('#respondentType-Individual').check();
    await this.page.locator('#respondentFirstName').fill(respondentFirstName);
    await this.page.locator('#respondentLastName').fill(respondentLastName);
    await this.clickContinue(url, 14);

    await this.enterPostCode('LS121AA');
    await this.clickContinue(url, 15);

    await this.page.locator('#didClaimantWorkAtSameAddress_Yes').check();
    await this.clickContinue(url, 17);

    await this.page.locator('#respondentAcasYesNo-Yes').check();
    await this.page.locator('#respondentAcasNumber').fill('R872259/22/64');
    await this.clickContinue(url, 19);

    await this.page.locator('#addAdditionalRespondent_No').check();
    await this.clickContinue(url+'/submit');
    //TODO use CheckYourAnswerPage function
    await expect(this.page.locator('form')).toContainText('Check your answers');
    await expect(this.page.locator('form')).toContainText('Did the claimant work for the respondent?');
    await expect(this.page.locator('form')).toContainText('Did the claimant work at this address?');
    await this.saveAsDraft();

    await expect(this.page.locator('#confirmation-body')).toContainText('Your answers have been saved.');
    await this.clickCloseAndReturn();
    await this.delay(2000);
  }

  async et1Section3() {
    const url = Events.et1SectionThreeClaimDetails.ccdCallback;
    await expect(this.page.getByText('ET1 Claim', { exact: true })).toBeVisible();
    await this.et1Section3Link.click();
    await this.page.waitForLoadState('load');
    await this.clickContinue(url, 2);

    await this.page.locator('#et1SectionThreeClaimDetails').fill('No supplemetary Details');
    await this.clickContinue(url, 3);

    await this.page.locator('#et1SectionThreeTypeOfClaim-discrimination').check();
    await this.page.locator('#discriminationTypesOfClaim-Age').check();
    await this.clickContinue(url, 4);

    await this.page.locator('#claimSuccessful-compensation').check();
    await this.page.locator('#compensationDetails').fill('Compensation £40,000');
    await this.clickContinue(url, 5);

    await this.page.locator('#linkedCasesYesNo-No').check();
    await this.clickContinue(url+'/submit');

    await expect(this.page.locator('form')).toContainText('Check your answers');
    await expect(this.page.locator('form')).toContainText('What type of claim is this?');
    await expect(this.page.locator('form')).toContainText('What type of discrimination are you claiming?');
    await expect(this.page.locator('form')).toContainText('What compensation is the claimant seeking?');
    await this.saveAsDraft();

    await expect(this.page.locator('#confirmation-body')).toContainText('Your answers have been saved.');
    await this.clickCloseAndReturn();
    await this.delay(2000);
  }

  async et1SubmitClaim() {
    await expect(this.page.getByText('ET1 Claim', { exact: true })).toBeVisible();
    await this.submitClaimLink.click();
    await this.delay(2000);

    await this.page.locator('#submitEt1Confirmation-Yes').check();
    await this.clickSubmitButton();

    await expect(this.page.locator('#confirmation-header')).toContainText('You have submitted the ET1 claim');
    await this.clickCloseAndReturn();

    await this.caseDetailsTab.isVisible();
    const submissionRef = await this.page.locator('//*[@id="case-viewer-field-read--feeGroupReference"]').innerText();
    return submissionRef;
  }
}
