const testConfig = require('../../../../config');
const {I} = inject();

function verifyCaseDetails() {

    I.waitForText('DO NOT POSTPONE', testConfig.TestTimeToWaitForText);
    //check case flags
    I.see('DO NOT POSTPONE');
    I.see('LIVE APPEAL');
    I.see('SENSITIVE');
    I.see('DIGITAL FILE');
    I.see('REASONABLE ADJUSTMENT');

    I.see('Claimant');
    I.see('Grayson Becker');

    I.see('Respondent')
    I.see('Mrs Test Auto');
    I.see('Case Status: Accepted');

    I.see('Tribunal Office');
    I.see('Leeds');
    I.see('Suggested hearing venue');
    I.see('Leeds');
    I.see('Current Position');
    I.see('Leeds');
    I.see('Physical Location');
    I.see('Casework Table');
    I.see('Clerk Responsible');
    I.see('A Clerk');
    I.see('Case Notes');
    I.see('Case notes section for case details');
    I.see('Single or Multiple');
    I.see('Single');
    I.see('Submission Reference');
    I.see('1675930663464973');
    I.see('Date of Receipt');
    I.see('17 Aug 2019');

    I.see('Case Accepted?');
    I.see('Yes');
    I.see('Date Accepted');
    I.see('18 Aug 2022');

    I.see('Target Hearing Date');
    I.see('13 Feb 2020');
    I.see('Conciliation Track');
    I.see('Open Track');

}

module.exports = {verifyCaseDetails};
