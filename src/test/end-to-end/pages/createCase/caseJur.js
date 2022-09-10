'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseJurConfig = require('./caseJurConfig.json');

module.exports =  async function () {
    const I = this;
    I.see('Create Case');
    I.see('Jurisdiction');
    I.see('Case type');
    I.see('Event');

    I.selectOption(caseJurConfig.jurisdiction, 'EMPLOYMENT');
    I.selectOption(caseJurConfig.case_type, 'ET_EnglandWales');
    I.selectOption(caseJurConfig.event, 'initiateCase');

    I.navByClick(commonConfig.start);
}
