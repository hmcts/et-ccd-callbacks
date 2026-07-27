const testConfig = require('../../../../config');
const { I } = inject();

function verifyClaimantWorkDetails() {

    I.waitForText('Claimant Details', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Work phone number (Optional)');
    I.see('Claimant Work Address');
    I.see('Enter a UK postcode');
    I.fillField('#claimantWorkAddress_claimant_work_address_claimant_work_address_postcodeInput','SS1 1AA');
    I.click('Find address');
    I.wait(5);

    I.selectOption('#claimantWorkAddress_claimant_work_address_claimant_work_address_addressList','1: Object');
    I.fillField('#claimantWorkAddress_claimant_work_phone_number','01234567870');
    I.see('Building and Street (Optional)');
    I.see('Address Line 2 (Optional)');
    I.see('Address Line 3 (Optional)');
    I.see('Town or City (Optional)');
    I.see('County (Optional)');
    I.see('Country (Optional)');
    I.see('Postcode');
    I.seeInField('#claimantWorkAddress_claimant_work_address__detailAddressLine1','ROyaL Mail, Southend-on-sea M l o');
    I.seeInField('#claimantWorkAddress_claimant_work_address__detailAddressLine2','Short Street');
    I.seeInField('#claimantWorkAddress_claimant_work_address__detailAddressLine3','');
    I.seeInField('#claimantWorkAddress_claimant_work_address__detailPostTown','Southend-on-sea');
    I.seeInField('#claimantWorkAddress_claimant_work_address__detailCounty','');
    I.seeInField('#claimantWorkAddress_claimant_work_address__detailCountry','United Kingdom');
    I.seeInField('#claimantWorkAddress_claimant_work_address__detailPostCode','SS1 1AA');
    I.click('Continue');

}
module.exports = { verifyClaimantWorkDetails };
