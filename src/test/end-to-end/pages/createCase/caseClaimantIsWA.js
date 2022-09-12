'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseClaimantWorkAddress = require('./caseClaimantWAConfig.json');

module.exports =  async function () {
    const I = this;
    I.see('Create Case');
    I.see('Is this the same as the claimant\'s work address?');
    I.checkOption(caseClaimantWorkAddress.claimants_work_address_question_no);

    I.navByClick('Continue');
}
