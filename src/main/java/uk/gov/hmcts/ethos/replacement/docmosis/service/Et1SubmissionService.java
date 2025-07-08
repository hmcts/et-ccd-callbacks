package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.PdfServiceException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.listingFormatLocalDate;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
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
@SuppressWarnings("PMD.DoNotUseThreads")
public class Et1SubmissionService {
    private static final String VEXATION_NOTIFICATION_TEMPLATE = """
            Due diligence check
            
            %s""";
    private final AcasService acasService;
    private final DocumentManagementService documentManagementService;
    private final PdfService pdfService;
    private final TornadoService tornadoService;
    private final UserIdamService userIdamService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final CcdClient ccdClient;

    @Value("${template.et1.et1ProfessionalSubmission}")
    private String et1ProfessionalSubmissionTemplateId;
    @Value("${template.et1.submitCaseEmailTemplateId}")
    private String claimantSubmissionTemplateId;
    @Value("${template.et1.cySubmitCaseEmailTemplateId}")
    private String claimantSubmissionTemplateIdWelsh;
    private static final String ET1_EN_PDF = "ET1_0224.pdf";
    private static final String ET1_CY_PDF = "CY_ET1_2222.pdf";

    /**
     * Creates the ET1 PDF and calls off to ACAS to retrieve the certificates.
     *
     * @param caseDetails the case details
     * @param userToken   the user token
     */
    public void createAndUploadEt1Docs(CaseDetails caseDetails, String userToken) {
        try {
            DocumentTypeItem englishEt1 = createEt1DocumentType(caseDetails, userToken, ET1_EN_PDF);
            DocumentTypeItem welshEt1 = null;
            if (WELSH_LANGUAGE.equals(findLanguagePreference(caseDetails.getCaseData()))) {
                welshEt1 = createEt1DocumentType(caseDetails, userToken, ET1_CY_PDF);
            }

            List<DocumentTypeItem> acasCertificates = new ArrayList<>();
            if (featureToggleService.isAcasCertificatePostSubmissionEnabled()) {
                caseDetails.getCaseData().setAcasCertificateRequired(YES);
            } else {
                acasCertificates = getAcasCertificates(caseDetails, userToken);
            }

            addDocsToClaim(caseDetails.getCaseData(), englishEt1, welshEt1, acasCertificates);
        } catch (Exception e) {
            log.error("Failed to create and upload ET1 documents", e);
        }
    }

    private List<DocumentTypeItem> getAcasCertificates(CaseDetails caseDetails, String userToken) {
        List<DocumentTypeItem> acasCertificates = new ArrayList<>();
        try {
            acasCertificates = retrieveAndAddAcasCertificates(caseDetails.getCaseData(),
                    userToken, caseDetails.getCaseTypeId());
        } catch (Exception e) {
            log.error("Failed to retrieve ACAS certificates", e);
        }
        return acasCertificates;
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

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public List<DocumentTypeItem> retrieveAndAddAcasCertificates(
            CaseData caseData, String userToken, String caseTypeId) throws Exception {
        if (isEmpty(caseData.getRespondentCollection())) {
            return new ArrayList<>();
        }
        List<String> acasNumbers = caseData.getRespondentCollection().stream()
                .filter(respondent -> !isNullOrEmpty(respondent.getValue().getRespondentAcas()))
                .map(respondent -> respondent.getValue().getRespondentAcas())
                .toList();

        if (isEmpty(acasNumbers)) {
            return new ArrayList<>();
        }

        List<DocumentInfo> documentInfoList = acasService.getAcasCertificates(caseData, acasNumbers, userToken,
                caseTypeId);

        if (isEmpty(documentInfoList)) {
            return new ArrayList<>();
        }

        List<DocumentTypeItem> documentTypeItems = synchronizedList(new ArrayList<>());
        ForkJoinPool customThreadPool = new ForkJoinPool(documentInfoList.size());
        try {
            customThreadPool.submit(() ->
                documentInfoList.parallelStream()
                    .map(documentManagementService::addDocumentToDocumentField)
                    .forEach(doc -> {
                        doc.setCategoryId(DocumentCategory.ACAS_CERTIFICATE.getCategory());
                        documentTypeItems.add(createDocumentTypeItem(doc, ACAS_CERTIFICATE));
                    })
            ).get();
        } finally {
            customThreadPool.shutdown();
        }

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

    /**
     * Checks for vexation by querying ElasticSearch for cases. It checks to see if a claimant has submitted 4 or more
     * claims in the last 6 months.
     * @param caseDetails the case details
     * @param userToken the user token
     */
    public void vexationCheck(CaseDetails caseDetails, String userToken) {
        String query = new SearchSourceBuilder()
            .size(MAX_ES_SIZE)
            .query(boolQuery()
                .mustNot(new TermsQueryBuilder("state.keyword", "AWAITING_SUBMISSION_TO_HMCTS"))
                .filter(new RangeQueryBuilder("data.receiptDate").gte(LocalDate.now().minusMonths(6L))))
            .toString();

        List<SubmitEvent> submitEventList = getSubmitEventList(userToken, query);

        if (submitEventList.size() < 4) {
            return;
        }

        AdditionalCaseInfoType additionalCaseInfoType = caseDetails.getCaseData().getAdditionalCaseInfoType();
        if (ObjectUtils.isEmpty(additionalCaseInfoType)) {
            additionalCaseInfoType = new AdditionalCaseInfoType();
            caseDetails.getCaseData().setAdditionalCaseInfoType(additionalCaseInfoType);
        }
        additionalCaseInfoType.setInterventionRequired(YES);

        caseDetails.getCaseData().setCaseNotes(setVexationNotes(submitEventList));
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);

    }

    private List<SubmitEvent> getSubmitEventList(String userToken, String query) {
        CompletableFuture<List<SubmitEvent>> englandWalesFuture = CompletableFuture.supplyAsync(
                () -> fetchSubmitEvents(userToken, ENGLANDWALES_CASE_TYPE_ID, query));
        CompletableFuture<List<SubmitEvent>> scotlandFuture = CompletableFuture.supplyAsync(
                () -> fetchSubmitEvents(userToken, SCOTLAND_CASE_TYPE_ID, query));

        return Stream.of(englandWalesFuture, scotlandFuture)
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(a -> a.getCaseData().getReceiptDate()))
                .toList();
    }

    private List<SubmitEvent> fetchSubmitEvents(String userToken, String caseTypeId, String query) {
        try {
            return ccdClient.buildAndGetElasticSearchRequest(userToken, caseTypeId, query);
        } catch (IOException e) {
            log.error("Failed to fetch submit events for case type {}: {}", caseTypeId, e.getMessage());
        }
        return emptyList();
    }

    private String setVexationNotes(List<SubmitEvent> submitEventList) {
        String caseListFormat = "%d - %s - %s - %s v %s - %s\n";
        StringBuilder cases = new StringBuilder();
        IntStream.range(0, submitEventList.size())
                .mapToObj(i -> {
                    SubmitEvent submitEvent = submitEventList.get(i);
                    return String.format(caseListFormat,
                            i + 1,
                            submitEvent.getCaseData().getEthosCaseReference(),
                            submitEvent.getState(),
                            submitEvent.getCaseData().getClaimant(),
                            submitEvent.getCaseData().getRespondent(),
                            listingFormatLocalDate(submitEvent.getCaseData().getReceiptDate()));
                })
                .forEach(cases::append);

        return String.format(VEXATION_NOTIFICATION_TEMPLATE, cases);
    }

}
