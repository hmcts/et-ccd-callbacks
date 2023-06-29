'use strict';
const testConfig = require("../../../config");
const claimantIndividualDetails = require('./helper/claimantIndividualDetails');
const claimantContactDetails = require('./helper/claimantContactDetails');
const claimantWorkDetails = require('./helper/claimantWorkDetails');
const claimantOtherDetails = require('./helper/claimantOtherDetails');
const claimantHearingPreferences = require('./helper/claimantHearingPreferences');
const claimantDetails = require('./helper/claimantDetails');

module.exports = async function () {

    const I = this;
    claimantIndividualDetails.verifyIndividualClaimantDetails();
    claimantContactDetails.verifyClaimantContactDetails();
    claimantWorkDetails.verifyClaimantWorkDetails();
    claimantOtherDetails.verifyClaimantOtherDetails();
    claimantHearingPreferences.verifyClaimantHearingPreferences();
    I.waitForText('has been updated with event: Claimant Details');

    //Verify the CYA page...
    I.click("//div[text()='Claimant']");
    claimantDetails.verifyClaimantDetails();

};
