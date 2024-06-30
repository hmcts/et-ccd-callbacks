package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;

public final class ClaimantTellSomethingElseHelper {

    private ClaimantTellSomethingElseHelper() {
    }

    public static TSEApplicationTypeData getSelectedApplicationType(CaseData caseData) {
        switch (caseData.getClaimantTseSelectApplication()) {
            case CLAIMANT_TSE_WITHDRAW_CLAIM:
                return new TSEApplicationTypeData(
                        caseData.getClaimantTseDocument13(), caseData.getClaimantTseTextBox13());
            default:
                throw new IllegalArgumentException(String.format("Unexpected application type %s",
                        caseData.getResTseSelectApplication()));
        }
    }
}
