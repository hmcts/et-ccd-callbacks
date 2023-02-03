'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    const I = this;
    I.see('Upload Document');
    I.see('Case Number:');
    I.see('Case documentation (Optional)');
    I.see('Add new');
    I.see('Upload documentation for the case');

    I.click("//button[@class='button write-collection-add-item__top']");
    I.see('Case documentation');
    I.see('Type of Document (Optional)'); 
    I.see('Document (Optional)');      
    I.attachFile('#documentCollection_0_uploadedDocument', 'data/fileUpload.txt');
    I.wait(5);
    I.see('Short Description (Optional)');
    I.fillField('#documentCollection_0_shortDescription', commonConfig.shortDescription);

    I.click(commonConfig.continue);
    I.click("//button[@class='button']");
    I.seeElement("//div[@class='alert-message']");
    I.wait(5);
    
    I.click("//div[text()='Documents']");
    I.see('Case documentation 1');
    I.see('Document');
    I.see('Short Description');
    I.see('fileUpload.txt');
    I.see('Upload Document Check');
};
