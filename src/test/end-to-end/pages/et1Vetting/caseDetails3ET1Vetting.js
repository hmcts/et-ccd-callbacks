'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 7 ET1 Vetting journey - Case Details Page 3
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.see('Case details');
    await I.checkOption('input[id="isLocationCorrect-Yes"]');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};