import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';

let caseId: string, caseNumber: string;

test.describe('Case File View', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
    test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
      ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
      await manageCaseDashboardPage.visit();
      await loginPage.processLogin(users.etCaseWorker);
      caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    });

    test('Case File View - Check ET 1 Claim in CFV folder - England-Singles', {tag: '@demo'}, async ({caseDetailsPage }) => {
      // Check case file view
        await caseDetailsPage.validateFileTree([
          {
            type: 'folder',
            label: 'Starting a Claim',
            children: [
              {
                type: 'folder',
                label: 'ET1 Vetting',
                children: [
                  {
                  type: 'file',
                  label: 'ET1 Vetting - Grayson Becker.pdf',
                  contentSnippets: [`ET1 Vetting - ${caseNumber}`]
                  }
                ]
              }
            ]
          }
        ]);
    });
});

test.describe('Case File View', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);
  });

  test('Case File View - Check ET 1 Claim in CFV folder - Scotland-Singles', {tag: '@demo'}, async ({ caseDetailsPage }) => {
      // Check case file view
    await caseDetailsPage.validateFileTree([
      {
        type: 'folder',
        label: 'Starting a Claim',
        children: [
          {
            type: 'folder',
            label: 'ET1',
            children: [
              {
                type: 'file',
                label: 'ET1 Vetting - Grayson Becker.pdf',
                contentSnippets: [`ET1 Vetting - ${caseNumber}`]
              }
            ]
          }
        ]
      }
    ]);
  });
});
