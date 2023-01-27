const testConfig = require('./../../../config');
const commonConfig = require('../../data/commonConfig.json');
const { I } = inject();

function trackAllocation() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
    /* I.see('Track allocation');
     I.see('Open');*/ //How do we check for this Data piece...?
    I.see('Is the track allocation correct?');
    I.see('Yes');
    I.see('No - suggest another track');
    I.click('#isTrackAllocationCorrect-Yes');
    I.fillField('#trackAllocationGeneralNotes', 'General Notes for Track Allocation');
    I.click(commonConfig.continue);
}

module.exports = { trackAllocation };
