package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RetentionCaseDataRepository {
    private static final String DRAFT_STATE = "AWAITING_SUBMISSION_TO_HMCTS";
    private static final String SELECT_CASE_DATA = """
        select reference, case_type_id, jurisdiction
        from ccd.case_data
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<RetentionCaseData> findExpiredDraftCases(Collection<String> caseTypeIds, int limit) {
        if (caseTypeIds.isEmpty() || limit <= 0) {
            return List.of();
        }

        String sql = SELECT_CASE_DATA + """
            where state = :state
              and case_type_id in (:caseTypeIds)
              and resolved_ttl < current_date
            order by resolved_ttl desc
            limit :limit
            """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource()
            .addValue("state", DRAFT_STATE)
            .addValue("caseTypeIds", caseTypeIds)
            .addValue("limit", limit), rowMapper());
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
            rs.getString("case_type_id"),
            rs.getString("jurisdiction")
        );
    }
}
