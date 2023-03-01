'use strict';

const commonConfig = require('../../data/commonConfig.json');
const et1Config = require('./et1ServingConfig.json');
const testConfig = require('./../../../config');

module.exports =  async function () {
    const I = this;
    I.waitForVisible(et1Config.et1Add_new_button, testConfig.TestTimeToWaitForText);
    I.see('ET1 serving');
    I.see('Upload documents');
    I.see('Case Number:');
    I.click(et1Config.et1Add_new_button);
    I.see('Type of Document');
    I.see('Document');
    I.see('Short Description');
    I.click(et1Config.et1Select_document_type);
    I.selectOption(et1Config.et1Select_document_type, '2.6 Notice of Claim');
    I.attachFile(et1Config.et1UploadET1ServingDocument, 'data/RET-1950_3.png');
    I.wait(5); // Hard Wait for the purpose of the Upload of Files....
    I.fillField(et1Config.et1ShortDescription, 'Description for the Notice of Claim');
    I.click(commonConfig.continueButton);

    I.waitForText('ET1 serving', testConfig.TestTimeToWaitForText);
    I.waitForVisible(commonConfig.continueButton, testConfig.TestTimeToWaitForText);
    I.see('Send documents');
    I.see('Case Number:');
    I.see('Print and send paper documents');
    I.see('Send documents by first class mail to:');
    I.see('Claimant');
    I.see('4 Little Meadows');
    I.see('Bradley');
    I.see('LL11 4AR');
    I.see('Respondent 1');
    I.see('78 East Wonford Hill');
    I.see('Exeter');
    I.see('EX1 3DD');
    I.click(commonConfig.continueButton);

    I.waitForText('ET1 serving', testConfig.TestTimeToWaitForText);
    I.waitForVisible(commonConfig.continueButton, testConfig.TestTimeToWaitForText);
    I.see('Email Acas');
    I.see('Case Number:');
    I.see('Email documents to Acas');
    I.see('Attach and send document PDFs to Acas at etsmail@acas.org.uk');
    I.see('Instructions for the content of the email are on the \'Sending general correspondence to Acas via email\' job card.');
    I.click(commonConfig.continueButton);

    I.waitForText('ET1 serving', testConfig.TestTimeToWaitForText);
    I.waitForVisible(commonConfig.continueButton, testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.click(commonConfig.submit);

    I.waitForText('ET1 serving', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Documents submitted');
    I.see('We have notified the following parties:');

    I.click('//button[contains(text(),\'Close and Return to case details\')]');
    I.waitForText('has been updated with event: ET1 serving');

    I.click("//div[text()='History']");
    I.waitForText('Event', testConfig.TestTimeToWaitForText);
    I.see('ET1 serving');
}
