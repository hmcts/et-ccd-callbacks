'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {
    const I = this;
    await I.navByClick(commonConfig.continue);
    await I.wait(2);


    await I.selectOption('#et3ChooseRespondent', '1: R: Initial Consideration Test Automation');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3IsThereAnEt3Response_No');
    await I.fillField('#et3NoEt3Response','Test');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3IsThereACompaniesHouseSearchDocument_No');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3IsThereAnIndividualSearchDocument_No');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3LegalIssue-No');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.fillField('#et3AdditionalInformation','Final notes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.navByClick('Cancel');
    await I.wait(2);
};