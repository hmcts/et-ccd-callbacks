'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseClaimantWorkAddress = require('./caseClaimantWAConfig.json');

module.exports =  async function () {
    const I = this;
    I.see('Create Case');
    I.see('Claimant Work Address');
    I.see('Enter a UK postcode');
    I.see('I can\'t enter a UK postcode');
    I.see('Phone number (Optional)');

    I.fillField(caseClaimantWorkAddress.claimant_work_address_enter_a_postcode,'YO18 7LT');
    I.click(caseClaimantWorkAddress.find_address_button);
    I.wait(2);
    I.selectOption(caseClaimantWorkAddress.claimant_work_address_select_an_address,'1: Object');
    I.fillField(caseClaimantWorkAddress.claimant_work_address_phone_number,'07315621019');

    I.click(commonConfig.continueButton);
}
