package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin;

import uk.gov.hmcts.ccd.sdk.api.CCD;

@SuppressWarnings("PMD.FieldNamingConventions")
public enum AdminState {
    @CCD(label = "Open", hint = "# ECM Admin", access = AdminAccess.class)
    Open
}
