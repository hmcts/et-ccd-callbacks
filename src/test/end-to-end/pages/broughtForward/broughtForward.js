'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.waitForText('B/F Action', testConfig.TestTimeToWaitForText);
    
    I.see('Case Number:')
    I.see('Add new');
    I.click(commonConfig.addNewButton);

    I.waitForText(commonConfig.bfActionDescription, testConfig.TestTimeToWaitForText);
    I.see('B/F Date');
    I.see('Day');
    I.see('Month');
    I.see('Year');
    I.see('Date Cleared (Optional)');
    I.see('Comments (Optional)');

    I.selectOption('#bfActions_0_cwActions', commonConfig.bfActionDescription);
    I.fillField('#bfDate-day', commonConfig.bfDateDay);
    I.fillField('#bfDate-month', commonConfig.bfDateMonth);
    I.fillField('#bfDate-year', commonConfig.bfDateYear);

    I.fillField('#cleared-day', commonConfig.bfDateDay);
    I.fillField('#cleared-month', commonConfig.bfDateMonth);
    I.fillField('#cleared-year', commonConfig.bfDateYear);
    I.fillField('#bfActions_0_notes', 'Automated Testing Notes for BF Action');

    I.click(commonConfig.submit);

    //Final Confirmation
    I.waitForText('has been updated with event: B/F Action', testConfig.TestTimeToWaitForText);
    I.click("//div[text()='BF Actions']");
    I.see('B/F Date');
    I.click('[alt=\'image\']');
    I.waitForText('Date/Time', testConfig.TestTimeToWaitForText);
    I.see('Description');
    I.see('Enquiry letter issued')
    I.see('Date Cleared');
    I.see('Comments');
    I.see('Automated Testing Notes for BF Action');
};
