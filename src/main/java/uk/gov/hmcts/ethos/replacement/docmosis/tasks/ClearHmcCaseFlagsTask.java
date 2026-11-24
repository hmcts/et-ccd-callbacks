package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Standalone runnable task to remove {@code caseAdditionalSecurityFlag} and
 * {@code caseInterpreterRequiredFlag} from {@code ccd.case_data.data} using SQL.
 *
 * <p>Set {@code TASK_NAME=ClearHmcCaseFlagsTask} to execute this task. Defaults to dry-run mode
 * ({@code cron.clearHmcCaseFlagsDryRun=true}).</p>
 */
@Component
@Slf4j
public class ClearHmcCaseFlagsTask implements Runnable {

    static final String COUNT_CASES_WITH_HMC_FLAGS = """
        SELECT COUNT(*)
        FROM ccd.case_data
        WHERE case_type_id = :caseTypeId
          AND (
              jsonb_exists(data, 'caseAdditionalSecurityFlag')
              OR jsonb_exists(data, 'caseInterpreterRequiredFlag')
          )
        """;

    static final String CLEAR_HMC_FLAGS_SQL = """
        UPDATE ccd.case_data
        SET data = data - 'caseAdditionalSecurityFlag' - 'caseInterpreterRequiredFlag'
        WHERE case_type_id = :caseTypeId
          AND (
              jsonb_exists(data, 'caseAdditionalSecurityFlag')
              OR jsonb_exists(data, 'caseInterpreterRequiredFlag')
          )
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String caseTypeIdsString;
    private final boolean dryRun;

    @Autowired
    public ClearHmcCaseFlagsTask(
            NamedParameterJdbcTemplate jdbcTemplate,
            @Value("${cron.caseTypeId:ET_EnglandWales,ET_Scotland}") String caseTypeIdsString,
            @Value("${cron.clearHmcCaseFlagsDryRun:true}") boolean dryRun) {
        this.jdbcTemplate = jdbcTemplate;
        this.caseTypeIdsString = caseTypeIdsString;
        this.dryRun = dryRun;
    }

    public ClearHmcCaseFlagsTask(NamedParameterJdbcTemplate jdbcTemplate, boolean dryRun) {
        this(jdbcTemplate, "ET_EnglandWales,ET_Scotland", dryRun);
    }

    @Override
    public void run() {
        log.info("Clear HMC case flags SQL task started (dryRun={})", dryRun);

        for (String rawCaseTypeId : caseTypeIdsString.split(",")) {
            String caseTypeId = rawCaseTypeId.trim();
            if (!caseTypeId.isEmpty()) {
                clearFlagsForCaseType(caseTypeId);
            }
        }

        log.info("Clear HMC case flags SQL task finished");
    }

    void clearFlagsForCaseType(String caseTypeId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("caseTypeId", caseTypeId);

        Long matchingCount = jdbcTemplate.queryForObject(COUNT_CASES_WITH_HMC_FLAGS, parameters, Long.class);
        long count = matchingCount == null ? 0L : matchingCount;
        log.info("Found {} cases with HMC flags for case type {}", count, caseTypeId);

        if (count == 0L) {
            return;
        }

        if (dryRun) {
            log.info("Dry run enabled: would clear HMC flags from {} cases for case type {}", count, caseTypeId);
            return;
        }

        int updatedRows = jdbcTemplate.update(CLEAR_HMC_FLAGS_SQL, parameters);
        log.info("Cleared HMC flags from {} cases for case type {}", updatedRows, caseTypeId);
    }
}
