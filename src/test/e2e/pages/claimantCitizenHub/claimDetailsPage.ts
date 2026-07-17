import { expect, Locator, Page } from '@playwright/test';
import CitizenHubPage from './CitizenHubPage.ts';

export default class ClaimDetailsPage extends CitizenHubPage{

  private readonly tellUsAboutYourClaimLink: Locator;
  private readonly whatTypeOfClaimHeading: Locator;
  private readonly claimTypeDiscriminationOption: Locator;
  private readonly claimTypeWhistleblowingOption: Locator;
  private readonly yourDiscriminationClaimHeading: Locator;
  private readonly yourDiscriminationAgeOption: Locator;
  private readonly yourDiscriminationDisabilityOption: Locator;
  private readonly describeYourClaimHeading: Locator;
  private readonly claimSummaryTextArea: Locator;
  private readonly whatDoYouWantHeading: Locator;
  private readonly compensationOnlyOption: Locator;
  private readonly tribunalRecommendationOption: Locator;
  private readonly oldJobOption: Locator;
  private readonly yourCompensationHeading: Locator;
  private readonly compensationOutcomeTextArea: Locator;
  private readonly compensationAmountField: Locator;
  private readonly tribunalRecommendationHeading: Locator;
  private readonly tribunalRecommendationRequestTextArea: Locator;
  private readonly whistleBlowingClaimsHeading: Locator;
  private readonly whistleBlowingClaimYesOption: Locator;
  private readonly whistleBlowingEntityNameField: Locator;
  private readonly linkedCasesHeading: Locator;
  private readonly linkedCasesNoOption: Locator;

  constructor(page: Page) {
    super(page);
    this.tellUsAboutYourClaimLink = this.page.locator(`a[href="/type-of-claim?lng=en"]`);
    this.whatTypeOfClaimHeading = this.page.getByRole('heading', { name: 'What type of claim are you making?' });
    this.claimTypeDiscriminationOption = this.page.locator('#discrimination');
    this.claimTypeWhistleblowingOption = this.page.locator('#whistleBlowing');
    this.yourDiscriminationClaimHeading = this.page.getByRole('heading', { name: 'Your discrimination claim' });
    this.yourDiscriminationAgeOption = this.page.locator('#age');
    this.yourDiscriminationDisabilityOption = this.page.locator('#disability');
    this.describeYourClaimHeading = this.page.getByRole('heading', { name: 'Describe your claim' });
    this.claimSummaryTextArea = this.page.locator(`#claim-summary-text`);
    this.whatDoYouWantHeading = this.page.getByRole('heading', { name: 'What do you want from your claim' });
    this.compensationOnlyOption = this.page.locator('#compensationOnly');
    this.tribunalRecommendationOption = this.page.locator('#tribunalRecommendation');
    this.oldJobOption = this.page.locator('#oldJob');
    this.yourCompensationHeading = this.page.getByRole('heading', { name: 'Your compensation (optional)' });
    this.compensationOutcomeTextArea = this.page.locator('#compensationOutcome');
    this.compensationAmountField = this.page.locator('#compensation-amount');
    this.tribunalRecommendationHeading = this.page.getByRole('heading', { name: 'Your tribunal recommendation' });
    this.tribunalRecommendationRequestTextArea = this.page.locator('#tribunalRecommendationRequest');
    this.whistleBlowingClaimsHeading = this.page.getByRole('heading', { name: 'Whistleblowing claims (optional)' });
    this.whistleBlowingClaimYesOption = this.page.locator('#whistleblowing-claim');
    this.whistleBlowingEntityNameField = this.page.locator('#whistleblowing-entity-name');
    this.linkedCasesHeading = this.page.getByRole('heading', { name: 'Linked cases' });
    this.linkedCasesNoOption = this.page.locator('#linkedCases');
  }

  async processClaimDetails() {
    await this.clickClaimDetailsLink();
    await this.selectWhatTypeOfClaim();
    await this.selectClaimTypeDiscrimination();
    await this.enterDescribeWhatHappened();
    await this.selectTellUsWhatYouWant();
    await this.enterCompensationDetails();
    await this.tribunalRecommendation();
    await this.whistleBlowingClaims();
    await this.confirmHaveYouCompletedThisSection();
  }

  //clicks on the claim details link
  async clickClaimDetailsLink() {
    await this.page.waitForLoadState('load');
    await expect(this.tellUsAboutYourClaimLink).toBeVisible();
    await this.tellUsAboutYourClaimLink.click();
  }

  async selectWhatTypeOfClaim(){
    await this.page.waitForLoadState('load');
    await expect(this.whatTypeOfClaimHeading).toBeVisible();
    await this.claimTypeDiscriminationOption.check();
    await this.claimTypeWhistleblowingOption.check();
    await this.saveAndContinueButton();
  }

  async selectClaimTypeDiscrimination() {
    await this.page.waitForLoadState('load');
    await expect(this.yourDiscriminationClaimHeading).toBeVisible();
    await this.yourDiscriminationAgeOption.check();
    await this.yourDiscriminationDisabilityOption.check();
    await this.saveAndContinueButton();
  }

  async enterDescribeWhatHappened() {
    await this.page.waitForLoadState('load');
    await expect(this.describeYourClaimHeading).toBeVisible();
    await this.claimSummaryTextArea.fill('Discrimination, Dismissal and Pay Cut.');
    await this.saveAndContinueButton();
  }

  async selectTellUsWhatYouWant() {
    await this.page.waitForLoadState('load');
    await expect(this.whatDoYouWantHeading).toBeVisible();
    await this.compensationOnlyOption.check();
    await this.tribunalRecommendationOption.check();
    await this.oldJobOption.check();
    await this.saveAndContinueButton();
  }

  async enterCompensationDetails() {
    await this.page.waitForLoadState('load');
    await expect(this.yourCompensationHeading).toBeVisible();
    await this.compensationOutcomeTextArea.fill('Seeking months wage and job back');
    await this.compensationAmountField.fill('2000');
    await this.saveAndContinueButton();
  }

  async tribunalRecommendation() {
    await this.page.waitForLoadState('load');
    await expect(this.tribunalRecommendationHeading).toBeVisible();
    await this.tribunalRecommendationRequestTextArea.fill('I would like the tribunal to recommend that I get my job back and receive 6 months wage as compensation');
    await this.saveAndContinueButton();
  }

  async whistleBlowingClaims() {
    await this.page.waitForLoadState('load');
    await expect(this.whistleBlowingClaimsHeading).toBeVisible();
    await this.whistleBlowingClaimYesOption.check();
    await this.whistleBlowingEntityNameField.fill('Rupert Regulator');
    await this.saveAndContinueButton();

    await this.page.waitForLoadState('load');
    await expect(this.linkedCasesHeading).toBeVisible();
    await this.linkedCasesNoOption.check();
    await this.saveAndContinueButton();
  }
}
