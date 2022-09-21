'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 9 ET1 Vetting journey - Further Questions 
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.see('Further questions');
    await I.checkOption('input[id="et1GovOrMajorQuestion_Yes"]');
    await I.checkOption('input[id="et1ReasonableAdjustmentsQuestion_No"]');
    await I.checkOption('input[id="et1VideoHearingQuestion_Yes"]');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};