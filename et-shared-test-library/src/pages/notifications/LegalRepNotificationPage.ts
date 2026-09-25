import { BasePage } from '../basePage.ts';
import { expect, Locator, Page } from '@playwright/test';

export default class LegalRepNotificationPage extends BasePage {

  private readonly notificationList: Locator;
  private readonly responseToTribunalText: Locator;
  private readonly anySupportingMaterialRadio: Locator;
  private readonly yesNotifyOtherPartyRadio: Locator;
  private readonly noNotifyOtherPartyRadio: Locator;

  constructor(page: Page) {
    super(page);
    // Added Respondent and Claimant locators in the same element as they are same and only differ in id which can be handled by locator
    this.notificationList = page.locator('#pseRespondentSelectOrderOrRequest, #claimantSelectNotification');
    this.responseToTribunalText = page.locator(`#claimantNotificationResponseText, #pseRespondentOrdReqResponseText`);
    this.anySupportingMaterialRadio = page.locator(
      `#claimantNotificationSupportingMaterial, #pseRespondentOrdReqHasSupportingMaterial`,
    );
    this.yesNotifyOtherPartyRadio = page.locator(
      '#claimantNotificationCopyToOtherParty-Yes, #pseRespondentOrdReqCopyToOtherParty-Yes',
    );
    this.noNotifyOtherPartyRadio = page.locator(
      '#claimantNotificationCopyToOtherParty-No, #pseRespondentOrdReqCopyToOtherParty-No',
    );
  }

  async navigateToRespondToOrderOrRequestFromTheTribunal() {
    const link = this.page.getByRole('link', { name: 'Respond to an order or request from the tribunal' });
    await expect(link).toBeVisible();
    await link.click();
  }

  async selectNotificationToRespond(notificationName: string) {
    await this.notificationList.selectOption({ label: notificationName });
  }

  async enterResponseToTribunal(responseText: string) {
    await expect(this.responseToTribunalText).toBeVisible();
    await this.responseToTribunalText.fill(responseText);
  }

  async selectAnySupportingMaterial(option: string) {
    await expect(this.anySupportingMaterialRadio).toBeVisible();
    const optionLocator = this.anySupportingMaterialRadio.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async uploadSupportDocument(filePaths: string[]) {
    for (let i=0; i< filePaths.length; i++) {
        await this.page.getByRole('button', { name: 'Add new' }).click();
        const documentUpload = this.page.locator(
          `#claimantNotificationDocuments_${i}_uploadedDocument, #pseRespondentOrdReqUploadDocument_${i}_uploadedDocument`,
        );
        await expect(documentUpload).toBeVisible();
        await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, documentUpload, filePaths[i]);

        const shortDesc = this.page.locator(
          `#claimantNotificationDocuments_${i}_shortDescription, #pseRespondentOrdReqUploadDocument_${i}_shortDescription`,
        );
        await expect(shortDesc).toBeVisible();
        await shortDesc.fill(`Support Document ${i+1}`);
        await this.delay(3000);
    }
  }

  async selectNotifyOtherParty(option: string) {
    if (option.toLowerCase() === 'yes') {
      await expect(this.yesNotifyOtherPartyRadio).toBeVisible();
      await this.yesNotifyOtherPartyRadio.check();
    } else {
      await expect(this.noNotifyOtherPartyRadio).toBeVisible();
      await this.noNotifyOtherPartyRadio.check();
    }
  }

  async respondToTribunal(
      params: {
        notificationName: string;
        responseText: string;
        supportingMaterialFiles: string[];
        notifyOtherParty: string;
    }) {
    await this.navigateToRespondToOrderOrRequestFromTheTribunal();
    await this.selectNotificationToRespond('1 - ' + params.notificationName);
    await this.clickContinue();
    await expect(this.page.locator(`//td[text()='Notification']/following-sibling::td[text()='${params.notificationName}']`)).toBeVisible();

    await this.enterResponseToTribunal(params.responseText);
    await this.selectAnySupportingMaterial(params.supportingMaterialFiles.length > 0 ? 'Yes' : 'No');
    if (params.supportingMaterialFiles.length > 0) {
      await this.uploadSupportDocument(params.supportingMaterialFiles);
    }
    await this.clickContinue();

    await this.selectNotifyOtherParty(params.notifyOtherParty);
    await this.clickContinue();

    await this.clickSubmitButton();
    await this.clickCloseAndReturn();

  }
}
