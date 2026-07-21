package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.RetainAndDisposePolicy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
public class EtRetainAndDisposePolicy implements RetainAndDisposePolicy {

    private static final int DRAFT_RETENTION_DAYS = 365;
    private static final String TERMINAL_STATE = "Deleting";
    private static final String TERMINAL_EVENT = "MarkForDisposal";
    private static final Set<String> CASE_TYPES = Set.of(
        ENGLANDWALES_CASE_TYPE_ID,
        SCOTLAND_CASE_TYPE_ID
    );
    private static final String FIND_CANDIDATES = """
        select reference
        from ccd.case_data
        where case_type_id in (:caseTypeIds)
          and (
            state = 'Delete'
            or (
              state = 'AWAITING_SUBMISSION_TO_HMCTS'
              and created_date::date + :retentionDays < current_date
            )
          )
        order by reference asc
        """;

    private final NamedParameterJdbcTemplate jdbc;

    public EtRetainAndDisposePolicy(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Set<String> caseTypes() {
        return CASE_TYPES;
    }

    @Override
    public String terminalState() {
        return TERMINAL_STATE;
    }

    @Override
    public String terminalEvent() {
        return TERMINAL_EVENT;
    }

    @Override
    public List<Long> findCandidates() {
        return jdbc.queryForList(
            FIND_CANDIDATES,
            Map.of(
                "caseTypeIds", CASE_TYPES,
                "retentionDays", DRAFT_RETENTION_DAYS
            ),
            Long.class
        );
    }
}
