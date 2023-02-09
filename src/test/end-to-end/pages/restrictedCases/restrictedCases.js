'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.waitForText(commonConfig.requestedBy, testConfig.TestTimeToWaitForText);
    I.see('Restricted Reporting');
    I.see('Case Number:');
    I.see('Restricted Case');
    I.see('Requested By');
    I.see('Rule 50(3)(d) Applies');
    I.see('Yes');
    I.see('No');
    I.see('Date Ceased (Optional)');
    I.see('Day');
    I.see('Month');
    I.see('Year');
    I.see('Rule 50(3)(b) Applies');
    I.see('Yes');
    I.see('No');
    I.see('Start Date');
    I.see('Day');
    I.see('Month');
    I.see('Year');
    I.see('Excluded from Register');
    I.see('Deleted from Physical Register');
    I.see('Yes');
    I.see('No');
    I.see('Names not for public release (Optional)')
    I.selectOption('#restrictedReporting_dynamicRequestedBy', commonConfig.requestedBy);
    I.click('#restrictedReporting_imposed_Yes');
    I.fillField('#dateCeased-day', '08');
    I.fillField('#dateCeased-month', '07');
    I.fillField('#dateCeased-year', '2021');
    I.click('#restrictedReporting_rule503b_Yes');
    I.fillField('#startDate-day', commonConfig.rule503bStartDate);
    I.fillField('#startDate-month', commonConfig.rule503bStartDateMonth);
    I.fillField('#startDate-year', commonConfig.rule503bStartDateYear);
    I.waitForText(commonConfig.excludedFromRegister, testConfig.TestTimeToWaitForText);
    I.selectOption('#restrictedReporting_excludedRegister', commonConfig.excludedFromRegister);
    I.click('#restrictedReporting_deletedPhyRegister_Yes');
    I.fillField('#restrictedReporting_excludedNames', 'Not for Public Release')
    I.click(commonConfig.continue);

    I.waitForText('Restricted Reporting');
    I.see('Case Number');
    I.click(commonConfig.submit);
    
    I.waitForText('has been updated with event: Restricted Reporting');
    I.see('RULE 50(3)b - REPORTING');
    I.click("//div[text()='Restricted Reporting']");
    I.see('Restricted Case');
    I.see('Requested By');
    I.see('Both Parties');
    I.see('Date Ceased');
    I.see('8 Jul 2021');
    I.see('Rule 50(3)(d) Applies');
    I.see('Yes');
    I.see('Rule 50(3)(b) Applies');
    I.see('Yes');
    I.see('Excluded from Register');
    I.see('No');
    I.see('Start Date');
    I.see('10 Dec 2021');
    I.see('Deleted from Physical Register');
    I.see('Yes');
    I.see('Names not for public release');
    I.see('Not for Public Release');
};