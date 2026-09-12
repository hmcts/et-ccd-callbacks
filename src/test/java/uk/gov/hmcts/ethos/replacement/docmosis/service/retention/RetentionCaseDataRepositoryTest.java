package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetentionCaseDataRepositoryTest {
    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Mock
    private ResultSet resultSet;

    private RetentionCaseDataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RetentionCaseDataRepository(jdbcTemplate);
    }

    @Test
    void findExpiredDraftCasesReturnsEmptyWhenNoCaseTypesOrLimitIsInvalid() {
        assertThat(repository.findExpiredDraftCases(List.of(), 100)).isEmpty();
        assertThat(repository.findExpiredDraftCases(List.of("ET_EnglandWales"), 0)).isEmpty();

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void findExpiredDraftCasesQueriesByCaseTypeDraftStateAndResolvedTtl() throws Exception {
        mockCaseDataQuery();

        List<RetentionCaseData> cases = repository.findExpiredDraftCases(List.of("ET_EnglandWales"), 25);

        assertMappedCase(cases);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(),
            ArgumentMatchers.<RowMapper<RetentionCaseData>>any());
        assertThat(sqlCaptor.getValue())
            .contains("state = :state", "case_type_id in (:caseTypeIds)", "resolved_ttl < current_date")
            .contains("order by resolved_ttl desc")
            .doesNotContain("created_date <");
        assertThat(paramsCaptor.getValue().getValue("state")).isEqualTo("AWAITING_SUBMISSION_TO_HMCTS");
        assertThat(paramsCaptor.getValue().getValue("caseTypeIds")).isEqualTo(List.of("ET_EnglandWales"));
        assertThat(paramsCaptor.getValue().getValue("limit")).isEqualTo(25);
    }

    @Test
    void deleteCasesReturnsZeroWhenNoReferences() {
        assertThat(repository.deleteCases(List.of())).isZero();

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    void deleteCasesDeletesByReference() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(2);

        int deleted = repository.deleteCases(List.of(111L, 222L));

        assertThat(deleted).isEqualTo(2);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), paramsCaptor.capture());
        assertThat(sqlCaptor.getValue()).isEqualTo("delete from ccd.case_data where reference in (:references)");
        assertThat(paramsCaptor.getValue().getValue("references")).isEqualTo(List.of(111L, 222L));
    }

    private void mockCaseDataQuery() throws SQLException {
        mockResultSet();
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                ArgumentMatchers.<RowMapper<RetentionCaseData>>any()))
            .thenAnswer(invocation -> {
                RowMapper<RetentionCaseData> rowMapper = invocation.getArgument(2);
                return List.of(rowMapper.mapRow(resultSet, 0));
            });
    }

    private void mockResultSet() throws SQLException {
        when(resultSet.getObject("reference", Long.class)).thenReturn(111L);
        when(resultSet.getString("case_type_id")).thenReturn("ET_EnglandWales");
        when(resultSet.getString("jurisdiction")).thenReturn("EMPLOYMENT");
    }

    private void assertMappedCase(List<RetentionCaseData> cases) {
        assertThat(cases).hasSize(1);
        RetentionCaseData retentionCaseData = cases.getFirst();
        assertThat(retentionCaseData.reference()).isEqualTo(111L);
        assertThat(retentionCaseData.caseTypeId()).isEqualTo("ET_EnglandWales");
        assertThat(retentionCaseData.jurisdiction()).isEqualTo("EMPLOYMENT");
    }
}
