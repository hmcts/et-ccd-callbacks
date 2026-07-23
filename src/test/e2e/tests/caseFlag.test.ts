import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';

let caseNumber: string;
let caseId: string;

test.describe('Case Flag', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  });
  test.beforeEach(async () => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
  });

  test('Create and remove case Flag for E/W-Single case', {tag: '@demo'}, async ({ manageCaseDashboardPage, loginPage, caseDetailsPage, createCaseFlagPage, manageCaseFlagPage }) => {
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);

    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    //Create case flag
    await caseDetailsPage.selectNextEvent(Events.createFlag);
    await createCaseFlagPage.createCaseFlag();

    //remove case flag
    await caseDetailsPage.selectNextEvent(Events.manageFlag);
    await manageCaseFlagPage.manageCaseFlag();
  });
});

test.describe('Case Flag', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  });
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etCaseWorker
    );
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);
  });

  test('Create and remove case Flag for Scotland-Single case', {tag: '@demo'}, async ({ caseDetailsPage, createCaseFlagPage, manageCaseFlagPage }) => {

    //Create case flag
    await caseDetailsPage.selectNextEvent(Events.createFlag);
    await createCaseFlagPage.createCaseFlag();

    //remove case flag
    await caseDetailsPage.selectNextEvent(Events.manageFlag);
    await manageCaseFlagPage.manageCaseFlag();
  });
});
