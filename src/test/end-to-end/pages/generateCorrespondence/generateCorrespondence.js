'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    //Before Letters Page Starts
    I.waitForText('Letters', testConfig.TestTimeToWaitForText);
    I.see('Case Number:')
    I.see('List of correspondence items');
    I.see('Top Level');
    I.waitForText(commonConfig.lettersCorrespondence, testConfig.TestTimeToWaitForText);
    I.selectOption('#correspondenceType_topLevel_Documents', commonConfig.lettersCorrespondence);
    I.waitForText(commonConfig.lettersCorrespondence1, testConfig.TestTimeToWaitForText);
    I.selectOption('#correspondenceType_part_2_Documents', commonConfig.lettersCorrespondence1);
    I.see('Letters');

    I.click(commonConfig.continue);
    I.click(commonConfig.submit)

    //After submitting letters
    I.waitForText('Please download the document from : ', testConfig.TestTimeToWaitForText);
    I.click('.button')

    //Final Confirmation
    I.waitForText('has been updated with event: Letters', testConfig.TestTimeToWaitForText);
};
