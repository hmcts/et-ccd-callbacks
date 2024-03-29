'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    //Before Letters Page Starts
    I.waitForText('Letters', testConfig.TestTimeToWaitForText);
    I.waitForText('Top Level', testConfig.TestTimeToWaitForText); 
    I.see('Case Number:')
    I.see('List of correspondence items');
    I.selectOption('#correspondenceType_topLevel_Documents', commonConfig.lettersCorrespondence);
    I.waitForElement('#correspondenceType_part_2_Documents',5);
    I.selectOption('#correspondenceType_part_2_Documents', commonConfig.lettersCorrespondence1);

    I.click(commonConfig.submit);
    I.click(commonConfig.submit);
    //After submitting letters
    I.waitForText('Letters', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Please download the document from : ');
    I.click('Close and Return to case details');

    //Final Confirmation
    I.waitForText('has been updated with event: Letters', testConfig.TestTimeToWaitForText);
};
