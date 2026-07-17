import { BasePage } from "./basePage";
import { Locator, Page } from '@playwright/test';

export default class ManageCaseFlag extends BasePage {
  private readonly urgentCaseCheckbox: Locator;
  private readonly makeInactiveButton: Locator;

  constructor(page: Page) {
    super(page);
    this.urgentCaseCheckbox = page.getByLabel('Case level - Urgent case (');
    this.makeInactiveButton = page.getByRole('button', { name: 'Make inactive' });
  }

  async manageCaseFlag() {
    await this.urgentCaseCheckbox.check();
    await this.clickContinue();
    await this.makeInactiveButton.click();
    await this.clickContinue();
    await this.clickSubmitButton();
  }
}
