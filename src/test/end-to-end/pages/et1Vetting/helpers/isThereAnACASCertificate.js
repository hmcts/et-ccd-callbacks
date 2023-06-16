const testConfig = require('../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyIsThereAnACASCertificate() {

    //The minimum reqired information....
    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Minimum required information');
    //Claimant Section
    I.see('Claimant');
    I.see('First name');
    I.see('Last name');
    I.see('Contact address');
    I.see('Respondent 1');
    I.see('Name');
    I.see('Contact address');
    I.see('Acas certificate');
    I.see('Certificate number 15678 has been provided.');
    I.see('Is there an Acas certificate?');
    I.click('#et1VettingAcasCertIsYesOrNo1_Yes');
    I.click(commonConfig.continue);
}

module.exports = { verifyIsThereAnACASCertificate };
