'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const {utilsComponent} = require("../../helpers/utils");

module.exports = async function (jurisdiction) {

    const I = this;
    I.waitForText(commonConfig.listHearing, testConfig.TestTimeToWaitForText);
    //await I.click(commonConfig.addNewButton);
    await I.fillField('#hearingCollection_0_hearingNumber', commonConfig.hearingNumber);
    await I.selectOption('#hearingCollection_0_Hearing_type', commonConfig.hearingType);
    await I.click('#hearingCollection_0_hearingFormat-Video');
    await I.selectOption('#hearingCollection_0_Hearing_venue', jurisdiction);
    await I.fillField('#hearingCollection_0_hearingEstLengthNum', commonConfig.hearingLength);
    await I.selectOption('#hearingCollection_0_hearingEstLengthNumType', commonConfig.hearingLengthType);
    await I.click('//input[@id=\'hearingCollection_0_hearingSitAlone-Sit Alone\']');
    await I.click('//div[@id=\'hearingCollection_0_hearingDateCollection\']/div/button');

    const today = new Date();
    switch(today.getDay()){
        case 0: //Sunday
            today.setDate(today.getDate() + 1);
            break;
        case 6: //Saturday
            today.setDate(today.getDate() + 2);
            break;
        default:
    }
    await I.fillField('#listedDate-day', today.getDate());
    await I.fillField('#listedDate-month', today.getMonth() + 1);
    await I.fillField('#listedDate-year', today.getFullYear());

    I.click(commonConfig.continue);
    I.wait(2);
    I.click(commonConfig.submit);
    await I.waitForEnabled({css: '#next-step'}, testConfig.TestTimeToWaitForText || 5);
};
