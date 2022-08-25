'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 8 ET1 Vetting journey - Case Details Page 4
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.see('Case details');
    await I.checkOption('input[id="et1SuggestHearingVenue_No"]');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};