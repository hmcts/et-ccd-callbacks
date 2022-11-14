'use strict';
const commonConfig = require('../../data/commonConfig.json');
const {utilsComponent} = require("../../helpers/utils");

module.exports = async function (hearingDisposalDate) {

    const I = this;

    switch (hearingDisposalDate) {
        case "Date contained in hearing collection":
            let currentDate = await utilsComponent.getCurrentDay();
            currentDate.setDate(await currentDate.getCurrentDay() +4);
            console.log(currentDate);
            await I.fillField('#disposalDate-day', currentDate.split('-')[2]);
            await I.fillField('#disposalDate-month', currentDate.split('-')[1]);
            await I.fillField('#disposalDate-year', currentDate.split('-')[0]);
            await I.navByClick(commonConfig.continue);
            await I.click(commonConfig.submit);
            break;
        //error message displayed if date entered doesn't match hearing date
        case "Date NOT contained in hearing collection":
            await I.fillField('#disposalDate-day', '13');
            await I.fillField('#disposalDate-month', '11');
            await I.fillField('#disposalDate-year', '2022');
            await I.navByClick(commonConfig.continue);
            await I.see('Disposal Date must match one of the hearing dates for jurisdiction code ADT.');
            break;
        //for Non-hearing outcomes date can't be in the future -- these dates will be refactored
        case "Date in the future":
            await I.fillField('#disposalDate-day', '10');
            await I.fillField('#disposalDate-month', '10');
            await I.fillField('#disposalDate-year', '2025');
            await I.navByClick(commonConfig.continue);
            await I.see("Disposal Date can't be in the future for jurisdiction code ADT.")
            break;
        default:
            console.log("Error with date entered:" + hearingDisposalDate);
    }
    await I.click(commonConfig.cancelProcess);
};