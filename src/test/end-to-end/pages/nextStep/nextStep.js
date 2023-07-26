'use strict';
const testConfig = require('../../../config');
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function (nextStep, webDriverWait) {

    const I = this;
    await I.waitForEnabled('#next-step',  30);
    await I.selectOption('#next-step', nextStep);
   I.wait(7);
    await I.doubleClick(commonConfig.goButton);
    I.wait(8);
};
