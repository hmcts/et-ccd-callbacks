'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../config");
const {utilsComponent} = require("../../helpers/utils");

module.exports = async function (jurisdiction) {

    const I = this;
    I.waitForText('Day', testConfig.TestTimeToWaitForText); //End of the Page Loading Verification Check...
    I.see(commonConfig.listHearing);
    I.see('Case Number');
    I.see('Hearing number');
    I.see('Hearing type');
    //I.see('Public or Private? (Optional)');
    I.see('Hearing Format');
    I.see('Judicial Mediation (Optional)');
    I.see('Hearing Venue');
    I.see('Estimated hearing length');
    I.see('Days, Hours or Minutes');
    I.see('Sit Alone or Full Panel');
    I.see('Sit Alone');
    I.see('Full Panel');
    I.see('EQP Stage Hearing (Optional)');
    I.see('Hearing Notes (Optional)');
    I.see('Day');

    I.fillField('#hearingCollection_0_hearingNumber', commonConfig.hearingNumber);
    I.selectOption('#hearingCollection_0_Hearing_type', commonConfig.hearingType); //Can we make this Preliminary Hearing so that the next Optional Field can be input....

    I.click('//input[@value=\'In person\']'); //Using this Locator as the CSS has a Space in the name making the Tests to Fail...
    I.click('#hearingCollection_0_hearingFormat-Video');
    I.click('#hearingCollection_0_hearingFormat-Telephone');
    I.click('#hearingCollection_0_hearingFormat-Hybrid');
    I.click('#hearingCollection_0_judicialMediation_Yes');

    I.selectOption('#hearingCollection_0_Hearing_venue', jurisdiction);
    I.fillField('#hearingCollection_0_hearingEstLengthNum', commonConfig.hearingLength);
    I.selectOption('#hearingCollection_0_hearingEstLengthNumType', commonConfig.hearingLengthType);
    I.click('//input[@id=\'hearingCollection_0_hearingSitAlone-Sit Alone\']'); //Using this Locator as the CSS has a Space in the name making the Tests to Fail...
    I.selectOption('#hearingCollection_0_Hearing_stage', '1: Stage 1');
    I.fillField('#hearingCollection_0_Hearing_notes', 'The hearing should be help as soon as possible....');
    I.click('#hearingCollection_0_hearingDateCollection');
    //I.waitForElement('#hearingCollection_0_hearingDateCollection_0_Hearing_typeReadingDeliberation',10);
    //pause();

    // const today = new Date();
    // switch (today.getDay()) {
    //     case 0: //Sunday
    //         today.setDate(today.getDate() + 1);
    //         break;
    //     case 6: //Saturday
    //         today.setDate(today.getDate() + 2);
    //         break;
    //     default:
    // }
    // I.fillField('#listedDate-day', today.getDate());
    // I.fillField('#listedDate-month', today.getMonth() + 1);
    // I.fillField('#listedDate-year', today.getFullYear());

    I.click(commonConfig.submit);

    //Verifying the Hearings Tab.
    I.waitForText('has been updated with event: List Hearing', testConfig.TestTimeToWaitForText);
    I.click("//div[text()='Hearings']");
    I.see('Hearing type');
    I.see('Hearing');
    I.see('Hearing Venue');
    I.see('Leeds');
    I.click('[alt="image"]');
    I.waitForText('Hearing Format', testConfig.TestTimeToWaitForText);
    I.see('Telephone');
    I.see('Video');
    I.see('Hybrid');
    I.see('Judicial Mediation');
    I.see('Yes');
    I.see('Hearing Number');
    I.see('2');
    I.see('Estimated hearing length');
    I.see('1');
    I.see('Days, Hours or Minutes');
    I.see('Hours');
    I.see('Sit Alone or Full Panel');
    I.see('Sit Alone');
    I.see('EQP Stage Hearing');
    I.see('Stage 1');
    I.see('Hearing Notes');
    I.see('The hearing should be help as soon as possible....');
    I.see('Day');
    I.see('Day 1');
    I.see('Hearing Date');
    I.see('Hearing Status');
    I.see('Listed');
    I.see('Hearing Venue');
    I.see('Leeds');

    //Making Sure that no Personnel resources are allocated
    I.dontSee('Employment Judge');
    I.dontSee('A Judge');
    I.dontSee('Employee Member');
    I.dontSee('EE Member');
    I.dontSee('Clerk');
    I.dontSee('A Clerk');
};
