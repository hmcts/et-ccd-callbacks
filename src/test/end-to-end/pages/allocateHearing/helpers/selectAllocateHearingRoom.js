const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function selectAllocateHearingRoom() {

    I.waitForClickable('#allocateHearingRoom'); //Wait for the page to be loaded and Elements enabled
    I.see('Allocate Hearing');
    I.see('Case Number:');
    I.see('Select Room (Optional)');
    I.selectOption('#allocateHearingRoom', '1');
}
module.exports = { selectAllocateHearingRoom };
