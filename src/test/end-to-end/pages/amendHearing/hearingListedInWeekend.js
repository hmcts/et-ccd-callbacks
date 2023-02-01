'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const {utilsComponent} = require("../../helpers/utils");

module.exports = async function (jurisdiction) {

    const I = this;
    I.waitForText(commonConfig.listHearing, testConfig.TestTimeToWaitForText);
    I.waitForText('Day', testConfig.TestTimeToWaitForText);
    I.fillField('#hearingCollection_0_hearingNumber', commonConfig.hearingNumber);
    I.selectOption('#hearingCollection_0_Hearing_type', commonConfig.hearingType);
    I.click('#hearingCollection_0_hearingFormat-Video');
    I.selectOption('#hearingCollection_0_Hearing_venue', jurisdiction);
    I.fillField('#hearingCollection_0_hearingEstLengthNum', commonConfig.hearingLength);
    I.selectOption('#hearingCollection_0_hearingEstLengthNumType', commonConfig.hearingLengthType);
    I.click('//input[@id=\'hearingCollection_0_hearingSitAlone-Sit Alone\']');
    I.click('//div[@id=\'hearingCollection_0_hearingDateCollection\']/div/button');

    let currentDate = await utilsComponent.isWeekend();
    I.fillField('#listedDate-day', currentDate.split('-')[2]);
    I.fillField('#listedDate-month', currentDate.split('-')[1]);
    I.fillField('#listedDate-year', currentDate.split('-')[0]);
    I.click(commonConfig.continue);
    I.waitForText(commonConfig.weekendHearingMsgError, testConfig.TestTimeToWaitForText);
};
