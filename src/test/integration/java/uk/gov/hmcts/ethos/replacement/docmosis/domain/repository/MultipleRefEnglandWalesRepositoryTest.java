package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MultipleRefEnglandWalesRepositoryTest {

    @Autowired
    MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;

    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    @Test
    void testGenerateRefs() {
        assertEquals("6000001", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
        assertEquals("6000002", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
        assertEquals("6000003", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
    }
}
