package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class HubLinkServiceTest {
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String CASE_ID = "1646225213651590";

    @MockitoBean
    CaseService caseService;
    @MockitoBean
    CaseDetailsConverter caseDetailsConverter;
    @MockitoBean
    private HubLinkService hubLinkService;
    private final TestData testData;
    private HubLinksStatusesRequest hubLinksStatusesRequest;

    HubLinkServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);
        hubLinkService = new HubLinkService(caseService, caseDetailsConverter);
        HubLinksStatuses hubLinksStatuses = new HubLinksStatuses();
        hubLinksStatusesRequest = HubLinksStatusesRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .hubLinksStatuses(hubLinksStatuses)
            .build();

        when(caseService.startUpdate(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(testData.getUpdateCaseEventResponse());

    }

    @Test
    void shouldUpdateHubLinks() {
        hubLinkService.updateHubLinkStatuses(hubLinksStatusesRequest, TEST_SERVICE_AUTH_TOKEN);

        verify(caseDetailsConverter, times(1)).caseDataContent(
            any(),
            any()
        );
    }
}
