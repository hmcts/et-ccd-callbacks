const testConfig = require('../../../../config');
const { I } = inject();

function verifyIndividualClaimantDetails() {

    I.waitForText('Claimant Details', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Type of claimant');
    I.see('Is the claimant an individual or a company?');
    I.see('Individual');
    I.see('Company');
    I.see('Claimant Details');
    I.see('Title (Optional)');
    I.see('First Name');
    I.see('Last Name');
    I.see('Date of birth (Optional)');
    I.see('Day');I.see('Month');I.see('Year');
    I.see('Gender Identity (Optional)');
    I.see('Gender Identity description (Optional)');

    I.checkOption('#claimant_TypeOfClaimant-Individual');
    I.selectOption('#claimantIndType_claimant_preferred_title', '6: Other');
    I.see('Other title (Optional)');
    I.fillField('#claimantIndType_claimant_title_other','Commander');
    I.fillField('#claimantIndType_claimant_first_names' ,'Harbour');
    I.fillField('#claimantIndType_claimant_last_name' ,'Vikrant');
    I.fillField('#claimant_date_of_birth-day' ,'27');
    I.fillField('#claimant_date_of_birth-month' ,'02');
    I.fillField('#claimant_date_of_birth-year' ,'1986');
    I.selectOption('#claimantIndType_claimant_sex','2: Female');
    I.selectOption('#claimantIndType_claimant_gender_identity_same', '1: Yes');
    I.fillField('#claimantIndType_claimant_gender_identity','Transitional');
    I.click('Continue');

}
module.exports = { verifyIndividualClaimantDetails };
