'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.waitForText(commonConfig.caseTransfer, testConfig.TestTimeToWaitForText);
    I.see('Case Transfer to ECM');
    I.see('Case Number:');
    I.see('Select office to transfer case to');
    I.selectOption('#ecmOfficeCT', commonConfig.caseTransferOffice);
    I.fillField('#reasonForCT', 'Claimant lives near to the selected Jurisdiction');
    I.click(commonConfig.continue);
    I.see('Case Transfer to ECM');
    I.see('Case Number:');
    I.click(commonConfig.submit);
    I.see('Case Transfer: Transferred to ECM');
    I.waitForText('has been updated with event: Case Transfer to ECM');

    I.click("//div[text()='History']");
    I.see('Case Transfer to ECM');
};
