package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import uk.gov.hmcts.ecm.common.model.helper.DocumentConstants;

import java.util.List;

public final class LegalRepDocumentConstants {
    public static final String DOCUMENT_HEADING = "Document (the documents below open in a new tab)";
    public static final String SUBMIT_ET1 = "submitEt1";
    public static final List<String> LEGAL_REP_HIDDEN_DOCS = List.of(
            DocumentConstants.ET1_VETTING,
            DocumentConstants.ET3_PROCESSING,
            DocumentConstants.INITIAL_CONSIDERATION,
            DocumentConstants.APP_FOR_A_WITNESS_ORDER_C,
            DocumentConstants.REFERRAL_JUDICIAL_DIRECTION,
            "Referral/Judicial direction",
            DocumentConstants.COT3,
            DocumentConstants.OTHER,
            DocumentConstants.REJECTION_OF_CLAIM,
            "Rejection of Claim",
            DocumentConstants.CLAIM_REJECTED,
            DocumentConstants.CONTACT_THE_TRIBUNAL_C,
            DocumentConstants.TRIBUNAL_CASE_FILE,
            DocumentConstants.APP_TO_AMEND_RESPONSE,
            DocumentConstants.CHANGE_OF_PARTYS_DETAILS,
            DocumentConstants.C_HAS_NOT_COMPLIED_WITH_AN_ORDER_R,
            DocumentConstants.APP_TO_HAVE_A_LEGAL_OFFICER_DECISION_CONSIDERED_AFRESH_R,
            DocumentConstants.CONTACT_THE_TRIBUNAL_R,
            DocumentConstants.APP_TO_ORDER_THE_C_TO_DO_SOMETHING,
            DocumentConstants.APP_FOR_A_WITNESS_ORDER_R,
            DocumentConstants.APP_TO_POSTPONE_R,
            DocumentConstants.APP_FOR_A_JUDGMENT_TO_BE_RECONSIDERED_R,
            DocumentConstants.APP_TO_RESTRICT_PUBLICITY_R,
            DocumentConstants.APP_TO_STRIKE_OUT_ALL_OR_PART_OF_THE_CLAIM,
            DocumentConstants.APP_TO_VARY_OR_REVOKE_AN_ORDER_R,
            DocumentConstants.WITHDRAWAL_OF_ALL_OR_PART_CLAIM,
            DocumentConstants.CHANGE_OF_PARTYS_DETAILS,
            DocumentConstants.APP_TO_POSTPONE_C,
            DocumentConstants.APP_TO_VARY_OR_REVOKE_AN_ORDER_C,
            DocumentConstants.APP_TO_HAVE_A_LEGAL_OFFICER_DECISION_CONSIDERED_AFRESH_C,
            DocumentConstants.APP_TO_AMEND_CLAIM,
            DocumentConstants.APP_TO_ORDER_THE_R_TO_DO_SOMETHING,
            DocumentConstants.APP_FOR_A_WITNESS_ORDER_C,
            DocumentConstants.R_HAS_NOT_COMPLIED_WITH_AN_ORDER_C,
            DocumentConstants.APP_TO_RESTRICT_PUBLICITY_C,
            DocumentConstants.APP_TO_STRIKE_OUT_ALL_OR_PART_OF_THE_RESPONSE,
            DocumentConstants.APP_FOR_A_JUDGMENT_TO_BE_RECONSIDERED_C,
            DocumentConstants.CONTACT_THE_TRIBUNAL_C
    );

    private LegalRepDocumentConstants() {
    }
}
