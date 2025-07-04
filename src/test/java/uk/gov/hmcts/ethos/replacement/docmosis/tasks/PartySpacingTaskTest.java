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
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class PartySpacingTaskTest {
    private PartySpacingTask partySpacingTask;
    @MockitoBean
    private AdminUserService adminUserService;
    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<CaseData> caseDataArgumentCaptor;

    @BeforeEach
    void setUp() {
        partySpacingTask = new PartySpacingTask(adminUserService, ccdClient, featureToggleService);
        when(featureToggleService.isPartySpacingCronEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("AuthToken");
        ReflectionTestUtils.setField(partySpacingTask, "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(partySpacingTask, "maxCases", 10);
    }

    @Test
    void testTask_featureOff() throws IOException {
        when(featureToggleService.isPartySpacingCronEnabled()).thenReturn(false);
        partySpacingTask.refactorPartySpacing();
        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(any(), any(), any());
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void testTask_featureOn() throws IOException, URISyntaxException {
        SubmitEvent submitEvent = new ObjectMapper().readValue(ResourceLoader.getResource("caseDetailsTest1.json"),
                SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(Collections.emptyList()).thenReturn(Collections.emptyList());
        CaseData caseData = submitEvent.getCaseData();
        caseData.getClaimantIndType().setClaimantFirstNames("John ");
        caseData.getClaimantIndType().setClaimantLastName(" Doe");
        caseData.getRespondentCollection().get(0).getValue().setRespondentName(" Jane Doe ");
        caseData.getRepresentativeClaimantType().setNameOfRepresentative("  Tim Doe  ");
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .build();
        ccdRequest.getCaseDetails().setJurisdiction(EMPLOYMENT);
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(ccdRequest);
        partySpacingTask.refactorPartySpacing();
        verify(ccdClient, times(1)).submitEventForCase(eq("AuthToken"), caseDataArgumentCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq("123456789"));
        CaseData caseDataCaptured = caseDataArgumentCaptor.getValue();
        assertEquals("John", caseDataCaptured.getClaimantIndType().getClaimantFirstNames());
        assertEquals("Doe", caseDataCaptured.getClaimantIndType().getClaimantLastName());
        assertEquals("Jane Doe", caseDataCaptured.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals("Tim Doe", caseDataCaptured.getRepresentativeClaimantType().getNameOfRepresentative());
    }
}
