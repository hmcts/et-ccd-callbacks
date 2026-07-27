import { test } from '../../fixtures/common.fixture.ts';
import { CaseworkerCaseFactory } from '../../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation } from '../../config/case-data.ts';
import { users } from '../../config/config.dynamic.ts';
import Et3LoginPage from '../../pages/respondentCitizenHub/et3LoginPage.ts';
import RespondentCaseOverviewPage from '../../pages/respondentCitizenHub/respondentCaseOverviewPage.ts';

let caseNumber: any;
let caseId:string;

test.describe.skip('Respondent Store Application for unrepresented cases', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  //TODO
  test.beforeEach(async () => {
    ({caseId, caseNumber} = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test.skip(
    'Unrepresented Respondent to store Rule 92 application- Correspondence Yes',
    async ({ loginPage, manageCaseDashboardPage, browserUtils  }) => {
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      //RET-5466
      const respondentBrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
      const et3LoginPage = new Et3LoginPage(respondentBrowserPage);
      const respondentCaseOverviewPage = new RespondentCaseOverviewPage(respondentBrowserPage);
      await et3LoginPage.processRespondentLoginForExistingCase(
        users.etRespondent,
        caseNumber,
      );
      await respondentCaseOverviewPage.unrepresentedRespondentMakeApplication('Rule92', true);
      await respondentCaseOverviewPage.unrepresentedRespondentValidateApplication(true);
    },
  );

  test.skip(
    "Unrepresented Respondent to submit Rule 92 application- Correspondence No'",
    async ({ loginPage, manageCaseDashboardPage, browserUtils }) => {
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      //RET-5466
      const respondentBrowserPage = await browserUtils.openNewBrowserContext(users.etRespondent.sessionFile);
      const et3LoginPage = new Et3LoginPage(respondentBrowserPage);
      const respondentCaseOverviewPage = new RespondentCaseOverviewPage(respondentBrowserPage);

      await et3LoginPage.processRespondentLoginForExistingCase(
        users.etRespondent, caseNumber
      );
      await respondentCaseOverviewPage.unrepresentedRespondentMakeApplication('Rule92', false);
      await respondentCaseOverviewPage.unrepresentedRespondentValidateApplication(false);
    },
  );
});
