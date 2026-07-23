import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";


export default class CaseTransferPage extends BasePage {
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

  async checkYourAnswer(caseNumber: string) {
    await expect(this.page.locator('ccd-case-edit-submit')).toContainText(
      'Select the office you want to transfer the case to',
    );
    await expect(this.page.locator('ccd-case-edit-submit')).toContainText('Reason for Case Transfer');
    await this.page.getByRole('button', { name: 'Transfer Case' }).click();

    await expect(this.page.locator('#case-viewer-field-read--positionType')).toContainText(
      'Case transferred - other country',
    );
    await expect(this.page.locator('h4')).toContainText('Case Status: Transferred');
    await this.delay(10000);
    await this.page.reload();
    try {
      // Check if the element is visible
      const isVisible = await this.page.getByRole('link', { name: '/2025' }).isVisible();

      if (!isVisible) {
        // Click the button if the element is not visible
        await this.page.reload();
      }
    } catch (error) {
      console.error('Scotland Case Transfer link not visible', error);
    }
    await expect(this.page.getByLabel('Case Details').getByRole('paragraph')).toContainText(
      'Case Transfer: Transferred to Glasgow ',
    );
/*    await expect(this.page.getByRole('link', { name: '/2026' })).toBeVisible();
    await this.page.getByRole('link', { name: '/2026' }).click();
    const page1Promise = this.page.waitForEvent('popup');
    const page1 = await page1Promise;
    await page1.reload();*/
    //await expect(this.page.locator('#case-viewer-field-read--managingOffice')).toContainText('Glasgow');
    const newSubRef = await this.page.locator('#case-viewer-field-read--feeGroupReference').textContent();
    return newSubRef? newSubRef.trim() : '';
  }
}
