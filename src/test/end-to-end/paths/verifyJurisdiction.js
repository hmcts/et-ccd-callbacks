const testConfig = require('./../../config');

const {eventNames} = require('../pages/common/constants.js');
const {jurisdiction} = require("../helpers/caseHelper");


Feature('Leeds Office Singles Case & Execute Jurisdiction Event');

Scenario('Verify Jurisdiction', async ({I}) => {


    await jurisdiction(I, eventNames.JURISDICTION);

}).tag('@wip')
    .tag('@nightly')
    .retry(testConfig.TestRetryScenarios);
