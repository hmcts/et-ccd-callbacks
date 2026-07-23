import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from "../basePage.ts";

export default class DraftJudgementPage extends BasePage {

    private readonly isThisAJudgement: Locator;
    private readonly directionsText: Locator;

    constructor(page: Page) {
      super(page);
      this.isThisAJudgement = this.page.locator('#draftAndSignJudgement_isJudgement');
      this.directionsText = this.page.locator(`#draftAndSignJudgement_furtherDirections`);
    }

    async assertDraftJudgementPageIsDisplayed() {
        await expect(this.page.getByText('Draft and sign judgment/order')).toBeVisible();
    }

    async selectIsThisAJudgement(option: string) {
      await expect(this.isThisAJudgement).toBeVisible();
      const optionLocator = this.isThisAJudgement.getByText(option);
      await expect(optionLocator).toBeVisible();
      await optionLocator.check();
    }

    async uploadDocument(position: number = 0, filePath: string) {
      const documentUpload = this.page.locator(`#draftAndSignJudgement_draftAndSignJudgementDocuments_${position}_uploadedDocument`)
      await expect(documentUpload).toBeVisible();
      await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, documentUpload, filePath);
      await this.delay(3000);
    }

    async fillAnyFurtherDirections(directions: string) {
      await expect(this.directionsText).toBeVisible();
      await this.directionsText.fill(directions);
    }

    async submitDraftJudgement() {
        await this.assertDraftJudgementPageIsDisplayed();
        await this.selectIsThisAJudgement('Yes');
        await this.addNewButtonClick();
        await this.uploadDocument(0,'src/test/e2e/resources/test_file/test.txt');
        await this.fillAnyFurtherDirections('Test Draft Judgement');
        await this.clickContinue();
        await this.clickSubmitButton();
    }
}
