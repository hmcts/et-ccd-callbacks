'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 10 ET1 Vetting journey - Possible referrals to judge or legal officer
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};