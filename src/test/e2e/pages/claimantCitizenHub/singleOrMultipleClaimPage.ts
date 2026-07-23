import { BasePage } from "../basePage.ts";
import { expect, Locator, Page } from '@playwright/test';
import CitizenHubPage from './CitizenHubPage.ts';


export default class SingleOrMultipleClaimPage extends CitizenHubPage{

  private readonly singleOrMultipleClaim: Locator;
  private readonly claimingWithOtherPersonRadio: Locator;
  private readonly addAnotherClaimantToYourGroupClaimLink: Locator;
  private readonly groupRepresentative: Locator;
  private readonly wantToAddOtherClaimants: Locator;
  private readonly addClaimantManually: Locator;
  private readonly uploadSpreadsheet: Locator;

  constructor(page: Page) {
    super(page);
    this.singleOrMultipleClaim = this.page.locator('a[href="/single-or-multiple-claim?lng=en"]');
    this.claimingWithOtherPersonRadio = this.page.locator('#single-or-multiple-claim-2');
    this.addAnotherClaimantToYourGroupClaimLink = this.page.locator('a[href=""]');
    this.groupRepresentative = this.page.locator('a[href=""]');
    this.wantToAddOtherClaimants = this.page.locator(
      'fieldset:has(legend:text-matches("How do you want to add the other claimants?"))'
    );
    this.addClaimantManually= this.page.locator('#add-claimant-method');
    this.uploadSpreadsheet= this.page.locator('#add-claimant-method-2');

  }

  async clickClaimingOnOwnOrOthersLink() {
    await this.singleOrMultipleClaim.click();
    await this.clickContinue();
  }

  async processClaimingWithOthers() {
    await this.clickClaimingOnOwnOrOthersLink();
    await expect(this.page.locator('h1')).toContainText('Claiming on your own or with others');
    await this.claimingWithOtherPersonRadio.click();
    await this.clickContinue();
    await expect(this.page.locator('h1')).toContainText('Add another claimant to your group claim');
    await expect(this.wantToAddOtherClaimants).toBeVisible();
    await this.addClaimantManually.check();
    await this.clickContinue();
  }

}
