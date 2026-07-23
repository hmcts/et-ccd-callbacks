import { BasePage } from "./basePage";
import { expect, Locator, Page } from '@playwright/test';

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

  async updateCaseFlag(flagStatus:string){

        await expect(this.page.locator('#flag-selection-0')).toBeVisible();
        await this.page.locator('#flag-selection-0').check();
    await this.clickContinue();

    switch (flagStatus) {
      case 'Active':
        await expect(this.page.locator('#status_ACTIVE')).toBeVisible();
        await this.page.locator('#status_ACTIVE').check();
        break;

      case 'Inactive':
        await expect(this.page.locator('#status_INACTIVE')).toBeVisible();
        await this.page.locator('#status_INACTIVE').check();
        break;

      case 'Not approved':
        await expect(this.page.locator('#status_NOT_APPROVED')).toBeVisible();
        await this.page.locator('#status_NOT_APPROVED').check();
        await this.page.locator('#flagStatusReasonChange').fill('not approved flag');
        break;

      default:
        throw new Error(`Can't update flag status: ${flagStatus}`);
    }
    await this.clickContinue();
    await expect(this.page.locator('dt', { hasText: 'Status' })).toBeVisible();
    await this.clickSubmitButton();
  }
}
