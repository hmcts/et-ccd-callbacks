'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const verifyJurisdictionTab = require("./helpers/verifyJurisdictionTab");

module.exports = async function (jurisdictionOutcome) {

    const I = this;
    I.waitForText('Jurisdiction',testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.waitForText(commonConfig.jurisdictionCode, testConfig.TestTimeToWaitForText);
    //switch statement, can add more jurisdiction outcomes for different scenarios if needed
    switch (jurisdictionOutcome) {
        case "Successful at hearing":
            I.selectOption('#jurCodesCollection_0_juridictionCodesList', commonConfig.jurisdictionCode);
            I.waitForText(commonConfig.jurisdictionRule1, testConfig.TestTimeToWaitForText);
            I.selectOption('#jurCodesCollection_0_judgmentOutcome', commonConfig.jurisdictionRule1);
            I.seeElement('#disposalDate-day');
            I.see('Disposal date');
        break;
        case "Not allocated":
            I.selectOption('#jurCodesCollection_0_juridictionCodesList', commonConfig.jurisdictionCode);
            I.waitForText("Discriminatory terms or rules", testConfig.TestTimeToWaitForText);
            I.see("Outcome (Optional)");
            I.selectOption('#jurCodesCollection_0_judgmentOutcome', commonConfig.jurisdictionRule2);
            I.click('Continue');
            I.waitForText('Jurisdiction',testConfig.TestTimeToWaitForText);
            I.click('Submit');
            I.see('has been updated with event: Jurisdiction');
            verifyJurisdictionTab.verifyJurisdictionTab(1, "ADT",
                "Discriminatory terms or rules","Not allocated");
            break;
        //ensure this outcome doesn't display the date notified field
        case "Withdrawn or private settlement":
            I.selectOption('#jurCodesCollection_0_juridictionCodesList', 'BOC');
            I.waitForText(commonConfig.jurisdictionRule2, testConfig.TestTimeToWaitForText);
            I.selectOption('#jurCodesCollection_0_judgmentOutcome', commonConfig.jurisdictionRule3);
            I.fillField('#disposalDate-day','10');
            I.fillField('#disposalDate-month','06');
            I.fillField('#disposalDate-year','2023');
            I.click('Continue');
            I.waitForText('Jurisdiction',testConfig.TestTimeToWaitForText);
            I.click('Submit');
            I.see('has been updated with event: Jurisdiction');
            verifyJurisdictionTab.verifyJurisdictionTab(1, "BOC",
                "(a) Claim of an employee for breach of contract of employment (b) Employer contract claim",
                "Withdrawn or private settlement", true);
            break;
        default:
            console.log("No jurisdiction code matched with" + jurisdictionOutcome);
            throw new Error("No jurisdiction code matched with" + jurisdictionOutcome);
    }
};
