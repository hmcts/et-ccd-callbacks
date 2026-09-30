import { expect, Locator, Page } from '@playwright/test';
import LoginPage from '../loginPage.ts';
import { config, UserCredentials } from '../../config/config.dynamic.ts';

export default class CitizenHubLoginPage extends LoginPage {
  private readonly syaLandingPageTitle: Locator;
  private readonly startNewClaimButton: Locator;
  private readonly returnToDraftOrSubmittedClaimLink: Locator;
  private readonly returnToExistingClaimPageTitle: Locator;
  private readonly returnTodraftClaimCheckbox: Locator;
  private readonly returntoSubmittedClaimCheckbox: Locator;
  private readonly employmentTribunalAccountCheckbox: Locator;
  private readonly signInOptionLink: Locator;
  private readonly returnToDraftOrSubmittedClaimPageTitle: Locator;

  constructor(page: Page) {
    super(page);
    this.syaLandingPageTitle = this.page.getByRole('heading', {name: 'Make a claim to an employment tribunal'})
    this.startNewClaimButton = this.page.locator(`xpath=//a[normalize-space()='Start a new claim']`);
    this.returnToDraftOrSubmittedClaimLink = this.page.locator(`xpath=//a[normalize-space()='Return to a draft or submitted claim']`);
    this.returnToExistingClaimPageTitle = this.page.getByRole('heading', {name: 'Return to a draft or submitted claim', level: 1});
    this.returnToDraftOrSubmittedClaimPageTitle = this.page.locator(`//title[normalize-space()='Return to a draft or submitted claim - Employment Tribunals - GOV.UK']`);
    this.returnTodraftClaimCheckbox = this.page.locator(`#return_to_existing`);
    this.returntoSubmittedClaimCheckbox = this.page.locator(`#return_to_existing-2`);
    this.employmentTribunalAccountCheckbox = this.page.locator(`#submitted_claim_option`);
    this.signInOptionLink = page.locator('//a[@href="/enter-email"]');
  }

  async assertSyaLandingPage() {
    await this.page.waitForLoadState('load');
    await expect(this.syaLandingPageTitle).toBeVisible();
    await expect(this.startNewClaimButton).toBeVisible();
    await expect(this.returnToDraftOrSubmittedClaimLink).toBeVisible();
  }

  async assertReturnToDraftOrSubmittedClaimPage() {
    await expect(this.returnToExistingClaimPageTitle).toBeVisible();
    await expect(this.returnToDraftOrSubmittedClaimPageTitle).toBeAttached();
    await expect(this.returnTodraftClaimCheckbox).toBeVisible();
    await expect(this.returntoSubmittedClaimCheckbox).toBeVisible();
  }

  async processCitizenHubLogin(user: UserCredentials){
    const claimantUri = config.etSyaUiUrl;
    await this.page.goto(claimantUri);
    await this.page.waitForLoadState('load');

    await this.assertSyaLandingPage();
    await this.page.waitForLoadState('load', {timeout: 1000});
    await this.returnToDraftOrSubmittedClaimLink.click();
    await this.page.waitForLoadState('load', {timeout: 1000});

    await this.assertReturnToDraftOrSubmittedClaimPage();
    await this.returntoSubmittedClaimCheckbox.check();
    await this.page.waitForLoadState('load');
    await this.employmentTribunalAccountCheckbox.check();
    await this.clickContinue();
    const isNewIdam = await this.signInOptionLink.isVisible().catch(() => false);
    if(isNewIdam) { // Remove if condition when new IDAM is rolled out to all environments
      await this.signInOptionLink.click();
      await this.page.waitForLoadState('load');
    }
    if (await this.signOutLink.isVisible({ timeout: 2000 }).catch(() => false)) return;
    await this.processLogin(user, config.etSyaUiUrl);
    //TODO remove url check to landing page, as UI will be logged as soon as user is created.
    const url = this.page.url();
    if (url.includes('claimant-applications')) {
      await this.page.waitForURL(`${claimantUri}claimant-applications**`, {timeout: 5000});
    } else {
      await this.assertSyaLandingPage();
    }
  }

}
