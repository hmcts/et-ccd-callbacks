import { BasePage } from "./basePage";
import { expect, Locator, Page } from '@playwright/test';

export default class RequestSupportPage extends BasePage {
  private readonly partySupportFlag: Locator;
  private readonly legalRepSupportFlag: Locator;


  constructor(page: Page) {
    super(page);
    this.partySupportFlag = page.locator('#flag-location-0');
    this.legalRepSupportFlag = page.locator('#flag-location-1');
  }

  async requestSupportFlag(partyName: string, requestTypes:string) {
    switch (partyName) {
      case 'Respondent':
        await expect(this.partySupportFlag).toBeVisible();
        await this.partySupportFlag.check();
        break;

      case 'Representative':
        await expect(this.legalRepSupportFlag).toBeVisible();
        await this.legalRepSupportFlag.check();
        break;

      default:
        throw new Error(`Invalid flagType: ${partyName}`);
    }
    await this.clickContinue();
    await expect(this.page.locator('#flag-type-0')).toBeVisible();
    await this.page.locator('#flag-type-0').check();
    await this.clickContinue();
    await this.reasonableAdjustmentRequestTypes(requestTypes, partyName);
  }

  async reasonableAdjustmentRequestTypes(requestTypes:string, partyName: string) {
    switch (requestTypes) {
      case 'forms':
        await expect(this.page.locator('#flag-type-1')).toBeVisible();
        await this.page.locator('#flag-type-1').check();
        break;

      case 'building access':
        await expect(this.page.locator('#flag-type-2')).toBeVisible();
        await this.page.locator('#flag-type-2').check();
        break;

      default:
        throw new Error(`incorrect request type: ${requestTypes}`);
    }
    await this.clickContinue();
    await expect(this.page.locator('#flag-type-0')).toBeVisible();
    await this.page.locator('#flag-type-0').check();
    await this.clickContinue();
    await this.page.locator('#flagComments').fill(
      'Requesting support flag for ' + partyName + ' - Request Type: ' + requestTypes
    );
    await this.clickContinue();
    await expect(this.page.locator('dt', { hasText: 'Status' })).toBeVisible();
    await this.clickSubmitButton();
  }
}
