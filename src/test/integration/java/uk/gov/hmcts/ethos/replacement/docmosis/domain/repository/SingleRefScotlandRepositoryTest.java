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
class SingleRefScotlandRepositoryTest {

    @Autowired
    SingleRefScotlandRepository singleRefScotlandRepository;

    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    @Test
    void testGenerateRefs() {
        assertEquals("8000001/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
        assertEquals("8000002/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
        assertEquals("8000003/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
        assertEquals("8000001/2023", singleRefScotlandRepository.ethosCaseRefGen(2023));
        assertEquals("8000002/2023", singleRefScotlandRepository.ethosCaseRefGen(2023));
        assertEquals("8000004/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
    }
}
