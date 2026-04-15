package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty("preview.data-store-case-id-sequence-base")
public class PreviewDataStoreCaseIdSequenceInitialiser implements ApplicationRunner {

    static final String ALIGN_SEQUENCE_SQL = """
        select setval(
            'case_data_id_seq',
            greatest(
                coalesce((select last_value from case_data_id_seq), 0),
                coalesce((select max(id) from case_data), 0),
                ?
            ),
            true
        )
        """;

    private final long sequenceBase;
    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final String password;
    private final String connectionOptions;

    public PreviewDataStoreCaseIdSequenceInitialiser(
        @Value("${preview.data-store-case-id-sequence-base}") long sequenceBase,
        @Value("${preview.data-store-db-host}") String host,
        @Value("${preview.data-store-db-port}") int port,
        @Value("${preview.data-store-db-name}") String databaseName,
        @Value("${preview.data-store-db-username}") String username,
        @Value("${preview.data-store-db-password:${ET_PREVIEW_FLEXI_DB_PASSWORD:}}") String password,
        @Value("${preview.data-store-db-conn-options:?sslmode=require}") String connectionOptions
    ) {
        this.sequenceBase = sequenceBase;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.connectionOptions = connectionOptions;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long alignedValue = createJdbcTemplate().queryForObject(ALIGN_SEQUENCE_SQL, Long.class, sequenceBase);
        log.info("Aligned preview data-store case_data_id_seq to {} using base {}", alignedValue, sequenceBase);
    }

    JdbcTemplate createJdbcTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://%s:%s/%s%s".formatted(host, port, databaseName, connectionOptions));
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }
}
