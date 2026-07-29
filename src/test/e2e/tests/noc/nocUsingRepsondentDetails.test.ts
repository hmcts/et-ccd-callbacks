import { test } from '../../fixtures/common.fixture.ts';
import { CitizenClaimantFactory } from '../../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseDetailsValues, CaseTypeLocation } from '../../config/case-data.ts';
import { CaseEventApi } from '../../data-utils/api/CaseEventApi.ts';
import { users } from '../../config/config.dynamic.ts';
import LoginPage from '../../pages/loginPage.ts';
import { ManageCaseDashboardPage } from '../../pages/ManageCaseDashboardPage.ts';
import { NocPage } from '../../pages/legalRepresentative/NocPage.ts';

let caseId: string;
let caseNumber: string;
let firstName: string;
let lastName: string;

test.describe('perform NOC for respondent', () => {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })

  test.beforeEach(async ({ manageCaseDashboardPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    firstName = CaseDetailsValues.claimantFirstName;
    lastName = CaseDetailsValues.claimantLastName;
  });

  //RET-5787
  test('Process NOC using claimant details, assign a new claimant representative and check original claimant representative cannot access a case', async ({
    manageCaseDashboardPage,
    caseListPage,
    loginPage,
    nocPage, browserUtils
  }) => {
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToNoticeOfChange();
    await nocPage.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);

    //Assign a case to another legal representative
    const legalRep2BroswerPage = await browserUtils.openNewBrowserContext(users.etLegalRepresentative2.sessionFile);
    const loginPageForRep2 = new LoginPage(legalRep2BroswerPage);
    const manageDashboardForRep2 = new ManageCaseDashboardPage(legalRep2BroswerPage);
    const nocPageRep2 = new NocPage(legalRep2BroswerPage);

    await manageDashboardForRep2.visit();
    await loginPageForRep2.processLogin(
      users.etLegalRepresentative2
    );
    await manageDashboardForRep2.navigateToNoticeOfChange();
    await nocPageRep2.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);

    //validate case no longer accessible by original legal representative
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await caseListPage.searchCaseApplicationWithSubmissionReference('Eng/Wales - Singles', caseId);
    await caseListPage.verifyNoCasesFoundMessage();
  });
});
