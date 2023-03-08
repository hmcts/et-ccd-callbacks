'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    /*
       Sunny day scenario for ET3 Response journey.
    */
    const I = this;
    //page 1
    I.see('ET3 - Response to Employment tribunal claim (ET1)');
    await I.click(commonConfig.continue);

    //Page 2
    await I.waitForText("Is this the correct claimant for the claim you're responding to?", testConfig.TestTimeToWaitForText);
    await I.click("#et3ResponseIsClaimantNameCorrect_Yes");
    await I.click(commonConfig.continue);

    //Page 3
    await I.waitForText("What is the respondent's name?", testConfig.TestTimeToWaitForText);
    await I.fillField("#et3ResponseRespondentLegalName", "Annie Thomas");
    await I.click(commonConfig.continue);

    //Page 4
    await I.waitForText("Respondent address", testConfig.TestTimeToWaitForText);
    await I.fillField("#et3RespondentAddress_et3RespondentAddress_postcodeInput", "CF3 6XE");
    await I.click("#et3RespondentAddress_et3RespondentAddress_postcodeLookup > button")
    await I.waitForText("18 addresses found");
    await I.selectOption("#et3RespondentAddress_et3RespondentAddress_addressList","Five Oaks, Druidstone Road, Old St. Mellons, Caerdydd");
    await I.click(commonConfig.continue);

    //Page 5
    await I.waitForText("What is your contact phone number? (Optional)", testConfig.TestTimeToWaitForText);
    await I.fillField("#et3ResponsePhone", "07897867878");
    await I.click(commonConfig.continue);

    //Page 6
    await I.waitForText("How would you prefer to be contacted?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseContactPreference-Email");
    await I.click(commonConfig.continue);

    //Page 7
    await I.waitForText("Hearing format", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseHearingRepresentative-Phone\\ hearings");
    await I.checkOption("#et3ResponseHearingRespondent-Video\\ hearings");
    await I.click(commonConfig.continue);

    //Page 8
    await I.waitForText("Respondent's workforce", testConfig.TestTimeToWaitForText);
    await I.fillField("#et3ResponseEmploymentCount", "12");
    await I.click(commonConfig.continue);

    //Page 9
    await I.waitForText("Do you agree with the details given by the claimant about early conciliation with Acas?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseAcasAgree_Yes");
    await I.click(commonConfig.continue);

    //Page 10
    await I.waitForText("Are the dates of employment given by the claimant correct?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseAreDatesCorrect-Yes");
    await I.click(commonConfig.continue);

    //Page 11
    await I.waitForText("Is the claimant's employment with the respondent continuing?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseContinuingEmployment-Yes");
    await I.click(commonConfig.continue);

    //Page 12
    await I.waitForText("Is the claimant's description of their job or job title correct?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseIsJobTitleCorrect-Yes");
    await I.click(commonConfig.continue);

    //Page 13
    await I.waitForText("Are the claimant's total weekly work hours correct?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseClaimantWeeklyHours-Yes");
    await I.click(commonConfig.continue);

    //Page 14
    await I.waitForText("Are the earnings details given by the claimant correct?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseEarningDetailsCorrect-Yes");
    await I.click(commonConfig.continue);

    //Page 15
    await I.waitForText("Is the information given by the claimant correct about their notice?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseIsNoticeCorrect-Yes");
    await I.click(commonConfig.continue);

    //Page 16
    await I.waitForText("Are the details about pension and other benefits correct?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseIsPensionCorrect-Yes");
    await I.click(commonConfig.continue);

    //Page 17
    await I.waitForText("Does the respondent contest the claim?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseRespondentContestClaim-No");
    await I.click(commonConfig.continue);

    //Page 18
    await I.waitForText("Does the respondent wish to make an Employer's Contract Claim?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseEmployerClaim_No");
    await I.click(commonConfig.continue);

    //Page 19
    await I.waitForText("In the respondent party - are you aware of any physical, mental or learning disability or health conditions which requires support?", testConfig.TestTimeToWaitForText);
    await I.checkOption("#et3ResponseRespondentSupportNeeded-No");
    await I.click(commonConfig.continue);

    //Page 20
    await I.waitForText("Check your answers", testConfig.TestTimeToWaitForText);
};