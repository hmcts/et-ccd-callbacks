'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {
    const I = this;
    await I.navByClick(commonConfig.continue);
    await I.wait(2);


    await I.selectOption('#et3ChooseRespondent', '1: R: Initial Consideration Test Automation');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3IsThereAnEt3Response_Yes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3ResponseInTime_Yes');
    await I.fillField('#et3ResponseInTimeDetails','Received response in time');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3DoWeHaveRespondentsName_Yes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3DoesRespondentsNameMatch_Yes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3DoWeHaveRespondentsAddress_Yes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3DoesRespondentsAddressMatch_Yes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3ContestClaim-Yes');
    await I.fillField('#et3ContestClaimGiveDetails','Given details');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3ContractClaimSection7_Yes');
    await I.fillField('#et3ContractClaimSection7Details','Given details');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3IsCaseListedForHearing_Yes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3IsThisLocationCorrect-Yes');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.checkOption('#et3Rule26_Yes');
    await I.fillField('#et3Rule26Details','Given details');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.fillField('#et3AdditionalInformation','Submitting ET3 processing');
    await I.navByClick(commonConfig.continue);
    await I.wait(2);

    await I.navByClick('Cancel');
    await I.wait(2);
    
};