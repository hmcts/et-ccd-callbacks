const {createAdminReferral, createJudgeReferral, createLegalRepReferral} = require("../helpers/caseHelper");
const {processCaseToAcceptedState} = require("../helpers/etCaseHepler");
Feature('Reform ET Referral Process');

const emailaddress = '';
const details = '';

Scenario('Create a new referral for admin', async ({ I }) => {

    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);

    await createAdminReferral(emailaddress, details);

}).tag('@biggerrefactoring');

Scenario('Create a new referral for a Judge', async ({ I }) => {

    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);

    await createJudgeReferral(emailaddress, details);

}).tag('@biggerrefactoring');

Scenario('Create a new referral for a legal Rep', async ({ I }) => {

    let caseId = await processCaseToAcceptedState();

    console.log("... case id =>" +caseId);

    await createLegalRepReferral(emailaddress, details);

}).tag('@wip');
