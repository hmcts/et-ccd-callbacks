const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
const {et3ProcessingPage} = require("../helpers/caseHelper");


Feature('ET3 Processing - England and Wales');

Scenario('Verify ET3 Processing Journey', async ({I}) => {
    await processCaseToAcceptedState();
    await et3ProcessingPage(I,eventNames.ET3_PROCESSING);
}).tag('@biggerrefactoring');

Scenario('Verify ET3 Processing Journey With No ET3 response', async ({I}) => {
    await processCaseToAcceptedState();
    await et3ProcessingPage(I,eventNames.ET3_PROCESSING);
}).tag('@biggerrefactoring');
