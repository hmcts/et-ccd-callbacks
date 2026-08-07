package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantEmailService {

    private static final PartyEmailUpdateSpec SPEC = PartyEmailUpdateSpec.claimant();
    private static final PartyEmailMessages MESSAGES = SPEC.messages();

    public static final String EMAIL_UNCHANGED_ERROR = MESSAGES.emailUnchangedError();
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

    /**
     * Updates claimant contact email and grants or reassigns [CREATOR] case access for both
     * LiP and represented claimants. Solicitor roles are not changed.
     */
    public List<String> initialise(CaseData caseData) {
        caseData.setNewClaimantEmail(null);
        caseData.setCurrentClaimantEmail(caseData.getClaimantType() == null
                ? null
                : caseData.getClaimantType().getClaimantEmailAddress());
        return List.of();
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = validateEmailInput(caseData);
        if (errors.isEmpty()) {
            partyEmailUpdateSupport.findCitizenUserByEmail(caseData.getNewClaimantEmail(), MESSAGES, errors);
        }
        return errors;
    }

    public List<String> prepareUpdate(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = validateEmailInput(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<UserDetails> newUser = partyEmailUpdateSupport.findCitizenUserByEmail(
                caseData.getNewClaimantEmail(), MESSAGES, errors);
        if (newUser.isEmpty()) {
            return errors;
        }

        AccessOutcome accessOutcome;
        try {
            accessOutcome = partyEmailUpdateSupport.ensureCaseAccess(
                    caseDetails.getCaseId(), null, newUser.get().getUid(), SPEC);
            caseData.setClaimantId(newUser.get().getUid());
        } catch (CcdInputOutputException exception) {
            log.error("Unable to update creator access for case {}", caseDetails.getCaseId(), exception);
            errors.add(exception.getMessage());
            return errors;
        }

        try {
            applyEmailUpdate(caseData, caseData.getNewClaimantEmail());
        } catch (RuntimeException exception) {
            log.error("Creator access outcome {} but email could not be updated for case {}",
                    accessOutcome, caseDetails.getCaseId(), exception);
            errors.add(partyEmailUpdateSupport.emailUpdateFailureMessage(accessOutcome, MESSAGES));
        }
        return errors;
    }

    private void applyEmailUpdate(CaseData caseData, String newEmail) {
        if (caseData.getClaimantType() == null) {
            caseData.setClaimantType(new ClaimantType());
        }
        caseData.getClaimantType().setClaimantEmailAddress(newEmail);
        caseData.setCurrentClaimantEmail(null);
        caseData.setNewClaimantEmail(null);
    }

    private List<String> validateEmailInput(CaseData caseData) {
        List<String> errors = new ArrayList<>(ReferralHelper.validateEmail(caseData.getNewClaimantEmail()));
        if (errors.isEmpty()) {
            String existingEmail = caseData.getClaimantType() == null
                    ? null
                    : caseData.getClaimantType().getClaimantEmailAddress();
            if (StringUtils.equalsIgnoreCase(existingEmail, caseData.getNewClaimantEmail())) {
                errors.add(EMAIL_UNCHANGED_ERROR);
            }
        }
        return errors;
    }
}
