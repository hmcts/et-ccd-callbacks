package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

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
    private static final String SIMULATION_CASE_TYPE = "ET_Scotland";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String TOKEN = "token";

    @Mock
    private RetentionCaseDataRepository repository;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;

    private CaseRetentionService service;

    @BeforeEach
    void setUp() {
        service = new CaseRetentionService(repository, adminUserService, ccdClient);
    }

    @Test
    void deletesExpiredDraftCaseWhenCcdPointerHasGone() throws Exception {
        RetentionCaseData caseData = caseData(111L, CASE_TYPE);
        when(repository.findExpiredDraftCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of(caseData));
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.retrieveCase(TOKEN, CASE_TYPE, JURISDICTION, "111"))
            .thenThrow(new HttpClientErrorException(NOT_FOUND));
        when(repository.deleteCases(Set.of(111L))).thenReturn(1);

        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(), 10);

        assertThat(result.deletedCases()).isEqualTo(1);
        assertThat(result.simulatedCases()).isZero();
        assertThat(result.skippedCases()).isZero();
        verify(repository).deleteCases(Set.of(111L));
    }

    @Test
    void skipsDeleteWhenCcdPointerStillExists() {
        RetentionCaseData caseData = caseData(111L, CASE_TYPE);
        when(repository.findExpiredDraftCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of(caseData));
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);

        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(), 10);

        assertThat(result.deletedCases()).isZero();
        assertThat(result.simulatedCases()).isZero();
        assertThat(result.skippedCases()).isEqualTo(1);
        verify(repository, never()).deleteCases(Set.of(111L));
    }

    @Test
    void skipsDeleteWhenCcdExistenceCheckFails() throws Exception {
        RetentionCaseData caseData = caseData(111L, CASE_TYPE);
        when(repository.findExpiredDraftCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of(caseData));
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.retrieveCase(TOKEN, CASE_TYPE, JURISDICTION, "111"))
            .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(), 10);

        assertThat(result.deletedCases()).isZero();
        assertThat(result.skippedCases()).isEqualTo(1);
        verify(repository, never()).deleteCases(Set.of(111L));
    }

    @Test
    void simulationModeCountsExpiredDraftCasesWithoutCcdPointerCheckOrDeletion() {
        RetentionCaseData caseData = caseData(222L, SIMULATION_CASE_TYPE);
        when(repository.findExpiredDraftCases(Set.of(SIMULATION_CASE_TYPE), 10)).thenReturn(List.of(caseData));

        RetentionTaskResult result = service.run(Set.of(), Set.of(SIMULATION_CASE_TYPE), 10);

        assertThat(result.deletedCases()).isZero();
        assertThat(result.simulatedCases()).isEqualTo(1);
        assertThat(result.skippedCases()).isZero();
        verifyNoInteractions(adminUserService, ccdClient);
        verify(repository, never()).deleteCases(Set.of(222L));
    }

    @Test
    void doesNothingWhenBatchSizeIsInvalid() {
        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(SIMULATION_CASE_TYPE), 0);

        assertThat(result.deletedCases()).isZero();
        assertThat(result.simulatedCases()).isZero();
        assertThat(result.skippedCases()).isZero();
        verifyNoInteractions(repository, adminUserService, ccdClient);
    }

    @Test
    void doesNotFetchAdminTokenWhenNoDeletionCandidatesFound() {
        when(repository.findExpiredDraftCases(Set.of(CASE_TYPE), 10)).thenReturn(List.of());

        RetentionTaskResult result = service.run(Set.of(CASE_TYPE), Set.of(), 10);

        assertThat(result.deletedCases()).isZero();
        assertThat(result.skippedCases()).isZero();
        verifyNoInteractions(adminUserService, ccdClient);
        verify(repository, never()).deleteCases(Set.of());
    }

    private RetentionCaseData caseData(Long reference, String caseType) {
        return new RetentionCaseData(reference, caseType, JURISDICTION);
    }
}
