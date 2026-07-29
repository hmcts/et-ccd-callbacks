import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';

let caseId: string;
let caseNumber: string;

test.describe('ET3 Process test', () => {

    test.use({
        storageState: users.etCaseWorker.sessionFile,
    })

    test.beforeEach(async () => {
      ({caseId, caseNumber} = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
    });

    test(
      'England - processing an ET3 response',
      { tag: '@demo' },
      async ({ manageCaseDashboardPage, loginPage, caseListPage, lettersPage,initialConsiderationPage, respondentRepPage, et3ProcessPage, caseDetailsPage }) => {

        await manageCaseDashboardPage.visit();
        await loginPage.processLogin(
          users.etCaseWorker
        );

        await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
        await caseDetailsPage.selectNextEvent(Events.letters);
        await lettersPage.generateNoHearingDateLetter();

        await caseDetailsPage.selectNextEvent(Events.respondentDetails);
        await respondentRepPage.enterRespType();

        await caseDetailsPage.selectNextEvent(Events.et3Processing);
        await et3ProcessPage.submitET3Response();

        await caseDetailsPage.navigateToTab('Respondents');
        await caseListPage.verifyET3DetailsOnRespondentTab();

        // RET-5796 Validate initial consideration links
        await caseDetailsPage.selectNextEvent(Events.initialConsideration);
        await initialConsiderationPage.validateET3ProcessingLink();
      },
    );
});
