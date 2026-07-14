package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.et.common.model.ccd.SingleDefinition;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesSingleDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandSingleDefinition;

@SuppressWarnings("PMD.FieldNamingConventions")
public enum CaseState {
    @CCD(
            label = "Submitted",
            omitDescription = true,
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 1,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Submitted",
            description = "Initial case state – create title as a minimum; add Applicant details, etc.",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 1,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    Submitted,
    @CCD(
            label = "Awaiting Submission to HMCTS",
            description = "Draft ET1 updates and this state carried till case submission ",
            hint = "# Digital Reference: ${[CASE_REFERENCE]}",
            displayOrder = 2,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Awaiting Submission to HMCTS",
            description = "Draft ET1 updates and this state carried till case submission",
            hint = "# Digital Reference: ${[CASE_REFERENCE]}",
            displayOrder = 2,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    AWAITING_SUBMISSION_TO_HMCTS,
    @CCD(
            label = "Vetted",
            description = "Case Vetted",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 3,
            includeInProfiles = SingleDefinition.class
    )
    Vetted,
    @CCD(
            label = "Accepted",
            description = "Case Accepted",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 4,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Accepted",
            description = "Case accepted – all application details are in order",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 4,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    Accepted,
    @CCD(
            label = "Closed",
            description = "Case Closed",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 5,
            includeInProfiles = SingleDefinition.class
    )
    Closed,
    @CCD(
            label = "Rejected",
            description = "Case Rejected",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 6,
            includeInProfiles = SingleDefinition.class
    )
    Rejected,
    @CCD(
            label = "Transferred",
            description = "Case Transferred",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 7,
            includeInProfiles = SingleDefinition.class
    )
    Transferred,
    @CCD(
            label = "Delete",
            description = "Cases to be deleted",
            hint = "# Case Number:${ethosCaseReference}</br><h1>${claimant} v ${respondent}<br>${flagsImageAltText}</h1>",
            displayOrder = 7,
            includeInProfiles = SingleDefinition.class
    )
    Delete
}
