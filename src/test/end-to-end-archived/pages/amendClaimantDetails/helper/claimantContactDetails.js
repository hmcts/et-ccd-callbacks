const testConfig = require('../../../../config');
const { I } = inject();

function verifyClaimantContactDetails() {

    I.waitForText('Claimant Details', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Phone number (Optional)');
    I.see('Alternative number (Optional)');
    I.see('Email address (Optional)');
    I.see('Contact preference (Optional)');
    I.see('Address');

    I.fillField('#claimantType_claimant_phone_number','01234567890');
    I.fillField('#claimantType_claimant_mobile_number','01234567891');
    I.fillField('#claimantType_claimant_email_address','test.xxx@hcts.net');
    I.selectOption('#claimantType_claimant_contact_preference','1: Email');
    I.fillField('#claimantType_claimant_addressUK_claimant_addressUK_postcodeInput','SS1 1AA');
    I.click('Find address');
    I.wait(5);
    //I.waitForVisible('#claimantType_claimant_addressUK_claimant_addressUK_addressList',testConfig.TestTimeToWaitForText);
    I.selectOption('#claimantType_claimant_addressUK_claimant_addressUK_addressList','1: Object');
    I.see('Building and Street');
    I.see('Address Line 2 (Optional)');
    I.see('Address Line 3 (Optional)');
    I.see('Town or City (Optional)');
    I.see('County (Optional)');
    I.see('Country (Optional)');
    I.see('Postcode');

    I.seeInField('#claimantType_claimant_addressUK__detailAddressLine1','ROyaL Mail, Southend-on-sea M l o');
    I.seeInField('#claimantType_claimant_addressUK__detailAddressLine2','Short Street');
    I.seeInField('#claimantType_claimant_addressUK__detailPostTown','Southend-on-sea');
    I.seeInField('#claimantType_claimant_addressUK__detailCountry','United Kingdom');
    I.seeInField('#claimantType_claimant_addressUK__detailPostCode','SS1 1AA');
    I.click('Continue');
}
module.exports = { verifyClaimantContactDetails };
