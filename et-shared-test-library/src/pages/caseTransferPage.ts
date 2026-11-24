import { BasePage } from "./basePage.js";
import { expect, Locator, Page } from "@playwright/test";

export class CaseTransferPage extends BasePage {
  private readonly caseTransferReason: Locator;

  constructor(page: Page) {
    super(page);
    this.caseTransferReason = page.locator('#reasonForCT');
  }

  async progressCaseTransfer() {
    await expect(this.caseTransferReason).toBeVisible();
    await this.caseTransferReason.fill('Transfer case to Scotland RET');
    await this.clickContinue();
  }

  async clickTransferNow() {
    await this.page.waitForLoadState('load');
    await this.page.getByRole('button', { name: 'Transfer Case' }).click();
  }

  async getNewDigitalCaseReferenceNumber() {
    const newSubRef = await this.page.locator('#case-viewer-field-read--feeGroupReference').textContent();
    return newSubRef? newSubRef.trim() : '';
  }
}
