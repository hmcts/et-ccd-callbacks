import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';

let caseNumber: string;
let caseId: string;

test.describe('Restrict a case by applying rule 43 flag', () => {
    test.use({
        storageState: users.etCaseWorker.sessionFile
    });
    test.beforeEach(async () => {
      ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
    });

    test(
      'Create and remove case Flag for E/W-Single case',
      { tag: '@demo' },
      async ({ manageCaseDashboardPage, loginPage, caseDetailsPage, restrictedReportingPage }) => {
        await manageCaseDashboardPage.visit();
        await loginPage.processLogin(
          users.etCaseWorker
        );

        caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
        //Create case flag
        await caseDetailsPage.selectNextEvent(Events.restrictedReporting);
        await restrictedReportingPage.selectRule49BOption();
        await restrictedReportingPage.verifyRule49BFlag();
      },
    );
});
