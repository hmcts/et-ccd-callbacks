import { BasePage } from './basePage';
import { expect, Locator, Page } from '@playwright/test';

export default class UploadDocumentPage extends BasePage {
  private readonly addNewButtonBottom: Locator;
  private readonly createDcfLink: Locator;
  private readonly createDcfRadio: Locator;

  constructor(page: Page) {
    super(page);
    this.addNewButtonBottom = page.locator('//button[@class="button write-collection-add-item__bottom ng-star-inserted"]');
    this.createDcfLink = page.locator('//a[contains(.,"Create, Upload or Remove DCF")]');
    this.createDcfRadio = page.locator('#uploadOrRemoveDcf-Create');
  }

  async uploadCaseManagementDocument() {
    await this.addNewButtonBottom.click();
    await this.page.locator('#documentCollection_1_topLevelDocuments').selectOption({ label: 'Case Management' });
    await this.page.locator('#documentCollection_1_caseManagementDocuments').selectOption({ label: 'Deposit Order' });
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.page.locator('#documentCollection_1_uploadedDocument'),
      'resources/test_file/welshTest.pdf',
    )
    await this.clickSubmitButton();
  }

  async createDCF() {
    await expect(this.createDcfLink).toBeVisible();
    await this.createDcfLink.click();
    await expect(this.createDcfRadio).toBeVisible();
    await this.createDcfRadio.check();
    await this.clickSubmitButton();
  }

  async uploadFile(fileName: string, docNumber: number) {
    await this.page.waitForSelector('text=Case documentation', { timeout: 10000 });
    await this.addNewUploadDocButtonClick();
    await this.page.locator(`#documentCollection_${docNumber}_topLevelDocuments`).selectOption({ label: 'Misc' });
    await expect(this.page.locator(`#documentCollection_${docNumber}_miscDocuments`)).toBeVisible();
    await this.page.locator(`#documentCollection_${docNumber}_miscDocuments`).selectOption({ label: 'Tribunal case file' });
    await this.page.waitForSelector(`#documentCollection_${docNumber}_uploadedDocument`);
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.page.locator(`#documentCollection_${docNumber}_uploadedDocument`),
      `resources/test_file/${fileName}`,
    );
    await this.clickSubmitButton();
  }

  async verifyUploadDocuments(fileName: string) {
    await expect(this.page.getByText(fileName)).toBeVisible();
  }
}
