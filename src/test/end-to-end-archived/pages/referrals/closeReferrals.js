const createReferralConfig = require('./createReferralsConfig.json');
const commonConfig = require('../../data/commonConfig.json');
const { I } = inject();

/*module.exports = async function() {
    await I.click(createReferralConfig.referals_tab);
    await I.click(createReferralConfig.reply_referral)
    await I.click(createReferralConfig.selectReferralToReply)
    await I.click(createReferralConfig.selectReferralToReply).at(1);
    await I.click(commonConfig.continueButton);
    await I.click(commonConfig.submit);
}
*/
