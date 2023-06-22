const testConfig = require('../config');

Feature('Verify login smoke scenario');

Scenario('login to the manage case application', async ({I}) => {
    await I.authenticateWithIdam(testConfig.TestEnvCWUser, testConfig.TestEnvCWPassword);

}).tag('@RET-BAT').tag('@crossbrowser').retry(testConfig.TestRetryScenarios);
