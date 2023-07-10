'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const amendRespondentRepresentative = require("./amendResponsdentRepresentative.json");

module.exports = async function (myHMMCTSOrganisation = false) {

    const I = this;
    I.see('Respondent Representative');
    I.see('Case Number:');
    I.see('Respondent Representative(s)');
    I.see('Respondent who is being represented');
    I.see('Name of representative');
    I.see('Phone number (Optional)');
    I.see('Alternative number (Optional)');
    I.see('Email address (Optional)');
    I.see('Contact preference (Optional)');
    I.see('Does the representative have a MyHMCTS account? (Optional)');

    I.selectOption('#repCollection_0_dynamic_resp_rep_name', commonConfig.respondentName);
    I.fillField('#repCollection_0_name_of_representative', commonConfig.respondentRepresentativeName);
    I.fillField('#repCollection_0_representative_phone_number', '01234567890');
    I.fillField('#repCollection_0_representative_mobile_number', '01234657895');
    I.fillField('#repCollection_0_representative_email_address', 'test.xxx@hmcts.net');
    I.selectOption('#repCollection_0_representative_preference', '1: Email');
    if (!myHMMCTSOrganisation) {
        I.checkOption('#repCollection_0_myHmctsYesNo_No');
        I.see('Name of Organisation (Optional)');
        I.see('Reference (Optional)');
        I.see('Occupation (Optional)');
        I.see('Address')
        I.see('Enter a UK postcode');
        I.fillField('#repCollection_0_name_of_organisation', 'Tester Organisation');
        I.wait(2);
        I.fillField('#repCollection_0_representative_reference', 'Tester Reference');
        I.selectOption('#repCollection_0_representative_occupation', 'Solicitor');
        I.wait(2);
        I.fillField('#repCollection_0_representative_address_representative_address_postcodeInput', 'BR1 4NN');
        I.click('Find address');
        I.wait(15);
        //I.waitForVisible('[name=\'address\']', testConfig.TestTimeToWaitForText);
        I.selectOption('[name=\'address\']', '15 Arcus Road, Bromley');
        I.waitForText('Postcode', testConfig.TestTimeToWaitForText);
        I.see('Building and Street');
        I.see('Address Line 2 (Optional)');
        I.see('Address Line 3 (Optional)');
        I.see('Town or City (Optional)');
        I.see('County (Optional)');
        I.see('Country (Optional)');
        I.seeInField('#repCollection_0_representative_address__detailAddressLine1', '15 Arcus Road');
        I.seeInField('#repCollection_0_representative_address__detailPostTown', 'Bromley');
        I.seeInField('#repCollection_0_representative_address__detailCountry', 'United Kingdom');
        I.seeInField('#repCollection_0_representative_address__detailPostCode', 'BR1 4NN');

        I.click(commonConfig.submit);

        I.click(commonConfig.submit);

        I.waitForText('has been updated with event: Respondent Representative', testConfig.TestTimeToWaitForText);

        I.click("//div[text()='Respondent Representative']");
        I.waitForText('Respondent Representative(s)', testConfig.TestTimeToWaitForText);
        I.see('Respondent Representative(s) 1');
        I.see('Respondent who is being represented');
        I.see(commonConfig.respondentName);
        I.see('Name of Representative');
        I.see(commonConfig.respondentRepresentativeName);
        I.see('Name of Organisation');
        I.see('ECMGita');
        I.see('Does the representative have a MyHMCTS account?');
        I.see('No');
        I.see('Reference');
        I.see('Tester Reference');
        I.see('Occupation');
        I.see('Solicitor');
        I.see('Address');
        I.see('Building and Street');
        I.see('15 Arcus Road');
        I.see('Town or City');
        I.see('Bromley');
        I.see('Postcode/Zipcode');
        I.see('BR1 4NN');
        I.see('Country');
        I.see('United Kingdom');
        I.see('Phone number');
        I.see('01234567890');
        I.see('Alternative number');
        I.see('01234657895');
        I.see('Email address');
        I.see('test.xxx@hmcts.net');
        I.see('Contact preference');
        I.see('Email');
    }

    //Click of the Respondent Representative is not working....So Further verification Code is not working...
    /*pause();
    I.clickLink('//div[contains(text(),\'Respondent Representative\')]');
    I.waitForText('Respondent Representative(s)', testConfig.TestTimeToWaitForText);
    I.see('Respondent Representative(s) 1');
    I.see('Respondent who is being represented');
    I.see('Mrs Test Auto');
    I.see('Name of Representative');
    I.see('ECMGita');
    I.see('Does the representative have a MyHMCTS account?');
    I.see('No');
    I.see('Address');
    I.see('Building and Street');
    I.see('15 Arcus Road');
    I.see('Town or City');
    I.see('Postcode/Zipcode');
    I.see('BR1 4NN');
    I.see('Country');
    I.see('United Kingdom');
    I.see('Phone number');
    I.see('01234567890');
    I.see('Alternative number');
    I.see('01234657890');
    I.see('test.xxx@hmcts.net');
    I.see('Contact preference');
    I.see('Email');
*/

    /* Code that is build but has to be tested for the Yes Option....
    I.checkOption('#repCollection_0_myHmctsYesNo_Yes');
    I.waitForText('Search for an organisation',testConfig.TestTimeToWaitForText);
    I.fillField('#search-org-text', 'ET Organisation 1');
    I.waitForElement('//a[contains(text(),\'Select\')]');
    I.click('//a[contains(text(),\'Select\')]');
    I.click('#content-why-can-not-find-organisation');
    I.see('If you know that the solicitor is already registered with MyHMCTS, check that you have entered their details correctly. Remember that organisations can only register one office address. This means that the details could be slightly different from what you\'re expecting. Contact the solicitor directly if you have any concerns.');
    I.click(commonConfig.continue);

    I.see('Respondent Representative');
    I.see('Case Number:');
    I.waitForElement(commonConfig.submit);
    I.click(commonConfig.submit);

    I.waitForNavigation();
    I.click('[tabindex=\'0\'] > .mat-tab-label-content');
    I.see('Respondent Representative(s)');
    I.see('Respondent Representative(s) 1');
    I.see('Respondent who is being represented');
    I.see('Mrs Test Auto');
    I.see('Name of Representative');
    I.see('ECMGita');
    I.see('Does the representative have a MyHMCTS account?');
    I.see('Yes');
    I.see('MyHMCTS Organisation');
    I.see('Name:');
    I.see('ET Organisation 1');
    I.see('Address:');
    I.see('15 Arcus Road');
    I.see('Bromley');
    I.see('BR1 4NN');
    I.see('Email address');
    I.see('test.xxx@hmcts.net');
    I.see('Contact preference');
    I.see('Email');*/
};
