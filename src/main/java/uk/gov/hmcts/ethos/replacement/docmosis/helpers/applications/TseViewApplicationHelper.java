package uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.RESPONDENT_REP_TITLE;

@Slf4j
public final class TseViewApplicationHelper {
    private TseViewApplicationHelper() {
        // Access through static methods
    }

    /**
     * Populates a dynamic list with either open or closed applications
     * for the tell something else 'view an application' dropdown selector.
     *
     * @param caseData - the caseData contains the values for the case
     * @return DynamicFixedListType
     */
    public static DynamicFixedListType populateOpenOrClosedApplications(CaseData caseData, boolean isClaimant) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        boolean selectedClosed = CLOSED_STATE.equals(caseData.getTseViewApplicationOpenOrClosed());

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
                .filter(o -> selectedClosed == CLOSED_STATE.equals(o.getValue().getStatus()))
                .filter(o -> isClaimant
                        ? TseViewApplicationHelper.applicationsSharedWithClaimant(o)
                        : TseViewApplicationHelper.applicationsSharedWithRespondent(o))
                .map(TseViewApplicationHelper::formatDropdownOption)
                .toList());
    }

    /**
     * Checks if application is viewable for respondent.
     *
     * @param applicationTypeItem - generic application type
     */
    public static boolean applicationsSharedWithRespondent(GenericTseApplicationTypeItem applicationTypeItem) {
        String applicant = applicationTypeItem.getValue().getApplicant();
        boolean isRespondentOrRespondentRep =
                RESPONDENT_TITLE.equals(applicant) || RESPONDENT_REP_TITLE.equals(applicant);
        String copyToRespondent = applicationTypeItem.getValue().getCopyToOtherPartyYesOrNo();
        boolean isClaimantAndRule92Shared = (CLAIMANT_TITLE.equals(applicant) || CLAIMANT_REP_TITLE.equals(applicant))
                && YES.equals(copyToRespondent);

        return isRespondentOrRespondentRep || isClaimantAndRule92Shared;
    }

    /**
     * Checks if application is viewable for respondent.
     *
     * @param applicationTypeItem - generic application type
     */
    public static boolean applicationsSharedWithClaimant(GenericTseApplicationTypeItem applicationTypeItem) {
        String applicant = applicationTypeItem.getValue().getApplicant();
        String copyToOtherParty = applicationTypeItem.getValue().getCopyToOtherPartyYesOrNo();
        boolean isRespondentAndRule92Shared = RESPONDENT_TITLE.equals(applicant)
                && YES.equals(copyToOtherParty);

        return CLAIMANT_REP_TITLE.equals(applicant) || CLAIMANT_TITLE.equals(applicant) || isRespondentAndRule92Shared;
    }

    private static DynamicValueType formatDropdownOption(GenericTseApplicationTypeItem genericTseApplicationTypeItem) {
        GenericTseApplicationType value = genericTseApplicationTypeItem.getValue();
        return DynamicValueType.create(value.getNumber(), String.format("%s %s", value.getNumber(), value.getType()));
    }
}
