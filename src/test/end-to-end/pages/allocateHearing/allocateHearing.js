'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function (jurisdiction) {

    const I = this;
    I.waitForText(commonConfig.allocateHearing, testConfig.TestTimeToWaitForText);
    const date = new Date();
    switch(date.getDay()){
        case 0: //Sunday
            date.setDate(date.getDate() + 1);
            break;
        case 6: //Saturday
            date.setDate(date.getDate() + 2);
            break;
        default:
    }
    const formattedDate = date.toLocaleString('en-GB', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
    });
    I.selectOption('#allocateHearingHearing', 'Hearing 1, '+ formattedDate + ' 00:00');
    await I.navByClick(commonConfig.continue);
    if (jurisdiction === 'Leeds')
    {
        I.selectOption('#allocateHearingJudge', 'Leeds Judge 1');
        I.selectOption('#allocateHearingClerk', 'Leeds Clerk 1');
        await I.navByClick(commonConfig.continue);
        I.selectOption('#allocateHearingRoom', 'Leeds Magistrates');

    }
    if (jurisdiction === 'Manchester')
    {
        I.selectOption('#hearingCollection_0_hearingDateCollection_0_Hearing_room_M', 'Manchester');
    }
    await I.navByClick(commonConfig.continue);
    await I.click(commonConfig.submit);
    await I.waitForEnabled({css: '#next-step'}, testConfig.TestTimeToWaitForText || 5);
};
