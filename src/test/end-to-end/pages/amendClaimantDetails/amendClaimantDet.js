'use strict';
const commonConfig = require('../../data/commonConfig.json');
const claimDetailConfig = require('./amendClaimantDetails.json')

module.exports = async function () {

    const I = this;
    await I.navByClick(commonConfig.continue);
    await I.navByClick(commonConfig.continue);
    await I.navByClick(commonConfig.continue);
    await I.navByClick(commonConfig.continue);
    await I.checkOption(claimDetailConfig.phoneHearingPreference);
    await I.navByClick(commonConfig.continue);
    await I.click(commonConfig.submit)
    await I.wait(2);
};
