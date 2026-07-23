const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyReferalToAJudgeOrALegalOfficer() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Possible referral to a judge or legal officer');
    I.see('Case Number:');
    I.see('Possible referral to a judge or legal officer (Optional)');
    I.see('Does the claim include any of the following?');
    I.see('A claim of interim relief');
    I.see('A statutory appeal');
    I.see('An allegation of commission of sexual offence');
    I.see('Insolvency');
    I.see('Jurisdictions unclear');
    I.see('Potentially linked cases in the ECM');
    I.see('Rule 50 issues');
    I.see('Another reason for judicial referral');

    I.click('#referralToJudgeOrLOList-aClaimOfInterimRelief');
    I.click('#referralToJudgeOrLOList-aStatutoryAppeal');
    I.click('#referralToJudgeOrLOList-anAllegationOfCommissionOfSexualOffence');
    I.click('#referralToJudgeOrLOList-insolvency');
    I.click('#referralToJudgeOrLOList-jurisdictionsUnclear');
    I.click('#referralToJudgeOrLOList-lengthOfService');
    I.click('#referralToJudgeOrLOList-potentiallyLinkedCasesInTheEcm');
    I.click('[value=\'rule50Issues\']');
    I.click('#referralToJudgeOrLOList-anotherReasonForJudicialReferral');

    I.fillField('#aClaimOfInterimReliefTextArea','Internal Releief - Give Details Notes...');
    I.fillField('#aStatutoryAppealTextArea','Statutory Appeal - Give Details Notes...');
    I.fillField('#anAllegationOfCommissionOfSexualOffenceTextArea','Allegation Commission of Sexual Offence - Give Details Notes...');
    I.fillField('#insolvencyTextArea','Insolvency - Give Details Notes...');
    I.fillField('#jurisdictionsUnclearTextArea', 'Jurisdiction Unclear - Give Details Notes...');
    I.fillField('#lengthOfServiceTextArea', 'Length of Service - Give Details Notes...');
    I.fillField('#potentiallyLinkedCasesInTheEcmTextArea', 'Potentially Linked Cases - Give Details Notes...');
    I.fillField('#rule50IssuesTextArea','Rule 50 Issues -  Give Details Notes...');
    I.fillField('#anotherReasonForJudicialReferralTextArea','Another reason for Judicial Referall -  Give Details Notes...');
    I.fillField('[field_id=\'et1JudgeReferralGeneralNotes\'] .form-control','General Notes for Possible referral to a judge or legal officer');
    I.click(commonConfig.continue);

}
module.exports = { verifyReferalToAJudgeOrALegalOfficer };
