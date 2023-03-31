const {createAdminReferral, createJudgeReferral, createLegalRepReferral} = require("../helpers/caseHelper");
const {processCaseToListedState} = require("../helpers/etCaseHepler");
//const testConfig = require('./../../config');
//const {eventNames} = require('../pages/common/constants.js');
//Feature('Reform ET Referral Process');
const testConfig = require('./../../config');
const {eventNames} = require('../pages/common/constants.js');

const emailaddress = '';
const details = '';

//Verifying Referrals for an Admin
Scenario('Create a new referral for admin', async ({ I }) => {

    let caseId = await processCaseToListedState();
    console.log("... case id =>" +caseId);

    await createAdminReferral(I);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);

//Verifying Referrals for a Judge
Scenario('Create a new referral for a Judge', async ({ I }) => {

    let caseId = await processCaseToListedState();
    console.log("... case id =>" +caseId);

    await createJudgeReferral(I);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);

//Verifying Referrals for a Legal Rep
Scenario('Create a new referral for a legal Rep', async ({ I }) => {

    let caseId = await processCaseToListedState();
    console.log("... case id =>" +caseId);

    await createLegalRepReferral(I);

}).tag('@RET-BAT').tag('@nightly').retry(testConfig.TestRetryScenarios);

