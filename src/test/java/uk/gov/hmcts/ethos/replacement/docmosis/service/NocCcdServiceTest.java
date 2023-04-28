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
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    NocCcdService.class,
    CcdClient.class
})
class NocCcdServiceTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String JURISDICTION = "EMPLOYMENT";
    public static final String CASE_TYPE = "ET_EnglandWales";
    public static final String CASE_ID = "12345";
    @MockBean
    private CcdClient ccdClient;

    private NocCcdService nocCcdService;

    @BeforeEach
    void setUp() {
        nocCcdService = new NocCcdService(ccdClient);
    }

    private AuditEventsResponse getAuditEventsResponse() {
        return AuditEventsResponse.builder().auditEvents(List.of(AuditEvent.builder()
                .userId("128")
                .userFirstName("John")
                .userLastName("Smith")
                .id("nocRequest")
                .createdDate(LocalDateTime.of(2022, 9, 10, 8, 0, 0))
                .build(),
            AuditEvent.builder()
                .userId("967")
                .userFirstName("Kate")
                .userLastName("Johnson")
                .id("nocRequest")
                .createdDate(LocalDateTime.of(2022, 10, 1, 0, 0, 0))
                .build(),
            AuditEvent.builder()
                .userId("774")
                .userFirstName("Patrick")
                .userLastName("Fitzgerald")
                .id("amendCase")
                .createdDate(LocalDateTime.of(2022, 11, 22, 0, 0, 0))
                .build())).build();
    }

    @Test
    void shouldGetLatestAuditEventByName() throws IOException {
        AuditEventsResponse auditEventsResponse = getAuditEventsResponse();
        when(ccdClient.retrieveCaseEvents(AUTH_TOKEN, CASE_ID)).thenReturn(auditEventsResponse);
        Optional<AuditEvent> event = nocCcdService.getLatestAuditEventByName(AUTH_TOKEN, CASE_ID, "nocRequest");
        assertThat(event).isPresent().hasValue(getAuditEventsResponse().getAuditEvents().get(1));
    }

    @Test
    void shouldThrowExceptionRetrievingCaseAssignments() throws IOException {
        when(ccdClient.retrieveCaseAssignments(AUTH_TOKEN, CASE_ID)).thenThrow(new IOException());
        CcdInputOutputException exception = assertThrows(
                CcdInputOutputException.class, () ->
                nocCcdService.getCaseAssignments(AUTH_TOKEN, CASE_ID));

        assertThat(exception.getMessage()).isEqualTo("Failed to retrieve case assignments");
    }

    @Test
    void shouldThrowExceptionRevokingCaseAssignments() throws IOException {
        CaseUserAssignmentData data = new CaseUserAssignmentData();
        when(ccdClient.revokeCaseAssignments(AUTH_TOKEN, data)).thenThrow(new IOException());
        CcdInputOutputException exception = assertThrows(
                CcdInputOutputException.class, () ->
                        nocCcdService.revokeCaseAssignments(AUTH_TOKEN, data));

        assertThat(exception.getMessage()).isEqualTo("Failed to revoke case assignments");
    }

    @Test
    void shouldCallCcdUpdateCaseRepresentation() throws IOException {
        CCDRequest request = new CCDRequest();
        when(ccdClient.startEventForUpdateRep(AUTH_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID)).thenReturn(request);
        when(ccdClient.submitUpdateRepEvent(eq(AUTH_TOKEN), any(), eq(CASE_TYPE), eq(JURISDICTION),
            eq(request), eq(CASE_ID))).thenReturn(new SubmitEvent());

        nocCcdService.updateCaseRepresentation(AUTH_TOKEN, JURISDICTION, CASE_TYPE,
            CASE_ID);

        verify(ccdClient, times(1)).startEventForUpdateRep(AUTH_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID);
    }
}