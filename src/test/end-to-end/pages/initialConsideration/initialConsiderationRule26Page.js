'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    /*
      Page 2/3 initial consideration journey - Rule 26.
      Again you can perform assertion to check claimant/respondent if needed. Future validation checks.
    */
    const I = this;
    await I.fillField('#icReceiptET3FormIssues', 'Incorrect due date');
    await I.fillField('#icRespondentsNameIdentityIssues', 'Incorrect Spelling');
    await I.fillField('#icJurisdictionCodeIssues', 'Jurs codes correct');
    await I.fillField('#icApplicationIssues', 'No');
    await I.fillField('#icEmployersContractClaimIssues', 'No ECC issues');
    await I.fillField('#icClaimProspectIssues', 'No issues for mandatory field');
    await I.fillField('#icListingIssues', 'Wrong Listing date');
    await I.fillField('#icDdaDisabilityIssues', 'No');
    await I.fillField('#icOrderForFurtherInformation', 'Provide more documentation');
    await I.fillField('#icOtherIssuesOrFinalOrders', 'Final orders info');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);
};