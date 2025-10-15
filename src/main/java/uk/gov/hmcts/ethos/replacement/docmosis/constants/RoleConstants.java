package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class RoleConstants {

    // Role names must be kept in sync with the values defined in the
    // "CaseRoles" reference data set in CCD.
    public static final String CREATOR = "[CREATOR]";
    public static final String DEFENDANT = "[DEFENDANT]";
    public static final String CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";

    // Role modification types
    public static final String ROLE_MODIFICATION_TYPE_ASSIGNMENT = "Assignment";
    public static final String ROLE_MODIFICATION_TYPE_UPDATE = "update";
    public static final String ROLE_MODIFICATION_TYPE_REVOKE = "Revoke";

    // Exception messages
    public static final String EXCEPTION_INVALID_MODIFICATION_TYPE = "Invalid modification type";
    public static final String EXCEPTION_CASE_USER_ROLE_NOT_FOUND = "Case user role not found for caseId: %s";
    public static final String EXCEPTION_INVALID_CASE_USER_ROLE = "Invalid case user role: %s";

    private RoleConstants() {
        // Final classes should not have a public or default constructor.
    }

}
