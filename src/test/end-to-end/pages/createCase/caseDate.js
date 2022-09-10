'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseDateConfig = require('./caseDateOfReceipt.json');
const moment = require('moment');

module.exports =  async function () {
    const I = this;
    I.see('Create Case');

    I.see('Date of Receipt');
    I.see('Day');
    I.see('Month');
    I.see('Year');

    I.see('Submission Reference');
    I.see('Submission Reference (12 digit number)');
    I.see('Tribunal Office');

    const now = moment();
    I.fillField(caseDateConfig.date_of_receipt_day, now.day());
    I.fillField(caseDateConfig.date_of_receipt_month, now.month());
    I.fillField(caseDateConfig.date_of_receipt_year, now.year());

    I.fillField(caseDateConfig.submission_reference,'123456789012');
    I.selectOption(caseDateConfig.tribunal_office,'3: London Central');

    I.navByClick(commonConfig.continueButton);
}
