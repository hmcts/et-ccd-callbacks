import { BasePage } from "./basePage";
import { expect, Locator, Page } from '@playwright/test';

export default class Et3NotificationPage extends BasePage {
  private readonly documentUpload: Locator;
  private readonly typeOfDocument: Locator;

  constructor(page: Page) {
    super(page);
    this.documentUpload = page.locator('#et3NotificationDocCollection_0_uploadedDocument');
    this.typeOfDocument = page.locator('#et3NotificationDocCollection_0_typeOfDocument');
  }

  async sendEt3Notification(shouldContinue:boolean = true) {
    await this.addNewButtonClick();
    await this.typeOfDocument.waitFor();
    await this.typeOfDocument.selectOption({ label: '2.11 Response accepted' });
    const file = `resources/test_file/welshTest.pdf`;
    await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.documentUpload, await this.commonActionsHelper.createAliasPDFPayload(file, 'ET3Form.pdf'));
    await this.page.waitForTimeout(5000);
    await this.clickContinue('', undefined, shouldContinue);
  }

  async processAcasPage() {
    await expect(this.page.locator('#caseEditForm')).toContainText('Email documents to Acas');
    await this.clickSubmitButton();

    await expect(this.page.locator('h5')).toContainText('We have notified the following parties:');
    await expect(this.page.locator('h3')).toContainText('Grayson Becker, Mrs Test Auto');
    await this.clickCloseAndReturn();
  }

  async verifyEt3NotificationErrorMessage() {
    await expect(this.page.getByLabel('Cannot continue because the').getByRole('listitem')).toContainText('Please upload the appropriate ET3 document for each respondent’s response status.');
  }
}
