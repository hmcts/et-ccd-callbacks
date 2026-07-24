package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Contact-only claimant email updates for represented claimants.
 * Does not change [CREATOR] / citizen portal access or solicitor roles.
 */
@Slf4j
@Service
public class RepresentedClaimantEmailService {

    static final String CLAIMANT_NOT_REPRESENTED_ERROR =
            "Update represented claimant email is only available when the claimant is represented. "
                    + "For an unrepresented claimant, use Update claimant email instead.";
    static final String EMAIL_UNCHANGED_ERROR = "Enter an email address that is different from the current email.";

    public List<String> initialise(CaseData caseData) {
        List<String> errors = validateClaimantIsRepresented(caseData);
        if (errors.isEmpty()) {
            caseData.setNewClaimantEmail(null);
            caseData.setCurrentClaimantEmail(caseData.getClaimantType() == null
                    ? null
                    : caseData.getClaimantType().getClaimantEmailAddress());
        }
        return errors;
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = validateClaimantIsRepresented(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }
        return validateEmailInput(caseData);
    }

    public List<String> prepareUpdate(CaseData caseData) {
        List<String> errors = validateNewEmail(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }
        applyEmailUpdate(caseData, caseData.getNewClaimantEmail());
        log.info("Updated represented claimant contact email (no case access change)");
        return errors;
    }

    private List<String> validateClaimantIsRepresented(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (!YES.equals(caseData.getClaimantRepresentedQuestion())) {
            errors.add(CLAIMANT_NOT_REPRESENTED_ERROR);
        }
        return errors;
    }

    private List<String> validateEmailInput(CaseData caseData) {
        List<String> errors = new ArrayList<>(ReferralHelper.validateEmail(caseData.getNewClaimantEmail()));
        if (errors.isEmpty() && StringUtils.equalsIgnoreCase(
                caseData.getCurrentClaimantEmail(), caseData.getNewClaimantEmail())) {
            errors.add(EMAIL_UNCHANGED_ERROR);
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
}
