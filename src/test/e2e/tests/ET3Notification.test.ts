import { test } from '../fixtures/common.fixture';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { expect } from '@playwright/test';
import { users } from '../config/config.dynamic.ts';

let caseNumber: string;
let subRef: string;

test.describe('ET3 Notification', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.describe.configure({ mode: 'default' });
  test.beforeEach(async ({}) => {
    subRef = await CitizenClaimantFactory.progressCaseFromCreateToEt3(CaseTypeLocation.EnglandAndWales);
  });

  test('Respondent Reject response and attempt to send ET3 Notification',
    async ({
           loginPage,
           caseDetailsPage,
           respondentDetailsPage,
           et3NotificationPage, manageCaseDashboardPage
         }) => {
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(subRef.toString(), CaseTypeLocation.EnglandAndWales);

    //reject ET3 Response
    await caseDetailsPage.selectNextEvent(Events.respondentDetails);
    await respondentDetailsPage.processRespondentDetailsET3(false);
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Respondent',
        tabContent: [
          {tabItem: 'Mrs Test Auto', value: 'Yes', clickable: true},
          { tabItem: 'Response', value:'Rejected'},
          { tabItem: 'ET3 Form', value: 'ET3Form.pdf', position: 0}
        ]
      },
      {
        tabName: 'Documents',
        tabContent: [
          { tabItem: 'ET1 Vetting - Grayson Becker.pdf', value: 'Starting a Claim | ET1 Vetting', exact: false },
        ],
        excludedContent: [
          'ET3 Form',
          'ET3Form.pdf'
        ]
      }
    ]);

    //attempt to send ET3 notification
    await caseDetailsPage.selectNextEvent(Events.et3Notification);
    await et3NotificationPage.sendEt3Notification(false);
    await et3NotificationPage.verifyEt3NotificationErrorMessage();
  });

  test('Respondent Accept response and sends ET3 Notification',
    async ({
        loginPage,
        caseDetailsPage,
        respondentDetailsPage,
        et3NotificationPage,initialConsiderationPage,
        manageCaseDashboardPage
     }) => {
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(subRef.toString(), CaseTypeLocation.EnglandAndWales);

    //reject ET3 Response
    await caseDetailsPage.selectNextEvent(Events.respondentDetails);
    await respondentDetailsPage.processRespondentDetailsET3(true);
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Respondent',
        tabContent: [
          {tabItem: 'Mrs Test Auto', value: 'Yes', clickable: true},
          { tabItem: 'Response', value:'Accepted'},
          { tabItem: 'ET3 Form', value: 'ET3Form.pdf', position: 0}
        ]
      },
      {
        tabName: 'Documents',
        tabContent: [
          { tabItem: 'ET1 Vetting - Grayson Becker.pdf', value: 'Starting a Claim | ET1 Vetting', exact: false },
        ],
        excludedContent: [
          'ET3 Form',
          'ET3Form.pdf'
        ]
      }
    ]);

    await caseDetailsPage.selectNextEvent(Events.et3Notification);
    await et3NotificationPage.sendEt3Notification();
    await et3NotificationPage.processAcasPage();
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Documents',
        tabContent: [
          { tabItem: 'ET1 Vetting - Grayson Becker.pdf', value: 'Starting a Claim | ET1 Vetting', exact: false },
          { tabItem: 'ET3Form.pdf', value: ' Response to a Claim | ET3', position: 0},
          { tabItem: 'ET3Form.pdf', value: ' Response to a Claim | Response accepted', position: 1},
        ]
      }
      ]);

    // Validate Initial Consideration Links RET-5796
    await manageCaseDashboardPage.navigateToCaseDetails(subRef.toString(), CaseTypeLocation.EnglandAndWales);
    await caseDetailsPage.selectNextEvent(Events.initialConsideration);
    await initialConsiderationPage.validateET1Links();
  });

  test('Respondent not Accepting or rejecting response and attempts to send ET3 Notification',
    async ({page,
           loginPage,
           caseDetailsPage,
           manageCaseDashboardPage
         }) => {
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(subRef.toString(), CaseTypeLocation.EnglandAndWales);

    //attempt to send ET3 notification
    await caseDetailsPage.selectNextEvent(Events.et3Notification, false);
    await expect(
      page.getByLabel('Errors').getByRole('listitem')
    ).toContainText('At least one respondent must have a selected response status.');
  });
});
