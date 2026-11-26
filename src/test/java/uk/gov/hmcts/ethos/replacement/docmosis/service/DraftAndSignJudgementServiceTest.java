package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(MockitoExtension.class)
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
    private ArgumentCaptor<Map<String, Object>> personalisationCaptor;

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
        caseDetails.getCaseData().setDraftAndSignJudgement(buildDraftAndSignJudgement(YES, YES));
    }

    @Test
    void shouldSendEmailWhenJudgementAndPublicationAreYes() throws Exception {
        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(buildUploadedDocument());

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
        caseDetails.getCaseData().getDraftAndSignJudgement().setShouldForPublicationReason(null);

        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(buildUploadedDocument());

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

    @Test
    void shouldThrowErrorIfCaseDetailsMissing() {
        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(null, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Case details is missing");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    @Test
    void shouldThrowErrorIfUserTokenMissing() {
        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("User token is missing");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    @Test
    void shouldThrowErrorIfCaseDataMissing() {
        caseDetails.setCaseData(null);

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Case data is missing or incomplete");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    @Test
    void shouldThrowErrorIfEthosCaseReferenceMissing() {
        caseDetails.getCaseData().setEthosCaseReference(null);

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Case data is missing or incomplete");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    @Test
    void shouldThrowErrorIfClaimantMissing() {
        caseDetails.getCaseData().setClaimant(null);

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Case data is missing or incomplete");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    @Test
    void shouldThrowErrorIfDraftAndSignIsNull() {
        caseDetails.getCaseData().setDraftAndSignJudgement(null);

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Draft and sign judgement is missing");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
        verify(reportDataService, never()).getUserFullName(any());
    }

    @Test
    void shouldThrowErrorIfDocMissing() {
        caseDetails.getCaseData().getDraftAndSignJudgement().setDraftAndSignJudgementDocuments(null);

        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Draft and sign judgement document is missing");

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldThrowErrorIfDocMoreThanOne() {
        caseDetails.getCaseData().getDraftAndSignJudgement().setDraftAndSignJudgementDocuments(
            List.of(buildDocuments().getFirst(), buildDocuments().getFirst()));

        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Expected exactly one draft and sign judgement document, but found multiple");

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldThrowErrorIfDocItemIsNull() {
        caseDetails.getCaseData().getDraftAndSignJudgement().setDraftAndSignJudgementDocuments(
            new java.util.ArrayList<>(java.util.Collections.singletonList(null))
        );

        when(reportDataService.getUserFullName(USER_TOKEN)).thenReturn("Judge Jane Doe");

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Downloaded document is null or has no content");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
    }

    @Test
    void shouldThrowErrorIfDocItemValueIsNull() {
        caseDetails.getCaseData().getDraftAndSignJudgement().setDraftAndSignJudgementDocuments(
            List.of(new DocumentTypeItem())
        );

        when(reportDataService.getUserFullName(USER_TOKEN)).thenReturn("Judge Jane Doe");

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Downloaded document is null or has no content");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
    }

    @Test
    void shouldThrowErrorIfUploadedDocumentIsNull() {
        DocumentType documentType = new DocumentType();
        DocumentTypeItem item = new DocumentTypeItem();
        item.setValue(documentType);

        caseDetails.getCaseData().getDraftAndSignJudgement().setDraftAndSignJudgementDocuments(List.of(item));
        when(reportDataService.getUserFullName(USER_TOKEN)).thenReturn("Judge Jane Doe");

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Downloaded document is null or has no content");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
    }

    @Test
    void shouldThrowErrorIfDocumentBinaryUrlIsBlank() {
        UploadedDocumentType uploadedDocumentType = buildUploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("   ");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);

        DocumentTypeItem item = new DocumentTypeItem();
        item.setValue(documentType);

        caseDetails.getCaseData().getDraftAndSignJudgement().setDraftAndSignJudgementDocuments(List.of(item));
        when(reportDataService.getUserFullName(USER_TOKEN)).thenReturn("Judge Jane Doe");

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Downloaded document is null or has no content");

        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(documentManagementService, never()).downloadFile(any(), any());
    }

    @Test
    void shouldPropagateErrorWhenDownloadFileThrowsRuntimeException() {
        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenThrow(new IllegalStateException("downloadFile-error"));

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("downloadFile-error");

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldThrowErrorIfDocContentEmpty() {
        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(null);

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Downloaded document is null or has no content");

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldThrowErrorIfGetInputStreamFailed() throws IOException {
        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");
        Resource brokenResource = org.mockito.Mockito.mock(Resource.class);
        when(brokenResource.getInputStream())
            .thenThrow(new IOException("stream-failed"));
        UploadedDocument uploadedDocument = UploadedDocument.builder()
            .content(brokenResource)
            .contentType(DOC_CONTENT_TYPE)
            .name(DOC_FILENAME)
            .build();
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(uploadedDocument);

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Error getting document input stream binary content")
            .hasCauseInstanceOf(IOException.class);

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldThrowErrorIfDocumentBinaryContentIsEmpty() {
        when(reportDataService.getUserFullName(USER_TOKEN)).thenReturn("Judge Jane Doe");

        UploadedDocument emptyContentDoc = UploadedDocument.builder()
            .content(new ByteArrayResource(new byte[0]))
            .contentType(DOC_CONTENT_TYPE)
            .name(DOC_FILENAME)
            .build();
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(emptyContentDoc);

        assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Document content is empty");

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void shouldThrowErrorIfPrepareUploadFailed() {
        when(reportDataService.getUserFullName(USER_TOKEN))
            .thenReturn("Judge Jane Doe");
        when(documentManagementService.downloadFile(eq(USER_TOKEN), any()))
            .thenReturn(buildUploadedDocument());

        try (MockedStatic<NotificationClient> notificationClientMockedStatic = mockStatic(NotificationClient.class)) {
            notificationClientMockedStatic
                .when(() -> NotificationClient.prepareUpload(any(byte[].class)))
                .thenThrow(new NotificationClientException("prepareUpload-error"));

            assertThatThrownBy(() -> draftAndSignJudgementService.checkAndSendEmails(caseDetails, USER_TOKEN))
                .isInstanceOf(NotificationClientException.class)
                .hasMessage("prepareUpload-error");
        }

        verify(emailService, never()).sendEmail(any(), any(), any());
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

    private UploadedDocumentType buildUploadedDocumentType() {
        return UploadedDocumentBuilder.builder()
            .withFilename(DOC_FILENAME)
            .withUuid(DOC_UUID)
            .build();
    }

    private List<DocumentTypeItem> buildDocuments() {
        UploadedDocumentType uploadedDocumentType = buildUploadedDocumentType();

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);

        DocumentTypeItem item = new DocumentTypeItem();
        item.setValue(documentType);

        return List.of(item);
    }
}