const createReferralConfig = require('./createReferralsConfig.json');
const commonConfig = require('../../data/commonConfig.json');
const { I } = inject();

module.exports = async function(emailaddress, details) {
    await I.click(createReferralConfig.referals_tab);
    await I.click(createReferralConfig.create_new_referral);
    await I.click(createReferralConfig.referCaseToLegalOfficer);
    await I.fillField(createReferralConfig.referralEmail, emailaddress);
    await I.selectOption(createReferralConfig.selectYesIfUrgent);
    await I.click(createReferralConfig.refrerralSubjectDropdown)
    await I.click(createReferralConfig.refrerralSubjectDropdown).at(3);
    await I.fillField(createReferralConfig.referralDetails, details);
    await I.click(commonConfig.continueButton);
    await I.click(commonConfig.submit);
}