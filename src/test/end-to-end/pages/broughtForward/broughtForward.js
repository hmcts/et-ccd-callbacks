'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    //I.wait();
    //Before BF Action Page Starts
    I.waitForText('B/F Action', testConfig.TestTimeToWaitForText);
    
    I.see('Case Number:')
    I.see('Add new');
    I.click(commonConfig.addNewButton);
    I.waitForText(commonConfig.bfActionDescription, testConfig.TestTimeToWaitForText);
    I.selectOption('#bfActions_0_cwActions', commonConfig.bfActionDescription);
    I.seeInField('Description', 'Enquiry letter issued');
    I.see('B/F Date');
    
    I.see('Day');
    I.see('Month');
    I.see('Year');
    I.fillField('#bfDate-day', commonConfig.bfDateDay);
    I.fillField('#bfDate-month', commonConfig.bfDateMonth);
    I.fillField('#bfDate-year', commonConfig.bfDateYear);
    I.seeInField('Day', '10');
    I.seeInField('Month', '11');
    I.seeInField('Year', '2021');
    
    I.see('Date Cleared (Optional)');
    I.see('Day');
    I.see('Month');
    I.see('Year');
  

    I.see('Comments (Optional)');
    I.seeInField('Comments', '');
   
    
    I.see('B/F Action');
    I.see('Case Number:')
    I.waitForText('Cancel', testConfig.TestTimeToWaitForText);

    I.click(commonConfig.continue);
    I.see('B/F Action');
    I.see('Case Number:');
    I.see('Previous');
    I.waitForText('Cancel', testConfig.TestTimeToWaitForText);

    I.click(commonConfig.submit);

    //Final Confirmation
    I.waitForText('has been updated with event: B/F Action', testConfig.TestTimeToWaitForText);
};
