package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

public final class ClaimantTellSomethingElseHelper {

    private static final String TSE_APP_WITHDRAW_CLAIM = "Withdraw all or part of claim";

    private ClaimantTellSomethingElseHelper() {
    }

    public static TSEApplicationTypeData getSelectedApplicationType(CaseData caseData) {
        switch (caseData.getClaimantTseSelectApplication()) {
            case TSE_APP_WITHDRAW_CLAIM:
                return new TSEApplicationTypeData(
                        caseData.getClaimantTseDocument13(), caseData.getClaimantTseTextBox13());
            default:
                throw new IllegalArgumentException(String.format("Unexpected application type %s",
                        caseData.getResTseSelectApplication()));
        }
    }
}
