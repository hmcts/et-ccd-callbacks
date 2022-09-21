'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 1 ET1 Vetting journey - Start Page.
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};