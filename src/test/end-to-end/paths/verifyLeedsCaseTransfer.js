const testConfig = require('./../../config');
const {createCaseInCcd} = require("../helpers/ccdDataStoreApi");
const {eventNames} = require('../pages/common/constants.js');
const {caseTransfer} = require("../helpers/caseHelper");
const testConfig = require('../../config');
let caseNumber;

Feature('Create a Leeds Single Case & Execute Case Transfer');

Scenario('Verify Leeds Case Transfer', async ({I}) => {
    caseNumber = await createCaseInCcd('src/test/end-to-end/data/ccd-case-basic-data.json');
    await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);
    await I.wait(5);
    await I.amOnPage('/case-details/' + caseNumber);
    await caseTransfer(I, eventNames.CASE_TRANSFER);

}).tag('@e2e')
    .tag('@nightly')
    .retry(testConfig.TestRetryScenarios);
