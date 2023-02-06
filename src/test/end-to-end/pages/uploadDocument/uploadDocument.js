'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    //Start of Upload document page
    I.waitForText('Upload Document', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Case documentation (Optional)');
    I.see('Add new');
    I.see('Upload documentation for the case');

    I.click('Add new');
    I.waitForText('Case documentation', testConfig.TestTimeToWaitForText);
    I.see('Type of Document (Optional)'); 
    I.see('Document (Optional)');
    I.see('Short Description (Optional)');
    I.selectOption('Type of Document (Optional)', 'ET1');    
    I.attachFile('#documentCollection_0_uploadedDocument', 'data/fileUpload.txt');
    I.fillField('#documentCollection_0_shortDescription', commonConfig.shortDescription);

    I.click(commonConfig.continue);
    I.see('Upload Document');
    I.see('Case Number:');
    I.click('Submit');

    I.see('has been updated with event: Upload Document');
    
    //Document Upload Confirmation
    I.click("//div[text()='Documents']");
    I.waitForText('Case documentation 1', testConfig.TestTimeToWaitForText);
    I.see('Document');
    I.see('fileUpload.txt');
    I.see('Short Description');
    I.see('Upload Document Check');
};
