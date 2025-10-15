package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import java.util.List;

public final class DocumentConstants {

    public static final List<String> HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT = List.of(
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_VETTING,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_PROCESSING,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REFERRAL_JUDICIAL_DIRECTION,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_WITNESS_ORDER_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CONTACT_THE_TRIBUNAL_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.COT3,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.INITIAL_CONSIDERATION,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.OTHER,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NEEDS_UPDATING
    );

    public static final List<String> RESPONDENT_APPLICATION_DOC_TYPE = List.of(
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_AMEND_RESPONSE,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CHANGE_OF_PARTYS_DETAILS,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.C_HAS_NOT_COMPLIED_WITH_AN_ORDER_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants
                    .APP_TO_HAVE_A_LEGAL_OFFICER_DECISION_CONSIDERED_AFRESH_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CONTACT_THE_TRIBUNAL_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_ORDER_THE_C_TO_DO_SOMETHING,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_WITNESS_ORDER_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_POSTPONE_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_JUDGMENT_TO_BE_RECONSIDERED_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_RESTRICT_PUBLICITY_R,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_STRIKE_OUT_ALL_OR_PART_OF_THE_CLAIM,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_VARY_OR_REVOKE_AN_ORDER_R
    );

    public static final List<String> CLAIMANT_APPLICATION_DOC_TYPE = List.of(
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.WITHDRAWAL_OF_ALL_OR_PART_CLAIM,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CHANGE_OF_PARTYS_DETAILS,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_POSTPONE_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_VARY_OR_REVOKE_AN_ORDER_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants
                    .APP_TO_HAVE_A_LEGAL_OFFICER_DECISION_CONSIDERED_AFRESH_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_AMEND_CLAIM,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_ORDER_THE_R_TO_DO_SOMETHING,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_WITNESS_ORDER_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.R_HAS_NOT_COMPLIED_WITH_AN_ORDER_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_RESTRICT_PUBLICITY_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_STRIKE_OUT_ALL_OR_PART_OF_THE_RESPONSE,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_JUDGMENT_TO_BE_RECONSIDERED_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CONTACT_THE_TRIBUNAL_C
    );

    public static final List<String> HIDDEN_DOCUMENT_TYPES_FOR_RESPONDENT = List.of(
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_VETTING,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_PROCESSING,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REJECTION_OF_CLAIM,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REFERRAL_JUDICIAL_DIRECTION,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_WITNESS_ORDER_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CONTACT_THE_TRIBUNAL_C,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.COT3,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.INITIAL_CONSIDERATION,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.OTHER,
            uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NEEDS_UPDATING
    );

    private DocumentConstants() {
        // Final classes should not have a public or default constructor.
    }
}
