'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.waitForText('Claimant Representative', testConfig.TestTimeToWaitForText);
    I.see('Case Number:')
    I.see('Is the Claimant Represented? (Optional)');
    I.see('Yes');
    I.see('No');
    I.click(commonConfig.claimantRepresentativeYes);

    I.see('Claimant Representative Details');
    I.see('Name of representative');
    I.see('Name of Organisation (Optional)');
    I.see('Occupation (Optional)');
    I.see('Reference (Optional)');
    I.see('Phone number (Optional)');
    I.see('Alternative number (Optional)');
    I.see('Email address (Optional)');
    I.see('Contact preference (Optional)');
    I.see('Address');
    I.see('Enter a UK postcode');

    I.fillField('#representativeClaimantType_name_of_representative', commonConfig.claimantRepresentativeName);
    I.fillField('Name of Organisation (Optional)', 'Organisation');
    I.selectOption('Occupation (Optional)', 'Solicitor');
    I.fillField('Reference (Optional)', 'ReferenceOptional');
    I.fillField('Phone number (Optional)', '07912345678');
    I.fillField('Alternative number (Optional)', '07998765432');
    I.fillField('Email address (Optional)', 'test@email.com');
    I.selectOption('Contact preference (Optional)', 'Email');
    
    I.fillField('#representativeClaimantType_representative_address_representative_address_postcodeInput', commonConfig.claimantRepresentativePostCode);
    I.click(commonConfig.findAddressButton);
    I.waitForText(commonConfig.claimantRepresentativeAddress, testConfig.TestTimeToWaitForText);
    I.selectOption('#representativeClaimantType_representative_address_representative_address_addressList', commonConfig.claimantRepresentativeAddress);
    I.click(commonConfig.continue);

    I.waitForText('Claimant Representative', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.click(commonConfig.submit);
    I.see('has been updated with event: Claimant Representative');

    I.click("//div[text()='Claimant Representative']");
    I.see('Claimant Representative Details');
    I.see('Name of Representative');
    I.see('Name of Organisation');
    I.see('Reference');
    I.see('Occupation');
    I.see('ECMRep1');
    I.see('Organisation');
    I.see('ReferenceOptional');
    I.see('Solicitor');

    I.see('Address');
    I.see('Building and Street');
    I.see('Address Line 2');
    I.see('Address Line 3');
    I.see('Town or City');
    I.see('Postcode/Zipcode');
    I.see('Country');
    I.see('Flat 13');
    I.see('Vermeer Court');
    I.see('1 Rembrandt Close');
    I.see('London');
    I.see('United Kingdom');

    I.see('Phone number');
    I.see('Alternative number');
    I.see('Email address');
    I.see('Contact preference');
    I.see('07912345678');
    I.see('07998765432');
    I.see('test@email.com');
    I.see('Email');
};
