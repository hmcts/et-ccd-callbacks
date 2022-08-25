'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 14 ET1 Vetting journey - Check your answers 
       Could perform some assertion to check page title or open document on this page, for now keep simple.
    */
    const I = this;
    await I.click(commonConfig.submit);
    await I.wait(5);
    await I.click(commonConfig.closeAndReturnToCaseDetailsButton);
    await I.wait(5);
};