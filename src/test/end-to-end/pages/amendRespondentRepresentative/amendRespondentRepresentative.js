'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const amendRespondentRepresentative = require("./amendResponsdentRepresentative.json");

module.exports = async function () {

    const I = this;
    await I.selectOption('#repCollection_0_dynamic_resp_rep_name', commonConfig.respondentName);
    await I.fillField('#repCollection_0_name_of_representative', commonConfig.respondentRepresentativeName);
    await I.checkOption(amendRespondentRepresentative.does_the_representative_have_an_account_no);
    I.wait(1);
    I.fillField(amendRespondentRepresentative.representative_work_address_enter_a_postcode,'YO18 7LT');
    I.click(amendRespondentRepresentative.find_address_button);
    I.wait(2);
    I.selectOption(amendRespondentRepresentative.representative_work_address_select_an_address,'1: Object');
    await I.navByClick(commonConfig.continue);
    await I.click(commonConfig.submit);
};
