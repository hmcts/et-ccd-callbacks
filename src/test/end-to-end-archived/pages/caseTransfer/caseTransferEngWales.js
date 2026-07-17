'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.waitForText(commonConfig.caseTransfer, testConfig.TestTimeToWaitForText);
    I.see('Case Transfer (Eng/Wales)');
    I.see('Case Number:');
    I.see('Select the office you want to transfer the case to');
    I.selectOption('#officeCT', commonConfig.caseTransferOffice);
    I.fillField('#reasonForCT', 'Claimant lives near to the selected Jurisdiction');
    I.click(commonConfig.continue);
    I.see('Case Transfer (Eng/Wales)');
    I.see('Case Number:');
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see('Select the office you want to transfer the case to');
    I.see('Wales');
    I.see('Reason for Case Transfer');
    I.see('Claimant lives near to the selected Jurisdiction');
    I.click('Transfer Case');
    I.waitForText('has been updated with event: Case Transfer (Eng/Wales)');

    I.click("//div[text()='History']");
    I.see('Case Transfer (Eng/Wales)');
};
