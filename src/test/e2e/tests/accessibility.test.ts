import { test } from '../fixtures/common.fixture';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../config/case-data.ts';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import { users } from '../config/config.dynamic.ts';

let caseId: string;
let caseNumber: string;

test.describe('Accessibility test for case workers', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etCaseWorker
    );
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  test('Scan exui pages- Caseworker journey', { tag: '@accessibility' }, async ({ axeUtils, caseDetailsPage }) => {
    await axeUtils.audit();

    //Scan case tabs
    await caseDetailsPage.navigateToTab('Claimant');
    await axeUtils.audit();

    await caseDetailsPage.navigateToTab('ET1 Vetting');
    await axeUtils.audit();

    await caseDetailsPage.navigateToTab('Respondent');
    await axeUtils.audit();

    await caseDetailsPage.navigateToTab('Jurisdictions');
    await axeUtils.audit();

    await caseDetailsPage.navigateToTab('Referrals');
    await axeUtils.audit();

    await caseDetailsPage.navigateToTab('Initial Consideration');
    await axeUtils.audit();

    await caseDetailsPage.navigateToTab('History');
    await axeUtils.audit();

    // await this.caseDetailsPage.selectNextEvent('ET3 Processing');
    // await this.caseListPage.delay(2000);
    // await this.scanEt3ProcessingEvent(page); // et3 processing (Got accessibility error)
  });

  test(
    'Scan exui pages- Work allocation journey',
    { tag: '@accessibility' },
    async ({ manageCaseDashboardPage, axeUtils, caseListPage, }) => {
      await manageCaseDashboardPage.navigateToCaseListPage();
      await caseListPage.delay(2000);
      await axeUtils.audit();

      /* (Got accessibility error)
      await caseDetailsPage.navigateToTab('All work');
      await this.caseListPage.delay(2000);
      await axeTest(page);
      await this.caseListPage.delay(3000);
      */

      await manageCaseDashboardPage.navigateToMyWork();
      await caseListPage.delay(2000);
      await axeUtils.audit();
    },
  );
});

test.describe('Accessibility test', () => {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile
  })
  test.beforeEach(async ({loginPage, manageCaseDashboardPage}) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
  });

  test(
    'Scan exui pages- Legal Representative journey',
    { tag: '@accessibility' },
    async ({ caseDetailsPage, applicationTabPage, axeUtils, manageCaseDashboardPage, nocPage }) => {
      //RET-5787
      await manageCaseDashboardPage.navigateToNoticeOfChange();
      await nocPage.processNocRequest(caseId, CaseDetailsValues.respondentName, caseNumber, true, axeUtils);
      await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);

      //legal rep make an application
      await caseDetailsPage.navigateToTab('Applications');
      await applicationTabPage.enterDetailsForMakingApplication('Amend response',axeUtils);
    },
  );
});
