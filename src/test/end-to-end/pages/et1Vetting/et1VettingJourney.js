'use strict';
const testConfig = require('./../../../config');
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    const I = this;
    //The start page for the ET1 Journey - Open your Documents page....
    I.waitForText('Check the Documents tab for additional ET1 documents the claimant may have uploaded.', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Before you start');
    I.see('Open these documents to help you complete this form:');
    //I.see('ET1 form (opens in new tab)'); //Does this Link Not always appear....?
    I.click(commonConfig.continue);

    //Can we serve the claim with the
    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Minimum required information');
    I.see('Contact Details');
    //Claimant Section
    I.see('Claimant');
    I.see('First name');
    I.see('Grayson');
    I.see('Last name');
    I.see('Becker');
    I.see('Contact address');
    I.see('4 Little Meadows');
    I.see('Bradley');
    I.see('LL11 4AR');
    //Respondent Section
    I.see('Respondent');
    I.see('Name');
    I.see('Mrs Test Auto');
    I.see('Contact address');
    I.see('78 East Wonford Hill');
    I.see('Exeter');
    I.see('EX1 3DD');
    //Other Section
    I.see('Can we serve the claim with these contact details?');

    I.click('#et1VettingCanServeClaimYesOrNo_Yes');
    I.fillField('#et1VettingCanServeClaimGeneralNote', 'ET1 Vetting can be served for this Customer...');
    I.click(commonConfig.continue);

    //The minimum reqired information....
    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Minimum required information');
    //Claimant Section
    I.see('Claimant');
    I.see('First name');
    I.see('Last name');
    I.see('Contact address');
    I.see('Respondent 1');
    I.see('Name');
    I.see('Contact address');
    I.see('Acas certificate');
    I.see('Certificate number 15678 has been provided.');
    I.see('Is there an Acas certificate?');
    I.click('#et1VettingAcasCertIsYesOrNo1_Yes');
    I.click(commonConfig.continue);

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Possible substantive defects (Optional)');
    I.see('Select all that apply. Does the claim, or part of it, appear to be a claim which:');
    I.see('The tribunal has no jurisdiction to consider - Rule 12(1)(a)');
    I.see('Is in a form which cannot sensibly be responded to or otherwise an abuse of process - Rule 12 (1)(b)');
    I.see('Has neither an EC number nor claims one of the EC exemptions - Rule 12 (1)(c)');
    I.see('States that one of the EC exceptions applies but it might not - Rule 12 (1)(d)');
    I.see('Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate - Rule 12 (1)(da)');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate - Rule 12 (1)(e)');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate - Rule 12 (1)(e)');
    I.see('Has a different respondent name on the ET1 to the respondent name on the Acas certificate - Rule 12 (1)(f)');
    I.see('General notes (Optional)')

    I.checkOption("[value='rule121a']");
    I.checkOption("[value='rule121b']");
    I.checkOption("[value='rule121c']");
    I.checkOption("[value='rule121d']");
    I.checkOption("[value='rule121 da'] ");
    I.checkOption("[value='rule121e']");
    I.checkOption("[value='rule121f']");

    I.see('The tribunal has no jurisdiction to consider');
    I.see('Is in a form which cannot sensibly be responded to or otherwise an abuse of process');
    I.see('Has neither an EC number nor claims one of the EC exemptions');
    I.see('States that one of the EC exceptions applies but it might not');
    I.see('Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate');
    I.see('Has a different respondent name on the ET1 to the respondent name on the Acas certificate');
    I.see('Give details');

    I.fillField('#rule121aTextArea', 'Rule 121 a - Give Details Text');
    I.fillField('#rule121bTextArea', 'Rule 121 b - Give Details Text');
    I.fillField('#rule121cTextArea', 'Rule 121 c - Give Details Text');
    I.fillField('#rule121dTextArea', 'Rule 121 d - Give Details Text');
    I.fillField('#rule121daTextArea', 'Rule 121 da - Give Details Text');
    I.fillField('#rule121eTextArea', 'Rule 121 e - Give Details Text');
    I.fillField('#rule121fTextArea', 'Rule 121 f - Give Details Text');
    I.fillField('#et1SubstantiveDefectsGeneralNotes',"General Notes for Possible substantive defects");
    I.click(commonConfig.continue);

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
    I.see('Are these codes correct?');
    I.see('Jurisdiction code (Optional)');

    I.click('#areTheseCodesCorrect_Yes');
    I.fillField('#et1JurisdictionCodeGeneralNotes','General Notes for Jurisdiction Codes');
    I.click(commonConfig.continue);

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
   /* I.see('Track allocation');
    I.see('Open');*/ //How do we check for this Data piece...?
    I.see('Is the track allocation correct?');
    I.see('Yes');
    I.see('No - suggest another track');
    I.click('#isTrackAllocationCorrect-Yes');
    I.fillField('#trackAllocationGeneralNotes', 'General Notes for Track Allocation');
    I.click(commonConfig.continue);

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
    I.see('Tribunal location');
    I.see('Tribunal');
    I.see('England & Wales');
    I.see('Office');
    I.see('Leeds');
    I.see('Yes');
    I.see('No - suggest another location');
    I.click('#isLocationCorrect-Yes');
    I.fillField('#et1LocationGeneralNotes', 'General Notes for Tribunal Location');
    I.click(commonConfig.continue);

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
    I.see('Listing details');
    I.see('Claimant');
    I.see('Contact address');
    I.see('4 Little Meadows');
    I.see('Bradley');
    I.see('LL11 4AR');
    I.see('Contact address');
    I.see('Work address');
    I.see('78 East Wonford Hill');
    I.see('Exeter')
    I.see('EX1 3DD');
    I.see('Respondent');
    I.see('Contact address');
    I.see('Do you want to suggest a hearing venue?');
    I.click('#et1SuggestHearingVenue_Yes');
    I.see('Leeds hearing venues');
    I.see('Hearing venue selected');
    I.selectOption('#et1HearingVenues',"2: Hull");
    I.fillField("#et1HearingVenues","General Notes for the Hearing Venues");
    I.click(commonConfig.continue);

    //Further Questions Page
    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Further questions');
    I.see('Case Number:');
    I.see('Is the respondent a government agency or a major employer?');
    I.see('Are reasonable adjustments required?');
    I.see('Can the claimant attend a video hearing?');

    I.click('#et1GovOrMajorQuestion_Yes');
    I.click('#et1ReasonableAdjustmentsQuestion_Yes');
    I.click('#et1VideoHearingQuestion_No');
    I.see('Give details');
    I.fillField('#et1ReasonableAdjustmentsTextArea', 'Reasonable adjustments are required');
    I.fillField('#et1VideoHearingTextArea', 'Video Hearing Required');
    I.fillField('#et1FurtherQuestionsGeneralNotes', 'General Notes for Further Questions....');
    I.click(commonConfig.continue);

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

    I.waitForText('Additional Information (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Final notes');
    I.see('Case Number:');
    I.fillField('#et1VettingAdditionalInformationTextArea', 'Vetting Additional Information - Give Details Notes...');
    I.click(commonConfig.continue);

    I.waitForText('Additional Information', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see('Contact Details');
    I.see('Can we serve the claim with these contact details?');
    I.see('Yes');
    I.see('General notes');
    I.see('ET1 Vetting can be served for this Customer...');
    I.see('Is there an Acas certificate?');
    I.see('Yes');
    I.see('Possible substantive defects');
    I.see('The tribunal has no jurisdiction to consider - Rule 12(1)(a)');
    I.see('Is in a form which cannot sensibly be responded to or otherwise an abuse of process - Rule 12 (1)(b)');
    I.see('Has neither an EC number nor claims one of the EC exemptions - Rule 12 (1)(c)');
    I.see('States that one of the EC exceptions applies but it might not - Rule 12 (1)(d)');
    I.see('Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate - Rule 12 (1)(da)');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate - Rule 12 (1)(e)');
    I.see('Has a different respondent name on the ET1 to the respondent name on the Acas certificate - Rule 12 (1)(f)');
    I.see('The tribunal has no jurisdiction to consider');
    I.see('Rule 121 a - Give Details Text');
    I.see('Is in a form which cannot sensibly be responded to or otherwise an abuse of process');
    I.see('Rule 121 b - Give Details Text');
    I.see('Has neither an EC number nor claims one of the EC exemptions');
    I.see('Rule 121 c - Give Details Text');
    I.see('States that one of the EC exceptions applies but it might not');
    I.see('Rule 121 d - Give Details Text');
    I.see('Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate');
    I.see('Rule 121 da - Give Details Text');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate');
    I.see('Rule 121 e - Give Details Text');
    I.see('Has a different respondent name on the ET1 to the respondent name on the Acas certificate');
    I.see('Rule 121 f - Give Details Text');
    I.see('General notes');
    I.see('General Notes for Possible substantive defects');
    I.see('Are these codes correct?');
    I.see('Yes');
    I.see('General Notes');
    I.see('General Notes for Jurisdiction Codes');
    I.see('Is the track allocation correct?');
    I.see('Yes');
    I.see('General Notes');
    I.see('General Notes for Track Allocation');
    I.see('Is this location correct?');
    I.see('Yes');
    I.see('General Notes');
    I.see('General Notes for Tribunal Location');
    I.see('Do you want to suggest a hearing venue?');
    I.see('Yes');
    I.see('Hearing venue selected');
    I.see('Hull');
    I.see('Is the respondent a government agency or a major employer?');
    I.see('Yes');
    I.see('Are reasonable adjustments required?');
    I.see('Yes');
    I.see('Give details');
    I.see('Reasonable adjustments are required');
    I.see('Can the claimant attend a video hearing?');
    I.see('No');
    I.see('Give details');
    I.see('Video Hearing Required');
    I.see('General notes');
    I.see('General Notes for Further Questions....');
    I.see('Possible referral to a judge or legal officer');
    I.see('A claim of interim relief');
    I.see('A statutory appeal');
    I.see('An allegation of commission of sexual offence');
    I.see('Insolvency');
    I.see('Jurisdictions unclear');
    I.see('Length of service');
    I.see('Potentially linked cases in the ECM');
    I.see('Rule 50 issues');
    I.see('Internal Releief - Give Details Notes...');
    I.see('Statutory Appeal - Give Details Notes...');
    I.see('Allegation Commission of Sexual Offence - Give Details Notes...');
    I.see('Insolvency - Give Details Notes...');
    I.see('Jurisdiction Unclear - Give Details Notes...');
    I.see('Length of Service - Give Details Notes...');
    I.see('Potentially Linked Cases - Give Details Notes...');
    I.see('Rule 50 Issues -  Give Details Notes...');
    I.see('Another reason for Judicial Referall -  Give Details Notes...');
    I.see('General Notes for Possible referral to a judge or legal officer');
    I.see('Possible referral to Regional Employment Judge or Vice-President');
    I.see('A claimant covered by vexatious litigant order');
    I.see('A national security issue');
    I.see('A part of national multiple / covered by Presidential case management order');
    I.see('A request for transfer to another ET region');
    I.see('A request for service abroad');
    I.see('A sensitive issue which may attract publicity or need early allocation to a specific judge');
    I.see('Any potential conflict involving judge, non-legal member or HMCTS staff member');
    I.see('Another reason for Regional Employment Judge / Vice-President referral');
    I.see('Another reason for Regional Employment Judge / Vice-President referral');
    I.see('Does the claim include any other factors');
    I.see('The whole or any part of the claim is out of time');
    I.see('The claim is part of a multiple claim');
    I.see('The claim has a potential issue about employment status');
    I.see('The claim has PID jurisdiction and claimant wants it forwarded to relevant regulator - Box 10.1');
    I.see('The claimant prefers a video hearing');
    I.see('The claim has Rule 50 issues');
    I.see('The claim has other relevant factors for judicial referral');
    I.see('Claim out of time - Give Details Notes...');
    I.see('Multiple - Give Details Notes...');
    I.see('Employment Status Issues - Give Details Notes.....');
    I.see('PID Jurisdiction - Give Details Notes...');
    I.see('Video Hearing - Give Details Notes...');
    I.see('Another reason for other - Give Details Notes...');
    I.see('Vetting Additional Information - Give Details Notes...');
    I.click('Submit');

    I.waitForText('You must accept or reject the case or refer the case.', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Do this next');
    I.click('Close and Return to case details');
    I.waitForText('Case Status: Vetted', testConfig.TestTimeToWaitForText);
};
