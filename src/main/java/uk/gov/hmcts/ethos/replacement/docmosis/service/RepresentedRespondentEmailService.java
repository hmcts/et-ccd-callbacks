package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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
 * Contact-only respondent email updates for represented respondents.
 * Does not change [DEFENDANT] / respondent portal access or solicitor roles.
 */
@Slf4j
@Service
public class RepresentedRespondentEmailService {

    static final String NO_REPRESENTED_RESPONDENTS_ERROR =
            "There are no represented respondents whose email address can be updated. "
                    + "For an unrepresented respondent, use Update respondent email instead.";
    static final String RESPONDENT_REQUIRED_ERROR = "Select a respondent.";
    static final String EMAIL_UNCHANGED_ERROR = "Enter an email address that is different from the current email.";

    public List<String> initialise(CaseData caseData) {
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);

        List<DynamicValueType> respondents = getEligibleRespondents(caseData).stream()
                .map(respondent -> DynamicValueType.create(
                        respondent.getId(), respondent.getValue().getRespondentName()))
                .toList();
        caseData.setRespondentEmailUpdateSelection(DynamicFixedListType.from(respondents));

        return respondents.isEmpty() ? List.of(NO_REPRESENTED_RESPONDENTS_ERROR) : List.of();
    }

    public List<String> populateCurrentEmail(CaseData caseData) {
        Optional<RespondentSumTypeItem> selectedRespondent = getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            caseData.setCurrentRespondentEmail(null);
            return List.of(getSelectionError(caseData));
        }

        RespondentSumType respondent = selectedRespondent.get().getValue();
        caseData.setCurrentRespondentEmail(StringUtils.firstNonBlank(
                respondent.getResponseRespondentEmail(), respondent.getRespondentEmail()));
        caseData.setNewRespondentEmail(null);
        return List.of();
    }

    public List<String> validateNewEmail(CaseData caseData) {
        return validateInput(caseData);
    }

    public List<String> prepareUpdate(CaseData caseData) {
        List<String> errors = validateInput(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<RespondentSumTypeItem> selectedRespondent = getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            return List.of(getSelectionError(caseData));
        }

        applyEmailUpdate(caseData, selectedRespondent.get().getValue(), caseData.getNewRespondentEmail());
        log.info("Updated represented respondent contact email (no case access change)");
        return errors;
    }

    private void applyEmailUpdate(CaseData caseData, RespondentSumType respondent, String newEmail) {
        respondent.setRespondentEmail(newEmail);
        respondent.setResponseRespondentEmail(newEmail);
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);
    }

    private List<String> validateInput(CaseData caseData) {
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

    private List<RespondentSumTypeItem> getEligibleRespondents(CaseData caseData) {
        return emptyIfNull(caseData.getRespondentCollection()).stream()
                .filter(this::isValidRespondent)
                .filter(respondent -> isRepresented(caseData, respondent))
                .toList();
    }

    private boolean isValidRespondent(RespondentSumTypeItem respondent) {
        return respondent != null && StringUtils.isNotBlank(respondent.getId())
                && respondent.getValue() != null
                && StringUtils.isNotBlank(respondent.getValue().getRespondentName());
    }

    private boolean isRepresented(CaseData caseData, RespondentSumTypeItem respondent) {
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

    private Optional<RespondentSumTypeItem> getSelectedEligibleRespondent(CaseData caseData) {
        String selectedId = getSelectedRespondentId(caseData);
        if (StringUtils.isBlank(selectedId)) {
            return Optional.empty();
        }
        return getEligibleRespondents(caseData).stream()
                .filter(respondent -> selectedId.equals(respondent.getId()))
                .findFirst();
    }

    private String getSelectedRespondentId(CaseData caseData) {
        DynamicFixedListType selection = caseData.getRespondentEmailUpdateSelection();
        return selection == null ? null : selection.getSelectedCode();
    }

    private String getSelectionError(CaseData caseData) {
        return getEligibleRespondents(caseData).isEmpty()
                ? NO_REPRESENTED_RESPONDENTS_ERROR
                : RESPONDENT_REQUIRED_ERROR;
    }
}
