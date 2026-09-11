import { BasePage } from '../basePage.ts';
import { Page, Locator, expect } from '@playwright/test';

export default class ResponseLandingPage extends BasePage {
  private readonly mainContent: Locator;
  private readonly responseFormLink: Locator;

  constructor(page: Page) {
    super(page);
    this.mainContent = page.locator('#main-content');
    this.responseFormLink = page.getByRole('link', { name: 'Your response form (ET3)' });
  }

  async startEt3() {
    await this.mainContent.waitFor();
    await expect(this.mainContent).toContainText('Case overview');
    await this.responseFormLink.click();
    await this.clickStartNow();
  }
}
