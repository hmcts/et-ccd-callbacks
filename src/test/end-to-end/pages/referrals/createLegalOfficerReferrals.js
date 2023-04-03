'use strict';
const commonConfig = require('../../data/commonConfig.json');
const createReferralConfig = require('./createReferralsConfig.json');
const testConfig = require("../../../config");

module.exports = async function() {
    const I = this;
    I.waitForText('Referral', testConfig.TestTimeToWaitForText);
    I.see('Refer to admin, legal officer or judge');
    I.see('Case Number:');
    I.see('Hearing details');
    I.see('Who are you referring this case to?');
    I.see('What is their email address?');
    I.see('Is this urgent?');
    I.see('What is the referral subject?');
    I.see('Give details of the referral');
    I.click(createReferralConfig.referCaseToAdmin);
    I.fillField('#referentEmail', 'et.caseworker.6@hmcts.net');
    I.click('Yes');
    I.selectOption('#referralSubject','ET1');
    I.fillField('#referralDetails', 'Test Referral Details');
    I.click(commonConfig.continueButton);
    
    I.waitForText('Referral');
    I.see('Case Number:');
    I.see('Check your answers');
    I.see('Check the information below carefully');
    I.see('Who are you referring this case to?');
    I.see('Admin');
    I.see('What is their email address?');
    I.see('et.caseworker.6@hmcts.net');
    I.see('Is this urgent?');
    I.see('Yes');
    I.see('What is the referral subject?');
    I.see('ET1');
    I.see('Give details of the referral');
    I.see('Test Referral Details');
    I.click(commonConfig.submit);

    I.waitForText('Referral');
    I.see('Case Number:');
    I.see('What happens next');
    I.see('Your referral has been sent. Replies and instructions will appear in the Referrals tab (opens in new tab).');
    I.click('Close and Return to case details');

    I.waitForText('has been updated with event: Referral', testConfig.TestTimeToWaitForText);
    I.click('#mat-tab-label-0-6'); ////div[text()='Referrals']"
    I.see('Referrals');
    I.click("[alt='image']");
    I.see('No');
    I.see('1');
    I.see('Subject');
    I.see('ET1');
    I.see('Email address');
    I.see('Details of the referral');
    I.see('Test Referral Details');
    I.see('Referral Document');
    I.see('Referral Summary.pdf');
}