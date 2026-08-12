import { BasePage } from './basePage.ts';
import { expect, Locator, Page } from '@playwright/test';
import { CaseTypeLocation } from '../config/case-data.ts';
import { config } from '../config/config.dynamic.ts';

export class ManageCaseDashboardPage extends BasePage {
  private readonly url: string;
  private readonly nocLink: Locator;
  private readonly caseListLink: Locator;
  private readonly myWorkLink: Locator;

  public constructor(page: Page) {
    super(page);
    this.url = config.manageCaseBaseUrl;
    this.nocLink = page.getByRole('link', { name: 'Notice of change' });
    this.caseListLink = page.getByText('Case list');
    this.myWorkLink = page.getByRole('link', { name: 'My work' });
  }

  async navigateToCaseDetails(
    caseId: string,
    caseTypeLocation: CaseTypeLocation,
    canView: boolean = true,
  ): Promise<string> {
    await this.page.waitForLoadState('load');
    const url = `${this.url}/case-details/EMPLOYMENT/${caseTypeLocation.toString()}/${caseId}#Case%20Details`;
    await this.page.goto(url);
    await this.page.waitForLoadState('load');
    if (canView) {
      await expect(this.page.getByText(String(caseId))).toBeVisible();
      const caseNumberText = await this.page.locator('h1', { hasText: 'Case Number:' }).textContent();
      const match = caseNumberText?.match(/Case Number:(\d+\/\d+)/);
      const caseNumber = match ? match[1] : '';
      console.log('Navigated to case number: ' + caseNumber);
      return caseNumber;
    } else {
      await expect(this.page.getByText(String(caseId))).not.toBeVisible();
      return "";
    }
  }

  async navigateToNoticeOfChange() {
    await this.page.waitForLoadState('load');
    await this.nocLink.click();
    await this.page.waitForLoadState('load');
   await expect(this.page.getByRole('heading', { name: 'Notice of change' })).toBeVisible();
  }

  async navigateToCaseListPage() {
    await this.page.waitForLoadState('load');
    await this.caseListLink.click();
    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name: 'Case list' })).toBeVisible();
  }

  async navigateToMyWork() {
    await this.page.waitForLoadState('load');
    await this.myWorkLink.click();
    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name: 'My work' })).toBeVisible();
  }

  async visit() {
    await this.page.goto(`${this.url}`);
  }
}
