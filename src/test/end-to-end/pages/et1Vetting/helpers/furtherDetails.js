const testConfig = require('../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyFurtherDetails() {

    //Further Questions Page
    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Further questions');
    I.see('Case Number:');
    I.see('Is the respondent a government agency or a major employer?');
    I.see('Are reasonable adjustments required?');
    I.see('Can the claimant attend a video hearing?');

    I.click('#et1GovOrMajorQuestion_Yes');
    I.click('#et1ReasonableAdjustmentsQuestion_Yes');
    I.click('#et1VideoHearingQuestion_No');
    I.see('Give details');
    I.fillField('#et1ReasonableAdjustmentsTextArea', 'Reasonable adjustments are required');
    I.fillField('#et1VideoHearingTextArea', 'Video Hearing Required');
    I.fillField('#et1FurtherQuestionsGeneralNotes', 'General Notes for Further Questions....');
    I.click(commonConfig.continue);
}

module.exports = { verifyFurtherDetails };
