const ccdUserType = {
    CASEWORKER: 'Caseworker',
    JUDGE: 'Judge'
};

const eventNames = {
    ACCEPT_CASE: 'Accept/Reject Case',
    REJECT_CASE: 'Accept/Reject Case',
    CASE_DETAILS: 'Case Details',
    PRE_ACCEPTANCE_CASE: 'preAcceptanceCase',
    ACCEPT_REJECTED_CASE: 'acceptRejectedCase',
    CLAIMANT_DETAILS: 'Claimant Details',
    CLAIMANT_REPRESENTATIVE: 'Claimant Representative',
    CLAIMANT_RESPONDENT_DETAILS: 'Respondent Details',
    RESPONDENT_REPRESENTATIVE: 'Respondent Representative',
    JURISDICTION: 'Jurisdiction',
    CLOSE_CASE: 'Close Case',
    LETTERS: 'Letters',
    RESTRICTED_REPORTING: 'Restricted Reporting',
    FIX_CASE_API: 'Fix Case API',
    BF_ACTION: 'B/F Action',
    PRINT_HEARING_LISTS: 'Print Hearing lists',
    LIST_HEARING: 'List Hearing',
    ALLOCATE_HEARING: 'Allocate Hearing',
    HEARING_DETAILS: 'Hearing Details',
    CASE_TRANSFER: 'Case Transfer',
    JUDGMENT: 'Judgment',
    CREATE_REPORT: 'Create Report',
    GENERATE_REPORT: 'Generate Report',
    UPLOAD_DOCUMENT: 'Upload Document',
    INITIAL_CONSIDERATION: 'Initial Consideration',
    ET1_VETTING: 'ET1 case vetting',
    ET3_PROCESSING: 'ET3 Processing',
    ET1_SERVING: 'ET1 serving',
    ET3_NOTIFICATION: 'ET3 notification',
    ET3_RESPONSE: 'ET3 response form'
};

const states = {
    SUBMITTED: 'Submitted',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    CLOSED: 'Closed',
    TRANSFERRED: 'Transferred'
};

const timings = {defaultPageClickWaitTime : 5};

module.exports = {
    timings,
    ccdUserType,
    eventNames,
    states
};
