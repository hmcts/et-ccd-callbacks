// Interface for Case Events
export enum CaseTypeLocation {
  EnglandAndWales = 'ET_EnglandWales',
  Scotland = 'ET_Scotland',
}

export type ReferralOption = 'Admin' | 'Judge' | 'Legal Officer';

export const CaseDetailsValues = {
  claimantFirstName: 'Grayson',
  claimantLastName: 'Becker',
  respondentName: 'Mrs Test Auto',
  respondentName2: 'Auto Test',
  respondentLegalName: 'ET Respondent Legal Name',
}

export const PayloadPath = {
  CaseWorker: {
    england: 'src/test/e2e/resources/payload/caseworker/et-england-case-data-caseworker.json',
    scotland: 'src/test/e2e/resources/payload/caseworker/et-scotland-case-data-caseworker.json',
  },
  LegalRep: {
    createEnglandWalesCase: 'src/test/e2e/resources/payload/legalRep/et-england-create-case-data.json',
    et1section1: 'src/test/e2e/resources/payload/legalRep/et1-claimant-details.json',
    et1Section2: 'src/test/e2e/resources/payload/legalRep/et1-employment-respondent-details.json',
    et1Section3: 'src/test/e2e/resources/payload/legalRep/et1-claim-details.json',
    et1SubmitClaim: 'src/test/e2e/resources/payload/legalRep/et1-submit-claim-details.json',
  },
  events: {
    et1vetting: 'src/test/e2e/resources/payload/events/et1Vetting.json',
    acceptCase: 'src/test/e2e/resources/payload/events/acceptCase.json',
    listHearingAmersham: 'src/test/e2e/resources/payload/events/listHearingEnglandAmersham.json',
  },
};

export interface CaseEvent {
  listItem: string;
  ccdCallback: string;
}

export const Events: Record<string, CaseEvent> = {
  createCase: {
    listItem: 'Create Case',
    ccdCallback: 'initiateCase',
  },
  createLegalRepCase: {
    listItem: '',
    ccdCallback: 'et1ReppedCreateCase',
  },
  et1SectionOneClaimantDetails: {
    listItem: 'ET1 Claimant',
    ccdCallback: 'et1SectionOne',
  },
  et1SectionTwoRespondentDetails: {
    listItem: 'ET1 Respondent',
    ccdCallback: 'et1SectionTwo',
  },
  et1SectionThreeClaimDetails: {
    listItem: 'ET1 Claim Details',
    ccdCallback: 'et1SectionThree',
  },
  et1SubmitClaim: {
    listItem: 'ET1 Submit Claim',
    ccdCallback: 'submitEt1Draft',
  },
  et1Vetting: {
    listItem: 'ET1 case vetting',
    ccdCallback: 'et1Vetting',
  },
  et1Serving: {
    listItem: 'ET1 serving',
    ccdCallback: 'uploadDocumentForServing',
  },
  acceptRejectCase: {
    listItem: 'Accept/Reject Case',
    ccdCallback: 'preAcceptanceCase',
  },
  listHearing: {
    listItem: 'List Hearing',
    ccdCallback: 'addAmendHearing',
  },
  uploadDocumentsForHearing: {
    listItem: 'Upload documents for hearing',
    ccdCallback: 'bundlesRespondentPrepareDoc',
  },
  refreshSharedUsers: {
    listItem: 'Refresh Shared Users',
    ccdCallback: 'refreshSharedUsers',
  },
  judgment: {
    listItem: 'Judgment',
    ccdCallback: 'addAmendJudgment',
  },
  claimantRepresentative: {
    listItem: 'Claimant Representative',
    ccdCallback: 'addAmendClaimantRepresentative',
  },
  et3RespondentDetails: {
    listItem: 'ET3 - Respondent Details',
    ccdCallback: 'et3Response',
  },
  et3EmploymentDetails: {
    listItem: 'ET3 - Employment Details',
    ccdCallback: 'et3ResponseEmploymentDetails',
  },
  et3ResponseDetails: {
    listItem: 'ET3 - Response Details',
    ccdCallback: 'et3ResponseDetails',
  },
  et3Notification: {
    listItem: 'ET3 notification',
    ccdCallback: 'et3Notification',
  },
  submitEt3Form: {
    listItem: 'Submit ET3 Form',
    ccdCallback: 'submitEt3',
  },
  initialConsideration: {
    listItem: 'Initial Consideration',
    ccdCallback: 'initialConsideration',
  },
  uploadDocument: {
    listItem: 'Upload Document',
    ccdCallback: 'uploadDocument'
  },
  respondentDetails: {
    listItem: 'Respondent Details',
    ccdCallback: 'amendRespondentDetails',
  },
  claimantDetails: {
    listItem: 'Claimant Details',
    ccdCallback: 'amendClaimantDetails',
  },
  generateReport: {
    listItem: 'Generate Report',
    ccdCallback: 'generateListing',
  },
  hearingDetails: {
    listItem: 'Hearing Details',
    ccdCallback: 'updateHearing',
  },
  et3Processing: {
    listItem: 'ET3 Processing',
    ccdCallback: 'et3Vetting',
  },
  letters: {
    listItem: 'Letters',
    ccdCallback: 'generateCorrespondence',
  },
  searchAcasCertificate: {
    listItem: 'Search ACAS Certificate',
    ccdCallback: 'retrieveAcasCertificate',
  },
  respondentRepresentative: {
    listItem: 'Respondent Representative',
    ccdCallback: 'amendRespondentRepresentative',
  },
  allocateHearing: {
    listItem: 'Allocate Hearing',
    ccdCallback: 'allocateHearing',
  },
  printHearingLists: {
    listItem: 'Print Hearing lists',
    ccdCallback: 'printHearing',
  },
  createFlag: {
    listItem: 'Create a case flag',
    ccdCallback: 'createFlag',
  },
  manageFlag: {
    listItem: 'Manage case flags',
    ccdCallback: 'manageFlags',
  },
  linkCases: {
    listItem: 'Link cases',
    ccdCallback: 'createCaseLink',
  },
  restrictedReporting: {
    listItem: 'Restricted Reporting',
    ccdCallback: 'restrictedCases',
  },
  depositOrder: {
    listItem: 'Deposit Order',
    ccdCallback: 'recordDeposit',
  },
  jurisdiction: {
    listItem: 'Jurisdiction',
    ccdCallback: 'addAmendJurisdiction',
  },
  closeCase: {
    listItem: 'Close Case',
    ccdCallback: 'disposeCase',
  },
  reinstateClosedCase: {
    listItem: 'Reinstate Case',
    ccdCallback: 'reinstateClosedCase',
  },
  broughtForwardAction: {
    listItem: 'B/F Action',
    ccdCallback: 'broughtForward',
  },
  adrPrivilegedDocuments: {
    listItem: 'ADR/Privileged Documents',
    ccdCallback: 'adrDocuments',
  },
  caseDetails: {
    listItem: 'Case Details',
    ccdCallback: 'amendCaseDetails',
  },
  caseTransferScotland: {
    listItem: 'Case Transfer (Scotland)',
    ccdCallback: '',
  },
  caseTransferToEcm: {
    listItem: 'Case Transfer to ECM',
    ccdCallback: 'caseTransferECM',
  },
  addTelephoneNote: {
    listItem: 'Add Telephone Note',
    ccdCallback: 'addCaseNote',
  },
  manageTelephoneNote: {
    listItem: 'Manage Telephone Note',
    ccdCallback: 'manageTelephoneNote',
  },
  uploadHearingDocuments: {
    listItem: 'Upload Hearing Documents',
    ccdCallback: 'uploadHearingDocuments',
  },
  amendContactDetails: {
    listItem: 'Amend contact details',
    ccdCallback: 'amendContactDetails',
  },
  amendContactDetailsClaimant: {
    listItem: 'Amend contact details',
    ccdCallback: 'amendClaimantRepresentativeContact',
  },
  requestSupport: {
    listItem: 'Request Support',
    ccdCallback: 'requestSupport',
  },
  manageSupport: {
    listItem: 'Manage Support',
    ccdCallback: 'manageSupport',
  },
};
