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
    title : {xpath : '//select[@id=\'claimantIndType_claimant_title1\']'},
    first_name : {xpath : '//input[@id=\'claimantIndType_claimant_first_names\']'},
    last_name : {xpath : '//input[@id=\'claimantIndType_claimant_last_name\']'},
    date_of_birth_day : {xpath : '//input[@id=\'claimant_date_of_birth-day\']'},
    date_of_birth_month : {xpath : '//input[@id=\'claimant_date_of_birth-month\']'},
    date_of_birth_year : {xpath : '//input[@id=\'claimant_date_of_birth-year\']'},
    sex : {xpath : '//select[@id=\'claimantIndType_claimant_sex\']'},
    gender_identity : {xpath : '//input[@id=\'claimantIndType_claimant_gender_identity\']'},
    gender : {xpath : '//select[@id=\'claimantIndType_claimant_gender\']'},
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
    respondent_select_an_address : {xpath : '//select[@name=\'address\']'},
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
    I.see('Gender (Optional)');
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
    I.fillField(this.locators.gender_identity,'Test Gender');
    I.selectOption(this.locators.gender, '1: Male');
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
    I.fillField('Trial Respondent');
    I.selectOption(this.locators.is_there_an_acas_certificate_number_yes);
    I.fillField(this.locators.acas_certificate_number_input,'ACAS1234');
    I.fillField(this.locators.respondent_phone_number,'077030372385');
    I.selectOption(this.locators.et3_form_received_option_no);
    I.fillField(this.locators.respondent_enter_uk_postcode,'SS1 1AA');
    I.click(this.locators.find_address_button);
    I.selectOption(this.locators.respondent_select_an_address,'1: Object');

  }
};
