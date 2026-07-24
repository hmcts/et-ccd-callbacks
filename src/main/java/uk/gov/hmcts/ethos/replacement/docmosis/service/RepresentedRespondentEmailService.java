package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentEmailUpdateHelper;

import java.util.List;
import java.util.Optional;

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
    static final String RESPONDENT_REQUIRED_ERROR = RespondentEmailUpdateHelper.RESPONDENT_REQUIRED_ERROR;
    static final String EMAIL_UNCHANGED_ERROR = RespondentEmailUpdateHelper.EMAIL_UNCHANGED_ERROR;

    private static final boolean REPRESENTED = true;

    public List<String> initialise(CaseData caseData) {
        return RespondentEmailUpdateHelper.initialise(caseData, REPRESENTED, NO_REPRESENTED_RESPONDENTS_ERROR);
    }

    public List<String> populateCurrentEmail(CaseData caseData) {
        return RespondentEmailUpdateHelper.populateCurrentEmail(
                caseData, REPRESENTED, NO_REPRESENTED_RESPONDENTS_ERROR);
    }

    public List<String> validateNewEmail(CaseData caseData) {
        return RespondentEmailUpdateHelper.validateInput(
                caseData, REPRESENTED, NO_REPRESENTED_RESPONDENTS_ERROR);
    }

    public List<String> prepareUpdate(CaseData caseData) {
        List<String> errors = RespondentEmailUpdateHelper.validateInput(
                caseData, REPRESENTED, NO_REPRESENTED_RESPONDENTS_ERROR);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<RespondentSumTypeItem> selectedRespondent =
                RespondentEmailUpdateHelper.getSelectedEligibleRespondent(caseData, REPRESENTED);
        if (selectedRespondent.isEmpty()) {
            return List.of(RespondentEmailUpdateHelper.getSelectionError(
                    caseData, REPRESENTED, NO_REPRESENTED_RESPONDENTS_ERROR));
        }

        RespondentEmailUpdateHelper.applyEmailUpdate(
                caseData, selectedRespondent.get().getValue(), caseData.getNewRespondentEmail());
        log.info("Updated represented respondent contact email (no case access change)");
        return errors;
    }
}
