'use strict';
const commonConfig = require('../../data/commonConfig.json');
const claimDetailConfig = require('./amendClaimantDetails.json')

module.exports = async function () {

    const I = this;
    await I.click(commonConfig.continue);
    await I.click(commonConfig.continue);
    await I.click(commonConfig.continue);
    await I.click(commonConfig.continue);
    await I.checkOption(claimDetailConfig.phoneHearingPreference);
    await I.click(claimDetailConfig.physicalConditionNo);
    await I.click(claimDetailConfig.contactLanguageEnglish);
    await I.click(claimDetailConfig.hearingLanguageEnglish);
    await I.click(commonConfig.continue);
    await I.click(commonConfig.submit)
    await I.wait(2);
};
