import { BasePage } from './basePage.ts';
import { expect, Locator, Page } from "@playwright/test";
import dateUtilComponent from '../data-utils/DateUtilComponent';
import icPageData from '../resources/payload/ic-page-content.json';
export default class InitialConsiderationPage extends BasePage {
  private readonly hearingDetails: Locator;
  private readonly jurisdictionCodeInvalidYes: Locator;
  private readonly claimCamProceedYes: Locator;
  private readonly proceedToHearingListed: Locator;
  private readonly preliminaryHearingOptions: Locator;
  private readonly alreadyDecided: Locator;
  private readonly respondentNameET1: Locator;
  private readonly respondentNameET3: Locator;
  private readonly claimantRespondentHearingPanel: Locator;
  private readonly claimantRespondentHearingFormat: Locator;
  private readonly et1VettingIssues: Locator;

  constructor(page: Page) {
    super(page);
    this.hearingDetails = page.locator('#etInitialConsiderationHearingLabel td:nth-child(2)');
    this.jurisdictionCodeInvalidYes = page.locator('#etICJuridictionCodesInvalid_No');
    this.claimCamProceedYes = page.locator('#etICCanProceed_Yes');
    this.proceedToHearingListed = page.locator('#etICHearingListedAnswers_etICHearingListed-Proceed to the hearing already listed');
    this.preliminaryHearingOptions = page.locator('#etICHearingNotListedListForPrelimHearingUpdated_prelimHearingLengthNumTypeV2');
    this.alreadyDecided = page.locator('#etICHearingListedAnswers_etICIsFinalHearingWithJudgeOrMembersJsaReason-Already decided');
    this.respondentNameET1 = page.locator('#etInitialConsiderationRespondentLabel td:nth-child(2)');
    this.respondentNameET3 = page.locator('#etInitialConsiderationRespondentLabel td:nth-child(3)');
    this.claimantRespondentHearingPanel = page.locator('#etIcPartiesHearingPanelPreferenceLabel td');
    this.claimantRespondentHearingFormat = page.locator('#etIcPartiesHearingFormatLabel td');
    this.et1VettingIssues = page.locator('#icEt1VettingIssuesDetailLabel');
  }

  async validateET1Links() {
    await expect(this.page.getByRole('link', { name: 'ET1', exact: true })).toBeVisible();
    await expect(this.page.getByRole('link', { name: 'ET1 Vetting', exact: true })).toBeVisible();
    await expect(this.page.getByRole('link', { name: 'ET3', exact: true })).toBeVisible();
  }

  async validateET3ProcessingLink() {
    await expect(this.page.getByRole('link', { name: 'ET3 Processing', exact: true })).toBeVisible();
  }

  async validateReferralLink() {
    await expect(this.page.getByRole('link', { name: 'Referrals', exact: true })).toBeVisible();
  }

  async validateLinksNotVisible() {
    await expect(this.page.getByRole('link', { name: 'ET1 Vetting', exact: true })).not.toBeVisible();
  }

  async validateHearingDetails() {
    const hearingItems = [
      dateUtilComponent.formatDateWithLeadingZeros(dateUtilComponent.addWeekdays(new Date(), 21)),
      'Costs Hearing',
      '1 Hour',
      'Hybrid',
      'Sit Alone',
      'Leeds ET',
    ];
    await this.hearingDetails.first().waitFor();
    const locators = await this.hearingDetails.all();
    const names: string[] = [];
    for (const locator of locators) {
      names.push(await locator.innerText());
    }
    expect(names).toEqual(hearingItems);
  }

  async completeSubmissionWithHearing() {
    await this.proceedToSecondPageIC();
    await this.page.getByRole('checkbox', { name: 'Proceed to the hearing' }).click();
    await this.page.getByRole('radio', { name: 'JSA' }).click();
    await this.page.getByRole('checkbox', { name: 'Already decided' }).click();
    await this.clickContinue();
    await this.clickSubmitButton();
    await expect(this.page.getByText(icPageData.icConfirmationText)).toBeVisible();
    await this.clickCloseAndReturn();
  }

  async proceedToSecondPageIC() {
    await this.jurisdictionCodeInvalidYes.click();
    await this.claimCamProceedYes.click();
    await this.clickContinue();
    await this.delay(2000);
  }

  async preliminaryHearingDetails() {
    await this.proceedToSecondPageIC();
    await this.page.getByRole('checkbox', { name: 'List for preliminary hearing' }).click();
    await this.page.getByRole('checkbox', { name: 'Video' }).click();
    await this.page.getByRole('checkbox', { name: 'Preliminary issue' }).click();
    await this.page.getByRole('textbox', { name: 'Give details of hearing notice' }).fill('Preliminary hearing details');
    await this.page.getByRole('textbox', { name: 'Length of hearing' }).fill('1');
    await this.preliminaryHearingOptions.selectOption('Hours');
    await this.page.getByRole('radio', { name: 'No' }).click();
    await this.clickContinue();
    await this.clickSubmitButton();
    await expect(this.page.getByText(icPageData.icConfirmationText)).toBeVisible();
    await this.clickCloseAndReturn();
  }

  async caseManagementHearingOptions() {
    await this.proceedToSecondPageIC();
    await this.page.getByRole('checkbox', { name: 'Postpone hearing' }).click();
    await this.page.getByRole('radio', { name: 'With members' }).click();
    await expect(this.page.locator('#etICHearingListedAnswers_etICIsHearingWithMembers')).toBeVisible();
    await this.page.getByRole('radio', { name: 'JSA' }).click();
    await expect(this.page.getByRole('radio', { name: 'Case management only' })).toBeVisible();
    await expect(this.page.getByRole('radio', { name: 'Already decided' })).toBeVisible();
    await expect(this.page.getByRole('radio', { name: 'Other' })).toBeVisible();
    await this.page.getByRole('radio', { name: 'Case management only' }).click();
    await this.clickContinue();
    await this.clickSubmitButton();
    await expect(this.page.getByText(icPageData.icConfirmationText)).toBeVisible();
    await this.clickCloseAndReturn();
  }

  async finalHearingOptions() {
    await this.proceedToSecondPageIC();
    await this.page.getByRole('checkbox', { name: 'Postpone hearing' }).click();
    await this.page.getByRole('radio', { name: 'With members' }).click();
    await expect(this.page.getByRole('checkbox', { name: 'Already decided' })).toBeVisible();
    await expect(this.page.getByRole('checkbox', { name: "Members' experience is likely to add significant value to the adjudication process" })).toBeVisible();
    await expect(this.page.getByRole('checkbox', { name: 'No views expressed by parties'})).toBeVisible();
    await expect(this.page.locator('#etICHearingListedAnswers_etICIsFinalHearingWithJudgeOrMembersReason').getByRole('checkbox', { name: 'Other' })).toBeVisible();
    await this.page.getByRole('checkbox', { name: 'Already decided' }).click();
    await this.clickContinue();
    await this.clickSubmitButton();
    await expect(this.page.getByText(icPageData.icConfirmationText)).toBeVisible();
    await this.clickCloseAndReturn();
  }

  async validateEnhancedAllPartyDetails() {
    await expect(this.respondentNameET1).toBeVisible();
    await expect(this.respondentNameET1).toContainText('Mrs Test Auto');
    await expect(this.respondentNameET3).toContainText('Test Auto');
    await expect(this.claimantRespondentHearingPanel.nth(1)).toContainText('Panel');
    await expect(this.claimantRespondentHearingPanel.nth(2)).toContainText('Test Panel Preference Reason');
    //await expect(this.claimantRespondentHearingPanel.nth(4)).toContainText('Judge');
    //await expect(this.claimantRespondentHearingPanel.nth(5)).toContainText('Test Panel reasons');
    await expect(this.claimantRespondentHearingFormat.nth(1)).toContainText('Video');
    await expect(this.claimantRespondentHearingFormat.nth(3)).toContainText('Video hearings');
  }

  async validateET1VettingIssues() {
    await this.isVettingIssueDisplayed('Details of Substantive Defects')
    await this.isVettingIssueDisplayed('Details of Track Allocation Issue');
  }

  async isVettingIssueDisplayed(fieldValue:string){
    await expect(this.page.locator(`//summary/span[text()="${fieldValue}"]`).first()).toBeVisible();
  }

  async validateTribunalCaseFileLink() {
    await expect(this.page.getByRole('link', { name: 'Tribunal case file', exact: true })).toBeVisible();
  }

  async waitForTribunalCaseFileLink() {
    await this.delay(20000)
  }
}
