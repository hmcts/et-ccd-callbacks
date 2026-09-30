import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";

export default class JurisdictionPage extends BasePage {
  private readonly jurisdictionDropdown: Locator;
  private readonly judgmentOutcome: Locator;

  constructor(page: Page) {
    super(page);
    this.jurisdictionDropdown = page.locator('#jurCodesCollection_1_juridictionCodesList');
    this.judgmentOutcome = page.locator('#jurCodesCollection_1_judgmentOutcome');
  }

  async addJurisdiction(jurisdictionCode:string, judgmentOutcome:string, position: number = 1): Promise<void> {
    await this.page.getByRole('button', { name: 'Add new' }).nth(1).click();
    const judgementDropdownList = this.page.locator(`#jurCodesCollection_${position}_juridictionCodesList`);
    const judgementOutcomeDropdown = this.page.locator(`#jurCodesCollection_${position}_judgmentOutcome`);

    await expect(judgementDropdownList).toBeVisible();
    await judgementDropdownList.selectOption({label: jurisdictionCode});

    await expect(judgementOutcomeDropdown).toBeVisible();
    await judgementOutcomeDropdown.selectOption({label: judgmentOutcome});
  }

  async addJurisdictionCodeAndSubmit(jurisdictionCode:string, judgmentOutcome:string, position: number = 1) {
    await this.addJurisdiction(jurisdictionCode, judgmentOutcome, position);
    await this.clickSubmitButton();
    await expect(this.page.getByRole('tab', { name: 'Case Details' }).locator('div')).toContainText('Case Details');
  }

  async closeJurisdictionCode() {
    await this.page.locator('#jurCodesCollection_0_judgmentOutcome').selectOption({ label: 'Input in error' });
    await this.clickSubmitButton();
    await expect(this.page.getByRole('tab', { name: 'Case Details' }).locator('div')).toContainText('Case Details');
  }
}
