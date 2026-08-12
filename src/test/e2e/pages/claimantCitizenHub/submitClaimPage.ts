import { BasePage } from "../basePage.ts";
import { expect, Locator, Page } from '@playwright/test';
import CitizenHubPage from './CitizenHubPage.ts';

export default class SubmitClaimPage extends CitizenHubPage{

  private readonly checkYourAnswersLink: Locator;
  private readonly equalityAndDiversityHeading: Locator;
  private readonly noEQualityAndDiversityQuestions: Locator;
  private readonly checkYourAnswersHeading: Locator;
  private readonly claimSubmittedHeading: Locator;
  private readonly submissionReferenceNumber: Locator;

  constructor(page: Page) {
    super(page);
    this.checkYourAnswersLink = this.page.locator(
      'a[href="/pcq?lng=en"], a[href="/pcq"]'
    );
    this.equalityAndDiversityHeading = this.page.getByRole('heading', { name: 'Equality and diversity questions' });
    this.noEQualityAndDiversityQuestions = this.page.locator('button[name=opt-out-button]');
    this.checkYourAnswersHeading = this.page.getByRole('heading', { name: 'Check your answers' });
    this.claimSubmittedHeading = this.page.getByRole('heading', { name: 'Your claim has been submitted' });
    this.submissionReferenceNumber = this.page.locator(
      `xpath=//dt[normalize-space()='Submission reference']/following-sibling::dd`
    );
  }

  async submitClaim() {
    await this.clickCheckYourAnswersLink();
    await this.noPcqQuestions();
    await this.clickSubmitOnCheckYourAnswers();
    return this.verifyClaimSubmitted();
  }

  //user clicks check your answers link
  async clickCheckYourAnswersLink() {
    await this.page.waitForLoadState('load');
    await expect(this.checkYourAnswersLink).toBeVisible();
    await this.checkYourAnswersLink.click();
  }

  async noPcqQuestions() {
    await this.page.waitForLoadState('load');
    await expect(this.equalityAndDiversityHeading).toBeVisible();
    await this.noEQualityAndDiversityQuestions.click();
  }

  async clickSubmitOnCheckYourAnswers() {
    await this.page.waitForLoadState('load');
    await expect(this.checkYourAnswersHeading).toBeVisible();
    await this.clickSubmitButton();
  }

  async verifyClaimSubmitted() {
    await this.page.waitForLoadState('load');
    await expect(this.claimSubmittedHeading).toBeVisible();
    await expect(this.submissionReferenceNumber).toBeVisible();
    const submissionRef = (await this.submissionReferenceNumber.innerText()).trim();
    console.log('you have successfully submitted claim...' +submissionRef);
    return submissionRef.toString();
  }

  async returnToCaseOverviewAndReturnCaseNumber() {
    await this.page.waitForLoadState('load');
    await this.closeAndReturnToCaseDetailsCui();

    const caseNumber = await this.caseNumberText.textContent();
    const match = caseNumber?.match(/Case number (\d+\/\d+)/);
    const caseNum = match ? match[1] : '';
    console.log('Returned to case overview page for case number: ' + caseNum);
    return caseNum;
  }
}
