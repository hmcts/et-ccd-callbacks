const testConfig = require('./../../../config');
const commonConfig = require('../../data/commonConfig.json');
const { I } = inject();

function verifyBeforeYouStartVetting() {
    I.waitForText('Check the Documents tab for additional ET1 documents the claimant may have uploaded.', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Before you start');
    I.see('Open these documents to help you complete this form:');
    //I.see('ET1 form (opens in new tab)'); //Does this Link Not always appear....?
    I.click(commonConfig.continue);
}

module.exports = { verifyBeforeYouStartVetting };
