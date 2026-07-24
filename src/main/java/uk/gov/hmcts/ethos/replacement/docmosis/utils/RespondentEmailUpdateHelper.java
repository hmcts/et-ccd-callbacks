package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Shared helpers for respondent email update events (unrepresented and represented).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RespondentEmailUpdateHelper {

    public static final String RESPONDENT_REQUIRED_ERROR = "Select a respondent.";
    public static final String EMAIL_UNCHANGED_ERROR =
            "Enter an email address that is different from the current email.";

    public static List<String> initialise(CaseData caseData, boolean represented, String emptySelectionError) {
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);

        List<DynamicValueType> respondents = getEligibleRespondents(caseData, represented).stream()
                .map(respondent -> DynamicValueType.create(
                        respondent.getId(), respondent.getValue().getRespondentName()))
                .toList();
        caseData.setRespondentEmailUpdateSelection(DynamicFixedListType.from(respondents));

        return respondents.isEmpty() ? List.of(emptySelectionError) : List.of();
    }

    public static List<String> populateCurrentEmail(CaseData caseData,
                                                    boolean represented,
                                                    String emptySelectionError) {
        Optional<RespondentSumTypeItem> selectedRespondent =
                getSelectedEligibleRespondent(caseData, represented);
        if (selectedRespondent.isEmpty()) {
            caseData.setCurrentRespondentEmail(null);
            return List.of(getSelectionError(caseData, represented, emptySelectionError));
        }

        RespondentSumType respondent = selectedRespondent.get().getValue();
        caseData.setCurrentRespondentEmail(StringUtils.firstNonBlank(
                respondent.getResponseRespondentEmail(), respondent.getRespondentEmail()));
        caseData.setNewRespondentEmail(null);
        return List.of();
    }

    public static List<String> validateInput(CaseData caseData,
                                             boolean represented,
                                             String emptySelectionError) {
        List<String> errors = new ArrayList<>();
        Optional<RespondentSumTypeItem> selectedRespondent =
                getSelectedEligibleRespondent(caseData, represented);
        if (selectedRespondent.isEmpty()) {
            errors.add(getSelectionError(caseData, represented, emptySelectionError));
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

    public static List<RespondentSumTypeItem> getEligibleRespondents(CaseData caseData, boolean represented) {
        return emptyIfNull(caseData.getRespondentCollection()).stream()
                .filter(RespondentUtils::isValidRespondent)
                .filter(respondent -> isRepresented(caseData, respondent) == represented)
                .toList();
    }

    public static boolean isRepresented(CaseData caseData, RespondentSumTypeItem respondent) {
        RespondentSumType respondentValue = respondent.getValue();
        if (YES.equalsIgnoreCase(respondentValue.getRepresentativeRemoved())) {
            return false;
        }
        if (YES.equalsIgnoreCase(respondentValue.getRepresented())) {
            return true;
        }
        return emptyIfNull(caseData.getRepCollection()).stream()
                .filter(rep -> rep != null && rep.getValue() != null)
                .map(RepresentedTypeRItem::getValue)
                .anyMatch(rep -> respondent.getId().equals(rep.getRespondentId())
                        || respondentValue.getRespondentName().equals(rep.getRespRepName()));
    }

    public static Optional<RespondentSumTypeItem> getSelectedEligibleRespondent(CaseData caseData,
                                                                                 boolean represented) {
        String selectedId = getSelectedRespondentId(caseData);
        if (StringUtils.isBlank(selectedId)) {
            return Optional.empty();
        }
        return getEligibleRespondents(caseData, represented).stream()
                .filter(respondent -> selectedId.equals(respondent.getId()))
                .findFirst();
    }

    public static String getSelectedRespondentId(CaseData caseData) {
        DynamicFixedListType selection = caseData.getRespondentEmailUpdateSelection();
        return selection == null ? null : selection.getSelectedCode();
    }

    public static String getSelectionError(CaseData caseData,
                                           boolean represented,
                                           String emptySelectionError) {
        return getEligibleRespondents(caseData, represented).isEmpty()
                ? emptySelectionError
                : RESPONDENT_REQUIRED_ERROR;
    }
}
