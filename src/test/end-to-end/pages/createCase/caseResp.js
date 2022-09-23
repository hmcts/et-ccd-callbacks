'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseResp = require('./caseRespondents.json');

module.exports =  async function () {
    const I = this;
    I.see('Respondents');
    I.click('Add new');
    I.wait(commonConfig.time_interval_1_second);
    I.see('Name of respondent');
    I.see('Is there an ACAS Certificate number?');
    I.see('Phone number (Optional)');
    I.see('Has the ET3 form been received? (Optional)');
    I.see('Respondent Address');
    I.see('Enter a UK postcode');
    I.see('I can\'t enter a UK postcode');

    I.fillField(caseResp.name_of_respondent,'Respondent Name');
    I.checkOption(caseResp.is_there_an_acas_certificate_number_yes);
    I.fillField(caseResp.acas_certificate_number_input,'ACAS1234');
    I.fillField(caseResp.respondent_phone_number,'05909671016');
    I.checkOption(caseResp.et3_form_received_option_no);
    I.fillField(caseResp.respondent_enter_uk_postcode,'YO18 7LT');
    I.click(caseResp.find_address_button);
    I.wait(1);
    I.selectOption(caseResp.respondent_select_an_address,'1: Object');

    I.navByClick('Continue');
}
