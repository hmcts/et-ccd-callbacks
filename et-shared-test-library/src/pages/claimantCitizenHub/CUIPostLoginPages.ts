import { BasePage } from '../basePage.ts';
import { expect } from '@playwright/test';

export default class CUIPostLoginPages extends BasePage {

  async processPostLoginPagesForTheDraftApplication() {
    await this.newAccountLanding();
    await this.stepsToMakeClaim();
  }

  //select continue on the /new-account-landing page
  async newAccountLanding() {
    await expect(this.page.getByRole('heading', {name:'You do not have to complete your claim in one go'})).toBeVisible();
    await this.clickContinue();
  }

  //Verify Steps to making your claim page
  async stepsToMakeClaim() {
    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name:'Steps to making your claim' })).toBeVisible();
  }
}
