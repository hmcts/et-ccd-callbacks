const testConfig = require('../../config');

Feature('Verify login smoke scenario');

Scenario('login to the manage case application', async ({I}) => {
    await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);

}).retry(testConfig.TestRetryScenarios)
    .tag('@smoke').tag('@crossbrowser');
