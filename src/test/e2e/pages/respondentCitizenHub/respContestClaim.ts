import { BasePage } from '../basePage.ts';
import { Page, Locator, expect } from '@playwright/test';

export default class RespContestClaim extends BasePage {
  private readonly contestClaimReason: Locator;
  private readonly clickContestClaimLink: Locator;
  private readonly claimSummaryFile: Locator;

  constructor(page: Page) {
    super(page);
    this.contestClaimReason = this.page.locator("#et3ResponseContestClaimDetails");
    this.clickContestClaimLink = this.page.locator('[href="/respondent-contest-claim"]');
    this.claimSummaryFile = this.page.locator('#claimSummaryFile');
  }

  async et3Section3() {
    await this.contestTheClaim();
    // RET-5953
    await this.employerContractClaim();
  }

  async contestTheClaim() {
    await expect(this.page.locator('h1')).toContainText('Your response form (ET3)');
    await this.clickContestClaimLink.click();
    await this.page.getByLabel('Yes').check();
    await this.saveAndContinueButton();
    await this.contestClaimReason.fill('Test Contest Claim');
    await this.saveAndContinueButton();
    await this.page.getByLabel('Yes, I’ve completed this').check();
    await this.saveAndContinueButton();
  }

  async employerContractClaim() {
    await expect(this.page.locator('h1')).toContainText('Employer’s Contract Claim');
    await this.page.getByLabel('Yes').check();
    await this.saveAndContinueButton();
    await this.page.locator('#et3ResponseEmployerClaimDetails').fill('Test ECC Text Box');
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.claimSummaryFile,
      'src/test/e2e/resources/test_file/test.txt'
    );
    await this.saveAndContinueButton();
    await this.page.getByText('Yes, I’ve completed this').click();
    await this.saveAndContinueButton();
  }
}
