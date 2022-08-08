const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {et3ProcessingPage} = require("../helpers/caseHelper");
let testUrl = '/cases/case-details/1659525858049156';

Feature('ET3 Processing - England and Wales');

Scenario('Verify ET3 Processing Journey', async ({I}) => {
    await I.authenticateWithIdam();
    await I.amOnPage(testUrl)
    await et3ProcessingPage(I,eventNames.ET3_PROCESSING);
}).tag('@BAT');

Scenario('Verify ET3 Processing Journey With No ET3 response', async ({I}) => {
    await I.authenticateWithIdam();
    await I.amOnPage(testUrl)
    await et3ProcessingPage(I,eventNames.ET3_PROCESSING);
}).tag('@BAT');