package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import uk.gov.hmcts.ccd.sdk.api.CCD;

@SuppressWarnings("PMD.FieldNamingConventions")
public enum PreHearingDepositState {
    @CCD(
        label = "Open",
        hint = "# Case Number: ${caseNumber}",
        access = PreHearingDepositAccess.class
    )
    Open
}
