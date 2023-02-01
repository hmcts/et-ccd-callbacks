const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyCanWeServeTheClaim() {
    //Can we serve the claim with the
    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Minimum required information');
    I.see('Contact Details');
    //Claimant Section
    I.see('Claimant');
    I.see('First name');
    I.see('Grayson');
    I.see('Last name');
    I.see('Becker');
    I.see('Contact address');
    I.see('4 Little Meadows');
    I.see('Bradley');
    I.see('LL11 4AR');
    //Respondent Section
    I.see('Respondent');
    I.see('Name');
    I.see('Mrs Test Auto');
    I.see('Contact address');
    I.see('78 East Wonford Hill');
    I.see('Exeter');
    I.see('EX1 3DD');
    //Other Section
    I.see('Can we serve the claim with these contact details?');

    I.click('#et1VettingCanServeClaimYesOrNo_Yes');
    I.fillField('#et1VettingCanServeClaimGeneralNote', 'ET1 Vetting can be served for this Customer...');
    I.click(commonConfig.continue);
}

module.exports = { verifyCanWeServeTheClaim };
