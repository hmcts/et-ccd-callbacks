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

    public static final String ET3_ATTACHMENT = "ET3 Attachment";
    public static final String SHORT_DESCRIPTION = "Attached document submitted with a Response";
    public static final String ET3_CATEGORY_ID = "C18";
    private static final String INVALID_USER_TOKEN = "Invalid user token";
    private static final String INVALID_CASE_ID = "Invalid case ID";
    private static final String CASE_ROLES_NOT_FOUND = "Case roles not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String USER_ID_NOT_FOUND = "User ID not found";
    private static final String NO_REPRESENTED_RESPONDENT_FOUND = "No represented respondent found";
    private static final String CASE_DATA_NOT_FOUND = "Case data not found";
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
     * Validates the representation of respondents in the given case data and extracts
     * contact details of the first represented respondent's representative.
     *
     * <p>This method performs the following steps:
     * <ul>
     *   <li>Attempts to retrieve the list of respondent indexes that are marked as represented
     *       using the provided user token and case ID.</li>
     *   <li>If an error occurs during this retrieval (e.g. service exception or I/O issue), the
     *       error message is added to the returned list.</li>
     *   <li>If no represented respondents are found, an appropriate error message is added.</li>
     *   <li>If at least one represented respondent is found, their representative's phone number
     *       and address are extracted and stored in the case data as ET3 response details.</li>
     * </ul>
     *
     * @param userToken the user authentication token used to retrieve represented respondent indexes.
     * @param caseData the case data containing respondent and representative information.
     * @return a list of error messages if any issues are encountered during validation.
     */
    public List<String> validateAndExtractRepresentativeContact(String userToken, CaseData caseData) {
        List<Integer> representedRespondentIndexes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try {
            representedRespondentIndexes = getRepresentedRespondentIndexes(userToken, caseData.getCcdID());
        } catch (GenericServiceException | IOException ex) {
            errors.add(ex.getMessage());
        }
        if (CollectionUtils.isEmpty(representedRespondentIndexes) || representedRespondentIndexes.getFirst() == null) {
            errors.add(NO_REPRESENTED_RESPONDENT_FOUND);
        }
        RepresentedTypeR representedTypeR = caseData.getRepCollection()
                .get(representedRespondentIndexes.get(representedRespondentIndexes.getFirst())).getValue();
        caseData.setEt3ResponsePhone(representedTypeR.getRepresentativePhoneNumber());
        caseData.setEt3ResponseAddress(representedTypeR.getRepresentativeAddress());
        return errors;
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
     * @throws IOException if an error occurs during communication with downstream services
     */
    public List<Integer> getRepresentedRespondentIndexes(String userToken, String caseId)
            throws GenericServiceException, IOException {
        checkUserTokenAndCaseid(userToken, caseId);

        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        if (ObjectUtils.isEmpty(userDetails)) {
            throw new GenericServiceException(USER_NOT_FOUND, new Exception(USER_NOT_FOUND),
                    USER_NOT_FOUND, caseId, "Et3ResponseService", "isRespondentRepresentative");
        }
        if (StringUtils.isBlank(userDetails.getUid())) {
            throw new GenericServiceException(USER_ID_NOT_FOUND, new Exception(USER_ID_NOT_FOUND),
                    USER_ID_NOT_FOUND, caseId, "Et3ResponseService", "isRespondentRepresentative");
        }

        CaseUserAssignmentData caseUserAssignmentData = ccdCaseAssignment.getCaseUserRoles(caseId);
        if (ObjectUtils.isEmpty(caseUserAssignmentData)) {
            throw new GenericServiceException(CASE_ROLES_NOT_FOUND, new Exception(CASE_ROLES_NOT_FOUND),
                    CASE_ROLES_NOT_FOUND, caseId, "Et3ResponseService", "isRespondentRepresentative");
        }

        List<Integer> solicitorIndexList = new ArrayList<>();
        for (CaseUserAssignment caseUserAssignment : caseUserAssignmentData.getCaseUserAssignments()) {
            if (userDetails.getUid().equals(caseUserAssignment.getUserId())) {
                SolicitorRole solicitorRole = SolicitorRole.from(caseUserAssignment.getCaseRole()).orElseThrow();
                solicitorIndexList.add(solicitorRole.getIndex());
            }
        }
        return solicitorIndexList;
    }

    private static void checkUserTokenAndCaseid(String userToken, String caseId) throws GenericServiceException {
        if (StringUtils.isBlank(userToken)) {
            throw new GenericServiceException(INVALID_USER_TOKEN, new Exception(INVALID_USER_TOKEN),
                    INVALID_USER_TOKEN, StringUtils.EMPTY, "Et3ResponseService", "isRespondentRepresentative");
        }
        if (StringUtils.isBlank(caseId)) {
            throw new GenericServiceException(INVALID_CASE_ID, new Exception(INVALID_CASE_ID),
                    INVALID_CASE_ID, caseId, "Et3ResponseService", "isRespondentRepresentative");
        }
    }

    /**
     * Updates the contact details (phone number and address) of representatives for the respondents
     * that are marked as represented in the case data. If an error occurs during retrieval of
     * represented respondent indexes or if no such respondents are found, appropriate error messages
     * are returned.
     *
     * <p>The method performs the following operations:
     * <ul>
     *   <li>Attempts to retrieve indexes of represented respondents using the provided user token and case ID.</li>
     *   <li>If retrieval fails or no represented respondents are found, adds corresponding error messages
     *   to the list.</li>
     *   <li>For each represented respondent index, if the representative exists, updates their phone number and address
     *       with the ET3 response contact details from the case data.</li>
     * </ul>
     *
     * @param userToken the user authentication token used to fetch represented respondent indexes.
     * @param caseData the case data object containing respondent representatives and ET3 response details.
     * @return a list of error messages encountered during the update process, if any.
     */
    public List<String> setRespondentRepresentsContactDetails(String userToken, CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (ObjectUtils.isEmpty(caseData)) {
            errors.add(CASE_DATA_NOT_FOUND);
            return errors;
        }
        if (StringUtils.isBlank(userToken)) {
            errors.add(INVALID_USER_TOKEN);
            return errors;
        }
        List<Integer> representedRespondentIndexes;
        try {
            representedRespondentIndexes = getRepresentedRespondentIndexes(userToken, caseData.getCcdID());
        } catch (GenericServiceException gex) {
            errors.add(gex.getCause().getMessage());
            return errors;
        } catch (NoSuchElementException | IOException ex) {
            errors.add(ex.getMessage());
            return errors;
        }
        if (representedRespondentIndexes.isEmpty() || CollectionUtils.isEmpty(caseData.getRepCollection())) {
            errors.add(NO_REPRESENTED_RESPONDENT_FOUND);
            return errors;
        }
        for (int i : representedRespondentIndexes) {
            if (ObjectUtils.isEmpty(caseData.getRepCollection().get(i))
                    || ObjectUtils.isEmpty(caseData.getRepCollection().get(i).getValue())) {
                continue;
            }
            caseData.getRepCollection().get(i).getValue().setRepresentativePhoneNumber(caseData.getEt3ResponsePhone());
            caseData.getRepCollection().get(i).getValue().setRepresentativeAddress(caseData.getEt3ResponseAddress());
        }
        return errors;
    }
}
