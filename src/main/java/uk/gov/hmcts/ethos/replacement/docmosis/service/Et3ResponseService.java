package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_ROLES_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_CASE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_USER_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_NO_REPRESENTED_RESPONDENT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ET3_CATEGORY_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.SHORT_DESCRIPTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.SYSTEM_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.ET3_RESPONSE_PDF_FILE_NAME;

/**
 * Service to support ET3 Response journey. Contains methods for generating and saving ET3 Response documents.
 */
@Slf4j
@Service("et3ResponseService")
@RequiredArgsConstructor
public class Et3ResponseService {

    private final DocumentManagementService documentManagementService;
    private final PdfBoxService pdfBoxService;
    private final EmailService emailService;
    private final UserIdamService userIdamService;
    private final CcdCaseAssignment ccdCaseAssignment;

    @Value("${template.et3Response.tribunal}")
    private String et3EmailTribunalTemplateId;

    @Value("${pdf.et3form}")
    private String et3FormTemplate;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    /**
     * This method calls the tornado service to generate the PDF for the ET3 Response journey.
     * @param caseData where the data is stored
     * @param userToken user authentication token
     * @param caseTypeId reference which caseType the document will be uploaded to
     * @return DocumentInfo which contains the URL and description of the document uploaded to DM Store
     */
    public DocumentInfo generateEt3ResponseDocument(CaseData caseData, String userToken, String caseTypeId,
                                                    String event) {
        try {
            return pdfBoxService.generatePdfDocumentInfo(
                    caseData, userToken, caseTypeId, ET3_RESPONSE_PDF_FILE_NAME, et3FormTemplate, event);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    /**
     * Saves the generated ET3 Response form document in the document collection and the respondent.
     * @param caseData where the data is stored
     */
    public void saveEt3Response(CaseData caseData, DocumentInfo documentInfo) {
        UploadedDocumentType uploadedDocument = documentManagementService.addDocumentToDocumentField(documentInfo);
        uploadedDocument.setCategoryId(ET3_CATEGORY_ID);
        saveEt3DetailsToRespondent(caseData, uploadedDocument);
    }

    private void saveEt3DetailsToRespondent(CaseData caseData, UploadedDocumentType uploadedDocument) {
        String respondentSelected = caseData.getSubmitEt3Respondent().getSelectedLabel().trim();

        Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
                .filter(r -> respondentSelected.equals(r.getValue().getRespondentName().trim()))
                .findFirst();
        if (respondent.isPresent()) {
            respondent.get().getValue().setEt3Form(uploadedDocument);
            respondent.get().getValue().setResponseReceived(YES);
            respondent.get().getValue().setResponseReceivedDate(LocalDate.now().toString());
            if (YES.equals(respondent.get().getValue().getExtensionRequested())
                    && YES.equals(respondent.get().getValue().getExtensionGranted())) {
                respondent.get().getValue().setExtensionResubmitted(YES);
            }
            for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                if (respondentSelected.equals(respondentSumTypeItem.getValue().getRespondentName())) {
                    respondentSumTypeItem.setValue(respondent.get().getValue());
                }
            }
        }
    }

    public void saveRelatedDocumentsToDocumentCollection(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }

        List<DocumentTypeItem> documents = caseData.getDocumentCollection();
        //Respondent Contest Claim - support doc
        if (caseData.getEt3ResponseRespondentContestClaim() != null) {
            for (DocumentTypeItem docTypeItem : Optional.ofNullable(caseData.getEt3ResponseContestClaimDocument())
                    .orElseGet(List::of)) {
                if (!isExistingDoc(documents, docTypeItem.getValue().getUploadedDocument())) {
                    documents.add(getDocumentTypeItemDetails(docTypeItem.getValue().getUploadedDocument(),
                            "Respondent Contest Claim."));
                }
            }
        }

        //ECC support doc
        if (caseData.getEt3ResponseEmployerClaimDocument() != null
                && !isExistingDoc(documents, caseData.getEt3ResponseEmployerClaimDocument())) {
            documents.add(getDocumentTypeItemDetails(caseData.getEt3ResponseEmployerClaimDocument(),
                    "Employer Claim."));
        }

        //Support needed - support doc
        if (caseData.getEt3ResponseRespondentSupportDocument() != null
                && !isExistingDoc(documents, caseData.getEt3ResponseRespondentSupportDocument())) {
            documents.add(getDocumentTypeItemDetails(caseData.getEt3ResponseRespondentSupportDocument(),
                    "Respondent Support."));
        }
    }

    private boolean isExistingDoc(List<DocumentTypeItem> documents, UploadedDocumentType uploadedDocType) {
        return documents.stream()
                .anyMatch(doc -> {
                    UploadedDocumentType uploadedDocument = doc.getValue().getUploadedDocument();
                    return uploadedDocument.getDocumentBinaryUrl().equals(uploadedDocType.getDocumentBinaryUrl())
                            && uploadedDocument.getDocumentFilename().equals(uploadedDocType.getDocumentFilename())
                            && uploadedDocument.getDocumentUrl().equals(uploadedDocType.getDocumentUrl());
                });
    }

    private static DocumentTypeItem getDocumentTypeItemDetails(UploadedDocumentType uploadedDocType,
                                                               String docSubGroup) {
        return createDocumentTypeItemFromTopLevel(uploadedDocType, RESPONSE_TO_A_CLAIM, ET3_ATTACHMENT,
                String.format("%s : %s", SHORT_DESCRIPTION, docSubGroup));
    }

    /**
     * Sends notification emails to Tribunal.
     * @param caseDetails Contains details about the case.
     */
    public void sendNotifications(CaseDetails caseDetails) {
        emailService.sendEmail(
            et3EmailTribunalTemplateId,
            caseDetails.getCaseData().getTribunalCorrespondenceEmail(),
            buildPersonalisation(caseDetails)
        );
    }

    // todo group all personalisation methods and NotificationServiceConstants to EmailService or to a
    // PersonalisationService. Might even be able to join into one single method that prepares all the data for all
    // emails (unless some of it is expensive / bespoke / overrides in which case send a map parameter of overrides)
    private Map<String, String> buildPersonalisation(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("case_number", caseData.getEthosCaseReference());
        personalisation.put(CLAIMANT, caseData.getClaimant());
        personalisation.put("list_of_respondents", getRespondentNames(caseData));
        personalisation.put(DATE, ReferralHelper.getNearestHearingToReferral(caseData, "Not set"));
        personalisation.put(LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId()));
        // TODO: Current templates in environments expect a ccdId - this should be removed later
        personalisation.put("ccdId", caseDetails.getCaseId());
        return personalisation;
    }

    // todo probably should be replaced with Helper.getRespondentNames
    private static String getRespondentNames(CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .map(o -> o.getValue().getRespondentName())
            .collect(Collectors.joining(", "));
    }

    /**
     * Validates the presence of a represented respondent for a given case and extracts the representative's contact
     * details (phone number and address) into the provided {@link CaseData} object.
     * <p>
     * This method performs the following:
     * <ul>
     *     <li>Retrieves the list of respondent indexes that are represented, using the provided user token and
     *     submission reference.</li>
     *     <li>Validates that there is at least one represented respondent with a non-null index.</li>
     *     <li>Extracts the phone number and address of the representative associated with the first valid respondent
     *     and sets them in {@link CaseData}.</li>
     * </ul>
     * If any validation fails or the list of represented respondent indexes is empty or contains a null first element,
     * a {@link GenericServiceException} is thrown.
     *
     * @param userToken          The user authorization token required for secure communication with downstream
     *                           services.
     * @param caseData           The case data containing respondent and representative information.
     * @param submissionReference A unique reference string identifying the submission associated with this operation.
     * @throws GenericServiceException if:
     *      <ul>
     *          <li>There is an issue retrieving the represented respondent indexes.</li>
     *          <li>No represented respondent is found.</li>
     *      </ul>
     */
    public void validateAndExtractRepresentativeContact(String userToken, CaseData caseData, String submissionReference)
            throws GenericServiceException {
        List<Integer> representedRespondentIndexes;
        try {
            representedRespondentIndexes = getRepresentedRespondentIndexes(userToken, submissionReference);
        } catch (GenericServiceException gse) {
            throw new GenericServiceException(gse.getMessage(),
                    new Exception(gse),
                    gse.getMessage(),
                    submissionReference,
                    "Et3ResponseService",
                    "validateAndExtractRepresentativeContact");
        }
        if (CollectionUtils.isEmpty(representedRespondentIndexes) || representedRespondentIndexes.getFirst() == null) {
            throw new GenericServiceException(ERROR_NO_REPRESENTED_RESPONDENT_FOUND,
                    new Exception(ERROR_NO_REPRESENTED_RESPONDENT_FOUND),
                    ERROR_NO_REPRESENTED_RESPONDENT_FOUND,
                    submissionReference,
                    "Et3ResponseService",
                    "validateAndExtractRepresentativeContact");
        }
        RepresentedTypeR representedTypeR = caseData.getRepCollection()
                .get(representedRespondentIndexes.get(representedRespondentIndexes.getFirst())).getValue();
        caseData.setEt3ResponsePhone(representedTypeR.getRepresentativePhoneNumber());
        caseData.setEt3ResponseAddress(representedTypeR.getRepresentativeAddress());
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
        checkUserTokenAndCaseid(userToken, caseId);
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        if (ObjectUtils.isEmpty(userDetails)) {
            throw new GenericServiceException(ERROR_USER_NOT_FOUND,
                    new Exception(ERROR_USER_NOT_FOUND),
                    ERROR_USER_NOT_FOUND,
                    caseId,
                    "Et3ResponseService",
                    "getRepresentedRespondentIndexes");
        }
        if (StringUtils.isBlank(userDetails.getUid())) {
            throw new GenericServiceException(ERROR_USER_ID_NOT_FOUND,
                    new Exception(ERROR_USER_ID_NOT_FOUND),
                    ERROR_USER_ID_NOT_FOUND,
                    caseId,
                    "Et3ResponseService",
                    "getRepresentedRespondentIndexes");
        }
        CaseUserAssignmentData caseUserAssignmentData;
        try {
            caseUserAssignmentData = ccdCaseAssignment.getCaseUserRoles(caseId);
        } catch (IOException ioe) {
            log.info("******* hereeeeeeee");
            throw new GenericServiceException(SYSTEM_ERROR,
                    new Exception(ioe),
                    ioe.getMessage(),
                    caseId,
                    "Et3ResponseService",
                    "getRepresentedRespondentIndexes");
        }
        if (ObjectUtils.isEmpty(caseUserAssignmentData)) {
            throw new GenericServiceException(ERROR_CASE_ROLES_NOT_FOUND,
                    new Exception(ERROR_CASE_ROLES_NOT_FOUND),
                    ERROR_CASE_ROLES_NOT_FOUND,
                    caseId, "Et3ResponseService",
                    "getRepresentedRespondentIndexes");
        }
        List<Integer> solicitorIndexList = new ArrayList<>();
        for (CaseUserAssignment caseUserAssignment : caseUserAssignmentData.getCaseUserAssignments()) {
            if (userDetails.getUid().equals(caseUserAssignment.getUserId())) {
                log.info("******* CASE ROLE = " + caseUserAssignment.getCaseRole());
                SolicitorRole solicitorRole = SolicitorRole.from(caseUserAssignment.getCaseRole()).orElseThrow();
                solicitorIndexList.add(solicitorRole.getIndex());
            }
        }
        return solicitorIndexList;
    }

    private static void checkUserTokenAndCaseid(String userToken, String caseId) throws GenericServiceException {
        if (StringUtils.isBlank(userToken)) {
            throw new GenericServiceException(ERROR_INVALID_USER_TOKEN,
                    new Exception(ERROR_INVALID_USER_TOKEN),
                    ERROR_INVALID_USER_TOKEN,
                    caseId,
                    "Et3ResponseService",
                    "checkUserTokenAndCaseid");
        }
        if (StringUtils.isBlank(caseId)) {
            throw new GenericServiceException(ERROR_INVALID_CASE_ID,
                    new Exception(ERROR_INVALID_CASE_ID),
                    ERROR_INVALID_CASE_ID,
                    caseId,
                    "Et3ResponseService",
                    "checkUserTokenAndCaseid");
        }
    }

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
    public void setRespondentRepresentsContactDetails(String userToken, CaseData caseData, String submissionReference)
            throws GenericServiceException {
        if (ObjectUtils.isEmpty(caseData)) {
            throw new GenericServiceException(ERROR_CASE_DATA_NOT_FOUND,
                    new Exception(ERROR_CASE_DATA_NOT_FOUND),
                    ERROR_CASE_DATA_NOT_FOUND,
                    StringUtils.EMPTY,
                    "Et3ResponseService",
                    "setRespondentRepresentsContactDetails - caseData is null or empty");
        }
        if (StringUtils.isBlank(userToken)) {
            throw new GenericServiceException(ERROR_INVALID_USER_TOKEN,
                    new Exception(ERROR_INVALID_USER_TOKEN),
                    ERROR_INVALID_USER_TOKEN,
                    submissionReference,
                    "Et3ResponseService",
                    "setRespondentRepresentsContactDetails - userToken is blank");
        }
        List<Integer> representedRespondentIndexes;
        try {
            representedRespondentIndexes = getRepresentedRespondentIndexes(userToken, submissionReference);
        } catch (GenericServiceException gex) {
            throw new GenericServiceException(gex.getMessage(),
                    new Exception(gex),
                    gex.getMessage(),
                    submissionReference,
                    "Et3ResponseService",
                    "setRespondentRepresentsContactDetails - getRepresentedRespondentIndexes failed");
        } catch (NoSuchElementException nse) {
            throw new GenericServiceException(SYSTEM_ERROR,
                    new Exception(nse),
                    nse.getMessage(),
                    submissionReference,
                    "Et3ResponseService",
                    "setRespondentRepresentsContactDetails - NoSuchElementException");
        }
        if (representedRespondentIndexes.isEmpty() || CollectionUtils.isEmpty(caseData.getRepCollection())) {
            throw new GenericServiceException(ERROR_NO_REPRESENTED_RESPONDENT_FOUND,
                    new Exception(ERROR_NO_REPRESENTED_RESPONDENT_FOUND),
                    ERROR_NO_REPRESENTED_RESPONDENT_FOUND,
                    submissionReference,
                    "Et3ResponseService",
                    "setRespondentRepresentsContactDetails - No represented respondents found");
        }
        for (int i : representedRespondentIndexes) {
            if (ObjectUtils.isEmpty(caseData.getRepCollection().get(i))
                    || ObjectUtils.isEmpty(caseData.getRepCollection().get(i).getValue())) {
                continue;
            }
            caseData.getRepCollection().get(i).getValue().setRepresentativePhoneNumber(caseData.getEt3ResponsePhone());
            caseData.getRepCollection().get(i).getValue().setRepresentativeAddress(caseData.getEt3ResponseAddress());
        }
    }
}
