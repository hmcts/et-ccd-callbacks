package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetentionCaseDataRepositoryTest {
    private static final String CASE_DATA_JSON = """
        {
          "TTL": {
            "SystemTTL": "2026-06-22",
            "Suspended": "No"
          }
        }
        """;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Mock
    private ResultSet resultSet;

    private RetentionCaseDataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RetentionCaseDataRepository(jdbcTemplate, new ObjectMapper());
    }

    @Test
    void findExpiredCasesReturnsEmptyWhenNoCaseTypesOrLimit() {
        assertThat(repository.findExpiredCases(List.of(), 100)).isEmpty();
        assertThat(repository.findExpiredCases(List.of("ET_EnglandWales"), 0)).isEmpty();

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void findExpiredCasesQueriesAndMapsRows() throws Exception {
        mockCaseDataQuery();

        List<RetentionCaseData> cases = repository.findExpiredCases(List.of("ET_EnglandWales"), 25);

        assertMappedCase(cases);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(),
            ArgumentMatchers.<RowMapper<RetentionCaseData>>any());
        assertThat(sqlCaptor.getValue())
            .contains("resolved_ttl < current_date", "order by resolved_ttl desc")
            .doesNotContain("SystemTTL", "OverrideTTL", "Suspended");
        assertThat(paramsCaptor.getValue().getValue("caseTypeIds")).isEqualTo(List.of("ET_EnglandWales"));
        assertThat(paramsCaptor.getValue().getValue("limit")).isEqualTo(25);
    }

    @Test
    void findByReferencesReturnsEmptyWhenNoReferences() {
        assertThat(repository.findByReferences(List.of())).isEmpty();

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void findByReferencesQueriesAndMapsRows() throws Exception {
        mockCaseDataQuery();

        List<RetentionCaseData> cases = repository.findByReferences(List.of(111L));

        assertMappedCase(cases);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(anyString(), paramsCaptor.capture(),
            ArgumentMatchers.<RowMapper<RetentionCaseData>>any());
        assertThat(paramsCaptor.getValue().getValue("references")).isEqualTo(List.of(111L));
    }

    @Test
    void findCasesReferencingReturnsEmptyWhenNoReferences() {
        assertThat(repository.findCasesReferencing(Set.of())).isEmpty();

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void findCasesReferencingQueriesWithReferenceStringsAndCaseDetailsRegex() throws Exception {
        mockCaseDataQuery();
        Set<Long> references = new LinkedHashSet<>(List.of(111L, 222L));

        List<RetentionCaseData> cases = repository.findCasesReferencing(references);

        assertMappedCase(cases);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(),
            ArgumentMatchers.<RowMapper<RetentionCaseData>>any());
        assertThat(sqlCaptor.getValue()).contains("transferredCaseLinkSourceCaseId", "linkedCaseCT", "caseLinks");
        assertThat(paramsCaptor.getValue().getValue("referenceStrings")).isEqualTo(List.of("111", "222"));
        assertThat(paramsCaptor.getValue().getValue("caseDetailsRegex"))
            .isEqualTo("/case-details/(111|222)([^0-9]|$)");
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
        when(resultSet.getObject("id", Long.class)).thenReturn(999L);
        when(resultSet.getString("case_type_id")).thenReturn("ET_EnglandWales");
        when(resultSet.getString("jurisdiction")).thenReturn("EMPLOYMENT");
        when(resultSet.getObject("resolved_ttl", LocalDate.class)).thenReturn(LocalDate.of(2026, 6, 22));
        when(resultSet.getString("data")).thenReturn(CASE_DATA_JSON);
    }

    private void assertMappedCase(List<RetentionCaseData> cases) {
        assertThat(cases).hasSize(1);
        RetentionCaseData retentionCaseData = cases.getFirst();
        assertThat(retentionCaseData.reference()).isEqualTo(111L);
        assertThat(retentionCaseData.id()).isEqualTo(999L);
        assertThat(retentionCaseData.caseTypeId()).isEqualTo("ET_EnglandWales");
        assertThat(retentionCaseData.jurisdiction()).isEqualTo("EMPLOYMENT");
        assertThat(retentionCaseData.resolvedTtl()).isEqualTo(LocalDate.of(2026, 6, 22));
        assertThat(retentionCaseData.data().path("TTL").path("SystemTTL").asText()).isEqualTo("2026-06-22");
    }
}
