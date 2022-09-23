'use strict';

const commonConfig = require('../../data/commonConfig.json');
const et1Config = require('./et1ServingConfig.json')

module.exports =  async function () {
    const I = this;
    I.waitForVisible(et1Config.et1Add_new_button, 15);
    I.see('Upload documents');
    await I.click(et1Config.et1Add_new_button);
    await I.click(et1Config.et1Select_document_type);
    await I.selectOption(et1Config.et1Select_document_type, '2.6 Notice of Claim');
    await I.attachFile(et1Config.et1UploadET1ServingDocument, 'data/RET-1950_3.png');
    I.wait(5)
    await I.fillField(et1Config.et1ShortDescription, 'blah');
    await I.click(commonConfig.continueButton);
    I.wait(2)
    I.waitForVisible(commonConfig.continueButton, 15)
    I.see('Send documents');
    await I.click(commonConfig.continueButton);
    I.waitForVisible(commonConfig.continueButton, 15);
    I.see('Email documents to Acas');
    await I.click(commonConfig.continueButton);
    I.waitForVisible(commonConfig.continueButton, 15);
    await I.click(commonConfig.submit);
}
