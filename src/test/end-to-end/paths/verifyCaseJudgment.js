const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');
const {judgment} = require("../helpers/caseHelper");
const {processCaseToAcceptedWithAJurisdiction} = require("../helpers/etCaseHepler");

Feature('Office Singles Case & Execute Judgment Event');

Scenario('Verify Case Judgment', async ({I}) => {

    let caseId = await processCaseToAcceptedWithAJurisdiction();
    console.log("... case id =>" +caseId);
    await judgment(I, eventNames.JUDGMENT);

}).tag('@pats');
