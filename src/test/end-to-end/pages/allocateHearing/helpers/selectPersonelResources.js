const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function selectPersonelResources() {

    I.waitForInvisible('.spinner-container', testConfig.TestTimeToWaitForText);
    I.waitForElement('#allocateHearingClerk',10); //Wait for the page to be loaded and Elements enabled
    I.see('Allocate Hearing');
    I.see('Case Number:');
    I.see('Sit Alone or Full Panel');
    I.see('Employment Judge (Optional)');
    I.see('Employer Member (Optional)');
    I.see('EmployeeMember (Optional)');
    I.see('Hearing Status (Optional)');
    I.see('Select Hearing Venue');
    I.see('Select Clerk (Optional)');
    I.selectOption('#allocateHearingJudge', 'A Judge');
    I.selectOption('#allocateHearingClerk', 'ER Member');
    I.selectOption('#allocateHearingEmployeeMember', 'EE Member');
    I.selectOption('#allocateHearingClerk', 'A Clerk');
    I.click(commonConfig.continue);
}
module.exports = { selectPersonelResources };
