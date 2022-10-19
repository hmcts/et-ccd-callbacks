'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseRepConfig = require('./caseRepConfig.json');

module.exports =  async function () {
    const I = this;
    I.see('Create Case');
    I.see('Claimant Hearing Preferences');
    I.see('What are the claimant\'s hearing preferences\n');
    I.see('Video');I.see('Phone');I.see('Neither');

    I.checkOption(caseRepConfig.hearing_preferences_neither);
    I.wait(commonConfig.time_interval_1_second);
    I.see('Why is the claimant unable to take part in video or phone hearings');
    I.fillField(caseRepConfig.why_cant_claimant_not_take_part, 'Because of a Learning Condition');

    I.navByClick('Continue');
}
