package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class HubLinkStatusBackfillTask implements Runnable {

    private static final int MAX_BATCH_ATTEMPTS = 3;
    private static final String BACKFILL_BATCH_SQL = """
        WITH candidates AS MATERIALIZED (
            SELECT reference, data -> 'hubLinksStatuses' AS data
            FROM ccd.case_data
            WHERE reference > ?
              AND jsonb_typeof(data -> 'hubLinksStatuses') = 'object'
            ORDER BY reference
            LIMIT ?
        ),
        inserted AS (
            INSERT INTO public.hub_link_status (case_reference, data)
            SELECT reference, data
            FROM candidates
            ON CONFLICT (case_reference) DO NOTHING
            RETURNING 1
        )
        SELECT COALESCE((SELECT MAX(reference) FROM candidates), ?) AS last_reference,
               (SELECT COUNT(*) FROM candidates) AS scanned,
               (SELECT COUNT(*) FROM inserted) AS inserted
        """;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Value("${hub-link-status-backfill.batch-size:1000}")
    private int batchSize;

    @Override
    public void run() {
        long lastReference = 0L;
        long totalScanned = 0L;
        long totalInserted = 0L;

        do {
            BatchResult result = backfillBatch(lastReference);
            lastReference = result.lastReference();
            totalScanned += result.scanned();
            totalInserted += result.inserted();
            log.info("Hub-link status backfill batch complete: scanned={}, inserted={}, lastReference={}",
                result.scanned(), result.inserted(), lastReference);
            if (result.scanned() < batchSize) {
                break;
            }
        } while (true);

        log.info("Hub-link status backfill complete: scanned={}, inserted={}", totalScanned, totalInserted);
    }

    private BatchResult backfillBatch(long lastReference) {
        for (int attempt = 1; attempt <= MAX_BATCH_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> {
                    jdbcTemplate.execute("SET LOCAL lock_timeout = '3s'");
                    jdbcTemplate.execute("SET LOCAL statement_timeout = '30s'");
                    return jdbcTemplate.queryForObject(
                        BACKFILL_BATCH_SQL,
                        (resultSet, rowNum) -> new BatchResult(
                            resultSet.getLong("last_reference"),
                            resultSet.getLong("scanned"),
                            resultSet.getLong("inserted")
                        ),
                        lastReference,
                        batchSize,
                        lastReference
                    );
                });
            } catch (ConcurrencyFailureException exception) {
                if (attempt == MAX_BATCH_ATTEMPTS) {
                    throw exception;
                }
                log.warn("Hub-link status backfill batch starting after {} failed; retrying", lastReference, exception);
            }
        }
        throw new IllegalStateException("Unable to backfill hub-link status batch");
    }

    private record BatchResult(long lastReference, long scanned, long inserted) {
    }
}
