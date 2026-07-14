package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state;

import uk.gov.hmcts.ccd.sdk.api.CCD;

@SuppressWarnings("PMD.FieldNamingConventions")
public enum MultipleCaseState {
    @CCD(
            label = "Open Multiple Cases",
            hint = "# Multiple Name: ${multipleName}\n# Multiple Number: ${multipleReference}")
    Open,
    @CCD(
            label = "Updating  Cases within a Multiple",
            hint = "# Multiple Name: ${multipleName}\n# Multiple Number: ${multipleReference}")
    Updating,
    @CCD(
            label = "Closed Multiple Cases",
            hint = "# Multiple Name: ${multipleName}\n# Multiple Number: ${multipleReference}")
    Closed,
    @CCD(
            label = "Error",
            hint = "# Multiple Name: ${multipleName}\n# Multiple Number: ${multipleReference}")
    Error,
    @CCD(
            label = "Transferred",
            description = "Multiple Transferred",
            hint = "# Multiple Name: ${multipleName}\n" + "# Multiple Number: ${multipleReference}")
    Transferred
}
