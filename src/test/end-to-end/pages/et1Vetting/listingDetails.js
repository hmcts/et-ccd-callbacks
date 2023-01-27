const testConfig = require('./../../../config');
const commonConfig = require('../../data/commonConfig.json');
const { I } = inject();

function listingDetails() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case details');
    I.see('Case Number:');
    I.see('Listing details');
    I.see('Claimant');
    I.see('Contact address');
    I.see('4 Little Meadows');
    I.see('Bradley');
    I.see('LL11 4AR');
    I.see('Contact address');
    I.see('Work address');
    I.see('78 East Wonford Hill');
    I.see('Exeter')
    I.see('EX1 3DD');
    I.see('Respondent');
    I.see('Contact address');
    I.see('Do you want to suggest a hearing venue?');
    I.click('#et1SuggestHearingVenue_Yes');
    I.see('Leeds hearing venues');
    I.see('Hearing venue selected');
    I.selectOption('#et1HearingVenues',"2: Hull");
    I.fillField("#et1HearingVenues","General Notes for the Hearing Venues");
    I.click(commonConfig.continue);
}

module.exports = { listingDetails };
