import { BasePage } from "./basePage";
import { Locator, Page, expect } from '@playwright/test';
import respPageData from '../resources/payload/respondent-page-content.json';

export default class ClaimantDetailsPage extends BasePage {
  private readonly firstName: Locator;
  private readonly lastName: Locator;
  private readonly addressLine1: Locator;
  private readonly claimantWorkPhone: Locator;
  private readonly occupation: Locator;
  private readonly hearingPreference: Locator;
  private readonly hearingPreferenceVideo: Locator;
  private readonly panelEle: Locator;
  private readonly panelPreferenceReason: Locator;

  constructor(page: Page) {
    super(page);
    this.firstName = page.locator('#claimantIndType_claimant_first_names');
    this.lastName = page.locator('#claimantIndType_claimant_last_name');
    this.addressLine1 = page.locator('#claimantType_claimant_addressUK__detailAddressLine1');
    this.claimantWorkPhone = page.locator('#claimantWorkAddress_claimant_work_phone_number');
    this.occupation = page.locator('#claimantOtherType_claimant_occupation');
    this.hearingPreference = page.locator('#claimantHearingPreference_contact_language-English');
    this.hearingPreferenceVideo = page.locator('#claimantHearingPreference_hearing_preferences-Video');
    this.panelEle = page.locator('#claimantHearingPreference_claimant_hearing_panel_preference-Panel');
    this.panelPreferenceReason = page.locator('#claimantHearingPreference_claimant_hearing_panel_preference_why');
  }

  async processClaimantDetails(hearingPanelPreference?: boolean) {
    await expect(this.firstName).toBeVisible();
    await this.firstName.fill('Laila');
    await this.lastName.fill('McDonald');
    await this.clickContinue();
    await expect(this.addressLine1).toBeVisible();
    await this.clickContinue();
    await expect(this.claimantWorkPhone).toBeVisible();
    await this.clickContinue();
    await expect(this.occupation).toBeVisible();
    await this.clickContinue();
    await expect(this.hearingPreference).toBeVisible();
    await this.hearingPreferenceVideo.click();
    await this.hearingPreference.check();
    if (hearingPanelPreference) await this.fillPanelPreference();
    await this.clickSubmitButton();
  }

  async processClaimantDetailsForIC(hearingPanelPreference?: boolean) {
    await expect(this.firstName).toBeVisible();
    await this.clickContinue();
    await expect(this.addressLine1).toBeVisible();
    await this.clickContinue();
    await expect(this.claimantWorkPhone).toBeVisible();
    await this.clickContinue();
    await expect(this.occupation).toBeVisible();
    await this.clickContinue();
    await expect(this.hearingPreference).toBeVisible();
    await this.hearingPreferenceVideo.click();
    await this.hearingPreference.check();
    if (hearingPanelPreference) await this.fillPanelPreference();
    await this.clickSubmitButton();
  }

  async verifyClaimantDetails() {
    await this.verifyClaimantDetailsOnTab("Panel preference reason", respPageData.panelReason);
    await this.verifyClaimantDetailsOnTab("Hearing panel preference", respPageData.preferenceNameisPanel);
  }

  async verifyClaimantDetailsOnTab(fieldLabel: string, fieldValue: string) {
    await expect(this.page.locator(`//*[normalize-space()="${fieldLabel}"]/../..//td[normalize-space()="${fieldValue}"]`)).toBeVisible();
  }

  async fillPanelPreference() {
    await expect(this.panelEle).toBeVisible();
    await this.panelEle.check();
    await expect(this.panelPreferenceReason).toBeVisible();
    await this.panelPreferenceReason.fill(respPageData.panelReason);
  }
}
