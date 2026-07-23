import { expect } from '@playwright/test';
import { BasePage } from './basePage';

export default class CloseCasePage extends BasePage {
  private readonly clerkResponsibleDropdown;
  private readonly caseDetailsTab;

  constructor(page: import('@playwright/test').Page) {
    super(page);
    this.clerkResponsibleDropdown = page.locator('#clerkResponsible');
    this.caseDetailsTab = page.getByRole('tab', { name: 'Case Details' }).locator('div');
  }

  async closeCase() {
    await this.clerkResponsibleDropdown.selectOption( '1: A Clerk' );
    await this.clickContinue();
    await expect(this.page.getByText('Check your answers')).toBeVisible();
    await this.clickSubmitButton();
    await expect(this.caseDetailsTab).toContainText('Case Details');
  }
}
