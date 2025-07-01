package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final DocumentManagementService documentManagementService;
    private final PdfBoxService pdfBoxService;
    private final EmailService emailService;

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
}
