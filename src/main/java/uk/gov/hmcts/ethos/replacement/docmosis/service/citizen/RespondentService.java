package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils.RespondentServiceUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MapperUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_NAME_REMOVE_OWN_REPRESENTATIVE;
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
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamApi idamApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseConverter caseConverter;

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

        return removeRespondentRepresentativeFromCaseData(authorization,
                caseDetails.getCaseTypeId(),
                caseDetails.getId().toString(),
                respondentIndex,
                caseUserRole);
    }

    public CaseDetails removeRespondentRepresentativeFromCaseData(String authorisation,
                                                                  String caseTypeId,
                                                                  String caseSubmissionReference,
                                                                  String respondentIndex,
                                                                  String caseUserRole) {
        UserDetails userInfo = idamApi.retrieveUserDetails(authorisation);
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
                authorisation,
                authTokenGenerator.generate(),
                userInfo.getUid(),
                EMPLOYMENT,
                caseTypeId,
                caseSubmissionReference,
                EVENT_NAME_REMOVE_OWN_REPRESENTATIVE
        );
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        CaseData caseData = MapperUtils.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        RespondentSumTypeItem respondentSumTypeItem =
                RespondentServiceUtils.findRespondentSumTypeItemByIndex(caseData.getRespondentCollection(),
                        respondentIndex,
                        caseDetails.getId().toString());
        if (StringUtils.isNotBlank(caseUserRole)) {
            RespondentServiceUtils.resetOrganizationPolicy(caseData, caseUserRole, caseDetails.getId().toString());
        }
        respondentSumTypeItem.getValue().setRepresentativeRemoved(YES);
        RepresentedTypeRItem representativeRItem =
                RespondentServiceUtils.findRespondentRepresentative(respondentSumTypeItem,
                        caseData.getRepCollection(),
                        caseDetails.getId().toString());
        if (ObjectUtils.isNotEmpty(representativeRItem)) {
            caseData.setRepCollectionToRemove(List.of(representativeRItem));
        }
        caseDetails.setData(MapperUtils.mapCaseDataToLinkedHashMap(caseData));
        CaseDataContent caseDataContent =  caseConverter.caseDataContent(startEventResponse, caseData);
        return coreCaseDataApi.submitEventForCitizen(
                authorisation,
                authTokenGenerator.generate(),
                userInfo.getUid(),
                EMPLOYMENT,
                caseDetails.getCaseTypeId(),
                caseDetails.getId().toString(),
                true,
                caseDataContent);
    }

}
