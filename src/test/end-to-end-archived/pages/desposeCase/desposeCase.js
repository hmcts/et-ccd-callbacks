'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function (clerkResponsible, physicalLocation) {

    const I = this;
    I.waitForText('Close Case', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Current Position');I.see('Case closed');
    I.see('Clerk Responsible');
    I.see('Physical Location');
    I.see('Case Notes (Optional)');

    I.selectOption('#clerkResponsible', clerkResponsible);
    I.fillField('#fileLocation', physicalLocation);
    I.fillField('#caseNotes', 'Case notes are Optional');
    I.click('Continue');

    I.waitForInvisible('.spinner-container', testConfig.TestTimeToWaitForText);
    I.waitForText('Close Case', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Check your answers');
    I.see('Check the information below carefully.');
    I.see('Current Position');
    I.see('Case closed');
    I.see('Clerk Responsible');
    I.see(clerkResponsible);
    I.see('Physical Location');
    I.see(physicalLocation);
    I.see('Case Notes');
    I.see('Case notes are Optional');
    I.click('Submit');

    I.waitForInvisible('.spinner-container', testConfig.TestTimeToWaitForText);
    //Verify Case Details with Case
    I.see('has been updated with event: Close Case');
    I.see('Case Status: Closed');
    I.see('Current Position');
    I.see('Case closed');
    I.see('Physical Location');
    I.see('Casework Table');
    I.see('Clerk Responsible');
    I.see('A Clerk');
    I.see('Case Notes');
    I.see('Case notes are Optional');
};
