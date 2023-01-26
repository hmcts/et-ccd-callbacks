'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.fillField('#responseReceivedDate-day', commonConfig.caseAcceptedDay);
    I.fillField('#responseReceivedDate-month', commonConfig.caseAcceptedMonth);
    I.fillField('#responseReceivedDate-year', commonConfig.caseAcceptedYear);
    I.fillField('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_postcodeInput', commonConfig.respondentPostCode);
    I.click('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_postcodeLookup > button');
    I.wait(2);
    I.waitForText(commonConfig.respondentAddress, testConfig.TestTimeToWaitForText);
    I.selectOption('#respondentCollection_0_responseRespondentAddress_responseRespondentAddress_addressList', commonConfig.respondentAddress);
    I.click(commonConfig.continue);
    I.click(commonConfig.submit)
};
