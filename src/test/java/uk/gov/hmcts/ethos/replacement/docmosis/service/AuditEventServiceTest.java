package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.NoticeOfChangeController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes ={
        AuditEventService.class,
        CcdClient.class
})
class AuditEventServiceTest {

    @MockBean
    private CcdClient ccdClient;

    private AuditEventService auditEventService;

    @BeforeEach
    void setUp() {
        auditEventService = new AuditEventService(ccdClient);
    }

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";

    private AuditEventsResponse getAuditEventsResponse(){

        return AuditEventsResponse.builder().auditEvents(List.of(AuditEvent.builder()
                .userId("12345")
                .userFirstName("John")
                .userLastName("Smith")
                .id("nocRequest")
                .createdDate( LocalDateTime.of(2022,11,22,22,22,22))
                .build())).build();
    }

    @Test
    void getLatestAuditEventByName() throws IOException {
        AuditEventsResponse auditEventsResponse = getAuditEventsResponse();
        when(ccdClient.retrieveCaseEvents(AUTH_TOKEN,"12345")).thenReturn(auditEventsResponse);
        Optional<AuditEvent> event = auditEventService.getLatestAuditEventByName(AUTH_TOKEN, "12345", "nocRequest");
        assertThat(event).isPresent().hasValue(getAuditEventsResponse().getAuditEvents().get(0));

    }
}