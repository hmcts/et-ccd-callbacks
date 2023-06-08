'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const {utilsComponent} = require("../../helpers/utils");

module.exports = async function (caseDisposed) {

    const I = this;
    I.waitForText(commonConfig.hearingDetails, testConfig.TestTimeToWaitForText);
    I.waitForElement('#hearingDetailsHearing',20);
    const date = new Date();
    const formattedDate = date.toLocaleString('en-GB', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
    });
    I.selectOption('#hearingDetailsHearing', 'Hearing 1, '+ formattedDate + ' 00:00');
    await I.click(commonConfig.continue);
    I.selectOption('#hearingDetailsStatus', 'Heard');
    await I.waitForElement('#hearingDetailsCaseDisposed_Yes');
    if (caseDisposed === 'Yes') {
        await I.checkOption('#hearingDetailsCaseDisposed_Yes')
    } else if (caseDisposed === 'No') {
        await I.checkOption('#hearingDetailsCaseDisposed_No')
    }
    let currentDate = await utilsComponent.getCurrentDay();
    await I.fillField('#hearingDetailsTimingStart-day', currentDate.split('-')[2]);
    await I.fillField('#hearingDetailsTimingStart-month', currentDate.split('-')[1]);
    await I.fillField('#hearingDetailsTimingStart-year', currentDate.split('-')[0]);
    //enter start time 1 hour before current
    await I.fillField('#hearingDetailsTimingStart-hour', currentDate.split('-')[3]-1);
    await I.fillField('#hearingDetailsTimingStart-minute', '00');
    await I.fillField('#hearingDetailsTimingStart-second', '00');
    await I.fillField('#hearingDetailsTimingFinish-day', currentDate.split('-')[2]);
    await I.fillField('#hearingDetailsTimingFinish-month', currentDate.split('-')[1]);
    await I.fillField('#hearingDetailsTimingFinish-year', currentDate.split('-')[0]);
    await I.fillField('#hearingDetailsTimingFinish-hour', currentDate.split('-')[3]);
    //enter finish time 1 minute before current
    await I.fillField('#hearingDetailsTimingFinish-minute',currentDate.split('-')[4]-1);
    await I.fillField('#hearingDetailsTimingFinish-second', '00');
    I.click(commonConfig.continue);
    I.wait(2);
    I.click(commonConfig.submit);
    await I.waitForEnabled({css: '#next-step'}, testConfig.TestTimeToWaitForText || 5);
};
