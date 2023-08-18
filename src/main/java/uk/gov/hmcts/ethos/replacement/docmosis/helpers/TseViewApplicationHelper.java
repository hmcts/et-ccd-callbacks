package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

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
    public static DynamicFixedListType populateOpenOrClosedApplications(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        boolean selectedClosed = CLOSED_STATE.equals(caseData.getTseViewApplicationOpenOrClosed());

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
                .filter(o -> selectedClosed == CLOSED_STATE.equals(o.getValue().getStatus()))
                .filter(TseViewApplicationHelper::applicationsSharedWithRespondent)
                .map(TseViewApplicationHelper::formatDropdownOption)
                .toList());
    }

    /**
     * Checks if application is viewable for respondent.
     *
     * @param applicationTypeItem - generic application type
     */
    public static boolean applicationsSharedWithRespondent(TypeItem<GenericTseApplicationType> applicationTypeItem) {
        String applicant = applicationTypeItem.getValue().getApplicant();
        String copyToRespondent = applicationTypeItem.getValue().getCopyToOtherPartyYesOrNo();
        boolean isClaimantAndRule92Shared = CLAIMANT_TITLE.equals(applicant)
                && YES.equals(copyToRespondent);

        return RESPONDENT_TITLE.equals(applicant) || isClaimantAndRule92Shared;
    }

    private static DynamicValueType formatDropdownOption(TypeItem<GenericTseApplicationType> applicationTypeItem) {
        GenericTseApplicationType value = applicationTypeItem.getValue();
        return DynamicValueType.create(value.getNumber(), String.format("%s %s", value.getNumber(), value.getType()));
    }
}
