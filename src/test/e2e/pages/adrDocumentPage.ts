import { BasePage } from "./basePage";
import { Locator, expect } from '@playwright/test';


export default class AdrDocumentPage extends BasePage {
    private readonly uploadInput: Locator;
    private readonly shortDescriptionInput: Locator;
    private readonly collectionField: Locator;

    constructor(page: import('@playwright/test').Page) {
        super(page);
        this.uploadInput = page.locator('#adrDocumentCollection_0_uploadedDocument');
        this.shortDescriptionInput = page.locator('#adrDocumentCollection_0_shortDescription');
        this.collectionField = page.locator('ccd-read-collection-field');
    }

    async adrUploadDocument() {
        await this.addNewButtonClick();
        await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.uploadInput, 'resources/test_file/welshTest.pdf')
        await this.shortDescriptionInput.fill('description');
        await this.clickSubmitButton();
    }

}
