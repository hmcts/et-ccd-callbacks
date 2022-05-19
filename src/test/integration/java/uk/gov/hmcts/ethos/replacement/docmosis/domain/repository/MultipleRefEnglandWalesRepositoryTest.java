package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class MultipleRefEnglandWalesRepositoryTest {

    @Autowired
    MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;

    @ClassRule
    public static final PostgreSQLContainer postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    @Test
    public void testGenerateRefs() {
        assertEquals("6000001", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
        assertEquals("6000002", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
        assertEquals("6000003", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
    }
}
