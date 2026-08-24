import { BasePage } from "../basePage.ts";
import respPageData from '../../resources/payload/respondent-page-content.json';
import DateUtilComponent from '../../data-utils/DateUtilComponent.ts';
import { expect, Locator, Page } from '@playwright/test';
import { CommonActionsHelper } from '../helpers/CommonActionsHelper.ts';

export default class RespondentDetailsPage extends BasePage {
  private readonly respondentReceivedDateField: Locator;
  private readonly respondentName: Locator;
  private readonly judgePanelEle: Locator;
  private readonly panelPreferenceReason: Locator;
  private readonly orgRespondentType: Locator;
  private readonly orgNameEle: Locator;
  private readonly expandImgIcon: Locator;

  constructor(page: Page) {
    super(page);
    this.respondentReceivedDateField = this.page.locator('#responseReceivedDate');
    this.respondentName = this.page.locator('#respondentCollection_0_respondent_name');
    this.judgePanelEle = this.page.locator('#respondentCollection_0_respondent_hearing_panel_preference-Judge');
    this.panelPreferenceReason = this.page.locator('#respondentCollection_0_respondent_hearing_panel_preference_reason');
    this.orgRespondentType = this.page.locator('#respondentCollection_0_respondentType-Organisation');
    this.orgNameEle = this.page.locator('#respondentCollection_0_respondentOrganisation');
    this.expandImgIcon = this.page.locator('div a img');
  }

  async enterResponseReceivedDate(position: number = 1) {
    const [year, month, day] = DateUtilComponent.getCurrentDateSliced();
    const dateGroups = await this.respondentReceivedDateField.all();
    const dateGroup = dateGroups[position - 1];
    await expect(dateGroup).toBeVisible();
    await dateGroup.locator('input[name="responseReceivedDate-day"]').fill(day);
    await dateGroup.locator('input[name="responseReceivedDate-month"]').fill(month);
    await dateGroup.locator('input[name="responseReceivedDate-year"]').fill(year);
  }

  async enterAddressForRespondentFromET3(position: number = 1 ){
    const postCodeLookup = this.page.locator(`#respondentCollection_${position-1}_responseRespondentAddress_responseRespondentAddress_postcodeInput`)
    const selectAddress = this.page.locator(`#respondentCollection_${position-1}_responseRespondentAddress_responseRespondentAddress_addressList`);
    await expect(postCodeLookup).toBeVisible();
    await this.commonActionsHelper.enterUkAddressWithPostcode(postCodeLookup, selectAddress);
  }

  async uploadET3Form(position: number =1, file: string = 'src/test/e2e/resources/test_file/welshTest.pdf' ){
    const fileUpload = this.page.locator(`#respondentCollection_${position-1}_et3Form`);
    await expect(fileUpload).toBeVisible();

    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      fileUpload,
      await this.commonActionsHelper.createAliasPDFPayload(file, 'ET3Form.pdf'));
  }

  async processRespondentDetails() {
    await expect(this.respondentName).toBeVisible();
    await this.respondentName.fill('Mr Mark Gill');
    await this.clickSubmitButton();
  }

  async processRespondentDetailsET3(et3accepted:boolean){
    await expect(this.respondentName).toBeVisible();
    await this.page.locator('#respondentCollection_0_responseReceived_Yes').check();
    await this.enterResponseReceivedDate();
    await this.enterAddressForRespondentFromET3();
    if(et3accepted){
      await this.page.locator('#respondentCollection_0_response_status').selectOption('1: Accepted');
    } else {
      await this.page.locator('#respondentCollection_0_response_status').selectOption('4: Rejected');
    }
    await this.page.locator('#respondentCollection_0_respondent_email').fill('email@email.com');
    await this.page.locator('#respondentCollection_0_respondent_contact_preference').selectOption('Email');
    await this.uploadET3Form();
    await this.clickSubmitButton();
  }

  async processPanelPreference() {
    await expect(this.judgePanelEle).toBeVisible();
    await this.judgePanelEle.check();
    await expect(this.panelPreferenceReason).toBeVisible();
    await this.panelPreferenceReason.fill(respPageData.panelReason);
    await expect(this.orgRespondentType).toBeVisible();
    await this.orgRespondentType.check();
    await expect(this.orgNameEle).toBeVisible();
    await this.orgNameEle.fill(respPageData.orgName);
    await this.clickSubmitButton();
  }

  async verifyRespondentDetails() {
    await this.expandImgIcon.waitFor();
    await this.expandImgIcon.click();
    await this.verifyRespondentDetailsOnTab("Type of respondent", respPageData.orgRespType);
    await this.verifyRespondentDetailsOnTab("Organisation or business name", "Test Organisation Name");
    await this.verifyRespondentDetailsOnTab("Hearing panel preference", respPageData.preferenceNameisJudge);
  }

  async verifyRespondentDetailsOnTab(fieldLabel: string, fieldValue: string) {
    await expect(this.page.locator(`//*[normalize-space()="${fieldLabel}"]/../..//td[normalize-space()="${fieldValue}"]`)).toBeVisible();
  }
}
