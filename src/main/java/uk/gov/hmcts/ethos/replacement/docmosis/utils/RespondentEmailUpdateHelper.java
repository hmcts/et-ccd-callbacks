package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

/**
 * Shared helpers for the Update respondent email event (LiP and represented).
 */
public final class RespondentEmailUpdateHelper {

    // Shown when the case has no eligible respondents for this event.
    public static final String NO_RESPONDENTS_ERROR =
            "There are no respondents whose email address can be updated.";
    // Shown when the user continues without selecting a respondent.
    public static final String RESPONDENT_REQUIRED_ERROR = "Select a respondent.";
    // Shown when the user enters a new email that is the same as the email already on the case.
    public static final String EMAIL_UNCHANGED_ERROR =
            "Enter an email address that is different from the current respondent email address.";

    private RespondentEmailUpdateHelper() {
        // Utility classes should not have a public or default constructor.
    }

    public static List<String> initialise(CaseData caseData) {
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);

        List<DynamicValueType> respondents = getEligibleRespondents(caseData).stream()
                .map(respondent -> DynamicValueType.create(
                        respondent.getId(), respondent.getValue().getRespondentName()))
                .toList();
        caseData.setRespondentEmailUpdateSelection(DynamicFixedListType.from(respondents));

        return respondents.isEmpty() ? List.of(NO_RESPONDENTS_ERROR) : List.of();
    }

    public static List<String> populateCurrentEmail(CaseData caseData) {
        Optional<RespondentSumTypeItem> selectedRespondent = getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            caseData.setCurrentRespondentEmail(null);
            return List.of(getSelectionError(caseData));
        }

        String existingEmail = getLiveRespondentEmail(selectedRespondent.get().getValue());
        caseData.setCurrentRespondentEmail(StringUtils.defaultIfBlank(existingEmail, "No email address on case"));
        caseData.setNewRespondentEmail(null);
        return List.of();
    }

    public static List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        Optional<RespondentSumTypeItem> selectedRespondent = getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            errors.add(getSelectionError(caseData));
            return errors;
        }

        errors.addAll(ReferralHelper.validateEmail(caseData.getNewRespondentEmail()));
        if (errors.isEmpty()) {
            String existingEmail = getLiveRespondentEmail(selectedRespondent.get().getValue());
            if (StringUtils.equalsIgnoreCase(existingEmail, caseData.getNewRespondentEmail())) {
                errors.add(EMAIL_UNCHANGED_ERROR);
            }
        }
        return errors;
    }

    /**
     * Live contact email on the selected respondent (ET3 response email preferred, else respondent email).
     */
    public static String getLiveRespondentEmail(RespondentSumType respondent) {
        if (respondent == null) {
            return null;
        }
        return StringUtils.firstNonBlank(respondent.getResponseRespondentEmail(), respondent.getRespondentEmail());
    }

    public static void applyEmailUpdate(CaseData caseData, RespondentSumType respondent, String newEmail) {
        respondent.setRespondentEmail(newEmail);
        respondent.setResponseRespondentEmail(newEmail);
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);
        caseData.setRespondentEmailUpdateSelection(null);
    }

    public static List<RespondentSumTypeItem> getEligibleRespondents(CaseData caseData) {
        return emptyIfNull(caseData.getRespondentCollection()).stream()
                .filter(RespondentUtils::isValidRespondent)
                .toList();
    }

    public static Optional<RespondentSumTypeItem> getSelectedEligibleRespondent(CaseData caseData) {
        String selectedId = getSelectedRespondentId(caseData);
        if (StringUtils.isBlank(selectedId)) {
            return Optional.empty();
        }
        return getEligibleRespondents(caseData).stream()
                .filter(respondent -> selectedId.equals(respondent.getId()))
                .findFirst();
    }

    public static String getSelectedRespondentId(CaseData caseData) {
        DynamicFixedListType selection = caseData.getRespondentEmailUpdateSelection();
        return selection == null ? null : selection.getSelectedCode();
    }

    public static String getSelectionError(CaseData caseData) {
        return getEligibleRespondents(caseData).isEmpty()
                ? NO_RESPONDENTS_ERROR
                : RESPONDENT_REQUIRED_ERROR;
    }
}
