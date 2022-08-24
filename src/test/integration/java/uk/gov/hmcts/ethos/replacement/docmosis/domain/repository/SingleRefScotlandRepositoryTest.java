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
public class SingleRefScotlandRepositoryTest {

    @Autowired
    SingleRefScotlandRepository singleRefScotlandRepository;

    @ClassRule
    public static final PostgreSQLContainer postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    @Test
    public void testGenerateRefs() {
        assertEquals("8000001/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
        assertEquals("8000002/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
        assertEquals("8000003/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
        assertEquals("8000001/2023", singleRefScotlandRepository.ethosCaseRefGen(2023));
        assertEquals("8000002/2023", singleRefScotlandRepository.ethosCaseRefGen(2023));
        assertEquals("8000004/2022", singleRefScotlandRepository.ethosCaseRefGen(2022));
    }
}
