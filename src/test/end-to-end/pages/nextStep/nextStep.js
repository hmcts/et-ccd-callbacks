'use strict';
const testConfig = require('../../config');
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function (nextStep, webDriverWait) {

    const I = this;

    await I.waitForEnabled({css: '#next-step'}, 30);
    await I.retry(3).selectOption('#next-step', nextStep);
    await I.waitForEnabled(commonConfig.goButton, 30);
    I.forceClick(commonConfig.goButton);
};
