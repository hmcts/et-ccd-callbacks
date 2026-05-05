package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.is;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class Et1DocumentGenerationTaskTest {
    private Et1DocumentGenerationTask et1DocumentGenerationTask;

    @MockitoBean
    private AdminUserService adminUserService;
    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        et1DocumentGenerationTask = new Et1DocumentGenerationTask(adminUserService, ccdClient, featureToggleService);
        when(featureToggleService.isEt1DocGenEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(et1DocumentGenerationTask, "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(et1DocumentGenerationTask, "maxCases", 10);
    }

    @Test
    void testEt1DocGen_featureOn() throws URISyntaxException, IOException {
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

        et1DocumentGenerationTask.generateEt1SubmissionDocuments();

        assertThat(ccdRequest.getCaseDetails().getCaseData().getRequiresSubmissionDocuments(), is(YES));
        verify(ccdClient, times(1)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testEt1DocGen_featureOff() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any())).thenThrow(new IOException());

        et1DocumentGenerationTask.generateEt1SubmissionDocuments();
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
    }
}
