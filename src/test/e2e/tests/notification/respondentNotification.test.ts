import { test } from '../../fixtures/common.fixture.ts';
import { CaseworkerCaseFactory } from '../../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../../config/case-data.ts';
import { users } from '../../config/config.dynamic.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import LoginPage from '../../pages/loginPage.ts';
import CaseWorkerNotificationPage from '../../pages/notifications/CaseWorkerNotificationPage.ts';

let caseId: string;
let caseNumber: string;
const respName = CaseDetailsValues.respondentName;
const firstName = CaseDetailsValues.claimantFirstName;
const lastName = CaseDetailsValues.claimantLastName;

test.describe('Respondent Notification scenarios tests', () =>{
  test.use({
    storageState: users.etRespondent.sessionFile,
  });
  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test('Respondent Notification - Verify respondent receives notification and can view case details',
    async ({ et3LoginPage,
             responseLandingPage, respContactDetailsPages, respClaimantDetails, respContestClaim,
             respSubmitEt3, browserUtils, respondentCaseOverviewPage
  }) => {
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.replyToNewClaim(caseId, caseNumber, respName, firstName, lastName);
    await responseLandingPage.startEt3();
    await respContactDetailsPages.et3Section1();
    await respClaimantDetails.et3Section2();
    await respContestClaim.et3Section3();
    await respSubmitEt3.checkYourAnswers();

    const caseWorkerBrowserPage = await browserUtils.openNewBrowserContext(users.etCaseWorker.sessionFile);
    const manageCaseDashboardPage = new ManageCaseDashboardPage(caseWorkerBrowserPage);
    await manageCaseDashboardPage.visit();
    const loginPage = new LoginPage(caseWorkerBrowserPage);
    await loginPage.processLogin(users.etCaseWorker);
    await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    const caseWorkerNotificationPage = new CaseWorkerNotificationPage(caseWorkerBrowserPage);
    await caseWorkerNotificationPage.navigateToSendANotifications();
    const notificationTitle = await caseWorkerNotificationPage.sendNotification('ET1 claim', 'No');
    await caseWorkerBrowserPage.close();

    //Respondent verify notification
    await et3LoginPage.processRespondentLogin(users.etRespondent);
    await et3LoginPage.navigateToCase(caseNumber, caseId);
    await respondentCaseOverviewPage.validateNotificationBanner(notificationTitle);
  });
});
