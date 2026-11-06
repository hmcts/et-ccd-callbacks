package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.NotificationViewRepository;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Set;
import java.util.function.LongConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;

@ExtendWith(MockitoExtension.class)
class NotificationViewServiceTest {

    private static final String AUTHORIZATION = "Bearer citizen";
    private static final String CASE_ID = "1646225213651590";
    private static final long CASE_REFERENCE = 1_646_225_213_651_590L;
    private static final String CASE_TYPE = "ET_EnglandWales";

    @Mock
    private CitizenSystemEventService citizenSystemEventService;
    @Mock
    private NotificationViewRepository notificationViewRepository;

    private NotificationViewService service;
    private CitizenSystemEventService.CitizenCase citizenCase;

    @BeforeEach
    void setUp() {
        service = new NotificationViewService(citizenSystemEventService, notificationViewRepository);
        CaseData caseData = new CaseData();
        caseData.setSendNotificationCollection(List.of(
            notification("earlier", VIEWED),
            notification("new", NOT_VIEWED_YET),
            notification("other", NOT_VIEWED_YET)
        ));
        citizenCase = new CitizenSystemEventService.CitizenCase(
            UserInfo.builder().uid("citizen-id").roles(List.of("citizen")).build(),
            CaseDetails.builder().id(CASE_REFERENCE).caseTypeId(CASE_TYPE)
                .data(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData)).build()
        );
    }

    @Test
    void recordsOnlyTheNewlyViewedItems() {
        when(citizenSystemEventService.readCase(AUTHORIZATION, CASE_ID, CASE_TYPE)).thenReturn(citizenCase);

        final CaseDetails result = service.recordViews(AUTHORIZATION, CASE_ID, CASE_TYPE,
            caseData -> caseData.getSendNotificationCollection().get(1).getValue().setNotificationState(VIEWED));

        ArgumentCaptor<LongConsumer> write = ArgumentCaptor.forClass(LongConsumer.class);
        verify(citizenSystemEventService).record(eq(citizenCase), eq(NotificationViewService.EVENT_ID),
            eq(NotificationViewService.EVENT_NAME), write.capture());
        verifyNoInteractions(notificationViewRepository);

        when(notificationViewRepository.findItemIds(CASE_REFERENCE)).thenReturn(Set.of("earlier"));
        write.getValue().accept(CASE_REFERENCE);

        verify(notificationViewRepository).markViewed(CASE_REFERENCE, "new");
        verify(notificationViewRepository, never()).markViewed(CASE_REFERENCE, "earlier");
        verify(notificationViewRepository, never()).markViewed(CASE_REFERENCE, "other");

        CaseData returned = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(result.getData());
        assertThat(returned.getSendNotificationCollection())
            .extracting(item -> item.getValue().getNotificationState())
            .containsExactly(VIEWED, VIEWED, NOT_VIEWED_YET);
    }

    @Test
    void recordsNothingWhenMarkingFails() {
        when(citizenSystemEventService.readCase(AUTHORIZATION, CASE_ID, CASE_TYPE)).thenReturn(citizenCase);

        assertThatThrownBy(() -> service.recordViews(AUTHORIZATION, CASE_ID, CASE_TYPE, caseData -> {
            throw new IllegalArgumentException("Response id is invalid");
        })).isInstanceOf(IllegalArgumentException.class);

        verify(citizenSystemEventService).readCase(AUTHORIZATION, CASE_ID, CASE_TYPE);
        verify(citizenSystemEventService, never()).record(any(), any(), any(), any());
        verifyNoMoreInteractions(citizenSystemEventService);
    }

    private static SendNotificationTypeItem notification(String id, String state) {
        return SendNotificationTypeItem.builder()
            .id(id)
            .value(SendNotificationType.builder().notificationState(state).build())
            .build();
    }
}
