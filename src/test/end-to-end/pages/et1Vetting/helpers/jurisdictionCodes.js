const testConfig = require('../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyJurisdictionCodes() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
    I.see('Are these codes correct?');
    I.see('Jurisdiction code (Optional)');

    I.click('#areTheseCodesCorrect_Yes');
    I.fillField('#et1JurisdictionCodeGeneralNotes','General Notes for Jurisdiction Codes');
    I.click(commonConfig.continue);
}

module.exports = { verifyJurisdictionCodes };
