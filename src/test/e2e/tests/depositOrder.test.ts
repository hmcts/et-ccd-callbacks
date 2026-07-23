import { test } from '../fixtures/common.fixture';
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { users } from '../config/config.dynamic.ts';

const depositOrderData = require('../resources/payload/deposit-order-content.json');
let formattedAmount: string;
let caseNumber: string;
let caseId: string;

test.describe('Deposit order test', () => {
    test.use({
        storageState: users.etCaseWorker.sessionFile
    });

    test.beforeEach(async () => {
      ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
        formattedAmount = new Intl.NumberFormat('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(depositOrderData.depositAmount);
    });

    test(
      'England - deposit an order for respondent',
      { tag: '@demo' },
      async ({ manageCaseDashboardPage, loginPage, caseListPage, depositOrderPage, caseDetailsPage }) => {
        await manageCaseDashboardPage.visit();
        await loginPage.processLogin(
          users.etCaseWorker
        );

        caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
        await caseDetailsPage.selectNextEvent(Events.depositOrder);
        await depositOrderPage.submitADepositOrder();

        await caseDetailsPage.navigateToTab('Deposit Order');
        await caseListPage.verifyDepositOrderDetailsOnTab('Deposit amount (£)', `£${formattedAmount}`);
        await caseListPage.verifyDepositOrderDetailsOnTab('Deposit ordered against', depositOrderData.respondentName);
        await caseListPage.verifyDepositOrderDetailsOnTab('Deposit requested by', depositOrderData.claimantName);
        await caseListPage.verifyDepositOrderDetailsOnTab('Deposit covers', depositOrderData.coverTypeAll);
        await caseListPage.verifyDepositOrderDetailsOnTab('Deposit order sent', '3 Mar 2025');
      },
    );
});
