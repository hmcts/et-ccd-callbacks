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
    I.fillField(this.locators.date_of_receipt_day,moment().day());
    I.fillField(this.locators.date_of_receipt_month,moment().month());
    I.fillField(this.locators.date_of_receipt_year,moment().year());
  },

};
