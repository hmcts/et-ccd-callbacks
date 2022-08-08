const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {et3Notification} = require("../helpers/caseHelper");

Feature('ET3 Notification Process');

//const case_detail_url = '/cases/case-details/1659609053222055'
const case_detail_url = '/cases/case-details/1645038889612387';

Scenario('progress application through et3 notification -  happy path England and Wales', async ({ I }) => {
    await I.authenticateWithIdam();
    await I.amOnPage(case_detail_url)
    await et3Notification(I,eventNames.ET3_NOTIFICATION);

}).tag('@new_test')