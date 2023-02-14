'use strict';

const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const {eventNames} = require('../common/constants.js');

module.exports = async function (jurisdiction, caseType, eventName) {
    const I = this;
    I.waitForText(commonConfig.createCase, testConfig.TestTimeToWaitForText);
    I.click('Create case');
    I.see('Create Case');
    I.see('Jurisdiction');
    I.selectOption('#cc-jurisdiction', jurisdiction);
    I.see('Case type');
    I.selectOption('#cc-case-type', caseType);
    I.see('Event');
    I.selectOption('#cc-event', eventName);
    I.click(commonConfig.start);

    I.waitForText('Create Report', testConfig.TestTimeToWaitForText);
    I.see('Type');
    I.selectOption('Type', 'Cases Completed'); 
    I.click(commonConfig.continue);
    I.see('Create Report');
    I.click(commonConfig.submit);

    I.click('Go');
    I.see('Generate Report');
    I.see('Reports');
    I.see('Single or Range');
    I.click('#hearingDateType-Single');
    I.see('Date')
    I.fillField('#listingDate-day', commonConfig.listingDateDay);
    I.fillField('#listingDate-month', commonConfig.listingDateMonth);
    I.fillField('#listingDate-year', commonConfig.listingDateYear);
    I.click(commonConfig.continue);
    I.click('Generate Report');

    I.see('Generate Report');
    I.see('Reports');
    I.see('Please download the document from : Document');
    I.click(commonConfig.closeAndReturnToCaseDetailsButton);
    I.waitForText('has been updated with event: Generate Report', testConfig.TestTimeToWaitForText);
};

