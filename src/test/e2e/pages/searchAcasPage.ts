import { expect, Locator, Page } from "@playwright/test";
import { BasePage } from "./basePage";
import acasCertData from '../resources/payload/acas-content.json';

export default class SearchAcasPage extends BasePage {
  private readonly acasCertificateTextBox: Locator;
  private readonly confirmationEle: Locator;

  constructor(page: Page) {
    super(page);
    this.acasCertificateTextBox = page.locator('#acasCertificate');
    this.confirmationEle = page.locator('#confirmation-body');
  }

  async findAcasCertificateSuccessfully(acasCertNum: string) {
    await expect(this.page.getByText('Please enter an ACAS Certificate number')).toBeVisible();
    await this.acasCertificateTextBox.fill(acasCertNum);
    await this.clickSubmitButton();

    await expect(this.confirmationEle).toBeVisible({ timeout: 10000 });
    await expect(this.page.getByText(`Please download the ACAS Certificate from : ${acasCertData.docName}`)).toBeVisible();
  }
}
