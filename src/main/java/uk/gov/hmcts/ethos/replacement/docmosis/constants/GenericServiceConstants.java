package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class GenericServiceConstants {

    // Exception Messages
    public static final String EXCEPTION_CASE_DETAILS_NOT_FOUND =
            "Case details not found with the given caseId, %s";
    public static final String EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA =
            "Case details with Case Id, %s doesn't have case data values";

    private GenericServiceConstants() {
        // Final classes should not have a public or default constructor.
    }

}
