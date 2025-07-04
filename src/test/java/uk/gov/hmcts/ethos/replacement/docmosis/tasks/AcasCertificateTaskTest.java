package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.STARTING_A_CLAIM;

@ExtendWith(SpringExtension.class)
class AcasCertificateTaskTest {
    private AcasCertificateTask acasCertificateTask;
    @MockitoBean
    private AdminUserService adminUserService;
    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @MockitoBean
    private Et1SubmissionService et1SubmissionService;
    @Captor
    private ArgumentCaptor<CaseData> caseDataArgumentCaptor;

    @BeforeEach
    void setUp() {
        acasCertificateTask = new AcasCertificateTask(
                et1SubmissionService,
                adminUserService,
                ccdClient,
                featureToggleService
        );
        when(adminUserService.getAdminUserToken()).thenReturn("AuthToken");
        when(featureToggleService.isAcasCertificatePostSubmissionEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(acasCertificateTask, "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(acasCertificateTask, "maxCases", 10);
    }

    @Test
    void testEt1DocGen_featureOff() throws Exception {
        when(featureToggleService.isAcasCertificatePostSubmissionEnabled()).thenReturn(false);
        acasCertificateTask.generateAcasCertificates();
        // Verify that the method does not proceed with the task when the feature is off
        verify(et1SubmissionService, times(0)).retrieveAndAddAcasCertificates(any(), any(), any());
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void testEt1DocGen_featureOn() throws Exception {
        String resource = ResourceLoader.getResource("et1GenerateDocTask.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(Collections.emptyList()).thenReturn(Collections.emptyList());

        CaseData caseData = submitEvent.getCaseData();
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .build();
        ccdRequest.getCaseDetails().setJurisdiction(EMPLOYMENT);
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(ccdRequest);
        when(et1SubmissionService.retrieveAndAddAcasCertificates(any(), any(), any()))
                .thenReturn(createAcasDocumentList());
        acasCertificateTask.generateAcasCertificates();
        verify(ccdClient, times(1)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(1)).submitEventForCase(eq("AuthToken"), caseDataArgumentCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq("1712760487710259"));
        CaseData caseDataCaptured = caseDataArgumentCaptor.getValue();
        assertEquals(NO, caseDataCaptured.getAcasCertificateRequired());
        assertTrue(isNotEmpty(caseDataCaptured.getDocumentCollection()));
        assertTrue(isNotEmpty(caseDataCaptured.getClaimantDocumentCollection()));
    }

    private List<DocumentTypeItem> createAcasDocumentList() {
        UploadedDocumentType uploadedDocumentType = UploadedDocumentBuilder.builder()
                .withFilename("ACAS_Certificate.pdf")
                .withUrl("http://example.com/ACAS_Certificate.pdf")
                .withUuid(UUID.randomUUID().toString())
                .build();

        DocumentType documentType = new DocumentType();
        documentType.setTopLevelDocuments(STARTING_A_CLAIM);
        documentType.setStartingClaimDocuments(ACAS_CERTIFICATE);
        documentType.setUploadedDocument(uploadedDocumentType);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(documentType);
        return List.of(documentTypeItem);
    }
}
