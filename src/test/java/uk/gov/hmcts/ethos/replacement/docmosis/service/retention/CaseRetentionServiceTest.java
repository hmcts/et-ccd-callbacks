package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class CaseRetentionServiceTest {
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String TOKEN = "token";

    @Mock
    private RetentionCaseDataRepository repository;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CaseRetentionService service;

    @BeforeEach
    void setUp() {
        service = new CaseRetentionService(repository, adminUserService, ccdClient,
            Clock.fixed(Instant.parse("2026-06-23T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void deletesLocalCaseWhenExpiredAndCcdPointerHasGone() throws Exception {
        RetentionCaseData caseData = caseData(111L, "2026-06-22", null, "No");
        when(repository.findExpiredCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of(caseData));
        when(repository.findByReferences(Set.of(111L))).thenReturn(List.of(caseData));
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.retrieveCase(TOKEN, CASE_TYPE, JURISDICTION, "111"))
            .thenThrow(new HttpClientErrorException(NOT_FOUND));
        when(repository.deleteCases(Set.of(111L))).thenReturn(1);

        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(), 10);

        assertThat(result.deletedCases()).isEqualTo(1);
        verify(repository).deleteCases(Set.of(111L));
    }

    @Test
    void skipsDeleteWhenCcdPointerStillExists() {
        RetentionCaseData caseData = caseData(111L, "2026-06-22", null, "No");
        when(repository.findExpiredCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of(caseData));
        when(repository.findByReferences(Set.of(111L))).thenReturn(List.of(caseData));
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);

        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(), 10);

        assertThat(result.deletedCases()).isZero();
        verify(repository, never()).deleteCases(Set.of(111L));
    }

    @Test
    void skipsLinkedGroupWhenLinkedCaseIsNotExpired() {
        RetentionCaseData expired = caseData(111L, "2026-06-22", null, "No", 222L);
        RetentionCaseData notExpired = caseData(222L, "2026-06-24", null, "No");
        when(repository.findExpiredCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of(expired));
        when(repository.findByReferences(Set.of(111L))).thenReturn(List.of(expired));
        when(repository.findByReferences(Set.of(222L))).thenReturn(List.of(notExpired));

        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(), 10);

        assertThat(result.deletedCases()).isZero();
        verifyNoInteractions(adminUserService, ccdClient);
        verify(repository, never()).deleteCases(Set.of(111L, 222L));
    }

    @Test
    void simulationModeDoesNotDeleteOrCheckCcdPointer() {
        RetentionCaseData caseData = caseData(111L, "2026-06-22", null, "No");
        when(repository.findExpiredCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of(caseData));
        when(repository.findByReferences(Set.of(111L))).thenReturn(List.of(caseData));

        RetentionTaskResult result = service.run(Set.of(), Set.of(CASE_TYPE), 10);

        assertThat(result.simulatedCases()).isEqualTo(1);
        verifyNoInteractions(adminUserService, ccdClient);
        verify(repository, never()).deleteCases(Set.of(111L));
    }

    private RetentionCaseData caseData(Long reference, String systemTtl, String overrideTtl, String suspended) {
        return caseData(reference, systemTtl, overrideTtl, suspended, null);
    }

    private RetentionCaseData caseData(Long reference,
                                       String systemTtl,
                                       String overrideTtl,
                                       String suspended,
                                       Long linkedReference) {
        return new RetentionCaseData(reference, reference, CASE_TYPE, JURISDICTION,
            objectMapper.valueToTree(new TestCaseData(systemTtl, overrideTtl, suspended, linkedReference)));
    }

    private record TestCaseData(TestTtl TTL, List<TestCaseLink> caseLinks) {
        TestCaseData(String systemTtl, String overrideTtl, String suspended, Long linkedReference) {
            this(new TestTtl(systemTtl, overrideTtl, suspended), linkedReference == null
                ? List.of()
                : List.of(new TestCaseLink(new TestCaseLinkValue(String.valueOf(linkedReference)))));
        }
    }

    private record TestTtl(String SystemTTL, String OverrideTTL, String Suspended) {
    }

    private record TestCaseLink(TestCaseLinkValue value) {
    }

    private record TestCaseLinkValue(String CaseReference) {
    }
}
