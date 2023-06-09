'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function (clerkResponsible, casePosition, physicalLocation, conciliationTrack) {

    const I = this;
    I.selectOption('#clerkResponsible', clerkResponsible);
    I.selectOption('#positionType', casePosition);
    I.selectOption('#fileLocation', physicalLocation);
    I.selectOption('#conciliationTrack', conciliationTrack);
    I.click(commonConfig.continue);
    I.click(commonConfig.continue);
    I.see(commonConfig.moveAcceptedCaseToCloseCaseErrorCheck);
};
