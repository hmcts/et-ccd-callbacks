import { test } from "../fixtures/common.fixture";
import { CaseworkerCaseFactory } from '../data-utils/factory/exui/CaseworkerCaseFactory.ts';
import { CaseTypeLocation, Events } from '../config/case-data.ts';
import { CitizenClaimantFactory } from '../data-utils/factory/citizen/ClaimantCitizenFactory.ts';
import { CaseEventApi } from '../data-utils/api/CaseEventApi.ts';
import { users } from '../config/config.dynamic.ts';
import CitizenHubLoginPage from '../pages/claimantCitizenHub/CitizenHubLoginPage.ts';
import CitizenHubPage from '../pages/claimantCitizenHub/CitizenHubPage.ts';

let caseNumber: string;
let caseId: string;

test.describe('Various events in mange case application', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createEnglandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  test('Create a claim and perform B/F action event', {tag: ['@ccd-callback-tests', '@demo']}, async ({ caseDetailsPage, bfActionPage }) => {
    //BF action
    await caseDetailsPage.selectNextEvent(Events.broughtForwardAction);
    await bfActionPage.addBfAction();
  });

  test('Create a claim and perform jurisdiction event', {tag: ['@ccd-callback-tests', '@demo']}, async ({ jurisdictionPage, caseDetailsPage }) => {
    //Jurisdiction event
    await caseDetailsPage.selectNextEvent(Events.jurisdiction);
    await jurisdictionPage.addJurisdictionCodeAndSubmit('DDA', 'Not allocated', 1);
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Jurisdictions',
        tabContent:[
          'Jurisdiction',
          'DDA'
        ]
      }
    ]);
  });

  //RET-5809
  test('Validate longer than 3 letters jurisdiction code in IC event', {tag: ['@ccd-callback-tests', '@demo']}, async ({ caseDetailsPage, jurisdictionPage, icUploadDocPage }) => {
    //Jurisdiction event
    await caseDetailsPage.selectNextEvent(Events.jurisdiction);
    await jurisdictionPage.addJurisdictionCodeAndSubmit('ADT(ST)', 'Not allocated', 1);
    await caseDetailsPage.selectNextEvent(Events.initialConsideration);
    await icUploadDocPage.verifyJurisdictionCodeInICevent();
  });

  //EXUI-3451
  test('Create a England/Wales claim and transfer to Scotland', {tag: '@demo'}, async ({ caseDetailsPage, caseTransferPage }) => {
    await caseDetailsPage.selectNextEvent(Events.caseTransferScotland);
    await caseTransferPage.progressCaseTransfer();
    await caseTransferPage.checkYourAnswer(caseNumber);
  });

  //RET-5790
  test('perform ADR document event', {tag: '@demo'}, async ({ adrDocument, caseDetailsPage }) => {
    await caseDetailsPage.selectNextEvent(Events.adrPrivilegedDocuments);
    await adrDocument.adrUploadDocument();
    await caseDetailsPage.assertTabData([
      {
        tabName: 'ADR/Privileged',
        tabContent:[
          'Documents',
          { tabItem : 'Document Link', value: 'welshTest.pdf' },
          { tabItem : 'Short Description', value: 'description' },
        ]
      }
    ]);
  });


  //RET-6511
  test('Add and manage telephone notes', async ({ caseNotesPage, caseDetailsPage, manageTelephoneNotePage}) => {
    await caseDetailsPage.selectNextEvent(Events.addTelephoneNote);
    await caseNotesPage.addCaseNotes();
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Telephone Notes',
        tabContent:[
          'Telephone notes',
          { tabItem: 'Case Notes', value: '', clickable: true },
          { tabItem: 'Note', value: 'This is test'}
        ]

      }
    ]);

    //edit telephone note
    await caseDetailsPage.selectNextEvent(Events.manageTelephoneNote);
    await manageTelephoneNotePage.editTelephoneNotes();
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Telephone Notes',
        tabContent:[
          'Telephone notes',
          { tabItem: 'Case Notes', value: '', clickable: true },
          { tabItem: 'Note', value: 'This is amended test'}
        ]

      }
    ])
  });
});

test.describe('Claimant retaining access to transferred case', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseTypeLocation.EnglandAndWales);
    ({caseId, caseNumber} = await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId));
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.EnglandAndWales);
  });

  //EXUI-3451
  test('Create a England/Wales claim and transfer to Scotland, Claimant retains case', async ({ browserUtils, caseDetailsPage, caseTransferPage }) => {
    await caseDetailsPage.selectNextEvent(Events.caseTransferScotland);
    await caseTransferPage.progressCaseTransfer();
    let newSubRef= await caseTransferPage.checkYourAnswer(caseNumber);

    //login as claimant and access transferred case
    const claimantBrowserPage = await browserUtils.openNewBrowserContext(users.etClaimant.sessionFile);
    const citizenHubLoginPage = new CitizenHubLoginPage(claimantBrowserPage)
    await citizenHubLoginPage.processCitizenHubLogin(users.etClaimant);
    const citizenHubPage = new CitizenHubPage(claimantBrowserPage);
    await citizenHubPage.navigateToSubmittedCaseOverviewOfClaimant(newSubRef);
  });
});

test.describe('Various events in mange case application for Scotland case', () => {
  test.use({
    storageState: users.etCaseWorker.sessionFile,
  })
  test.beforeEach(async ({ manageCaseDashboardPage, loginPage }) => {
    ({ caseId, caseNumber } = await CaseworkerCaseFactory.createScotlandAndAcceptCase());
    await manageCaseDashboardPage.visit();
    await loginPage.processLogin(users.etCaseWorker);
    caseNumber = await manageCaseDashboardPage.navigateToCaseDetails(caseId, CaseTypeLocation.Scotland);
  });

//RET-5806
  test('Add speak to VP case flag for Scotland case', {tag: '@demo'}, async ({ caseDetailsPage }) => {
    await caseDetailsPage.selectNextEvent(Events.caseDetails);
    await caseDetailsPage.addVPCaseFlag();
  });

  //RET-5931, 5961
  test('Add Case Notes and validate links on Initial Consideration', async ({ caseNotesPage, caseDetailsPage,initialConsiderationPage}) => {
    await caseDetailsPage.selectNextEvent(Events.addTelephoneNote);
    await caseNotesPage.addCaseNotes();
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Telephone Notes',
        tabContent:[
          'Telephone notes',
          { tabItem: 'Case Notes', value: '', clickable: true },
          { tabItem: 'Note', value: 'This is test'}
        ]

      }
    ])
    // RET-5796 Validate initial consideration links
    await caseDetailsPage.selectNextEvent(Events.initialConsideration);
    await initialConsiderationPage.validateLinksNotVisible();
  });

});
