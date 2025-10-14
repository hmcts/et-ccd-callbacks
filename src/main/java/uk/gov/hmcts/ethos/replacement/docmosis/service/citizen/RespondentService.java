package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils.RespondentServiceUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.DEFENDANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.CLASS_RESPONDENT_SERVICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.METHOD_REVOKE_SOLICITOR_ROLE;


/**
 * Provides services for respondents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentService {

    private final CaseSearchService caseSearchService;
    private final CaseRoleService caseRoleService;

    public CaseDetails revokeRespondentSolicitorRole(String authorization,
                                                     String caseSubmissionReference,
                                                     String respondentIndex) throws GenericServiceException {
        log.info("Revoke respondent solicitor role for case submission reference: {}", caseSubmissionReference);
        CaseDetails caseDetails = caseSearchService.findAuthorizedCaseBySubmissionReferenceAndRole(
                authorization, caseSubmissionReference, DEFENDANT);
        if (ObjectUtils.isEmpty(caseDetails) || caseDetails.getId() == null) {
            String errorMessage = String.format(
                    EXCEPTION_CASE_DETAILS_NOT_FOUND, caseSubmissionReference);
            throw new GenericServiceException(
                    errorMessage, new Exception(errorMessage), errorMessage, caseSubmissionReference,
                    CLASS_RESPONDENT_SERVICE, METHOD_REVOKE_SOLICITOR_ROLE);
        }

        String caseUserRole = null;
        try {
            caseUserRole = RespondentServiceUtils.getRespondentSolicitorType(
                    caseDetails,
                    respondentIndex
            ).getLabel();
        } catch (CallbacksRuntimeException e) {
            log.info("Case user role not found for respondent index: {}, in case with submission reference: {}. "
                            + "This maybe because respondent representative does not have any organisation defined in "
                            + "ref data.",
                    respondentIndex, caseSubmissionReference);
        }

        if (StringUtils.isNotBlank(caseUserRole)) {
            try {
                caseRoleService.revokeCaseUserRole(caseDetails, caseUserRole);
            } catch (GenericServiceException | CallbacksRuntimeException e) {
                log.info(
                        "No case user role revoked for respondent index: {}, in case with submission reference: {}. "
                                + "This maybe because respondent representative does not have any organisation defined "
                                + "in ref data.",
                        respondentIndex, caseSubmissionReference
                );
            }
        }

        return caseDetails;
    }
}
