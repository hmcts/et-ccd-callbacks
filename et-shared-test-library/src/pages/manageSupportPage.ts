import { BasePage } from "./basePage";
import { expect, Locator, Page } from '@playwright/test';

export default class ManageSupportPage extends BasePage {
  private readonly caseFlagField: Locator;

  constructor(page: Page) {
    super(page);

    this.caseFlagField = page.locator('ccd-read-case-flag-field');


  }
  async validateSupportFlag(buildingAccessFlag:boolean){
      await expect(this.caseFlagField).toContainText('Guidance on how to complete forms');
       await expect(this.caseFlagField).toContainText('Requested');
       if(buildingAccessFlag){
         await expect(this.caseFlagField).toContainText('Step free / wheelchair access');
         await expect(this.caseFlagField).toContainText('Active');
       }
  }

  async validateUpdatedSupportFlag(flagStatus:string){
    await expect(this.caseFlagField).toContainText('Guidance on how to complete forms');

    switch (flagStatus) {
      case 'Active':
        await expect(this.caseFlagField).toContainText('Active');
        break;

      case 'Inactive':
        await expect(this.caseFlagField).toContainText('Inactive');
        break;

      case 'Not approved':
        await expect(this.caseFlagField).toContainText('Not approved');
        break;

      default:
        throw new Error(`Not valid status`);
    }
  }

  async manageSupportFlag() {

    await expect(this.page.locator('#flag-selection-0')).toBeVisible();
    await this.page.locator('#flag-selection-0').check();
    await this.clickContinue();

    await expect(this.page.locator('#flagStatusReasonChange')).toBeVisible();
    await this.page.locator('#flagStatusReasonChange').fill('manage flag');
    await this.clickContinue();
    await expect(this.page.locator('dt', { hasText: 'Status' })).toBeVisible();
    await this.clickSubmitButton();
  }

  async validateInactiveFlag(){
    await expect(this.caseFlagField).toContainText('Guidance on how to complete forms');
    await expect(this.caseFlagField).toContainText('Inactive');
  }

  async validateSupportTab() {
    await expect(this.caseFlagField).not.toContainText('Active');
    //only external flag flag visible
    await expect(this.caseFlagField).toContainText('Requested');
  }
}
