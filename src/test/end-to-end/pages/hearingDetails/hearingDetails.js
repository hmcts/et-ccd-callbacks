'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../config");

module.exports = async function () {

    const I = this;
    I.waitForText(commonConfig.hearingDetails, testConfig.TestTimeToWaitForText);
    I.waitForElement('#hearingDetailsHearing',20);
    I.selectOption('#hearingDetailsHearing', 'Hearing 2');
    await I.click(commonConfig.continue);
    I.waitForElement('.write-collection-add-item__bottom');
    I.see('Listed');
    I.wait(2);
    I.click('Continue');
    await I.waitForEnabled('#next-step', testConfig.TestTimeToWaitForText);
};
