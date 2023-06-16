const testConfig = require('../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyIncludeAnyOtherFactors() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Other factors');
    I.see('Case Number:');
    I.see('Does the claim include any other factors (Optional)');
    I.see('Select all that apply');
    I.see('The whole or any part of the claim is out of time');
    I.see('The claim is part of a multiple claim');
    I.see('The claim has PID jurisdiction and claimant wants it forwarded to relevant regulator - Box 10.1');
    I.see('The claimant prefers a video hearing');
    I.see('The claim has Rule 50 issues');
    I.see('The claim has other relevant factors for judicial referral');

    I.click('#otherReferralList-claimOutOfTime');
    I.click('#otherReferralList-multipleClaim');
    I.click('#otherReferralList-employmentStatusIssues');
    I.click('#otherReferralList-pidJurisdictionRegulator');
    I.click('#otherReferralList-videoHearingPreference');
    I.click('#otherReferralList-rule50IssuesOtherFactors');
    I.click('#otherReferralList-otherRelevantFactors');

    I.fillField('#claimOutOfTimeTextArea','Claim out of time - Give Details Notes...');
    I.fillField('#multipleClaimTextArea','Multiple - Give Details Notes...');
    I.fillField('#employmentStatusIssuesTextArea','Employment Status Issues - Give Details Notes.....');
    I.fillField('#pidJurisdictionRegulatorTextArea','PID Jurisdiction - Give Details Notes...');
    I.fillField('#videoHearingPreferenceTextArea', 'Video Hearing - Give Details Notes...');
    I.fillField('#rule50IssuesForOtherReferralTextArea', 'Rule 50 Issues - Give Details Notes...');
    I.fillField('#anotherReasonForOtherReferralTextArea', 'Another reason for other - Give Details Notes...');
    I.click(commonConfig.continue);

}
module.exports = { verifyIncludeAnyOtherFactors };
