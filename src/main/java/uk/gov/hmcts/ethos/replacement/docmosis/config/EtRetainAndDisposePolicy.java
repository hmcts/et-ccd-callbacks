package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.RetainAndDisposePolicy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
public class EtRetainAndDisposePolicy implements RetainAndDisposePolicy {

    private static final int DRAFT_RETENTION_DAYS = 365;
    private static final Set<String> CASE_TYPES = Set.of(
        ENGLANDWALES_CASE_TYPE_ID,
        SCOTLAND_CASE_TYPE_ID
    );

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Set<String> caseTypes() {
        return CASE_TYPES;
    }

    @Override
    public List<Long> findCandidatesForDisposal() {
        return jdbc.queryForList(
            """
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
                """,
            Map.of(
                "caseTypeIds", CASE_TYPES,
                "retentionDays", DRAFT_RETENTION_DAYS
            ),
            Long.class
        );
    }
}
