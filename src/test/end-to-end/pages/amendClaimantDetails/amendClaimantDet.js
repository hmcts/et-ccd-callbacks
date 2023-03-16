'use strict';
const testConfig = require("../../../config");
const claimantIndividualDetails = require('./helper/claimantIndividualDetails');
const claimantContactDetails = require('./helper/claimantContactDetails');
const claimantWorkDetails = require('./helper/claimantWorkDetails');
const claimantOtherDetails = require('./helper/claimantOtherDetails');
const claimantHearingPreferences = require('./helper/claimantHearingPreferences');
const claimantDetails = require('./helper/claimantDetails');

module.exports = async function () {

    const I = this;
    await I.click(commonConfig.continue);
    await I.click(commonConfig.continue);
    await I.click(commonConfig.continue);
    await I.click(commonConfig.continue);
    await I.checkOption(claimDetailConfig.phoneHearingPreference);
    await I.click(claimDetailConfig.physicalConditionYes);
    await I.click(claimDetailConfig.contactLanguageEnglish);
    await I.click(claimDetailConfig.hearingLanguageEnglish);
    await I.click(commonConfig.continue);
    await I.click(commonConfig.submit)
    await I.wait(2);
};
