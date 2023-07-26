'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function (clerkResponsible, casePosition, physicalLocation, conciliationTrack) {

    const I = this;
    I.waitForElement('#clerkResponsible',10);
    I.selectOption('#clerkResponsible', clerkResponsible);
    I.selectOption('#positionType', casePosition);
    I.selectOption('#fileLocation', physicalLocation);
    I.selectOption('#conciliationTrack', conciliationTrack);
    I.click(commonConfig.continue);
    I.click(commonConfig.continue);
    I.click(commonConfig.submit);
    I.waitForText(commonConfig.moveAcceptedCaseToCloseCaseErrorCheck,15);
    I.see(commonConfig.moveAcceptedCaseToCloseCaseErrorCheck);
};
