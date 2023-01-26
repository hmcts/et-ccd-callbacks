'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");

module.exports = async function () {

    const I = this;
    I.see('List of correspondence items');
    I.see('Top Level');
    I.waitForText(commonConfig.lettersCorrespondence, testConfig.TestTimeToWaitForText);
    I.selectOption('#correspondenceType_topLevel_Documents', commonConfig.lettersCorrespondence);
    I.waitForText(commonConfig.lettersCorrespondence1, testConfig.TestTimeToWaitForText);
    I.selectOption('#correspondenceType_part_2_Documents', commonConfig.lettersCorrespondence1);
    I.see('Letters');
    I.click(commonConfig.continue);
    I.click(commonConfig.submit)
};
