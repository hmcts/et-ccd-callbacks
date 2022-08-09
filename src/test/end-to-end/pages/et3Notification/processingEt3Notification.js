'use strict';

const commonConfig = require('../../data/commonConfig.json');
const et3NotificationConfig = require('./et3NotificationConfig.json');

module.exports =  async function () {
    const I = this;
    I.waitForVisible(et3NotificationConfig.addNew_et3_document_button, 15);
    I.see('Upload documents');
    await I.click(et3NotificationConfig.addNew_et3_document_button);
    await I.click(et3NotificationConfig.select_et3_document_type_dropdown);
    await I.selectOption(et3NotificationConfig.select_et3_document_type_dropdown, '4.18 Rule 26 referral to EJ - response received');
    await I.attachFile(et3NotificationConfig.attach_et3_document, 'data/RET-1950_3.png');
    I.wait(5)
    await I.fillField(et3NotificationConfig.et3_short_description_of_document, 'test');
    await I.click(commonConfig.continueButton);
    I.waitForVisible(commonConfig.continueButton, 15)
    I.see('Who are you sending this document to?');
    await I.selectOption(et3NotificationConfig.select_acas_option);
    await I.selectOption(et3NotificationConfig.select_claimant_option);
    await I.click(commonConfig.continueButton);
    I.see('Send documents');
    I.waitForVisible(commonConfig.continueButton, 15);
    I.see('Email documents to Acas');
    await I.click(commonConfig.continueButton);
    await I.click(commonConfig.cancelProcess);
}