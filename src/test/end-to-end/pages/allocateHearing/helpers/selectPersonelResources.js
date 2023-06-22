const testConfig = require('../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function selectPersonelResources() {
    const date = new Date();
    const formattedDate = date.toLocaleString('en-GB', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
    });
    let hearingDate = 'Hearing 2, '+ formattedDate + ' 00:00'
    I.waitForInvisible('.spinner-container', testConfig.TestTimeToWaitForText);
    I.waitForElement('#allocateHearingHearing', 20); //Wait for the page to be loaded and Elements enabled
    I.selectOption('#allocateHearingHearing', hearingDate);
    I.click(commonConfig.continue);
    I.waitForElement('#caseEditForm',20);
    I.see('Employment Judge (Optional)');
    I.see('Hearing Status (Optional)');
    I.see('Select Hearing Venue');
    I.see('Select Clerk (Optional)');
    I.selectOption('#allocateHearingJudge','1: A Judge');
    I.selectOption('#allocateHearingClerk','1: A Clerk');
    I.click(commonConfig.continue);
}
module.exports = { selectPersonelResources };
