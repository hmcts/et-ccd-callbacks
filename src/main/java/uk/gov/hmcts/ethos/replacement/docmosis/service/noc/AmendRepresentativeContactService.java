package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.MyHmctsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_ROLES_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_CASE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_USER_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_NO_REPRESENTED_RESPONDENT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.SYSTEM_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils.getOrganisationAddressAsText;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils.mapOrganisationAddressToAddress;

@Slf4j
@Service("amendRepresentativeContactService")
@RequiredArgsConstructor
public class AmendRepresentativeContactService {

    private final UserIdamService userIdamService;
    private final MyHmctsService myHmctsService;
    private final CcdCaseAssignment ccdCaseAssignment;

    private static final String CLASS_NAME = AmendRepresentativeContactService.class.getSimpleName();

    /**
     * Updates the contact details (phone number and address) of the representatives for all represented respondents
     * within the given {@link CaseData} object.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates that the {@code caseData} object and {@code userToken} are not null or empty.</li>
     *     <li>Retrieves the indexes of respondents that are represented using the {@code userToken} and CCD ID from
     *     the case data.</li>
     *     <li>Iterates over the list of represented respondent indexes and updates each representative's phone number
     *     and address with the values stored in {@code caseData.getEt3ResponsePhone()} and
     *     {@code caseData.getEt3ResponseAddress()}.</li>
     * </ul>
     * <p>
     * If any validation fails, or if there are no represented respondents or the representative data is missing, a
     * {@link GenericServiceException} is thrown with relevant context.
     *
     * @param userToken           The authorization token used to authenticate service calls.
     * @param caseData            The case data containing respondent and representative information, including
     *                            ET3 response contact details.
     * @param submissionReference A unique identifier for the current case submission, used for error tracking
     *                            and logging.
     * @throws GenericServiceException if:
     *      <ul>
     *          <li>{@code caseData} is null or empty.</li>
     *          <li>{@code userToken} is blank.</li>
     *          <li>An error occurs while retrieving represented respondent indexes.</li>
     *          <li>No represented respondents are found or the representative collection is empty.</li>
     *      </ul>
     */
    public void updateRepresentativeContactDetails(String userToken, CaseData caseData, String submissionReference)
            throws GenericServiceException {
        final String methodName = "updateRepresentativeContactDetails";
        // Set the representative contact details in caseData
        if (ObjectUtils.isEmpty(caseData)) {
            throw new GenericServiceException(ERROR_CASE_DATA_NOT_FOUND,
                    new Exception(ERROR_CASE_DATA_NOT_FOUND),
                    ERROR_CASE_DATA_NOT_FOUND,
                    StringUtils.EMPTY,
                    CLASS_NAME,
                    methodName + " - caseData is null or empty");
        }
        if (StringUtils.isBlank(userToken)) {
            throw new GenericServiceException(ERROR_INVALID_USER_TOKEN,
                    new Exception(ERROR_INVALID_USER_TOKEN),
                    ERROR_INVALID_USER_TOKEN,
                    submissionReference,
                    CLASS_NAME,
                    methodName + " - userToken is blank");
        }
        if (REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS.equals(
                caseData.getRepresentativeContactChangeOption())) {
            setRepresentativeMyHmctsContactAddress(userToken, caseData, submissionReference);
        }
        List<Integer> representedRespondentIndexes;
        try {
            representedRespondentIndexes = getRepresentedRespondentIndexes(userToken, submissionReference);
        } catch (GenericServiceException gex) {
            throw new GenericServiceException(gex.getMessage(),
                    new Exception(gex),
                    gex.getMessage(),
                    submissionReference,
                    CLASS_NAME,
                    methodName + " - getRepresentedRespondentIndexes failed");
        } catch (NoSuchElementException nse) {
            throw new GenericServiceException(SYSTEM_ERROR,
                    new Exception(nse),
                    nse.getMessage(),
                    submissionReference,
                    CLASS_NAME,
                    methodName + " - NoSuchElementException");
        }
        if (org.apache.commons.collections.CollectionUtils.isEmpty(representedRespondentIndexes)
                || org.apache.commons.collections.CollectionUtils.isEmpty(caseData.getRepCollection())) {
            throw new GenericServiceException(ERROR_NO_REPRESENTED_RESPONDENT_FOUND,
                    new Exception(ERROR_NO_REPRESENTED_RESPONDENT_FOUND),
                    ERROR_NO_REPRESENTED_RESPONDENT_FOUND,
                    submissionReference,
                    CLASS_NAME,
                    methodName + " - No represented respondents found");
        }
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, representedRespondentIndexes);
    }

    /**
     * Sets the representative's MyHMCTS contact address in the provided {@link CaseData} object.
     *
     * <p>
     * This method retrieves user details using the provided user token, fetches the associated
     * organisation's contact information, and then updates the ET3 response address and
     * MyHMCTS address text fields in the case data. It throws a {@link GenericServiceException}
     * if user or organisation details cannot be found.
     *
     * @param userToken            the JWT bearer token representing the authenticated user
     * @param caseData             the case data object to be updated with address information
     * @param submissionReference  the reference string for identifying the current case submission
     *
     * @throws GenericServiceException if user details or organisation contact information
     *                                  cannot be retrieved, or are missing or invalid
     */
    public void setRepresentativeMyHmctsContactAddress(String userToken, CaseData caseData, String submissionReference)
            throws GenericServiceException {
        final String methodName = "setRepresentativeMyHmctsContactAddress";
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        if (ObjectUtils.isEmpty(userDetails)) {
            throw new GenericServiceException(ERROR_USER_NOT_FOUND,
                    new Exception(ERROR_USER_NOT_FOUND),
                    ERROR_USER_NOT_FOUND,
                    submissionReference,
                    CLASS_NAME,
                    methodName + " - user not found");
        }
        OrganisationAddress organisationAddress = myHmctsService.getOrganisationAddress(userToken);
        caseData.setEt3ResponseAddress(mapOrganisationAddressToAddress(organisationAddress));
        caseData.setMyHmctsAddressText(getOrganisationAddressAsText(organisationAddress));
    }

    /**
     * Determines whether the currently authenticated user, identified by the given token, is
     * a representative (solicitor) for any respondent in the specified case.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the provided user token and case ID.</li>
     *     <li>Retrieves user details using the user token. Throws an exception if the user is not found or
     *     lacks a UID.</li>
     *     <li>Fetches the case user role assignments for the given case.</li>
     *     <li>Parses solicitor roles from the case user roles and collects their indices.</li>
     * </ul>
     *
     * @param userToken the authentication token of the user
     * @param caseId the ID of the case to check roles against
     * @return a list of integer indices indicating which respondents the user represents
     * @throws GenericServiceException if the user or required data (e.g., user details, case roles)
     *         cannot be found or parsed
     */
    public List<Integer> getRepresentedRespondentIndexes(String userToken, String caseId)
            throws GenericServiceException {
        final String methodName = "getRepresentedRespondentIndexes";
        if (StringUtils.isBlank(userToken)) {
            throw new GenericServiceException(ERROR_INVALID_USER_TOKEN,
                    new Exception(ERROR_INVALID_USER_TOKEN),
                    ERROR_INVALID_USER_TOKEN,
                    caseId,
                    CLASS_NAME,
                    methodName);
        }
        if (StringUtils.isBlank(caseId)) {
            throw new GenericServiceException(ERROR_INVALID_CASE_ID,
                    new Exception(ERROR_INVALID_CASE_ID),
                    ERROR_INVALID_CASE_ID,
                    caseId,
                    CLASS_NAME,
                    methodName);
        }
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        if (ObjectUtils.isEmpty(userDetails)) {
            throw new GenericServiceException(ERROR_USER_NOT_FOUND,
                    new Exception(ERROR_USER_NOT_FOUND),
                    ERROR_USER_NOT_FOUND,
                    caseId,
                    CLASS_NAME,
                    methodName);
        }
        if (StringUtils.isBlank(userDetails.getUid())) {
            throw new GenericServiceException(ERROR_USER_ID_NOT_FOUND,
                    new Exception(ERROR_USER_ID_NOT_FOUND),
                    ERROR_USER_ID_NOT_FOUND,
                    caseId,
                    CLASS_NAME,
                    methodName);
        }
        CaseUserAssignmentData caseUserAssignmentData;
        try {
            caseUserAssignmentData = ccdCaseAssignment.getCaseUserRoles(caseId);
        } catch (IOException ioe) {
            throw new GenericServiceException(SYSTEM_ERROR,
                    new Exception(ioe),
                    ioe.getMessage(),
                    caseId,
                    CLASS_NAME,
                    methodName);
        }
        if (ObjectUtils.isEmpty(caseUserAssignmentData)) {
            throw new GenericServiceException(ERROR_CASE_ROLES_NOT_FOUND,
                    new Exception(ERROR_CASE_ROLES_NOT_FOUND),
                    ERROR_CASE_ROLES_NOT_FOUND,
                    caseId, CLASS_NAME,
                    methodName);
        }
        List<Integer> solicitorIndexList = new ArrayList<>();
        List<CaseUserAssignment> caseUserAssignments = RoleUtils
                .findLastAssignmentsBySolicitorRole(caseUserAssignmentData);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(caseUserAssignments)) {
            throw new GenericServiceException(ERROR_CASE_ROLES_NOT_FOUND,
                    new Exception(ERROR_CASE_ROLES_NOT_FOUND),
                    ERROR_CASE_ROLES_NOT_FOUND,
                    caseId, CLASS_NAME,
                    methodName);
        }
        for (CaseUserAssignment caseUserAssignment : caseUserAssignments) {
            if (userDetails.getUid().equals(caseUserAssignment.getUserId())) {
                SolicitorRole solicitorRole = SolicitorRole.from(caseUserAssignment.getCaseRole()).orElseThrow();
                solicitorIndexList.add(solicitorRole.getIndex());
            }
        }
        return solicitorIndexList;
    }
}
