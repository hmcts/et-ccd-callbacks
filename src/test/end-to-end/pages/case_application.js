'use strict';
const CCPBConstants = require('./common/constants');
const moment = require('moment');

const { I } = inject();

module.exports = {
  locators: {
    //Create Case Page
    case_number: { xpath: '//h1[starts-with(\'Case Number:\')]' },
    case_tab_details : {xpath: '//div[contains(text(),\'Case Details\')]'},
    claimant_tab_details : {xpath : '//div[contains(text(),\'Claimant\')]'},
    respondent_tab_details : {xpath : '//div[contains(text(),\'Respondent\')]'},
    referrals_tab_details : {xpath : '//div[contains(text(),\'Referrals\')]'},
    history_tab_details : {xpath : '//div[contains(text(),\'History\')]'}
  },

  async getCaseNumberValue() {
    const caseNumber = await I.grabTextFrom(this.locators.case_number);
    if (caseNumber.length !== 7) {
      throw new Error('The Case Number is not of proper format');
    }
  },

  verifyGeneralApplicationScreenValues() {
    I.see('has been created');
    I.see('Next step');
    I.see('Print');
  },

  inputCreateCaseDetailsPage() {
    I.selectOption(this.locators.jurisdiction, 'EMPLOYMENT');
    I.selectOption(this.locators.case_type, 'ET_EnglandWales');
    I.selectOption(this.locators.event, 'initiateCase');
  },


  verifyCaseDetailsTab() {
    I.click(this.locators.case_tab_details);

    I.see('Claimant');
    I.see('Joe Bloggs');
    I.see('Respondent');
    I.see('Respondent Name');
    I.see('Case Status: Submitted');
    I.see('Tribunal Office');
    I.see('London Central');
    I.see('Current Position');
    I.see('Manually created');
    I.see('Single or Multiple');
    I.see('Single');
    I.see('Submission Reference');
    I.see('Date of Receipt');
    I.see('Target Hearing Date');
  },

  verifyClaimantDetailsTab() {
    I.click(this.locators.claimant_tab_details);

    //Claimant Personal Details
    I.see('Claimant Details');
    I.see('First Name');
    I.see('Joe');
    I.see('Last Name');
    I.see('Bloggs');
    I.see('Date of birth');
    I.see('Sex');
    I.see('Male');
    I.see('Gender Identity description');

    //Address
    I.see('Test Gender');
    I.see('Address');
    I.see('Building and Street');
    I.see('ROyaL Mail, Southend-on-sea M l o');
    I.see('Address Line 2');
    I.see('Short Street');
    I.see('Town or City');
    I.see('Southend-on-sea');
    I.see('Postcode/Zipcode');
    I.see('SS1 1AA');
    I.see('Country');
    I.see('United Kingdom');
    I.see('Phone number');
    I.see('07928621415');
    I.see('Alternative number');
    I.see('07928621415');
    I.see('Email address');
    I.see('xxxx@test.com');
    I.see('Contact preference');
    I.see('Email');

    //Other details
    I.see('Other details');
    I.see('Employment Details');
    I.see('Occupation');
    I.see('Test - Occupation');
    I.see('Employed from');
    I.see('Is the employment continuing?');
    I.see('Notice Period End Date');
    I.see('Are there any disabilities or special requirements?');
    I.see('No');

    //Claimant Work Address
    I.see('Claimant Work Address');
    I.see('Building and Street');
    I.see('ROyaL Mail, Southend-on-sea M l o');
    I.see('Address Line 2');
    I.see('Short Street');
    I.see('Town or City');
    I.see('Southend-on-sea');
    I.see('Postcode/Zipcode');
    I.see('SS1 1AA');
    I.see('Country');
    I.see('United Kingdom');

    //Claimant Hearing Preferences
    I.see('What are the claimant\'s hearing preferences');
    I.see('Neither');
    I.see('Why is the claimant unable to take part in video or phone hearings');
    I.see('Has a condition');
  },

  verifyRespondentsTab() {
    I.click(this.locators.respondent_tab_details);
    I.see('Respondents');
    I.see('Name of respondent');
    I.see('Has the ET3 form been received?');
    I.see('Respondent Name');
    I.see('No');
  },

  verifyReferralsTab() {
    I.click(this.locators.referrals_tab_details);

    I.see('Referrals');
    I.see('Send a new referral');
    I.see('Reply to a referral');
    I.see('Close a referral');
  },

  verifyHistoryTab() {

    I.click(this.locators.history_tab_details);

    I.see('Details');
    I.see('Date');
    I.see('Author');
    I.see('End state');
    I.see('Event');
    I.see('Summary');
    I.see('Comment');
  }
};
