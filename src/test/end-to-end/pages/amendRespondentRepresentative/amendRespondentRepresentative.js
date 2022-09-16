'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const amendRespondentRepresentative = require("./amendResponsdentRepresentative.json");

module.exports = async function () {

    const I = this;
    await I.selectOption('#repCollection_0_dynamic_resp_rep_name', commonConfig.respondentName);
    await I.fillField('#repCollection_0_name_of_representative', commonConfig.respondentRepresentativeName);
    await I.checkOption(amendRespondentRepresentative.doesTheRepresentativeHaveAnAccount);
    I.wait(1);

    await I.navByClick(commonConfig.continue);
    await I.click(commonConfig.submit);
};
