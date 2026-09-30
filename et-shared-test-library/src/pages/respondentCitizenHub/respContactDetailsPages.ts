import { BasePage } from '../basePage.ts';
import { Locator, Page, expect } from '@playwright/test';

export default class RespContactDetailsPages extends BasePage {
  private readonly contactDetailsLink: Locator;

  constructor(page: Page) {
    super(page);
    this.contactDetailsLink = this.page.locator('[href="/respondent-name"]');
  }

  async et3Section1() {
    await this.contactDetails();
    await this.hearingFormatEmployerDetails();
  }

  async contactDetails() {
    await this.clickContactDetailsLink();
    await this.respondentName();
    await this.typeOfOrganization();
    await this.respondentAddress();
    await this.nameOfContact();
    await this.dxAddress();
    await this.contactPhoneNumber();
    await this.contactPreference();
    await this.cyaPage();
  }

  async hearingFormatEmployerDetails() {
    await this.hearingFormat();
    await this.respondentEmployee();
    await this.respondentSite();
    await this.numberOfEmployeeAtsite();
    await this.hearingFormatCya();
  }

  async clickContactDetailsLink() {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('h1')).toContainText('Your response form (ET3)');
    await this.contactDetailsLink.click();
  }

  async respondentName() {
    await this.page.waitForLoadState('load');
    await this.page.getByLabel('Yes').check();
    await this.saveAndContinueButton();
  }

  async typeOfOrganization() {
    await this.page.waitForLoadState('load');
    await this.page.getByLabel('Individual').check();
    await this.page.getByLabel('Preferred title (optional)').fill('test');
    await this.saveAndContinueButton();
  }

  async respondentAddress() {
    await this.page.waitForLoadState('load');
    await this.page.getByLabel('Yes').check();
    await this.saveAndContinueButton();
  }

  async nameOfContact() {
    await this.page.waitForLoadState('load');
    await this.saveAndContinueButton();
  }

  async dxAddress() {
    await this.page.waitForLoadState('load');
    await this.page.locator('#et3ResponseDXAddress').fill('DX12345');
    await this.saveAndContinueButton();
  }

  async contactPhoneNumber() {
    await this.page.waitForLoadState('load');
    await this.page.locator('#responseRespondentPhone1').fill('07803456789');
    await this.saveAndContinueButton();
  }

  async contactPreference() {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('#main-form')).toContainText('How would you prefer to be contacted? (optional)');
    await expect(this.page.locator('#main-form')).toContainText('Which language should the tribunal use to contact you? (optional)');
    await this.page.getByLabel('Email').check();
    await this.page.getByLabel('English').check();
    await this.saveAndContinueButton();
  }

  async cyaPage() {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('dl')).toContainText('Respondent name');
    await expect(this.page.locator('dl')).toContainText('What type of organisation is the respondent?');
    await expect(this.page.locator('dl')).toContainText('DX address (optional)');
    await expect(this.page.locator('dl')).toContainText('What language would you like to be contacted in? (optional)');
    await this.page.getByLabel('Yes, I’ve completed this').check();
    await this.saveAndContinueButton();
  }

  async hearingFormat() {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('legend')).toContainText('Would you be able to take part in hearings by video and phone? (optional)');
    await this.page.getByText('Yes, I can take part in video').click();
    await this.saveAndContinueButton();
    await this.page.getByLabel('Yes').check();
    await expect(this.page.getByLabel('Tell us what support you need')).toBeVisible();
    await this.page.getByLabel('Tell us what support you need').fill('disable access');
    await this.saveAndContinueButton();
  }

  async respondentEmployee() {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('#main-form')).toContainText('How many people does the respondent employ in Great Britain? (optional)');
    await this.page.getByLabel('How many people does the').fill('10');
    await this.saveAndContinueButton();
  }

  async respondentSite() {
    await this.page.waitForLoadState('load');
    await this.page.getByLabel('No').check();
    await this.saveAndContinueButton();
  }

  async numberOfEmployeeAtsite() {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('#main-form')).toContainText('How many people are employed at the site where the claimant worked? (optional)');
    await this.page.getByLabel('How many people are employed').fill('10');
    await this.saveAndContinueButton();
  }

  async hearingFormatCya() {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('dl')).toContainText('Would you be able to take part in hearings by video and phone? (optional)');
    await expect(this.page.locator('dl')).toContainText('Tell us what support you need to request');
    await expect(this.page.locator('dl')).toContainText('How many employed at the site the claimant worked at? (optional)');
    await this.page.getByText('Yes, I’ve completed this').click();
    await this.saveAndContinueButton();
  }
}
