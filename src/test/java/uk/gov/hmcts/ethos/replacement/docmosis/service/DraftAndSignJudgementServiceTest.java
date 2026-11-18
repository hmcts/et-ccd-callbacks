package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.DraftAndSignJudgement;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class DraftAndSignJudgementServiceTest {

    private static final String USER_TOKEN = "some-token";
    private static final String TEMPLATE_ID = "template-id";
    private static final String EMAIL = "find-case-law@example.com";
    private static final String CASE_REF = "123456/2026";
    private static final String DOC_FILENAME = "doc_file_name.pdf";
    private static final String DOC_UUID = "3f8b9d42-6c1e-4a77-b2d8-91c0e5f4a6ab";
    private static final String DOC_CONTENT_TYPE = "application/pdf";

    private DraftAndSignJudgementService draftAndSignJudgementService;
    private CaseDetails caseDetails;

    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private ReportDataService reportDataService;
    @MockitoBean
    private DocumentManagementService documentManagementService;

    @Captor
    ArgumentCaptor<Map<String, Object>> personalisationCaptor;

    @BeforeEach
    void setUp() {
        draftAndSignJudgementService = new DraftAndSignJudgementService(
            emailService,
            reportDataService,
            documentManagementService
        );
        ReflectionTestUtils.setField(draftAndSignJudgementService, "forFindCaseLawEmailTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(draftAndSignJudgementService, "emailAddressToSend", EMAIL);
        caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference(CASE_REF)
            .withClaimant("Claimant A")
            .withRespondent(RespondentBuilder.builder()
                .withName("Respondent A")
                .build()
            )
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
    }

    @Test
    void shouldSendEmailWhenJudgementAndPublicationAreYes() throws Exception {
        caseDetails.getCaseData().setDraftAndSignJudgement(buildDraftAndSignJudgement(YES, YES));
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(buildUploadedDocument());
        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");

        draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN);

        verify(emailService, times(1)).sendEmail(eq(TEMPLATE_ID), eq(EMAIL), personalisationCaptor.capture());
        verify(reportDataService, times(1)).getUserFullName(USER_TOKEN);
        verify(documentManagementService, times(1)).downloadFile(eq(USER_TOKEN), any());

        Map<String, Object> personalisation = personalisationCaptor.getValue();
        assertThat(personalisation).containsEntry("caseNumber", CASE_REF);
        assertThat(personalisation).containsEntry("claimant", "Claimant A");
        assertThat(personalisation).containsEntry("respondentNames", "Respondent A");
        assertThat(personalisation).containsEntry("reasonForPublication", "Public interest");
        assertThat(personalisation).containsEntry("submittedDate", UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(personalisation).containsEntry("userName", "Judge Jane Doe");
        assertThat(personalisation).containsKey("linkToDocument");
    }

    @Test
    void shouldSendEmailWhenReasonForPublicationEmpty() throws Exception {
        caseDetails.getCaseData().setDraftAndSignJudgement(buildDraftAndSignJudgement(YES, YES));
        caseDetails.getCaseData().getDraftAndSignJudgement().setShouldForPublicationReason(null);
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(buildUploadedDocument());
        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");

        draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN);

        verify(emailService, times(1)).sendEmail(eq(TEMPLATE_ID), eq(EMAIL), personalisationCaptor.capture());

        Map<String, Object> personalisation = personalisationCaptor.getValue();
        assertThat(personalisation).containsEntry("reasonForPublication", "Not provided");
    }

    @Test
    void shouldNotSendEmailWhenConditionsNotMet() throws NotificationClientException {
        caseDetails.getCaseData().setDraftAndSignJudgement(buildDraftAndSignJudgement(NO, YES));

        draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN);

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    @Test
    void shouldNotSendEmailWhenRecommendationNo() throws NotificationClientException {
        caseDetails.getCaseData().setDraftAndSignJudgement(buildDraftAndSignJudgement(YES, NO));

        draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN);

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    private DraftAndSignJudgement buildDraftAndSignJudgement(String isJudgement, String shouldForPublication) {
        DraftAndSignJudgement judgement = new DraftAndSignJudgement();
        judgement.setIsJudgement(isJudgement);
        judgement.setShouldForPublication(shouldForPublication);
        judgement.setShouldForPublicationReason("Public interest");
        judgement.setDraftAndSignJudgementDocuments(buildDocuments());
        return judgement;
    }

    private UploadedDocument buildUploadedDocument() {
        Resource resource = new ByteArrayResource(new byte[] { 10, 20, 15});
        return UploadedDocument.builder().content(resource)
            .contentType(DOC_CONTENT_TYPE)
            .name(DOC_FILENAME)
            .build();
    }

    private List<DocumentTypeItem> buildDocuments() {
        UploadedDocumentType uploadedDocumentType = UploadedDocumentBuilder.builder()
            .withFilename(DOC_FILENAME)
            .withUuid(DOC_UUID)
            .build();

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);

        DocumentTypeItem item = new DocumentTypeItem();
        item.setValue(documentType);

        return List.of(item);
    }
}