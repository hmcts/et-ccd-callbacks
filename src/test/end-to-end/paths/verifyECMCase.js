const testConfig = require('./../../config');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");

Feature('Verify CCD Case Creation...');
let caseNumber;

Scenario('Check whether the user able to create a ccd case or not...', async () => {
    caseNumber = await processCaseToAcceptedState();
    console.log('CCD CaseID ==>::  ' + caseNumber);

}).retry(testConfig.TestRetryScenarios)
    .tag('@smoke')
    .tag('@nightly').tag('@toberefactored');
    //.retry(testConfig.TestRetryFeatures);

