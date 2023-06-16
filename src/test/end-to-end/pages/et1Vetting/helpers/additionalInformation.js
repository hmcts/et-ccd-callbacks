const testConfig = require('../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyAdditionalInformation() {

    I.waitForText('Additional Information (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Final notes');
    I.see('Case Number:');
    I.fillField('#et1VettingAdditionalInformationTextArea', 'Vetting Additional Information - Give Details Notes...');
    I.click(commonConfig.continue);

}
module.exports = { verifyAdditionalInformation };
