'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 3 ET1 Vetting journey - Min Req info pg2 - claimant/respondent details 
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.see('Minimum required information');
    await I.checkOption('input[id="et1VettingAcasCertIsYesOrNo1_Yes"]');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};