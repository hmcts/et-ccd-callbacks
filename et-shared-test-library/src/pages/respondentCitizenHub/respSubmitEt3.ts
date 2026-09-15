import { BasePage } from '../basePage.ts';
import { expect, Page, Locator } from '@playwright/test';

export default class RespSubmitEt3 extends BasePage {
  private readonly cya: Locator;
  private readonly submitHeader: Locator;
  private readonly closeAndReturnToCaseOverviewButton: Locator;

  constructor(page: Page) {
    super(page);
    this.cya = page.locator('[href="/check-your-answers-et3"]');
    this.submitHeader = page.locator('h1');
    this.closeAndReturnToCaseOverviewButton = page.getByRole('button', { name: 'Close and return to case overview' });
  }

  async checkYourAnswers() {
    await this.submitEt3();
  }

  async submitEt3() {
    await this.cya.click();
    await this.clickSubmitButton();
    await expect(this.submitHeader).toContainText('Your response has been submitted');
    await this.closeAndReturnToCaseOverviewButton.click();
  }
}
