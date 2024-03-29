'use strict';
const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const selectListedHearing = require('./helpers/selectListedHearing');
const selectPersonelResources = require('./helpers/selectPersonelResources');
const selectAllocateHearingRoom = require('./helpers/selectAllocateHearingRoom');
const verifyHearingsAllocated = require('./helpers/verifyHearingsAllocated');

module.exports = async function (jurisdiction) {

    const I = this;
    selectListedHearing.selectListedHearing();
    if (jurisdiction === 'Leeds')
    {
        selectPersonelResources.selectPersonelResources();
        selectAllocateHearingRoom.selectAllocateHearingRoom();
    }
    //New expected behaviour introduced
    //by EXUI. The POs are delibrating on it
    I.click(commonConfig.continue);
    I.wait(2)
    I.click(commonConfig.submit);
    I.waitForEnabled({css: '#next-step'}, testConfig.TestTimeToWaitForText || 5);
    I.see('has been updated with event: Allocate Hearing\n');
    verifyHearingsAllocated.verifyHearingsAllocated();
};
