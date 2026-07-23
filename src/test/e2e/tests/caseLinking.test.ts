import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';

let caseId: string, caseId2: string;
let caseNumber1: string, caseNumber2: string;

  test.describe('Link-2-Scottish-Cases - Multiple Reasons - Scotland', async () => {
    test.use({
      storageState: users.etCaseWorker.sessionFile
    });

    test.beforeEach(async () => {
        // Create, vet & accept first case
        ({ caseId, caseNumber: caseNumber1 } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
        // Create, vet & accept second case
        ({ caseId: caseId2, caseNumber: caseNumber2 } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
      });

      test('Link-2-Cases - Multiple Reasons - Scotland', {tag: '@demo'}, async ({ manageCaseDashboardPage,loginPage, caseLinkPage, caseDetailsPage }) => {
        await manageCaseDashboardPage.visit();
        await loginPage.processLogin(
          users.etCaseWorker
        );

        caseNumber2 = await manageCaseDashboardPage.navigateToCaseDetails(caseId2, CaseTypeLocation.Scotland);
        // link cases
        await caseDetailsPage.selectNextEvent(Events.linkCases);
        await caseLinkPage.checksCaseLinkStartingPage();
        await caseLinkPage.enterCaseLinkReferenceWithoutHearing(caseId);
        const caseIdText = String(caseId).replace(/(\d{4})(?=\d)/g, '$1-');
        await caseDetailsPage.navigateToTab('Linked cases');
        await caseDetailsPage.assertTabData([
          {
            tabName: 'Linked cases',
            tabContent: [
              'Linked cases',
              `${CaseDetailsValues.claimantFirstName} ${CaseDetailsValues.claimantLastName} vs ${CaseDetailsValues.respondentName} ${caseIdText}`,
            ],
          },
        ]);
      });
  });

  test.describe('Link-2-Cases - Multiple Reasons - England & Wales', async () => {
    test.use({
      storageState: users.etCaseWorker.sessionFile
    });

    test.beforeEach(async () => {
        // Create, vet & accept first case
        ({ caseId, caseNumber: caseNumber1 } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
        // Create, vet & accept second case
        ({ caseId: caseId2, caseNumber: caseNumber2 } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
      });

      test(
        'Link-2-Cases - Multiple Reasons - England',
        { tag: '@demo' },
        async ({ manageCaseDashboardPage, loginPage, caseLinkPage, caseDetailsPage }) => {
        await manageCaseDashboardPage.visit();
        await loginPage.processLogin(
          users.etCaseWorker
        );

        caseNumber2 = await manageCaseDashboardPage.navigateToCaseDetails(
          caseId2,
          CaseTypeLocation.EnglandAndWales,
        );
        // link cases
        await caseDetailsPage.selectNextEvent(Events.linkCases);
        await caseLinkPage.checksCaseLinkStartingPage();
        await caseLinkPage.enterCaseLinkReferenceWithoutHearing(caseId);
        },
      );
    });
