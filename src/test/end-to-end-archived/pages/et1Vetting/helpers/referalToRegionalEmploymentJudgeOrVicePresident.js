const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyReferalToARegionalJudgeOrVicepresident() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Possible referral to Regional Employment Judge or Vice-President');
    I.see('Case Number:');
    I.see('Possible referral to Regional Employment Judge or Vice-President (Optional)');
    I.see('Does the claim include any of the following?');
    I.see('A claimant covered by vexatious litigant order');
    I.see('A national security issue');
    I.see('A part of national multiple / covered by Presidential case management order');
    I.see('A request for transfer to another ET region');
    I.see('A request for service abroad');
    I.see('A sensitive issue which may attract publicity or need early allocation to a specific judge');
    I.see('Any potential conflict involving judge, non-legal member or HMCTS staff member');
    I.see('Another reason for Regional Employment Judge / Vice-President referral');

    I.click('#referralToREJOrVPList-vexatiousLitigantOrder');
    I.click('#referralToREJOrVPList-aNationalSecurityIssue');
    I.click('#referralToREJOrVPList-nationalMultipleOrPresidentialOrder');
    I.click('#referralToREJOrVPList-transferToOtherRegion');
    I.click('#referralToREJOrVPList-serviceAbroad');
    I.click('#referralToREJOrVPList-aSensitiveIssue');
    I.click('#referralToREJOrVPList-anyPotentialConflict');
    I.click('#referralToREJOrVPList-anotherReasonREJOrVP');

    I.fillField('#vexatiousLitigantOrderTextArea','Vexatious Litigant - Give Details Notes...');
    I.fillField('#aNationalSecurityIssueTextArea','National Security Issue - Give Details Notes...');
    I.fillField('#nationalMultipleOrPresidentialOrderTextArea','National Multiple or Presidential Order - Give Details Notes.....');
    I.fillField('#transferToOtherRegionTextArea','Request for Transfer - Give Details Notes...');
    I.fillField('#serviceAbroadTextArea', 'Service Abroad - Give Details Notes...');
    I.fillField('#aSensitiveIssueTextArea', 'Sensitive Issue - Give Details Notes...');
    I.fillField('#anyPotentialConflictTextArea', 'Any potential conflict - Give Details Notes...');
    I.fillField('#anotherReasonREJOrVPTextArea','Another reason for Regional Employment Judge-  Give Details Notes...');
    I.fillField('#et1REJOrVPReferralGeneralNotes','General Notes - Possible referral to Regional Employment Judge or Vice-President');
    I.click(commonConfig.continue);
}
module.exports = { verifyReferalToARegionalJudgeOrVicepresident };
