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

    public static final String NO_RESPONDENTS_ERROR =
            "There are no respondents whose email address can be updated.";
    public static final String RESPONDENT_REQUIRED_ERROR = "Select a respondent.";
    public static final String EMAIL_UNCHANGED_ERROR =
            "Enter an email address that is different from the current email.";

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

        RespondentSumType respondent = selectedRespondent.get().getValue();
        String existingEmail = StringUtils.firstNonBlank(
                respondent.getResponseRespondentEmail(), respondent.getRespondentEmail());
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
        if (errors.isEmpty() && StringUtils.equalsIgnoreCase(
                caseData.getCurrentRespondentEmail(), caseData.getNewRespondentEmail())) {
            errors.add(EMAIL_UNCHANGED_ERROR);
        }
        return errors;
    }

    public static void applyEmailUpdate(CaseData caseData, RespondentSumType respondent, String newEmail) {
        respondent.setRespondentEmail(newEmail);
        respondent.setResponseRespondentEmail(newEmail);
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);
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
