const testConfig = require('../../config');

Feature('Verify login smoke scenario');

Scenario('login to the manage case application', async ({ I }) => {
    await I.authenticateWithIdam();
    I.wait(10);

}).retry(testConfig.TestRetryScenarios)
    .tag('@somethingNew')
