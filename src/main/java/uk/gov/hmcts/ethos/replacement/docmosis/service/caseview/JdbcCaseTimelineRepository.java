package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcCaseTimelineRepository implements CaseTimelineRepository {

    private static final String RECENT_EVENTS_SQL = """
        SELECT ce.created_date,
               ce.event_id,
               ce.event_name,
               ce.state_name,
               ce.user_first_name,
               ce.user_last_name
        FROM ccd.case_event ce
        JOIN ccd.case_data cd ON cd.id = ce.case_data_id
        WHERE cd.reference = :caseReference
          AND ce.event_id NOT IN ('UPDATE_HUBLINK_STATUS', 'UPDATE_NOTIFICATION_RESPONSE')
        ORDER BY ce.created_date DESC, ce.id DESC
        LIMIT 5
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<CaseTimelineEvent> findRecent(long caseReference) {
        return jdbcTemplate.query(
            RECENT_EVENTS_SQL,
            new MapSqlParameterSource("caseReference", caseReference),
            (resultSet, rowNumber) -> new CaseTimelineEvent(
                resultSet.getTimestamp("created_date").toLocalDateTime(),
                resultSet.getString("event_id"),
                resultSet.getString("event_name"),
                resultSet.getString("state_name"),
                resultSet.getString("user_first_name"),
                resultSet.getString("user_last_name")
            )
        );
    }
}
