import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './basePage';

export default class ReinstateCasePage extends BasePage {
  private readonly positionType: Locator;

  constructor(page: Page) {
    super(page);
    this.positionType = page.locator('#positionType');
  }

  async reinstateCase() {
    await expect(this.page.locator('h1').filter({ hasText: 'Reinstate Case'})).toBeVisible();
    await this.positionType.selectOption({ label: 'Awaiting appeal judgment' });
    await this.clickSubmitButton();
    await this.delay(7000);
  }
}
