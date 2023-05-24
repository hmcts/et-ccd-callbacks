'use strict';
const testConfig = require("../../../config");
const commonConfig = require('../../data/commonConfig.json');
module.exports = async function () {

    const I = this;
    I.waitForText('Has a reconsideration application been made? (Optional)', testConfig.TestTimeToWaitForText);
    I.see('Judgment');
    I.see('Case Number:');
    I.see('Non Hearing Judgment?');
    I.see('Judgment Type');
    I.see('Liability (Optional)');
    I.see('Jurisdiction');
    I.see('Date Judgment made');
    I.see('Day');
    I.see('Month');
    I.see('Year');
    I.see('Date Judgment sent');
    I.see('Judgment Notes (Optional)');
    I.see('Upload outcome of Judgment (Optional)');
    I.see('Judgment details');
    I.see('Reasons given (Optional)');
    I.see('Yes');
    I.see('No');
    I.see('Award made? (Optional)');
    I.see('Non-financial award (Optional)');
    I.see('Certificate of Correction Issued? (Optional)');
    I.see('Non-financial award (Optional)');
    I.see('Costs');
    I.see('Have costs been awarded? (Optional)');
    I.see('Reconsideration');
    I.see('Has a reconsideration application been made? (Optional)');

    I.click('#judgementCollection_0_non_hearing_judgment_No');
    let today = new Date().toLocaleDateString('en-GB', {
        day : 'numeric',
        month : 'short',
        year : 'numeric'
    }).split(' ').join(' ');
    let hearingOption = '1 : Preliminary Hearing - Hull Combined Court Centre - '+today
    I.selectOption('#judgementCollection_0_dynamicJudgementHearing', hearingOption);
    I.selectOption('#judgementCollection_0_judgement_type', commonConfig.judgmentType);
    I.selectOption('#judgementCollection_0_liability_optional', 'Liability');
    I.click('//div[@id=\'judgementCollection_0_jurisdictionCodes\']/div/button');
    I.selectOption('#judgementCollection_0_jurisdictionCodes_0_juridictionCodesList', commonConfig.jurisdictionCode);
    I.fillField('#date_judgment_made-day', commonConfig.judgmentMadeDate);
    I.fillField('#date_judgment_made-month', '10');
    I.fillField('#date_judgment_made-year', commonConfig.judgmentMadeYear);
    I.fillField('#date_judgment_sent-day', commonConfig.judgmentSentDate);
    I.fillField('#date_judgment_sent-month', commonConfig.judgmentSentMonth);
    I.fillField('#date_judgment_sent-year', commonConfig.judgmentSentYear);

    I.click('#judgementCollection_0_judgement_details_reasons_given_No');
    I.click('#judgementCollection_0_judgement_details_awardMade_No');
    I.click('Continue');
    I.waitForText('Judgment', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.click('Submit');
    I.waitForText('has been updated with event: Judgment', testConfig.TestTimeToWaitForText);

    //Verifying the Judgements Tab
    I.click("//div[text()='Judgments']");
    I.see('1');
    I.see('Jurisdiction');
    I.see('Jurisdiction 1');
    I.see('Jurisdiction Code');
    I.see('ADT');
    I.see('Discriminatory terms or rules');
    I.see('Non Hearing Judgment?');
    I.see('No');
    I.see('Judgment Type');
    I.see('Judgment');
    I.see('Liability');
    I.see('Date Judgment made');
    I.see('4 Oct 2022');
    I.see('Date Judgment sent');
    I.see('6 Jan 2022');
    I.dontSee('Judgment Notes');
    I.dontSee('fileUpload.txt');
    I.see('Judgment details');
    I.see('Reasons given');
    I.see('Award made?');
    I.dontSee('Financial award made?');
    I.dontSee('Remedy left to parties');
    I.dontSee('Certificate of Correction Issued?');
    I.dontSee('Costs');
    I.dontSee('Reconsideration');
};
