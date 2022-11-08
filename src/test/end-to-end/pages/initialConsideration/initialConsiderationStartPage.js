'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 1/3 initial consideration journey - Start Page.
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    I.seeElement('.govuk-heading-l');
    I.seeElement('#caseEditForm');
    I.seeElement('a[.="Cancel"]');
    I.seeElement('button[@class="button"]');
    await I.navByClick(commonConfig.continue);
    I.wait(2);
};