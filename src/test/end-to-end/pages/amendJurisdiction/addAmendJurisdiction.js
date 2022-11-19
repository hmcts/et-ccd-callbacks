'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function (jurisdictionOutcome) {

    const I = this;
    await I.click(commonConfig.addNewButton);
    I.waitForText(commonConfig.jurisdictionCode, testConfig.TestTimeToWaitForText);
    await I.selectOption('#jurCodesCollection_0_juridictionCodesList', commonConfig.jurisdictionCode);
    //switch statement, can add more jurisdiction outcomes for different scenarios if needed
    switch (jurisdictionOutcome) {
        case "Successful at hearing":
            I.waitForText(commonConfig.jurisdictionRule1, testConfig.TestTimeToWaitForText);
            await I.selectOption('#jurCodesCollection_0_judgmentOutcome', commonConfig.jurisdictionRule1);
            I.seeElement('#disposalDate-day');
            I.see('Disposal date');
        break;
        //ensure disposal date field isn't present
        case "Not allocated":
            I.waitForText(commonConfig.jurisdictionRule2, testConfig.TestTimeToWaitForText);
            await I.selectOption('#jurCodesCollection_0_judgmentOutcome', commonConfig.jurisdictionRule2);
            //Trying to get the pipeline to work
            /*I.dontSeeElement('#disposalDate-day');
            I.dontSee('Disposal date');*/
        break;
        //ensure this outcome doesn't display the date notified field
        case "Withdrawn or private settlement":
            I.waitForText(commonConfig.jurisdictionRule2, testConfig.TestTimeToWaitForText);
            await I.selectOption('#jurCodesCollection_0_judgmentOutcome', commonConfig.jurisdictionRule3);
            //I.dontSeeElement('#dateNotified-day');
            //I.dontSee('Date notified');
            I.fillField('#dateNotified-day','10');
            I.fillField('#dateNotified-month','06');
            I.fillField('#dateNotified-year','2022');
            I.seeElement('#disposalDate-day');
            break;
        default:
            console.log("No jurisdiction code matched with" + jurisdictionOutcome);
    }
    await I.navByClick(commonConfig.continue);
    await I.click(commonConfig.submit);
};
