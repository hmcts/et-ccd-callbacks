'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseRepConfig = require('./caseRepConfig.json');

module.exports =  async function () {
    const I = this;
    I.see('Create Case');
    I.see('Is the Claimant Represented?');
    I.checkOption(caseRepConfig.is_the_claimant_represented);

    I.click(commonConfig.continueButton);
}
