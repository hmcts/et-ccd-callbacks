import { expect, Locator, Page } from '@playwright/test';
import * as fs from 'node:fs';

export class CommonActionsHelper {

  async waitForAllUploadsToBeCompleted(page: Page) {
    const cancelUploadLocators = await page.getByText('Cancel upload').all();
    for (let i = 0; i < cancelUploadLocators.length; i++) {
      await expect(cancelUploadLocators[i]).toBeDisabled({ timeout: 15000 });
    }
    const uploadingSpan = await page.locator('span', { hasText: 'Uploading...' }).all();
    for (let i = 0; i < uploadingSpan.length; i++) {
      await expect(uploadingSpan[i]).toBeHidden({ timeout: 10000 });
    }
  }

  /**
   * Creates a payload object for a PDF file with a new alias name.
   * Can be passed to the setInputFiles method of a locator.
   *
   * @param filePath - The path to the original PDF file.
   * @param newFilename - The new name to assign to the PDF file in the payload.
   * @returns An object containing the new filename, pdf MIME type, and file buffer.
   */
  async createAliasPDFPayload(filePath: string, newFilename: string) {
    const fileBuffer = fs.readFileSync(filePath);
    return {
      name: newFilename,
      mimeType: 'application/pdf',
      buffer: fileBuffer
    };
  }

  async uploadWithRateLimitRetry(
    page: Page,
    uploadField: Locator,
    fileToUpload: { name: string; mimeType: string; buffer: Buffer<ArrayBuffer> } | string,
    maxRetries: number = 5,
    waitMs: number = 5000
  ) {
    for (let attempt = 0; attempt < maxRetries; attempt++) {
      await uploadField.setInputFiles(fileToUpload);

      await this.waitForAllUploadsToBeCompleted(page);

      // Check for rate limit error in a preceding span sibling
      const errorLocator = uploadField.locator(
        'xpath=../preceding-sibling::span[contains(text(), "Your request was rate limited. Please wait a few seconds before retrying your document upload")]'
      );
      const isErrorVisible = await errorLocator.isVisible({ timeout: 2000 });

      if (!isErrorVisible) {
        return; // Success
      }
      if (attempt < maxRetries - 1) {
        await page.waitForTimeout(waitMs);
      } else {
        throw new Error('Rate limit error persists after retries');
      }
    }
  }

  async enterUkAddressWithPostcode(postCodeLocator: Locator, selectAddress: Locator, postcode: string = 'NW11NW'): Promise<void> {
    await expect(postCodeLocator).toBeVisible();
    await postCodeLocator.fill(postcode);
    const findAddressButton = postCodeLocator.locator('xpath=..//following-sibling::button[contains(text(), "Find address")]');
    await expect(findAddressButton).toBeVisible();
    await findAddressButton.click();
    await expect(selectAddress).toBeVisible();
    await selectAddress.selectOption({ index: 1 }); // select the first address in the dropdown
  }

  async selectOrganisation(page: Page, orgName: string) {
    const searchOrganisationField = page.locator('#search-org-text');
    await expect(searchOrganisationField).toBeVisible();
    await searchOrganisationField.fill(orgName);

    const selectOrgLink = page.locator('//a[contains(.,"Select")]').first();
    await expect(selectOrgLink).toBeVisible();
    await selectOrgLink.click();

    await page.waitForLoadState('load');
  }

}
