'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../config");

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
    I.click(commonConfig.submit);

    I.waitForText('has been updated with event: Claimant Representative', testConfig.TestTimeToWaitForText);

    I.click("//div[text()='Claimant Representative']");
    I.waitForText('Claimant Representative Details', testConfig.TestTimeToWaitForText);
    I.see('Claimant Representative Details');
    I.see('Name of Representative');
    I.see('ECMRep1');
    I.see('Name of Organisation');
    I.see('Organisation');
    I.see('Reference');
    I.see('ReferenceOptional');
    I.see('Occupation');
    I.see('Solicitor');

    I.see('Address');
    I.see('Building and Street');
    I.see('Flat 13');
    I.see('Address Line 2');
    I.see('Vermeer Court');
    I.see('Address Line 3');
    I.see('1 Rembrandt Close');
    I.see('Town or City');
    I.see('London');
    I.see('Postcode/Zipcode');
    I.see('E14 3XA');
    I.see('Country');
    I.see('United Kingdom');

    I.see('Phone number');
    I.see('07912345678');
    I.see('Alternative number');
    I.see('07998765432');
    I.see('Email address');
    I.see('test@email.com');
    I.see('Contact preference');
    I.see('Email');
};
