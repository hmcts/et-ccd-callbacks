import { test } from '../fixtures/et.test.fixture.ts';
import {CaseTypeLocation, CaseworkerCaseFactory, Events, users} from "@et-shared-test-library/core";

let caseId: string;
let caseNumber: string;

test.describe('E/W Hearing Reports', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test('Generate Hearing Reports for Newcastle office', async ({
    manageCaseDashboardPage,
    loginPage, caseListPage,
    listHearingPage,
    caseDetailsPage,
  }) => {
    await manageCaseDashboardPage.visit();
    //judge log in
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

    //List 1 hearing for the case
    await caseDetailsPage.selectNextEvent(Events.listHearing);
    await listHearingPage.listCase('EnglandWales', 0, 'Newcastle CFCTC');
    await caseDetailsPage.checkHasBeenCreated(Events.listHearing);

    await caseListPage.searchHearingReports('Eng/Wales - Hearings/Reports', 'Hearing Documents', 'Newcastle');
    await caseListPage.selectHearingReport();
    await caseDetailsPage.selectNextEvent(Events.generateReport);
    await caseListPage.generateReport();
    await caseListPage.validateHearingReport(caseNumber);
  });
});
