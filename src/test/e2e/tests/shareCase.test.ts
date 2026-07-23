import { test } from '../fixtures/common.fixture';
import { CaseDetailsValues, CaseTypeLocation, Events } from '../config/case-data.ts';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import { users } from '../config/config.dynamic.ts';

let caseId: string;
let caseNumber: string;
let firstName: string;
let lastName: string;
let respName: string;

test.describe('Share Case scenarios', () => {
  test.use({
    storageState: users.etLegalRepresentative.sessionFile,
  })

  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId.toString()));
    await manageCaseDashboardPage.visit();
    firstName = CaseDetailsValues.claimantFirstName;
    lastName = CaseDetailsValues.claimantLastName;
    respName = CaseDetailsValues.respondentName;
    await loginPage.processLogin(
      users.etLegalRepresentative
    );
    await manageCaseDashboardPage.navigateToNoticeOfChange();
  });

  //RET-5425
  test('Share case (respondent representative)', async ({
                                                          manageCaseDashboardPage,
                                                          caseListPage,
                                                          caseDetailsPage,
                                                          baseEventPage,
                                                          nocPage,
                                                        }) => {
    await nocPage.processNocRequest(caseId, respName, caseNumber);
    await caseListPage.searchCaseApplicationWithSubmissionReference('Eng/Wales - Singles', caseId);
    await caseListPage.checkAndShareCaseFromList(caseId);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.selectNextEvent(Events.refreshSharedUsers);
    await baseEventPage.clickSubmitButton();

    //validate share case details
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Respondent Representative',
        tabContent: [
          { tabItem: 'Respondent who is being represented', value: respName },
          { tabItem: 'First name', value: 'Test' },
          { tabItem: 'Last name', value: 'Factory' },
          { tabItem: 'Email address', value: users.etManageOrgSuperUser.email, position: 1 }, // position starts from 0
        ],
      },
    ]);
  });

  //RET-5425
  test('Share case (claimant representative)', async ({
                                                        manageCaseDashboardPage,
                                                        caseListPage,
                                                        caseDetailsPage,
                                                        baseEventPage,
                                                        nocPage,
                                                      }) => {
    await nocPage.processNocRequest(caseId, `${firstName} ${lastName}`, caseNumber);
    await caseListPage.searchCaseApplicationWithSubmissionReference('Eng/Wales - Singles', caseId);
    await caseListPage.checkAndShareCaseFromList(caseId);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.selectNextEvent(Events.refreshSharedUsers);
    await baseEventPage.clickSubmitButton();
    //validate share case details
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Claimant Representative',
        tabContent: [
          'Claimant Representative Details',
          { tabItem: 'Email address', value: users.etManageOrgSuperUser.email, position: 2 }, // position starts from 0
        ],
      },
    ]);
  });
});
