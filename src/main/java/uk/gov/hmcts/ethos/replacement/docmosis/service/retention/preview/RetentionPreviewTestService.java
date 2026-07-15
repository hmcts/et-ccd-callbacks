package uk.gov.hmcts.ethos.replacement.docmosis.service.retention.preview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.service.retention.CaseRetentionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.retention.RetentionTaskResult;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RetentionPreviewTestService {
    private static final long REFERENCE_BASE = 9_999_000_000_000_000L;
    private static final String TEST_FLAG = "retentionPreviewTest";
    private static final String RUN_ID = "retentionPreviewTestRunId";
    private static final String ROLE = "retentionPreviewRole";
    private static final String DEFAULT_CASE_TYPE = "ET_EnglandWales";
    private static final String DEFAULT_JURISDICTION = "EMPLOYMENT";
    private static final String DRAFT_STATE = "AWAITING_SUBMISSION_TO_HMCTS";
    private static final String SIMULATION_MODE = "simulation";
    private static final String DELETION_MODE = "deletion";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final CaseRetentionService caseRetentionService;
    private final Clock clock;

    public SeedResponse seed(SeedRequest request) {
        SeedOptions options = SeedOptions.from(request, clock);
        List<PreviewCase> cases = new ArrayList<>();

        for (int i = 0; i < options.numberOfExpiredCases(); i++) {
            cases.add(insertPreviewCase(options, options.nextReference(), "expired-draft", options.expiredDate()));
        }

        for (int i = 0; i < options.numberOfFutureCases(); i++) {
            cases.add(insertPreviewCase(options, options.nextReference(), "future-draft", options.futureDate()));
        }

        return new SeedResponse(options.seedRunId(), cases);
    }

    public int expire(ExpireRequest request) {
        LocalDate resolvedTtl = LocalDate.now(clock).minusDays(Math.max(request.effectiveDaysInPast(), 1));
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("resolvedTtl", resolvedTtl)
            .addValue("ttlText", resolvedTtl.toString());

        String whereClause = whereClause(request.runId(), request.references(), parameters);
        if (StringUtils.isBlank(whereClause)) {
            return 0;
        }

        return jdbcTemplate.update("""
            update ccd.case_data
               set resolved_ttl = :resolvedTtl,
                   data = jsonb_set(data, '{TTL,SystemTTL}', to_jsonb(cast(:ttlText as text)), true),
                   last_modified = now() at time zone 'UTC'
             where """ + whereClause, parameters);
    }

    public RetentionTaskResult run(RunRequest request) {
        Set<String> caseTypeIds = request.effectiveCaseTypeIds();
        int batchSize = request.effectiveBatchSize() <= 0 ? 25 : request.effectiveBatchSize();
        if (!request.effectiveIncludeNonPreviewCases() && hasExpiredNonPreviewCases(caseTypeIds)) {
            throw new IllegalArgumentException(
                "Expired non-preview draft cases exist. Set includeNonPreviewCases=true to run anyway."
            );
        }
        return switch (request.normalisedMode()) {
            case SIMULATION_MODE -> caseRetentionService.run(Set.of(), caseTypeIds, batchSize);
            case DELETION_MODE -> caseRetentionService.run(caseTypeIds, Set.of(), batchSize);
            default -> throw new IllegalArgumentException("mode must be 'deletion' or 'simulation'");
        };
    }

    public List<PreviewCase> list(String runId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String whereClause = "data ->> '" + TEST_FLAG + "' = 'true'";
        if (StringUtils.isNotBlank(runId)) {
            whereClause += " and data ->> '" + RUN_ID + "' = :runId";
            parameters.addValue("runId", runId);
        }

        return jdbcTemplate.query("""
            select reference,
                   case_type_id,
                   state,
                   resolved_ttl,
                   data ->> 'retentionPreviewRole' as role,
                   data ->> 'retentionPreviewTestRunId' as run_id
              from ccd.case_data
             where
            """ + whereClause + """
             order by reference
            """, parameters, (rs, rowNum) -> new PreviewCase(
            rs.getObject("reference", Long.class),
            rs.getString("case_type_id"),
            rs.getString("state"),
            rs.getObject("resolved_ttl", LocalDate.class),
            rs.getString("role"),
            rs.getString("run_id")
        ));
    }

    public int cleanup(String runId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String whereClause = "data ->> '" + TEST_FLAG + "' = 'true'";
        if (StringUtils.isNotBlank(runId)) {
            whereClause += " and data ->> '" + RUN_ID + "' = :runId";
            parameters.addValue("runId", runId);
        }

        return jdbcTemplate.update("delete from ccd.case_data where " + whereClause, parameters);
    }

    private boolean hasExpiredNonPreviewCases(Set<String> caseTypeIds) {
        Integer count = jdbcTemplate.queryForObject("""
            select count(*)
              from ccd.case_data
             where state = :state
               and case_type_id in (:caseTypeIds)
               and resolved_ttl < current_date
               and coalesce(data ->> 'retentionPreviewTest', 'false') <> 'true'
            """, new MapSqlParameterSource()
            .addValue("state", DRAFT_STATE)
            .addValue("caseTypeIds", caseTypeIds), Integer.class);
        return count != null && count > 0;
    }

    private PreviewCase insertPreviewCase(SeedOptions options, long reference, String role, LocalDate resolvedTtl) {
        String data = caseData(options.seedRunId(), role, resolvedTtl);

        jdbcTemplate.update("""
            insert into ccd.case_data (
                reference,
                id,
                version,
                created_date,
                security_classification,
                last_state_modified_date,
                resolved_ttl,
                last_modified,
                jurisdiction,
                case_type_id,
                state,
                data,
                supplementary_data,
                case_revision
            ) values (
                :reference,
                :reference,
                1,
                now() at time zone 'UTC',
                'PUBLIC'::ccd.securityclassification,
                now() at time zone 'UTC',
                :resolvedTtl,
                now() at time zone 'UTC',
                :jurisdiction,
                :caseTypeId,
                :state,
                cast(:data as jsonb),
                '{}'::jsonb,
                1
            )
            on conflict (reference) do update
               set resolved_ttl = excluded.resolved_ttl,
                   last_modified = now() at time zone 'UTC',
                   data = excluded.data,
                   case_type_id = excluded.case_type_id,
                   jurisdiction = excluded.jurisdiction,
                   state = excluded.state
            """, new MapSqlParameterSource()
            .addValue("reference", reference)
            .addValue("resolvedTtl", resolvedTtl)
            .addValue("jurisdiction", options.selectedJurisdiction())
            .addValue("caseTypeId", options.selectedCaseTypeId())
            .addValue("state", options.selectedState())
            .addValue("data", data));

        return new PreviewCase(reference, options.selectedCaseTypeId(), options.selectedState(), resolvedTtl, role,
            options.seedRunId());
    }

    @SneakyThrows
    private String caseData(String runId, String role, LocalDate resolvedTtl) {
        ObjectNode data = objectMapper.createObjectNode();
        data.put(TEST_FLAG, true);
        data.put(RUN_ID, runId);
        data.put(ROLE, role);
        ObjectNode ttl = data.putObject("TTL");
        ttl.put("SystemTTL", resolvedTtl.toString());
        ttl.putNull("OverrideTTL");
        ttl.put("Suspended", "No");
        return objectMapper.writeValueAsString(data);
    }

    private String whereClause(String runId, Collection<Long> references, MapSqlParameterSource parameters) {
        List<String> predicates = new ArrayList<>();
        predicates.add("data ->> '" + TEST_FLAG + "' = 'true'");

        if (StringUtils.isNotBlank(runId)) {
            predicates.add("data ->> '" + RUN_ID + "' = :runId");
            parameters.addValue("runId", runId);
        }
        if (!references.isEmpty()) {
            predicates.add("reference in (:references)");
            parameters.addValue("references", references);
        }

        if (StringUtils.isBlank(runId) && references.isEmpty()) {
            return "";
        }
        return String.join(" and ", predicates);
    }

    public record SeedRequest(
        String runId,
        String caseTypeId,
        String jurisdiction,
        String state,
        Integer expiredCount,
        Integer futureCount,
        Integer linkedFutureCount
    ) {
    }

    public record SeedResponse(String runId, List<PreviewCase> cases) {
    }

    public record ExpireRequest(String runId, List<Long> references, Integer daysInPast) {
        public List<Long> references() {
            return references == null ? List.of() : references;
        }

        public int effectiveDaysInPast() {
            return daysInPast == null ? 1 : daysInPast;
        }
    }

    public record RunRequest(String mode, List<String> caseTypeIds, Integer batchSize, Boolean includeNonPreviewCases) {
        public Set<String> effectiveCaseTypeIds() {
            if (caseTypeIds == null || caseTypeIds.isEmpty()) {
                return Set.of(DEFAULT_CASE_TYPE);
            }
            List<String> selectedCaseTypeIds = caseTypeIds.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .toList();
            return selectedCaseTypeIds.isEmpty() ? Set.of(DEFAULT_CASE_TYPE) : Set.copyOf(selectedCaseTypeIds);
        }

        public String normalisedMode() {
            return defaultIfBlank(mode, DELETION_MODE).toLowerCase(Locale.ENGLISH);
        }

        public int effectiveBatchSize() {
            return batchSize == null ? 25 : batchSize;
        }

        public boolean effectiveIncludeNonPreviewCases() {
            return Boolean.TRUE.equals(includeNonPreviewCases);
        }
    }

    public record PreviewCase(
        Long reference,
        String caseTypeId,
        String state,
        LocalDate resolvedTtl,
        String role,
        String runId
    ) {
    }

    private static final class SeedOptions {
        private final String runId;
        private final String caseTypeId;
        private final String jurisdiction;
        private final String state;
        private final int expiredCount;
        private final int futureCount;
        private final LocalDate expiredTtl;
        private final LocalDate futureTtl;
        private final long referenceSeed;
        private long sequence;

        private SeedOptions(SeedRequest request, Clock clock) {
            runId = defaultIfBlank(request.runId(), "preview-" + System.currentTimeMillis());
            caseTypeId = defaultIfBlank(request.caseTypeId(), DEFAULT_CASE_TYPE);
            jurisdiction = defaultIfBlank(request.jurisdiction(), DEFAULT_JURISDICTION);
            state = defaultIfBlank(request.state(), DRAFT_STATE);
            expiredCount = positiveOrDefault(request.expiredCount(), 2);
            futureCount = positiveOrDefault(request.futureCount(), 1);
            expiredTtl = LocalDate.now(clock).minusDays(1);
            futureTtl = LocalDate.now(clock).plusDays(30);
            referenceSeed = REFERENCE_BASE + System.currentTimeMillis() % 1_000_000_000L;
            sequence = 1;
        }

        private static SeedOptions from(SeedRequest request, Clock clock) {
            SeedRequest safeRequest = request == null
                ? new SeedRequest(null, null, null, null, null, null, null)
                : request;
            return new SeedOptions(safeRequest, clock);
        }

        private static int positiveOrDefault(Integer value, int defaultValue) {
            if (value == null) {
                return defaultValue;
            }
            return Math.max(value, 0);
        }

        private String seedRunId() {
            return runId;
        }

        private String selectedCaseTypeId() {
            return caseTypeId;
        }

        private String selectedJurisdiction() {
            return jurisdiction;
        }

        private String selectedState() {
            return state;
        }

        private int numberOfExpiredCases() {
            return expiredCount;
        }

        private int numberOfFutureCases() {
            return futureCount;
        }

        private LocalDate expiredDate() {
            return expiredTtl;
        }

        private LocalDate futureDate() {
            return futureTtl;
        }

        private long nextReference() {
            return referenceSeed + sequence++;
        }
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }
}
