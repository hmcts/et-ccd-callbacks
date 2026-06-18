const path = require('path');

const scenario = file => path.posix.join('src/test/end-to-end/paths', file);

const seedProfiles = [
    {
        seedId: 'submitted-et1-vetting',
        controllerCategory: 'et1-vetting',
        description: 'Submitted case for ET1 vetting callbacks.',
        targetState: 'Submitted',
        verifyEventId: 'et1Vetting',
        transitions: [],
        scenarioRefs: [
            scenario('verifyET1Vetting.js'),
            scenario('moveSubmittedCaseToCaseCloseErrorCheck.js')
        ]
    },
    {
        seedId: 'accepted-case-details',
        controllerCategory: 'case-details',
        description: 'Accepted case for case details callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'amendCaseDetails',
        transitions: ['vet', 'accept'],
        scenarioRefs: [
            scenario('verifyCaseDetails.js'),
            scenario('moveAcceptedCaseToCaseCloseStateErrorCheck.js')
        ]
    },
    {
        seedId: 'accepted-claimant-details',
        controllerCategory: 'claimant-details',
        description: 'Accepted case for claimant details callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'amendClaimantDetails',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyClaimantDetails.js')]
    },
    {
        seedId: 'accepted-claimant-representative',
        controllerCategory: 'claimant-representative',
        description: 'Accepted case for claimant representative callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'addAmendClaimantRepresentative',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyClaimantRepresentative.js')]
    },
    {
        seedId: 'accepted-respondent-details',
        controllerCategory: 'respondent-details',
        description: 'Accepted case for respondent details callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'amendRespondentDetails',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyRespondentDetails.js')]
    },
    {
        seedId: 'accepted-respondent-representative',
        controllerCategory: 'respondent-representative',
        description: 'Accepted case for respondent representative callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'amendRespondentRepresentative',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyRespondentRepresentative.js')]
    },
    {
        seedId: 'accepted-jurisdiction',
        controllerCategory: 'jurisdiction',
        description: 'Accepted case for jurisdiction callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'addAmendJurisdiction',
        transitions: ['vet', 'accept'],
        scenarioRefs: [
            scenario('verifyJurisdiction.js'),
            scenario('verifyValidationForDisposalDateJurisdiction.js')
        ]
    },
    {
        seedId: 'accepted-letters',
        controllerCategory: 'letters',
        description: 'Accepted case for letters callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'generateCorrespondence',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyLetters.js')]
    },
    {
        seedId: 'accepted-restricted-reporting',
        controllerCategory: 'restricted-reporting',
        description: 'Accepted case for restricted reporting callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'restrictedCases',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyRestrictedReporting.js')]
    },
    {
        seedId: 'accepted-fix-case-api',
        controllerCategory: 'fix-case-api',
        description: 'Accepted case for Fix Case API callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'fixCaseAPI',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyFixCaseAPI.js')]
    },
    {
        seedId: 'accepted-bf-action',
        controllerCategory: 'bf-action',
        description: 'Accepted case for B/F action callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'broughtForward',
        transitions: ['vet', 'accept'],
        scenarioRefs: [
            scenario('verifyBFAction.js'),
            scenario('verifyBFActionOutstandingError.js')
        ]
    },
    {
        seedId: 'accepted-initial-consideration',
        controllerCategory: 'initial-consideration',
        description: 'Accepted case for initial consideration callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'initialConsideration',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyInitialConsideration.js')]
    },
    {
        seedId: 'accepted-upload-document',
        controllerCategory: 'upload-document',
        description: 'Accepted case for upload document callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'uploadDocument',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyUploadDocument.js')]
    },
    {
        seedId: 'accepted-case-transfer',
        controllerCategory: 'case-transfer',
        description: 'Accepted case for case transfer callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'caseTransferSameCountry',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyCaseTransfer.js')]
    },
    {
        seedId: 'accepted-et3-processing',
        controllerCategory: 'et3-processing',
        description: 'Accepted case for ET3 processing callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'et3Vetting',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyET3processing.js')]
    },
    {
        seedId: 'accepted-et1-serving',
        controllerCategory: 'et1-serving',
        description: 'Accepted case for ET1 serving callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'uploadDocumentForServing',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyET1Serving.js')]
    },
    {
        seedId: 'accepted-et3-notification',
        controllerCategory: 'et3-notification',
        description: 'Accepted case for ET3 notification callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'et3Notification',
        respondentResponseStatus: 'Not Accepted',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyET3Notification.js')]
    },
    {
        seedId: 'accepted-et3-response',
        controllerCategory: 'et3-response',
        description: 'Accepted case for ET3 response callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'et3Response',
        respondentResponseStatus: 'Not Accepted',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyET3response.js')]
    },
    {
        seedId: 'accepted-list-hearing',
        controllerCategory: 'list-hearing',
        description: 'Accepted case for list hearing callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'addAmendHearing',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('verifyCaseListHearing.js')]
    },
    {
        seedId: 'accepted-hearing-details',
        controllerCategory: 'hearing-details',
        description: 'Accepted case for hearing details callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'updateHearing',
        transitions: ['vet', 'accept', 'list'],
        scenarioRefs: [scenario('verifyLeedsCaseHearingDetails.js')]
    },
    {
        seedId: 'accepted-print-hearing-lists',
        controllerCategory: 'print-hearing-lists',
        description: 'Accepted case for print hearing lists callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'printHearing',
        transitions: ['vet', 'accept', 'list'],
        scenarioRefs: [scenario('verifyLeedsCasePrintHearingLists.js')]
    },
    {
        seedId: 'accepted-weekend-hearing-validation',
        controllerCategory: 'hearing-validation',
        description: 'Accepted case for hearing date validation callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'addAmendHearing',
        transitions: ['vet', 'accept'],
        scenarioRefs: [scenario('validateHearingListedErrorMsgInWeekend.js')]
    },
    {
        seedId: 'rejected-close-validation',
        controllerCategory: 'close-case-validation',
        description: 'Rejected case for close case validation callbacks.',
        targetState: 'Rejected',
        verifyEventId: 'amendCaseDetails',
        transitions: ['vet', 'reject'],
        scenarioRefs: [scenario('moveRejectCaseToCaseCloseStateErrorCheck.js')]
    },
    {
        seedId: 'accepted-with-jurisdiction-close-case',
        controllerCategory: 'close-case',
        description: 'Accepted case with jurisdiction for close case callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'disposeCase',
        transitions: ['vet', 'accept', 'jurisdiction'],
        scenarioRefs: [scenario('verifyCaseClose.js')]
    },
    {
        seedId: 'accepted-with-jurisdiction-judgment',
        controllerCategory: 'judgment',
        description: 'Accepted case with jurisdiction for judgment callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'addAmendJudgment',
        transitions: ['vet', 'accept', 'jurisdiction'],
        scenarioRefs: [scenario('verifyCaseJudgment.js')]
    },
    {
        seedId: 'listed-with-jurisdiction-judgment',
        controllerCategory: 'judgment-listed',
        description: 'Listed case with jurisdiction for judgment callbacks.',
        targetState: 'Accepted',
        verifyEventId: 'addAmendJudgment',
        transitions: ['vet', 'accept', 'list', 'jurisdiction'],
        scenarioRefs: [scenario('verifyCaseJudgment.js')]
    }
];

module.exports = {
    seedProfiles
};
