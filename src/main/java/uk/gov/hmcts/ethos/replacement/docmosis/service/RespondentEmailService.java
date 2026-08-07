package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentEmailUpdateHelper;

import java.util.List;
import java.util.Optional;

/**
 * Updates respondent contact email and grants or reassigns [DEFENDANT] case access for both
 * LiP and represented respondents. Solicitor roles are not changed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentEmailService {

    private static final PartyEmailUpdateSpec SPEC = PartyEmailUpdateSpec.respondent();
    private static final PartyEmailMessages MESSAGES = SPEC.messages();

    public static final String NO_RESPONDENTS_ERROR = RespondentEmailUpdateHelper.NO_RESPONDENTS_ERROR;
    public static final String RESPONDENT_REQUIRED_ERROR = RespondentEmailUpdateHelper.RESPONDENT_REQUIRED_ERROR;
    public static final String EMAIL_UNCHANGED_ERROR = RespondentEmailUpdateHelper.EMAIL_UNCHANGED_ERROR;
    public static final String IDAM_USER_NOT_FOUND_ERROR = MESSAGES.idamUserNotFoundError();
    public static final String IDAM_USER_AMBIGUOUS_ERROR = MESSAGES.idamUserAmbiguousError();
    public static final String IDAM_USER_NOT_CITIZEN_ERROR = MESSAGES.idamUserNotCitizenError();
    public static final String IDAM_USER_LOOKUP_ERROR = MESSAGES.idamUserLookupError();
    public static final String ACCESS_LOOKUP_ERROR = MESSAGES.accessLookupError();
    public static final String ACCESS_REVOKE_ERROR = MESSAGES.accessRevokeError();
    public static final String ACCESS_GRANT_ERROR = MESSAGES.accessGrantError();
    public static final String EMAIL_UPDATE_AFTER_REASSIGN_ERROR = MESSAGES.emailUpdateAfterReassignError();
    public static final String EMAIL_UPDATE_AFTER_GRANT_ERROR = MESSAGES.emailUpdateAfterGrantError();
    public static final String EMAIL_UPDATE_ERROR = MESSAGES.emailUpdateError();

    private final PartyEmailUpdateSupport partyEmailUpdateSupport;

    public List<String> initialise(CaseData caseData) {
        return RespondentEmailUpdateHelper.initialise(caseData);
    }

    public List<String> populateCurrentEmail(CaseData caseData) {
        return RespondentEmailUpdateHelper.populateCurrentEmail(caseData);
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = RespondentEmailUpdateHelper.validateInput(caseData);
        if (errors.isEmpty()) {
            partyEmailUpdateSupport.findCitizenUserByEmail(caseData.getNewRespondentEmail(), MESSAGES, errors);
        }
        return errors;
    }

    public List<String> prepareUpdate(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = RespondentEmailUpdateHelper.validateInput(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<RespondentSumTypeItem> selectedRespondent =
                RespondentEmailUpdateHelper.getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            return List.of(RespondentEmailUpdateHelper.getSelectionError(caseData));
        }

        Optional<UserDetails> newUser = partyEmailUpdateSupport.findCitizenUserByEmail(
                caseData.getNewRespondentEmail(), MESSAGES, errors);
        if (CollectionUtils.isNotEmpty(errors) || newUser.isEmpty()) {
            return errors;
        }

        RespondentSumTypeItem respondentItem = selectedRespondent.get();
        AccessOutcome accessOutcome;
        try {
            accessOutcome = partyEmailUpdateSupport.ensureCaseAccess(
                    caseDetails.getCaseId(),
                    respondentItem.getValue().getIdamId(),
                    newUser.get().getUid(),
                    SPEC);
            respondentItem.getValue().setIdamId(newUser.get().getUid());
        } catch (CcdInputOutputException exception) {
            log.error("Unable to update defendant access for case {}", caseDetails.getCaseId(), exception);
            errors.add(exception.getMessage());
            return errors;
        }

        try {
            RespondentEmailUpdateHelper.applyEmailUpdate(
                    caseData, respondentItem.getValue(), caseData.getNewRespondentEmail());
        } catch (RuntimeException exception) {
            log.error("Defendant access outcome {} but email could not be updated for case {}",
                    accessOutcome, caseDetails.getCaseId(), exception);
            errors.add(partyEmailUpdateSupport.emailUpdateFailureMessage(accessOutcome, MESSAGES));
        }
        return errors;
    }
}
