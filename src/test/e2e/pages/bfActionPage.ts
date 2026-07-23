import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";


export default class BfActionPage extends BasePage {
  private readonly bfActionDropDown: Locator;
  private readonly bfDate: Locator;
  private readonly bfMonth: Locator;
  private readonly bfYear: Locator;
  private readonly bfNotes: Locator;
  private readonly caseEditPage: Locator;
  private readonly addNewButton: Locator;
  private readonly bfActionsTab: Locator;

  constructor(page: Page) {
    super(page);
    this.bfActionDropDown = page.locator('#bfActions_0_cwActions');
    this.bfDate = page.locator('#bfDate-day');
    this.bfMonth = page.locator('#bfDate-month');
    this.bfYear = page.locator('#bfDate-year');
    this.bfNotes = page.locator('#bfActions_0_notes');
    this.caseEditPage = page.locator('ccd-case-edit-page');
    this.addNewButton = page.getByRole('button', { name: 'Add new' });
    this.bfActionsTab = page.getByRole('tab', { name: 'BF Actions' });
  }

  async addBfAction() {
    await expect(this.caseEditPage).toContainText('B/F Action');
    await this.addNewButton.click();
    await this.bfActionDropDown.selectOption({ label: 'Application of letter to ACAS/RPO' });
    await this.bfDate.fill('11');
    await this.bfMonth.fill('09');
    await this.bfYear.fill('2024');
    await this.bfNotes.fill('bf action test');
    await this.clickSubmitButton();
    await expect(this.bfActionsTab).toBeVisible();
    console.log('...BF action event completed successfully...')
  }
}
