'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
       Page 3/3 initial consideration journey - Check your answers Page.
       For future select the change link.
    */
    const I = this;
    await I.navByClick(commonConfig.submit);
    await I.wait(2);
};