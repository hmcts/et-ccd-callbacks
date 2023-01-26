'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    //Before Respondents Page Starts
    I.waitForText('Respondent Details'), testConfig.TestTimeToWaitForText;
    I.see('Case Number');
    I.see('Respondents');
    I.see('Name of respondent');
    //I.see('#respondentCollection_0_respondent_name')
    I.see('Is there an ACAS Certificate number? (Optional)');
    I.click('#respondentCollection_0_respondent_ACAS_question_Yes');
    I.see('Enter a UK postcode');
    I.see('Building and Street');
    I.see('Address Line 2 (Optional)');
    I.see('Address Line 3 (Optional)');
    I.see('Town or City (Optional)');
    I.see('County (Optional)');
    I.see('Country (Optional)');
    I.see('Postcode');
    I.see('Phone number (Optional)');
    I.see('Alternative number (Optional)');
    I.see('Email address (Optional)');
    I.see('Contact preference (Optional)');

    I.see('Is the claim against this Respondent continuing?')
    I.click('#respondentCollection_0_responseContinue_Yes');
    I.see('Has there been a request for an extension? (Optional)')
    I.click('#respondentCollection_0_extensionRequested_No');
    I.see('Has the ET3 form been received?')
    I.click('#respondentCollection_0_responseReceived_Yes');
    
    I.see('Response received date');
    I.fillField('#responseReceivedDate-day', commonConfig.caseAcceptedDay);
    I.fillField('#responseReceivedDate-month', commonConfig.caseAcceptedMonth);
    I.fillField('#responseReceivedDate-year', commonConfig.caseAcceptedYear);
    I.see('Response Struck Out (Optional)')
    I.click('#respondentCollection_0_responseStruckOut_No');

    I.fillField('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_postcodeInput', commonConfig.respondentPostCode);
    I.click('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_postcodeLookup > button');
    I.wait(2);
    I.waitForText(commonConfig.respondentAddress, testConfig.TestTimeToWaitForText);
    I.selectOption('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_addressList', commonConfig.respondentAddress);
    I.click(commonConfig.continue);
    I.click(commonConfig.submit)

    //Final Confirmation
    I.waitForText('has been updated with event: Respondent Details'), testConfig.TestTimeToWaitForText;
};
