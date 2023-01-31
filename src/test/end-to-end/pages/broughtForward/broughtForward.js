'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.click(commonConfig.addNewButton);
    I.waitForText(commonConfig.bfActionDescription, testConfig.TestTimeToWaitForText);
    I.selectOption('#bfActions_0_cwActions', commonConfig.bfActionDescription);

    I.fillField('#bfDate-day', commonConfig.bfDateDay);
    I.fillField('#bfDate-month', commonConfig.bfDateMonth);
    I.fillField('#bfDate-year', commonConfig.bfDateYear);
    I.click(commonConfig.continue);
    I.click(commonConfig.submit)
};
