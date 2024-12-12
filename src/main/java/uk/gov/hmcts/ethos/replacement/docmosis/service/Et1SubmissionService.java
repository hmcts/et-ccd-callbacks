package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.PdfServiceException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.ecm.common.model.helper.DocumentConstants;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.LegalRepDocumentConstants.SUBMIT_ET1;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItem;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getFirstListItem;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.letters.InvalidCharacterCheck.sanitizePartyName;

@Service
@Slf4j
@RequiredArgsConstructor
public class Et1SubmissionService {
    private final AcasService acasService;
    private final DocumentManagementService documentManagementService;
    private final PdfService pdfService;
    private final TornadoService tornadoService;
    private final UserIdamService userIdamService;
    private final EmailService emailService;

    @Value("${template.et1.et1ProfessionalSubmission}")
    private String et1ProfessionalSubmissionTemplateId;
    @Value("${template.et1.submitCaseEmailTemplateId}")
    private String claimantSubmissionTemplateId;
    @Value("${template.et1.cySubmitCaseEmailTemplateId}")
    private String claimantSubmissionTemplateIdWelsh;
    private static final String ET1_EN_PDF = "ET1_0224.pdf";
    private static final String ET1_CY_PDF = "CY_ET1_2222.pdf";

    /**
     * Creates the ET1 PDF and calls of to ACAS to retrieve the certificates.
     * @param caseDetails the case details
     * @param userToken the user token
     */
    public void createAndUploadEt1Docs(CaseDetails caseDetails, String userToken) {
        try {
            DocumentTypeItem englishEt1 = createEt1DocumentType(caseDetails, userToken, ET1_EN_PDF);
            DocumentTypeItem welshEt1 = null;
            if (WELSH_LANGUAGE.equals(findLanguagePreference(caseDetails.getCaseData()))) {
                welshEt1 = createEt1DocumentType(caseDetails, userToken, ET1_CY_PDF);
            }

            List<DocumentTypeItem> acasCertificates = retrieveAndAddAcasCertificates(caseDetails.getCaseData(),
                    userToken, caseDetails.getCaseTypeId());
            addDocsToClaim(caseDetails.getCaseData(), englishEt1, welshEt1, acasCertificates);
        } catch (Exception e) {
            log.error("Failed to create and upload ET1 documents", e);
        }
    }

    private void addDocsToClaim(CaseData caseData, DocumentTypeItem englishEt1, DocumentTypeItem welshEt1,
                                List<DocumentTypeItem> acasCertificates) {
        List<DocumentTypeItem> documentList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(englishEt1)) {
            documentList.add(englishEt1);
        }
        if (!ObjectUtils.isEmpty(welshEt1)) {
            documentList.add(welshEt1);
        }
        if (caseData.getEt1SectionThreeDocumentUpload() != null) {
            UploadedDocumentType et1Attachment = caseData.getEt1SectionThreeDocumentUpload();
            et1Attachment.setCategoryId(DocumentCategory.ET1_ATTACHMENT.getCategory());
            documentList.add(createDocumentTypeItem(caseData.getEt1SectionThreeDocumentUpload(), ET1_ATTACHMENT));
        } else if (!ObjectUtils.isEmpty(caseData.getClaimantRequests())
                   && !ObjectUtils.isEmpty(caseData.getClaimantRequests().getClaimDescriptionDocument())) {
            UploadedDocumentType et1Attachment = caseData.getClaimantRequests().getClaimDescriptionDocument();
            et1Attachment.setCategoryId(DocumentCategory.ET1_ATTACHMENT.getCategory());
            documentList.add(createDocumentTypeItem(caseData.getClaimantRequests().getClaimDescriptionDocument(),
                    ET1_ATTACHMENT));
        }
        if (CollectionUtils.isNotEmpty(acasCertificates)) {
            documentList.addAll(acasCertificates);
        }
        caseData.setClaimantDocumentCollection(documentList);
        caseData.setDocumentCollection(documentList);
        DocumentHelper.setDocumentNumbers(caseData);
    }

    private DocumentTypeItem createEt1DocumentType(CaseDetails caseDetails, String userToken, String pdfSource)
            throws PdfServiceException {
        DocumentInfo documentInfo = createEt1(caseDetails, userToken, pdfSource);

        UploadedDocumentType uploadedDocumentType = documentManagementService.addDocumentToDocumentField(documentInfo);
        uploadedDocumentType.setCategoryId(DocumentCategory.ET1.getCategory());
        return createDocumentTypeItem(uploadedDocumentType, ET1);
    }

    public DocumentInfo createEt1(CaseDetails caseDetails, String userToken, String pdfSource)
            throws PdfServiceException {
        // last 2 parameters don't have any effect fot the creation of ET1 PDF form. Those parameters are used
        // just for discrimination between representative and citizen client types.
        byte[] pdf = pdfService.convertCaseToPdf(
                caseDetails.getCaseData(),
                pdfSource,
                ET1,
                "claimantparty",
                SUBMIT_ET1);
        if (ObjectUtils.isEmpty(pdf)) {
            throw new PdfServiceException("Failed to create ET1 PDF", new NullPointerException());
        }
        return tornadoService.createDocumentInfoFromBytes(userToken,
                pdf,
                getEt1DocumentName(caseDetails.getCaseData(), pdfSource),
                caseDetails.getCaseTypeId());
    }

    private String getEt1DocumentName(CaseData caseData, String pdfSource) {
        return ET1_CY_PDF.equals(pdfSource) ? "ET1 CY - " + caseData.getClaimant() + ".pdf"
                : "ET1 - " + sanitizePartyName(caseData.getClaimant()) + ".pdf";
    }

    private List<DocumentTypeItem> retrieveAndAddAcasCertificates(CaseData caseData, String userToken,
                                                                  String caseTypeId) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return new ArrayList<>();
        }
        List<String> acasNumbers = caseData.getRespondentCollection().stream()
                .filter(respondent -> !isNullOrEmpty(respondent.getValue().getRespondentAcas()))
                .map(respondent -> respondent.getValue().getRespondentAcas())
                .toList();

        if (CollectionUtils.isEmpty(acasNumbers)) {
            return new ArrayList<>();
        }

        List<DocumentInfo> documentInfoList = acasNumbers.stream()
                .map(acasNumber -> {
                    try {
                        return acasService.getAcasCertificates(caseData, acasNumber, userToken, caseTypeId);
                    } catch (Exception e) {
                        log.error("Failed to process ACAS Certificate {}", acasNumber, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (CollectionUtils.isEmpty(documentInfoList)) {
            return new ArrayList<>();
        }

        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        documentInfoList.stream()
                .map(documentManagementService::addDocumentToDocumentField)
                .forEach(uploadedDocumentType -> {
                    uploadedDocumentType.setCategoryId(DocumentCategory.ACAS_CERTIFICATE.getCategory());
                    documentTypeItems.add(createDocumentTypeItem(uploadedDocumentType,
                            DocumentConstants.ACAS_CERTIFICATE));
                });

        return documentTypeItems;
    }

    public void sendEt1ConfirmationMyHmcts(CaseDetails caseDetails, String userToken) {
        log.info("Sending ET1 confirmation email for case {}", caseDetails.getCaseId());
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        emailService.sendEmail(et1ProfessionalSubmissionTemplateId,
                userDetails.getEmail(),
                Map.of(CASE_NUMBER, caseDetails.getCaseId(),
                        "firstName", userDetails.getFirstName(),
                        "lastName", userDetails.getLastName(),
                        CLAIMANT, caseDetails.getCaseData().getClaimant()));
    }

    public void sendEt1ConfirmationClaimant(CaseDetails caseDetails, String userToken) {
        try {
            log.info("Sending ET1 confirmation email for case {}", caseDetails.getCaseId());
            UserDetails userDetails = userIdamService.getUserDetails(userToken);
            String templateId = WELSH_LANGUAGE.equals(findLanguagePreference(caseDetails.getCaseData()))
                    ? claimantSubmissionTemplateIdWelsh
                    : claimantSubmissionTemplateId;
            emailService.sendEmail(templateId,
                    userDetails.getEmail(),
                    Map.of(CASE_NUMBER, isNullOrEmpty(caseDetails.getCaseData().getEthosCaseReference())
                                    ? caseDetails.getCaseId()
                                    : caseDetails.getCaseData().getEthosCaseReference(),
                            "firstName", userDetails.getFirstName(),
                            "lastName", userDetails.getLastName(),
                            LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseDetails.getCaseId())
                    ));

        } catch (Exception e) {
            log.error("Failed to send ET1 confirmation email", e);
        }
    }

    private static String findLanguagePreference(CaseData caseData) {
        return switch (caseData.getCaseSource()) {
            case "MyHMCTS" -> WELSH_LANGUAGE.equals(getFirstListItem(caseData.getHearingContactLanguage()))
                              || WELSH_LANGUAGE.equals(getFirstListItem(caseData.getContactLanguageQuestion()))
                    ? WELSH_LANGUAGE
                    : ENGLISH_LANGUAGE;
            case "ET1 Online" ->
                    caseData.getClaimantHearingPreference() != null
                    && caseData.getClaimantHearingPreference().getContactLanguage() != null
                    && WELSH_LANGUAGE.equals(caseData.getClaimantHearingPreference().getContactLanguage())
                            ? WELSH_LANGUAGE
                            : ENGLISH_LANGUAGE;
            default -> ENGLISH_LANGUAGE;
        };
    }

}
