'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 2 ET1 Vetting journey - Min req info pg1 - Contact details.
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.see('Minimum required information');
    await I.checkOption('input[id="et1VettingCanServeClaimYesOrNo_Yes"]');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};