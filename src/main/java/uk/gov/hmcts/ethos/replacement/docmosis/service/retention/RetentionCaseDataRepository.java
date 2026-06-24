package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RetentionCaseDataRepository {
    private static final String SELECT_CASE_DATA = """
        select reference, id, case_type_id, jurisdiction, resolved_ttl, data::text as data
        from ccd.case_data
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public List<RetentionCaseData> findExpiredCases(Collection<String> caseTypeIds, int limit) {
        if (caseTypeIds.isEmpty() || limit <= 0) {
            return List.of();
        }

        String sql = SELECT_CASE_DATA + """
            where case_type_id in (:caseTypeIds)
              and resolved_ttl < current_date
            order by resolved_ttl desc
            limit :limit
            """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource()
            .addValue("caseTypeIds", caseTypeIds)
            .addValue("limit", limit), rowMapper());
    }

    public List<RetentionCaseData> findByReferences(Collection<Long> references) {
        if (references.isEmpty()) {
            return List.of();
        }

        return jdbcTemplate.query(SELECT_CASE_DATA + " where reference in (:references)",
            new MapSqlParameterSource("references", references), rowMapper());
    }

    public List<RetentionCaseData> findCasesReferencing(Set<Long> references) {
        if (references.isEmpty()) {
            return List.of();
        }

        String sql = SELECT_CASE_DATA + """
            where data ->> 'transferredCaseLinkSourceCaseId' in (:referenceStrings)
               or coalesce(data ->> 'linkedCaseCT', '') ~ :caseDetailsRegex
               or coalesce(data ->> 'transferredCaseLink', '') ~ :caseDetailsRegex
               or exists (
                    select 1
                    from jsonb_array_elements(case
                        when jsonb_typeof(data -> 'caseLinks') = 'array' then data -> 'caseLinks'
                        else '[]'::jsonb
                    end) item
                    where coalesce(item #>> '{value,CaseReference}', item ->> 'CaseReference') in (:referenceStrings)
                  )
            """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource()
            .addValue("referenceStrings", references.stream().map(String::valueOf).toList())
            .addValue("caseDetailsRegex", caseDetailsRegex(references)),
            rowMapper());
    }

    public int deleteCases(Collection<Long> references) {
        if (references.isEmpty()) {
            return 0;
        }

        return jdbcTemplate.update("delete from ccd.case_data where reference in (:references)",
            new MapSqlParameterSource("references", references));
    }

    private RowMapper<RetentionCaseData> rowMapper() {
        return (rs, rowNum) -> new RetentionCaseData(
            rs.getObject("reference", Long.class),
            rs.getObject("id", Long.class),
            rs.getString("case_type_id"),
            rs.getString("jurisdiction"),
            rs.getObject("resolved_ttl", java.time.LocalDate.class),
            readJson(rs.getString("data"))
        );
    }

    private String caseDetailsRegex(Set<Long> references) {
        if (references.isEmpty()) {
            return "__retention_no_match__";
        }

        return "/case-details/(" + String.join("|", references.stream().map(String::valueOf).toList())
            + ")([^0-9]|$)";
    }

    @SneakyThrows
    private com.fasterxml.jackson.databind.JsonNode readJson(String data) {
        return objectMapper.readTree(data);
    }
}
