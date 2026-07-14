package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state;

import uk.gov.hmcts.ccd.sdk.api.CCD;

@SuppressWarnings("PMD.FieldNamingConventions")
public enum ListingCaseState {
    @CCD(label = "Hearing Documents", hint = "# Listings", omitDescription = true)
    Submitted,
    @CCD(label = "Reports", hint = "# Reports", omitDescription = true)
    SubmittedReport
}
