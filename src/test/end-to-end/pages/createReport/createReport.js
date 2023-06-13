'use strict';

const commonConfig = require('../../data/commonConfig.json');
const testConfig = require("../../../config");
const {eventNames} = require('../common/constants.js');

module.exports = async function (jurisdiction, caseType, eventName) {
    const I = this;

    I.click('[href="/cases"]');
    I.waitForText('Manage Cases', testConfig.TestTimeToWaitForText);
    I.see('Case list');
    I.see('Filters');
    I.see('Jurisdiction');
    I.see('Case type');
    I.see('State');
    I.click('Case list');
    I.selectOption('Case type', 'Eng/Wales - Hearings/Reports'); 
    I.click('.workbasket-filters-apply');
    I.wait(1);
    I.click('Brought Forward Report');

    I.waitForText('Reports', testConfig.TestTimeToWaitForText);
    I.see('Brought Forward Report');

    I.click('Go');
    I.see('Generate Report');
    I.see('Reports');
    I.see('Single or Range');
    I.click('#hearingDateType-Single');
    I.see('Date')
    I.fillField('#listingDate-day', commonConfig.listingDateDay);
    I.fillField('#listingDate-month', commonConfig.listingDateMonth);
    I.fillField('#listingDate-year', commonConfig.listingDateYear);
    I.click(commonConfig.submit);

    I.waitForText('Please download the document from : Document', testConfig.TestTimeToWaitForText);
    I.see('Generate Report');
    I.see('Reports');
    I.click(commonConfig.closeAndReturnToCaseDetailsButton);
    I.waitForText('has been updated with event: Generate Report', testConfig.TestTimeToWaitForText);
};

