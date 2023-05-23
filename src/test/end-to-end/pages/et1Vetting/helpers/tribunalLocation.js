const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyTribunalLocation() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
    I.see('Tribunal location');
    I.see('Tribunal');
    I.see('England & Wales');
    I.see('Office');
    I.see('Leeds');
    I.see('Yes');
    I.see('No - suggest another location');
    I.click('#isLocationCorrect-Yes');
    I.fillField('#et1LocationGeneralNotes', 'General Notes for Tribunal Location');
    I.click(commonConfig.continue);
}

module.exports = { verifyTribunalLocation };
