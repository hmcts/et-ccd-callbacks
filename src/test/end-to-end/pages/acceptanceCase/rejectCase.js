'use strict';
const commonConfig = require('../../data/commonConfig.json');

module.exports = async function () {

    const I = this;
    I.click('#preAcceptCase_caseAccepted_No');
    I.fillField('#dateRejected-day', commonConfig.caseAcceptedDay);
    I.fillField('#dateRejected-month', commonConfig.caseAcceptedMonth);
    I.fillField('#dateRejected-year', commonConfig.caseAcceptedYear);
    I.checkOption('#preAcceptCase_rejectReason-Not\\ on\\ Prescribed\\ Form');
    I.click(commonConfig.submit);
};

