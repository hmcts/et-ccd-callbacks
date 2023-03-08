const testConfig = require('../../../../config');
const { I } = inject();

function verifyClaimantHearingPreferences() {

    I.waitForText('Claimant Details', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    //I.see('Additional Claimant Information'); This is not coming up in automation - Possibly as such in Demo not deplyed into AAT yet.
    I.see('Additional Claimant Information');
    I.see('What are the claimant\'s hearing preferences');
    I.see('Video');
    I.see('Phone');
    I.see('Neither');
    I.checkOption('#claimantHearingPreference_hearing_preferences-Video');
    I.checkOption('#claimantHearingPreference_hearing_preferences-Phone');
    I.click('#claimantHearingPreference_reasonable_adjustments_Yes');
    I.click('#claimantHearingPreference_contact_language-English');
    I.click('#claimantHearingPreference_hearing_language-English');
    I.click('Continue');
}
module.exports = { verifyClaimantHearingPreferences };
