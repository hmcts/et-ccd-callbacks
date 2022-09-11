'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseOtherDetails = require('./caseOtherDetailsConfig.json');
const moment = require('moment');

module.exports =  async function () {
    const I = this;
    I.see('Create Case');
    I.see('Other details');
    I.see('Occupation (Optional)');
    I.see('Employed from (Optional)');
    I.see('Day');I.see('Month');I.see('Year');
    I.see('Is the employment continuing? (Optional)');
    I.see('Are there any disabilities or special requirements? (Optional)');

    I.fillField(caseOtherDetails.other_details_claimant_occupation,'Test - Occupation');

    const currentDate = new Date();
    const day = currentDate.getDate();
    const month = currentDate.getMonth() + 1;
    const year = currentDate.getFullYear();
    I.fillField(caseOtherDetails.claimant_employed_from_day, day);
    I.fillField(caseOtherDetails.claimant_employed_from_month, month);
    I.fillField(caseOtherDetails.claimant_employed_from_year, year);
    I.checkOption(caseOtherDetails.currently_employed);

    I.wait(1);
    I.see('Notice Period End Date (Optional)');
    I.fillField(caseOtherDetails.notice_period_end_date_day,day);
    I.fillField(caseOtherDetails.notice_period_end_date_month,month);
    I.fillField(caseOtherDetails.notice_period_end_date_year,year);
    I.checkOption(caseOtherDetails.any_disabilities_or_special_needs);
    I.wait(1);
    I.see('Notice Period End Date (Optional)');
    I.fillField(caseOtherDetails.disabilities_please_provide_details, 'Has a condition');


    I.navByClick(commonConfig.continueButton);
}
