'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function (clerkResponsible, physicalLocation, conciliationTrack) {

    const I = this;
    await I.selectOption('#clerkResponsible', clerkResponsible);
    await I.selectOption('#fileLocation', physicalLocation)
    await I.selectOption('#conciliationTrack', conciliationTrack)
    await I.click(commonConfig.continue);
    await I.click(commonConfig.continue);
    await I.click(commonConfig.submit);
};
