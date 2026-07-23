import { test } from '../fixtures/common.fixture';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../config/case-data.ts';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import { users } from '../config/config.dynamic.ts';

let caseNumber: string;
let caseId: string;

test.describe('ET3/Respondent Journey', () => {
  test.use({
    storageState: users.etRespondent.sessionFile,
  });
  test.beforeEach(async () => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
  });

  test('Respondent Assign a claim (ET3)', async ({ et3LoginPage, responseLandingPage, respContactDetailsPages, respClaimantDetails, respContestClaim, respSubmitEt3}) => {
    //Assign a claim to respondent
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.replyToNewClaim(caseId, caseNumber, CaseDetailsValues.respondentName, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);
    await responseLandingPage.startEt3();
    await respContactDetailsPages.et3Section1();
    await respClaimantDetails.et3Section2();
    await respContestClaim.et3Section3();
    await respSubmitEt3.checkYourAnswers();
  });
});
