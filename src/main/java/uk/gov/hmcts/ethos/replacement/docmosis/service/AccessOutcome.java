package uk.gov.hmcts.ethos.replacement.docmosis.service;

/**
 * Result of granting or reassigning a party's case-user role when updating their email.
 */
public enum AccessOutcome {
    UNCHANGED,
    REASSIGNED,
    GRANTED
}
