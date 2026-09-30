import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";
import { AxeUtils } from '@hmcts/playwright-common';

export default class Et1VettingPages extends BasePage {
  private readonly beforeYouStartText: Locator;
  private readonly caseEditPage: Locator;
  private readonly minimumRequiredInfoText: Locator;
  private readonly caseEditForm: Locator;
  private readonly acasCertYes: Locator;
  private readonly substantiveDefectsList: Locator;
  private readonly caseEditFormNotes: Locator;
  private readonly jurisdictionCodes: Locator;
  private readonly trackAllocation: Locator;
  private readonly tribunalLocation: Locator;
  private readonly suggestHearingVenue: Locator;
  private readonly furtherQuestions: Locator;
  private readonly referralToJudgeOrLOList: Locator;
  private readonly referralToREJOrVPList: Locator;
  private readonly otherFactors: Locator;
  private readonly finalNotes: Locator;
  private readonly checkYourAnswers: Locator;
  private readonly confirmationBody: Locator;

  constructor(page: Page) {
    super(page);
    this.beforeYouStartText = page.getByText('ET1 case vetting');
    this.caseEditPage = page.locator('ccd-case-edit-page');
    this.minimumRequiredInfoText = page.getByText('Minimum required information');
    this.caseEditForm = page.locator('#caseEditForm');
    this.acasCertYes = page.locator('#et1VettingAcasCertIsYesOrNo1_Yes');
    this.substantiveDefectsList = page.locator('#substantiveDefectsList');
    this.caseEditFormNotes = page.locator('#caseEditForm');
    this.jurisdictionCodes = page.locator('#areTheseCodesCorrect');
    this.trackAllocation = page.locator('tbody');
    this.tribunalLocation = page.locator('h3');
    this.suggestHearingVenue = page.locator('#et1SuggestHearingVenue');
    this.furtherQuestions = page.locator('ccd-case-edit-page');
    this.referralToJudgeOrLOList = page.locator('#referralToJudgeOrLOList');
    this.referralToREJOrVPList = page.locator('#referralToREJOrVPList');
    this.otherFactors = page.locator('ccd-case-edit-page');
    this.finalNotes = page.locator('ccd-case-edit-page');
    this.checkYourAnswers = page.locator('ccd-case-edit-submit');
    this.confirmationBody = page.locator('#confirmation-body');
  }

  async processET1CaseVettingPages(accessibilityEnabled?: boolean, axeUtils?: AxeUtils) {
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.processBeforeYourStartPage();
    await this.processMinimumRequiredInformationPage();
    await this.processACASCertificatePage();
    await this.processPossibleSubstantiveDefectsPage();
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.processJurisdictionCodePage();
    await this.processTrackAllocationPage();
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.processTribunalLocationPage();
    await this.processListingDetailsPage();
    await this.processFurtherQuestionsPage();
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.processPossibleReferralToACaseOfficerPage();
    await this.processPossibleReferralToARegionalEmploymentJudgeOrPresidentPage();
    await this.processOtherFactorsPage();
    await this.processFinalNotesPage();
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.processCheckYourAnswersPage();
    await this.processET1CaseVettingPage();
  }

  async processBeforeYourStartPage() {
    await expect(this.beforeYouStartText).toBeVisible();
    await expect(this.caseEditPage).toContainText('Before you start');
    await this.clickContinue();
  }

  async processMinimumRequiredInformationPage() {
    await expect(this.caseEditPage).toContainText('Minimum required information');
    await this.caseEditForm.getByRole('group').getByText('Yes').click();
    await this.clickContinue();
  }

  async processACASCertificatePage() {
    await this.acasCertYes.click();
    await this.clickContinue();
  }

  async processPossibleSubstantiveDefectsPage() {
    await expect(this.substantiveDefectsList).toContainText('Possible substantive defects (Optional)');
    await expect(this.caseEditFormNotes).toContainText('General notes (Optional)');
    await this.clickContinue();
  }

  async processJurisdictionCodePage() {
    await expect(this.jurisdictionCodes).toContainText('Are these codes correct?');
    await this.page.getByLabel('Yes').check();
    await this.clickContinue();
  }

  async processTrackAllocationPage() {
    await expect(this.trackAllocation).toContainText('Track allocation');
    await this.page.getByLabel('Yes').check();
    await this.clickContinue();
  }

  async processTribunalLocationPage() {
    await expect(this.tribunalLocation).toContainText('Tribunal location');
    await this.page.getByLabel('Yes').check();
    await this.clickContinue();
  }

  async processListingDetailsPage() {
    await expect(this.suggestHearingVenue).toContainText('Do you want to suggest a hearing venue?');
    await this.page.getByLabel('No', { exact: true }).check();
    await this.clickContinue();
  }

  async processFurtherQuestionsPage() {
    await expect(this.furtherQuestions).toContainText('Further questions');
    await this.page.getByRole('group', { name: 'Is the respondent a government agency or a major employer?' }).getByLabel('No').check();
    await this.page.getByRole('group', { name: 'Are reasonable adjustments required?' }).getByLabel('No').check();
    await this.page.getByRole('group', { name: 'Can the claimant attend a video hearing?' }).getByLabel('Yes').check();
    await this.clickContinue();
  }

  async processPossibleReferralToACaseOfficerPage() {
    await expect(this.referralToJudgeOrLOList).toContainText('Possible referral to a judge or legal officer (Optional)');
    await this.clickContinue();
  }

  async processPossibleReferralToARegionalEmploymentJudgeOrPresidentPage() {
    await expect(this.referralToREJOrVPList).toContainText('Possible referral to Regional Employment Judge or Vice-President (Optional)');
    await this.clickContinue();
  }

  async processOtherFactorsPage() {
    await expect(this.otherFactors).toContainText('Other factors');
    await this.clickContinue();
  }

  async processFinalNotesPage() {
    await expect(this.finalNotes).toContainText('Final notes');
    await this.clickContinue();
  }

  async processCheckYourAnswersPage() {
    await expect(this.checkYourAnswers).toContainText('Check your answers');
    await expect(this.checkYourAnswers).toContainText('Check the information below carefully.');
    await this.clickSubmitButton();
  }

  async processET1CaseVettingPage() {
    await expect(this.confirmationBody).toContainText('You must accept or reject the case or refer the case.');
    await this.clickCloseAndReturn();
  }
}
