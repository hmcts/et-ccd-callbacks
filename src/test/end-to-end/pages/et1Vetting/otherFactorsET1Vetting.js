'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 12 ET1 Vetting journey - Other Factors
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};