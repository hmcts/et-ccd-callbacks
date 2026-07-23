import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { users } from '../config/config.dynamic.ts';
import { CaseDetailsValues } from '../config/case-data.ts';
import Et3LoginPage from '../pages/respondentCitizenHub/et3LoginPage.ts';

let caseNumber: string;
let caseId: string;

test.describe('ET3/Respondent Journey', () => {
  test.use({
    storageState: users.etRespondent.sessionFile,
  });

  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test('Validate ET3 Form start page and check case sensitivity', async ({et3LoginPage,
    respondentCaseOverviewPage,
    respondentTaskListPage
  }) => {
    const respName = 'mrs test auto';
    const firstName = 'graYson';
    const lastName = 'beCker';

    //Assign a claim to respondent
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.replyToNewClaim(caseId, caseNumber, respName, firstName, lastName);
    await respondentCaseOverviewPage.validateRespondentCaseOverviewPage();
    await respondentTaskListPage.validateTaskListPage();
  });

  test('Validate claimant details in respondent application', async ({ et3LoginPage }) => {
    //RET-5517
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.replyToNewClaim(caseId, caseNumber, CaseDetailsValues.respondentName, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);
    await et3LoginPage.validateClaimantDetailsInRespondentApp(CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);
  });
  //RET-6200
  test('Validating the error message after the respondent tries to reassign the same case', async ({ et3LoginPage, browserUtils}) => {
      await et3LoginPage.processRespondentLogin(users.etRespondent);
      await et3LoginPage.replyToNewClaim(caseId, caseNumber, CaseDetailsValues.respondentName, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);

      const respondent2BrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent2.sessionFile);
      const et3LoginPageForRespondent2 = new Et3LoginPage(respondent2BrowserPage);

      await et3LoginPageForRespondent2.processRespondentLogin(users.etRespondent2);
      await et3LoginPageForRespondent2.replyToClaimAsNewRespondent(caseId, caseNumber, CaseDetailsValues.respondentName, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);
      await et3LoginPageForRespondent2.assertErrorMessageIsVisible('Case has already assigned to a respondent');
    });
});
