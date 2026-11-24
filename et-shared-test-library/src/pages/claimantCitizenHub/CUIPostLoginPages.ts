import { BasePage } from '../basePage.js';
import { expect } from '@playwright/test';

export class CUIPostLoginPages extends BasePage {

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
    await expect(this.page.getByText('Application Details')).toBeVisible();

    // your details
    await expect(this.page.getByText('Your details')).toBeVisible();
    await expect(this.page.getByText('Personal details')).toBeVisible();
    await expect(this.page.getByText('Contact details')).toBeVisible();
    await expect(this.page.getByText('Your preferences')).toBeVisible();

    // Employment details
    await expect(this.page.getByText('Employment and respondent details')).toBeVisible();
    await expect(this.page.getByText('Employment status')).toBeVisible();
    await expect(this.page.getByRole('link', { name: 'Respondent details' })).toBeVisible();

    // Claim details
    await expect(this.page.getByText('Claim details')).toBeVisible();
    await expect(this.page.getByText('Tell us about your claim')).toBeVisible();
    await expect(this.page.getByText('Tell us what you want from your claim')).toBeVisible();
    await expect(this.page.getByText('Check and submit')).toBeVisible();
    await expect(this.page.getByText('Check your answers')).toBeVisible();
  }

}

