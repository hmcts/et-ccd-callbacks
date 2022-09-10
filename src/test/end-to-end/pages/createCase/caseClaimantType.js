'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseTypeOfClaimant = require('./caseTypeOfClaimant.json');
const moment = require('moment');

module.exports =  async function () {
    const I = this;
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

    const now = moment();

    I.checkOption(caseTypeOfClaimant.type_of_claimant_individual);
    I.selectOption(caseTypeOfClaimant.title,'1: Mr');
    I.fillField(caseTypeOfClaimant.first_name,'Joe');
    I.fillField(caseTypeOfClaimant.last_name,'Bloggs');
    I.fillField(caseTypeOfClaimant.date_of_birth_day, now.day());
    I.fillField(caseTypeOfClaimant.date_of_birth_month, now.month());
    I.fillField(caseTypeOfClaimant.date_of_birth_year, now.year());
    I.selectOption(caseTypeOfClaimant.sex, '1: Male');
    I.selectOption(caseTypeOfClaimant.gender_identity, '1: Yes');
    I.fillField(caseTypeOfClaimant.gender_identity_description,'Test Gender');
    I.fillField(caseTypeOfClaimant.claimant_phone_number, '07928621415');
    I.fillField(caseTypeOfClaimant.claimant_alternative_number, '07928621415');
    I.fillField(caseTypeOfClaimant.claimant_enter_uk_postcode, 'SS1 1AA');
    I.click(caseTypeOfClaimant.find_address_button);
    I.wait(1);
    I.see('Select an address');
    I.selectOption(caseTypeOfClaimant.claimant_select_an_address,'1: Object');
    I.fillField(caseTypeOfClaimant.email_address, 'xxxx@test.com');
    I.selectOption(caseTypeOfClaimant.contact_preference,'1: Email');

    I.navByClick(commonConfig.continueButton);
}
