const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function selectListedHearing() {

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
    I.waitForElement('#allocateHearingHearing',10);
    I.see('Allocate Hearing');
    I.see('Case Number:');
    I.selectOption('#allocateHearingHearing', 'Hearing 1, '+ formattedDate + ' 00:00');
    I.click(commonConfig.continue);

}
module.exports = { selectListedHearing };
