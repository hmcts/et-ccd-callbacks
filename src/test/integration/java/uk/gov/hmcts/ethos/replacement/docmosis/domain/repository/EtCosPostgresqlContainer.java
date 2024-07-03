package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared container for Postgres database testing.
 */
public final class EtCosPostgresqlContainer extends PostgreSQLContainer<EtCosPostgresqlContainer> {
    private static final DockerImageName myImage = DockerImageName
            .parse("hmctspublic.azurecr.io/imported/postgres:16-alpine").asCompatibleSubstituteFor("postgres");
    private static EtCosPostgresqlContainer container;

    private EtCosPostgresqlContainer() {
        super(myImage);
    }

    public static EtCosPostgresqlContainer getInstance() {
        if (container == null) {
            container = new EtCosPostgresqlContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shutdown
    }
}
