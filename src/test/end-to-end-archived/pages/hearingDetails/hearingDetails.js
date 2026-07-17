'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const { I } = inject();

module.exports = async function () {

    I.waitForElement('#hearingDetailsHearing',20);
    I.selectOption('#hearingDetailsHearing', 'Hearing 2');
    await I.click(commonConfig.continue);
    I.waitForElement('.write-collection-add-item__bottom');
    I.see('Listed');
    I.wait(2);
    I.click(commonConfig.continue);
    I.click(commonConfig.submit);
    await I.waitForEnabled({css: '#next-step'}, testConfig.TestTimeToWaitForText || 5);
};
