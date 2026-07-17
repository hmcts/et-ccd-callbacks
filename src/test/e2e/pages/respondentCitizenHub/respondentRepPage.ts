import { BasePage } from "../basePage.ts";
import { expect, Locator, Page } from '@playwright/test';
import { CaseDetailsValues } from '../../config/case-data.ts';

export default class RespondentRepPage extends BasePage {
  private readonly nonRegisteredLegalOrg: Locator;
  private readonly registeredLegalOrg: Locator;
  private readonly searchForRegisteredOrg: Locator;
  private readonly selectRespondent: Locator;
  private readonly selectOrgFromSearchResult: Locator;
  private readonly respondentName: Locator;
  private readonly respondentRepresentativeName: Locator;
  private readonly respondentRepresentativePhoneNumber: Locator;
  private readonly respondentRepresentativeEmailAddress: Locator;
  private readonly respondentRepresentativeContactPreference: Locator;
  private readonly respondentTypeInd: Locator;
  private readonly respFirstName: Locator;
  private readonly respLastName: Locator;
  private readonly respReceivedYes: Locator;
  private readonly respFullName: Locator;
  private readonly respStatusDropDown: Locator;
  private readonly respClaimYes: Locator;
  private readonly respReceivedDay: Locator;
  private readonly respReceivedMonth: Locator;
  private readonly respReceivedYear: Locator;
  private readonly respPostCode: Locator;
  private readonly respAddressDropDown: Locator;
  private readonly respEccYes: Locator;
  private readonly respEccReplyYes: Locator;
  private readonly respHearingPanel: Locator;
  private readonly respHearingPanelReason: Locator;
  private readonly removeRespondentRepresentative: Locator;

  constructor(page: Page) {
    super(page);
    this.nonRegisteredLegalOrg = page.locator('#repCollection_0_myHmctsYesNo_No');
    this.registeredLegalOrg = page.locator('#repCollection_0_myHmctsYesNo_Yes');
    this.searchForRegisteredOrg = page.locator('#search-org-text');
    this.selectRespondent = page.locator('#repCollection_0_dynamic_resp_rep_name');
    this.selectOrgFromSearchResult = page.locator('//a[contains(.,"Select")]');
    this.respondentName = page.locator('#repCollection_0_name_of_representative');
    this.respondentRepresentativeName = page.locator('#repCollection_0_name_of_representative');
    this.respondentRepresentativePhoneNumber = page.locator('#repCollection_0_representative_phone_number');
    this.respondentRepresentativeEmailAddress = page.locator('#repCollection_0_representative_email_address');
    this.respondentRepresentativeContactPreference = page.locator('#repCollection_0_representative_preference');
    this.respondentTypeInd = page.locator('#respondentCollection_0_respondentType-Individual');
    this.respFirstName = page.locator('#respondentCollection_0_respondentFirstName');
    this.respLastName = page.locator('#respondentCollection_0_respondentLastName');
    this.respReceivedYes = page.locator('#respondentCollection_0_responseReceived_Yes');
    this.respFullName = page.locator('#respondentCollection_0_responseRespondentName');
    this.respStatusDropDown = page.locator('#respondentCollection_0_response_status');
    this.respClaimYes = page.locator('#respondentCollection_0_responseToClaim_Yes');
    this.respReceivedDay = page.locator('#responseReceivedDate-day');
    this.respReceivedMonth = page.locator('#responseReceivedDate-month');
    this.respReceivedYear = page.locator('#responseReceivedDate-year');
    this.respPostCode = page.locator('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_postcodeInput');
    this.respAddressDropDown = page.locator('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_addressList');
    this.respEccYes = page.locator('#respondentCollection_0_respondentEcc_Yes');
    this.respEccReplyYes = page.locator('#respondentCollection_0_respondentEccReply_Yes');
    this.respHearingPanel = page.locator('#respondentCollection_0_respondent_hearing_panel_preference-Judge');
    this.respHearingPanelReason = page.locator('#respondentCollection_0_respondent_hearing_panel_preference_reason');
    this.removeRespondentRepresentative = this.page.locator(`xpath=//button[normalize-space()='Remove']`);
  }

  async addRespondentRepresentative(regOption: string, orgName: string) {
    await expect(this.page.getByRole('heading', { name: 'Respondent Representative(s)', level: 2})).toBeVisible();
    await this.selectRespondent.selectOption(CaseDetailsValues.respondentName);
    await this.respondentName.fill(CaseDetailsValues.respondentName);
    await this.respondentRepresentativePhoneNumber.fill('01234657895');
    await this.respondentRepresentativeEmailAddress.fill('et.tester@hmcts.net');
    await this.respondentRepresentativeContactPreference.selectOption('1: Email');

    try {
      switch (regOption) {
        case 'registered':
          await this.registeredLegalOrg.check();
          await expect(this.page.locator('ccd-write-organisation-field')).toContainText('Search for an organisation');
          await this.searchForRegisteredOrg.fill(orgName);
          await this.selectOrgFromSearchResult.click();
          await this.clickSubmitButton(false);
          await this.clickIgnoreAndContinueButton();
          break;
        case 'notRegisteredSingle':
        case 'partResgisteredMultiple':
          break;
        default:
          throw new Error('... check you options or add new option');
      }
    } catch (e) {
      console.error('invalid option', e);
    }
  }

  async enterRespType() {
    await this.respReceivedYes.click();
    await this.respFullName.fill('Mark Gill');
    await this.respStatusDropDown.selectOption('Accepted');
    await this.respClaimYes.click();

    await this.respReceivedDay.fill('01');
    await this.respReceivedMonth.fill('03');
    await this.respReceivedYear.fill('2025');

    await this.respPostCode.fill('EX1 3DD');
    await this.page.locator('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_postcodeLookup button').click();
    await this.delay(2000);
    await this.respAddressDropDown.selectOption('68 East Wonford Hill, Exeter');
    await this.delay(2000);

    await this.respEccYes.click();
    await this.delay(2000);
    await this.respEccReplyYes.click();
    await this.clickSubmitButton();
  }

  async enterRespTypeforIC() {
    await this.respReceivedYes.click();
    await this.respFullName.fill('Test Auto');
    await this.respStatusDropDown.selectOption('Accepted');
    await this.respClaimYes.click();

    await this.respReceivedDay.fill('01');
    await this.respReceivedMonth.fill('03');
    await this.respReceivedYear.fill('2025');

    await this.respPostCode.fill('EX1 3DD');
    await this.page.locator('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_postcodeLookup button').click();
    await this.delay(2000);
    await this.respAddressDropDown.selectOption('68 East Wonford Hill, Exeter');
    await this.delay(2000);

    await this.respEccYes.click();
    await this.delay(2000);
    await this.respEccReplyYes.click();
    await this.clickSubmitButton();
  }

  async validateRespondentRepDetail() {
    await expect(this.page.locator('#case-viewer-field-read--repCollection')).toContainText(
      'et.legalrep.superuser@gmail.com',
    );
  }

  async clickRemoveRespondentRepresentative(position: number = 0) {
    await this.page.waitForLoadState('load');
    await this.removeRespondentRepresentative.nth(position).click();
    const removePopup = this.page.locator(`xpath=//button[normalize-space()='Remove' and @title='Remove']`);
    await removePopup.click();
    await this.page.waitForLoadState('load');
  }
}
