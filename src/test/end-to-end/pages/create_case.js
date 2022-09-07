'use strict';
const CCPBConstants = require('./common/constants');
const moment = require('moment');

const { I } = inject();

module.exports = {
  locators: {

    //Create Case Page
    jurisdiction: { xpath: '//select[@id=\'cc-jurisdiction\']' },
    case_type: { xpath: '//select[@id=\'cc-case-type\']' },
    event: { xpath: '//select[@id=\'cc-event\']' },

    //Date of Receipt Page
    date_of_receipt_day : {xpath : '//input[@id=\'receiptDate-day\']'},
    date_of_receipt_month : {xpath : '//input[@id=\'receiptDate-month\']'},
    date_of_receipt_year : {xpath : '//input[@id=\'receiptDate-year\']'},
    submission_reference : {xpath : '//input[@id=\'feeGroupReference\']'},
    tribunal_office : {xpath : '//select[@id=\'managingOffice\']'},

    //Type of Claimant Page
    type_of_claimant_individual : {xpath : '//input[@id=\'claimant_TypeOfClaimant-Individual\']'},
    type_of_claimant_company : {xpath : '//input[@id=\'claimant_TypeOfClaimant-Company\']'},
    title : {xpath : '//select[@id=\'claimantIndType_claimant_preferred_title\']'},
    first_name : {xpath : '//input[@id=\'claimantIndType_claimant_first_names\']'},
    last_name : {xpath : '//input[@id=\'claimantIndType_claimant_last_name\']'},
    date_of_birth_day : {xpath : '//input[@id=\'claimant_date_of_birth-day\']'},
    date_of_birth_month : {xpath : '//input[@id=\'claimant_date_of_birth-month\']'},
    date_of_birth_year : {xpath : '//input[@id=\'claimant_date_of_birth-year\']'},
    sex : {xpath : '//select[@id=\'claimantIndType_claimant_sex\']'},
    gender_identity : {xpath : '//select[@id=\'claimantIndType_claimant_gender_identity_same\']'},
    gender_identity_description : {xpath : '//input[@id=\'claimantIndType_claimant_gender_identity\']'},
    claimant_phone_number : { xpath : '//input[@id=\'claimantType_claimant_phone_number\']'},
    claimant_alternative_number : {xpath : '//input[@id=\'claimantType_claimant_mobile_number\']'},
    claimant_enter_uk_postcode : {xpath : '//input[@id=\'claimantType_claimant_addressUK_claimant_addressUK_postcodeInput\']'},
    find_address_button : {xpath : '//button[contains(text(),\'Find address\')]'},
    claimant_select_an_address : {xpath : '//select[@id=\'claimantType_claimant_addressUK_claimant_addressUK_addressList\']'},
    email_address : {xpath : '//input[@id=\'claimantType_claimant_email_address\']'},
    contact_preference : {xpath : '//select[@id=\'claimantType_claimant_contact_preference\']'},

    //Add Respondent Page
    name_of_respondent : {xpath: '//textarea[@id=\'respondentCollection_0_respondent_name\']'},
    is_there_an_acas_certificate_number_yes : {xpath : '//input[@id=\'respondentCollection_0_respondent_ACAS_question_Yes\']'},
    is_there_an_acas_certificate_number_no : {xpath : '//input[@id=\'respondentCollection_0_respondent_ACAS_question_No\']'},
    acas_certificate_number_input : {xpath: '//input[@id=\'respondentCollection_0_respondent_ACAS\']'},
    respondent_phone_number : {xpath : '//input[@id=\'respondentCollection_0_respondent_phone1\']'},
    et3_form_received_option_no : {xpath : '//input[@id=\'respondentCollection_0_responseReceived_No\']'},
    respondent_enter_uk_postcode : {xpath : '//input[@id=\'respondentCollection_0_respondent_address_respondent_address_postcodeInput\']'},
    respondent_select_an_address : {xpath : '//select[@id=\'respondentCollection_0_respondent_address_respondent_address_addressList\']'},

    //Respondent Premises page
    premises_title : {xpath: '//input[@id=\'companyPremises_premises\']'},
    premises_enter_uk_postcode : {xpath : '//input[@id=\'companyPremises_address_address_postcodeInput\']'},
    premises_select_an_address : {xpath : '//select[@id=\'companyPremises_address_address_addressList\']'},

    //Claimant Work Address Page
    claimants_work_address_question_no : {xpath : '//input[@id=\'claimantWorkAddressQuestion_No\']'},
    claimant_work_address_enter_a_postcode : {xpath : '//input[@id=\'claimantWorkAddress_claimant_work_address_claimant_work_address_postcodeInput\']'},
    claimant_work_address_select_an_address : {xpath : '//select[@id=\'claimantWorkAddress_claimant_work_address_claimant_work_address_addressList\']'},
    claimant_work_address_phone_number : {xpath : '//input[@id=\'claimantWorkAddress_claimant_work_phone_number\']'},

    //Other Details Page
    other_details_claimant_occupation : {xpath : '//input[@id=\'claimantOtherType_claimant_occupation\']'},
    claimant_employed_from_day : {xpath : '//input[@id=\'claimant_employed_from-day\']'},
    claimant_employed_from_month : {xpath : '//input[@id=\'claimant_employed_from-month\']'},
    claimant_employed_from_year : {xpath : '//input[@id=\'claimant_employed_from-year\']'},
    currently_employed : {xpath : '//input[@id=\'claimantOtherType_claimant_employed_currently_Yes\']'},
    notice_period_end_date_day : {xpath : '//input[@id=\'claimant_employed_notice_period-day\']'},
    notice_period_end_date_month : {xpath : '//input[@id=\'claimant_employed_notice_period-month\']'},
    notice_period_end_date_year : {xpath : '//input[@id=\'claimant_employed_notice_period-year\']'},
    any_disabilities_or_special_needs : {xpath : '//input[@id=\'claimantOtherType_claimant_disabled_Yes\']'},
    disabilities_please_provide_details : {xpath : '//textarea[@id=\'claimantOtherType_claimant_disabled_details\']'},

    //Claimant Represented Page
    is_the_claimant_represented : {xpath : '//input[@id=\'claimantRepresentedQuestion_No\']'},
    hearing_preferences_neither : {xpath : '//input[@id=\'claimantHearingPreference_hearing_preferences-Neither\']'},
    why_cant_claimant_not_take_part : {xpath : '//textarea[@id=\'claimantHearingPreference_hearing_assistance\']'},
  },

  async getHeaderValue() {
    const headerValue = await I.grabTextFrom(this.locators.header);
    return headerValue;
  },

  verifyCreateCaseInputPage() {
    I.see('Create Case');
    I.see('Jurisdiction');
    I.see('Case type');
    I.see('Event');
  },

  clickCreateCaseLink() {
    I.click('Create case');
  },

  clickStartButton() {
    I.click('Start');
  },

  clickContinueButton() {
    I.click('Continue');
  },

  clickSubmitButton() {
    I.click('Submit');
  },

  inputCreateCaseDetailsPage() {
    I.selectOption(this.locators.jurisdiction, 'EMPLOYMENT');
    I.selectOption(this.locators.case_type, 'ET_EnglandWales');
    I.selectOption(this.locators.event, 'initiateCase');
  },


  verifyCreateCaseDateOfReceiptInputPage() {
    I.see('Create Case');

    I.see('Date of Receipt');
    I.see('Day');
    I.see('Month');
    I.see('Year');

    I.see('Submission Reference');
    I.see('Submission Reference (12 digit number)');
    I.see('Tribunal Office');
  },

  inputCreateCaseDateOfReceiptInputPage() {
    const now = moment();
    I.fillField(this.locators.date_of_receipt_day, now.day());
    I.fillField(this.locators.date_of_receipt_month, now.month());
    I.fillField(this.locators.date_of_receipt_year, now.year());

    I.fillField(this.locators.submission_reference,'123456789012');
    I.selectOption(this.locators.tribunal_office,'3: London Central');
  },

  verifyCreateCaseTypeOfClaimantPage() {
    I.see('Create Case');

    I.see('Type of claimant');
    I.see('Is the claimant an individual or a company?');
    I.see('Individual');
    I.see('Company');

    I.see('Claimant Details');
    I.see('Title (Optional)');
    I.see('First Name');
    I.see('Last Name');
    I.see('Date of birth (Optional)');
    I.see('Day');
    I.see('Month');
    I.see('Year');
    I.see('Sex (Optional)');
    I.see('Gender Identity (Optional)');
    I.see('Gender Identity description (Optional)');
    I.see('Phone number (Optional)');
    I.see('Alternative number (Optional)');

    I.see('Address');
    I.see('Enter a UK postcode');
    I.see('I can\'t enter a UK postcode');
    I.see('Email address (Optional)');
    I.see('Contact preference (Optional)');
  },

  inputCreateCaseTypeOfClaimantPage() {
    const now = moment();

    I.checkOption(this.locators.type_of_claimant_individual);
    I.selectOption(this.locators.title,'1: Mr');
    I.fillField(this.locators.first_name,'Joe');
    I.fillField(this.locators.last_name,'Bloggs');
    I.fillField(this.locators.date_of_birth_day, now.day());
    I.fillField(this.locators.date_of_birth_month, now.month());
    I.fillField(this.locators.date_of_birth_year, now.year());
    I.selectOption(this.locators.sex, '1: Male');
    I.selectOption(this.locators.gender_identity, '1: Yes');
    I.fillField(this.locators.gender_identity_description,'Test Gender');
    I.fillField(this.locators.claimant_phone_number, '07928621415');
    I.fillField(this.locators.claimant_alternative_number, '07928621415');
    I.fillField(this.locators.claimant_enter_uk_postcode, 'SS1 1AA');
    I.click(this.locators.find_address_button);
    I.wait(1);
    I.see('Select an address');
    I.selectOption(this.locators.claimant_select_an_address,'1: Object');
    I.fillField(this.locators.email_address, 'xxxx@test.com');
    I.selectOption(this.locators.contact_preference,'1: Email');
  },

  verifyCreateCaseRespondentsPage() {
    I.see('Respondents');
    I.click('Add new');
    I.wait(1);
    I.see('Name of respondent');
    I.see('Is there an ACAS Certificate number?');
    I.see('Phone number (Optional)');
    I.see('Has the ET3 form been received? (Optional)');
    I.see('Respondent Address');
    I.see('Enter a UK postcode');
    I.see('I can\'t enter a UK postcode');
  },

  inputCreateCaseRespondentsPage() {
    I.fillField(this.locators.name_of_respondent,'Respondent Name');
    I.checkOption(this.locators.is_there_an_acas_certificate_number_yes);
    I.fillField(this.locators.acas_certificate_number_input,'ACAS1234');
    I.fillField(this.locators.respondent_phone_number,'05909671016');
    I.checkOption(this.locators.et3_form_received_option_no);
    I.fillField(this.locators.respondent_enter_uk_postcode,'SS1 1AA');
    I.click(this.locators.find_address_button);
    I.wait(1);
    I.selectOption(this.locators.respondent_select_an_address,'1: Object');
  },

  verifyCreateCaseClaimantWorkAddressPage() {
    I.see('Create Case');
    I.see('Claimant Work Address');
    I.see('Enter a UK postcode');
    I.see('I can\'t enter a UK postcode');
    I.see('Phone number (Optional)');
  },

  inputCreateCaseClaimantWorkAddressPage() {
    I.fillField(this.locators.claimant_work_address_enter_a_postcode,'SS1 1AA');
    I.click(this.locators.find_address_button);
    I.selectOption(this.locators.claimant_work_address_select_an_address,'1: Object');
    I.fillField(this.locators.claimant_work_address_phone_number,'07315621019')
  },
  verifyCreateCasePremisesPage() {
    I.see('Premises');
    I.see('Premises (Optional)');
    I.see('Address');
    I.see('Enter a UK postcode');
    I.see('I can\'t enter a UK postcode');
  },

  inputCreateCasePremisesPage() {
    I.fillField(this.locators.premises_title,'Respondents Premises');
    I.fillField(this.locators.premises_enter_uk_postcode,'SS1 1AA');
    I.click(this.locators.find_address_button);
    I.selectOption(this.locators.premises_select_an_address,'1: Object');
  },

  verifyCreateCaseOtherDetailsPage() {
    I.see('Create Case');
    I.see('Other details');
    I.see('Occupation (Optional)');
    I.see('Employed from (Optional)');
    I.see('Day');I.see('Month');I.see('Year');
    I.see('Is the employment continuing? (Optional)');
    I.see('Are there any disabilities or special requirements? (Optional)');
  },

  inputCreateCaseOtherDetailsPage() {
    const now = moment();
    I.fillField(this.locators.other_details_claimant_occupation,'Test - Occupation');
    I.fillField(this.locators.claimant_employed_from_day,now.day());
    I.fillField(this.locators.claimant_employed_from_month,now.month());
    I.fillField(this.locators.claimant_employed_from_year,now.year());
    I.checkOption(this.locators.currently_employed);
    I.wait(1);
    I.see('Notice Period End Date (Optional)');
    I.fillField(this.locators.notice_period_end_date_day,now.day());
    I.fillField(this.locators.notice_period_end_date_month,now.month());
    I.fillField(this.locators.notice_period_end_date_year,now.year());
    I.checkOption(this.locators.any_disabilities_or_special_needs);
    I.wait(1);
    I.see('Notice Period End Date (Optional)');
    I.fillField(this.locators.disabilities_please_provide_details, 'Has a condition');
  },

  verifyCreateCaseHearingPreferencesPage() {
    I.see('Create Case');
    I.see('Claimant Hearing Preferences');
    I.see('What are the claimant\'s hearing preferences\n');
    I.see('Video');I.see('Phone');I.see('Neither');
  },

  inputCreateCaseHearingPreferencesPage() {
    I.checkOption(this.locators.hearing_preferences_neither);
    I.wait(1);
    I.see('Why is the claimant unable to take part in video or phone hearings');
    I.fillField(this.locators.why_cant_claimant_not_take_part, 'Because of a Learning Condition');
  },

  processCreateCaseInputPage ()  {
    this.verifyCreateCaseInputPage();
    this.inputCreateCaseDetailsPage();
    this.clickStartButton();
  },

  processCreateCaseDateOfReceiptPage() {
    this.verifyCreateCaseDateOfReceiptInputPage();
    this.inputCreateCaseDateOfReceiptInputPage();
    this.clickContinueButton();
  },

  processCreateCaseTypeOfClaimantPage() {
    this.verifyCreateCaseTypeOfClaimantPage();
    this.inputCreateCaseTypeOfClaimantPage();
    this.clickContinueButton();
  },

  processCreateCaseRespondentPage() {
    this.verifyCreateCaseRespondentsPage();
    this.inputCreateCaseRespondentsPage();
    this.clickContinueButton();
  },

  processClaimantWorkAddress() {
    I.see('Create Case');
    I.see('Is this the same as the claimant\'s work address?');
    I.checkOption(this.locators.claimants_work_address_question_no);
    this.clickContinueButton();
    this.verifyCreateCaseClaimantWorkAddressPage();
    this.inputCreateCaseClaimantWorkAddressPage();
    this.clickContinueButton();
  },

  processCreateCaseOtherDetailsPage() {
    this.verifyCreateCaseOtherDetailsPage();
    this.inputCreateCaseOtherDetailsPage();
    this.clickContinueButton();
  },

  processIsClaimantRepresented() {
    I.see('Create Case');
    I.see('Is the Claimant Represented?');
    I.checkOption(this.locators.is_the_claimant_represented);
    this.clickContinueButton();
  },

  processCreateCaseClaimantHearingPreferences() {
    this.verifyCreateCaseHearingPreferencesPage();
    this.inputCreateCaseHearingPreferencesPage()
    this.clickContinueButton();
  },
};
