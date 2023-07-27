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
    I.wait(commonConfig.time_interval_5_seconds); //Wait for 5 Seconds for the File Upload to take place and complete before proceeding otherwise other issues would come up...
    I.fillField('#documentCollection_0_shortDescription', commonConfig.shortDescription);

    I.click(commonConfig.submit);

    I.click(commonConfig.submit);

    //Wait for Case Details to correctly load
    I.waitForInvisible('.spinner-container', testConfig.TestTimeToWaitForText);
    I.waitForElement('#case-viewer-control-print', testConfig.TestTimeToWaitForText);
    I.see('has been updated with event: Upload Document');

    //Document Upload Confirmation
    I.click("//div[text()='Documents']");

    I.waitForText('Upload Document Check', testConfig.TestTimeToWaitForText);
    I.see('Case documentation 1');
    I.see('Document');
    I.see('fileUpload.txt');
    I.see('Short Description');
};
