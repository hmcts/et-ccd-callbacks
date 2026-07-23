'use strict';

const testConfig = require('./../../../config');
const commonConfig = require('../../data/commonConfig.json');
const et3NotificationConfig = require('./et3NotificationConfig.json');

module.exports =  async function () {
    const I = this;
    I.waitForVisible(et3NotificationConfig.addNew_et3_document_button, testConfig.TestTimeToWaitForText);
    I.see('ET3 notification');
    I.see('Upload documents');
    I.see('Case Number:');
    I.see('Upload document PDF');
    I.click(et3NotificationConfig.addNew_et3_document_button);
    I.see('Type of Document');
    I.see('Document');
    I.see('Short Description');
    I.click(et3NotificationConfig.select_et3_document_type_dropdown);
    I.selectOption(et3NotificationConfig.select_et3_document_type_dropdown, '4.18 Rule 26 referral to EJ - response received');
    I.attachFile(et3NotificationConfig.attach_et3_document, 'data/RET-1950_3.png');
    I.wait(5); // Hard Wait for the purpose of the Upload of Files....
    I.fillField(et3NotificationConfig.et3_short_description_of_document, 'Description for the Notice of Claim');
    I.click(commonConfig.continueButton);

    I.waitForVisible(commonConfig.continueButton, testConfig.TestTimeToWaitForText);
    I.waitForText('ET3 notification');
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

    I.waitForText('ET3 notification', testConfig.TestTimeToWaitForText);
    I.waitForVisible(commonConfig.continueButton, testConfig.TestTimeToWaitForText);
    I.see('Email Acas');
    I.see('Case Number:');
    I.see('Email documents to Acas');
    I.see('Attach and send document PDFs to Acas at ET3@acas.org.uk');
    I.see('Instructions for the content of the email are on the \'Sending general correspondence to Acas via email\' job card.');
    I.click(commonConfig.continueButton);

    I.waitForText('ET3 notification');
    I.see('Case Number:');
    I.see('Documents submitted');
    I.see('We have notified the following parties:');
    I.click('//button[contains(text(),\'Close and Return to case details\')]');
    
    I.waitForText('has been updated with event: ET3 notification');
    I.click("//div[text()='History']");
    I.waitForText('Event', testConfig.TestTimeToWaitForText);
    I.see('ET3 notification');

    //Looks like that there is not Document that is Updated on the Documents Section even if the Notification process had a document that was uploaded...
}
