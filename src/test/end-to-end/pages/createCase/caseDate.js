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

    const currentDate = new Date();
    const day = currentDate.getDate();
    const month = currentDate.getMonth() + 1;
    const year = currentDate.getFullYear();
    I.fillField(caseDateConfig.date_of_receipt_day, day);
    I.fillField(caseDateConfig.date_of_receipt_month, month);
    I.fillField(caseDateConfig.date_of_receipt_year, year);

    I.fillField(caseDateConfig.submission_reference,'123456789012');
    I.selectOption(caseDateConfig.tribunal_office,'3: London Central');

    I.navByClick(commonConfig.continueButton);
}
