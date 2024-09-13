'use strict';
const commonConfig = require('../../data/commonConfig.json');
const caseDetails = require("../amendCaseDetails/helper/caseDetails");
const testConfig = require("../../../config");

module.exports = async function (clerkResponsible, physicalLocation, suggestedHearingVenue) {

    const I = this;
    //navigate to the case details event and enter fields
    I.waitForText('Case Details', testConfig.TestTimeToWaitForText);
    I.waitForElement('#receiptDate-day', testConfig.TestTimeToWaitForText)
    I.selectOption('#clerkResponsible', clerkResponsible);
    I.selectOption('#fileLocation', physicalLocation);
    I.selectOption('#suggestedHearingVenues', suggestedHearingVenue);
    I.click(commonConfig.continue);
    //amendCaseDetails2 - second page
    I.waitForText('Case Notes', testConfig.TestTimeToWaitForText);
    I.fillField('#caseNotes', 'Case notes section for case details');
    I.checkOption('#additionalCaseInfo_additional_live_appeal_Yes');
    I.checkOption('#additionalCaseInfo_additional_sensitive_Yes');
    I.checkOption('#additionalCaseInfo_doNotPostpone_Yes');
    I.checkOption('#additionalCaseInfo_digitalFile_Yes');
    I.checkOption('#additionalCaseInfo_reasonableAdjustment_Yes');
    I.click(commonConfig.submit);

    I.click(commonConfig.submit);
    //Verify the Case Details tab
    //I.click("//div[text()='Case Details']");
    caseDetails.verifyCaseDetails();
};
