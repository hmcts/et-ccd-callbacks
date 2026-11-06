package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.function.LongConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class HubLinkServiceTest {
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String CASE_ID = "1646225213651590";
    private static final long CASE_REFERENCE = 1_646_225_213_651_590L;

    @Mock
    private CitizenSystemEventService citizenSystemEventService;
    @Mock
    private HubLinkStatusRepository hubLinkStatusRepository;

    private HubLinkService hubLinkService;
    private HubLinksStatuses hubLinksStatuses;
    private HubLinksStatusesRequest request;
    private CitizenSystemEventService.CitizenCase citizenCase;

    @BeforeEach
    void before() {
        hubLinkService = new HubLinkService(citizenSystemEventService, hubLinkStatusRepository);
        hubLinksStatuses = new HubLinksStatuses();
        request = HubLinksStatusesRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .hubLinksStatuses(hubLinksStatuses)
            .build();
        citizenCase = new CitizenSystemEventService.CitizenCase(
            UserInfo.builder().uid("citizen-id").roles(List.of("citizen")).build(),
            CaseDetails.builder().id(CASE_REFERENCE).caseTypeId(CASE_TYPE).data(new HashMap<>()).build()
        );
    }

    @Test
    void shouldStoreHubLinkStatusesAsASystemEventForTheCitizen() {
        when(citizenSystemEventService.readCase(TEST_SERVICE_AUTH_TOKEN, CASE_ID, CASE_TYPE)).thenReturn(citizenCase);

        final CaseDetails result = hubLinkService.updateHubLinkStatuses(request, TEST_SERVICE_AUTH_TOKEN);

        ArgumentCaptor<LongConsumer> write = ArgumentCaptor.forClass(LongConsumer.class);
        verify(citizenSystemEventService).record(eq(citizenCase), eq(HubLinkService.EVENT_ID),
            eq(HubLinkService.EVENT_NAME), write.capture());
        verifyNoInteractions(hubLinkStatusRepository);

        write.getValue().accept(CASE_REFERENCE);

        ArgumentCaptor<HubLinkStatus> saved = ArgumentCaptor.forClass(HubLinkStatus.class);
        verify(hubLinkStatusRepository).save(saved.capture());
        assertThat(saved.getValue().getCaseReference()).isEqualTo(CASE_REFERENCE);
        assertThat(saved.getValue().getData()).isSameAs(hubLinksStatuses);
        assertThat(result).isSameAs(citizenCase.caseDetails());
        assertThat(result.getData()).containsKey("hubLinksStatuses");
    }

    @Test
    void shouldRejectARequestWithoutStatuses() {
        request.setHubLinksStatuses(null);

        assertThatThrownBy(() -> hubLinkService.updateHubLinkStatuses(request, TEST_SERVICE_AUTH_TOKEN))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verifyNoInteractions(citizenSystemEventService, hubLinkStatusRepository);
    }

    @Test
    void shouldNotWriteWhenTheCitizenCannotReadTheCase() {
        when(citizenSystemEventService.readCase(any(), any(), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> hubLinkService.updateHubLinkStatuses(request, TEST_SERVICE_AUTH_TOKEN))
            .isInstanceOf(ResponseStatusException.class);
        verify(citizenSystemEventService, never()).record(any(), any(), any(), any());
        verifyNoInteractions(hubLinkStatusRepository);
    }
}
