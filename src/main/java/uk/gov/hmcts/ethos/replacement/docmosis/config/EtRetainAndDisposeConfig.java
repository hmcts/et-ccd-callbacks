package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.ccd.sdk.RetainAndDisposeConfig;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
public class EtRetainAndDisposeConfig implements RetainAndDisposeConfig {

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
            state = :deleteState
            or (
              state = :draftState
              and created_date::date + :retentionDays < current_date
            )
          )
        order by reference asc
        limit :maxResults
        """;

    private final NamedParameterJdbcTemplate jdbc;

    public EtRetainAndDisposeConfig(NamedParameterJdbcTemplate jdbc) {
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
    public List<Long> findCandidates(int maxResults) {
        Assert.isTrue(maxResults > 0, "maxResults must be greater than zero");

        return jdbc.queryForList(
            FIND_CANDIDATES,
            Map.of(
                "caseTypeIds", CASE_TYPES,
                "deleteState", CaseState.Delete.name(),
                "draftState", CaseState.AWAITING_SUBMISSION_TO_HMCTS.name(),
                "retentionDays", DRAFT_RETENTION_DAYS,
                "maxResults", maxResults
            ),
            Long.class
        );
    }
}
