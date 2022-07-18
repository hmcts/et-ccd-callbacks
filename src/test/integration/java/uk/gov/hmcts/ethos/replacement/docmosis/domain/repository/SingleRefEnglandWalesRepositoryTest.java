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
public class SingleRefEnglandWalesRepositoryTest {

    @Autowired
    SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;

    @ClassRule
    public static final PostgreSQLContainer postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    @Test
    public void testGenerateRefs() {
        assertEquals("6000001/2022", singleRefEnglandWalesRepository.ethosCaseRefGen(2022));
        assertEquals("6000002/2022", singleRefEnglandWalesRepository.ethosCaseRefGen(2022));
        assertEquals("6000003/2022", singleRefEnglandWalesRepository.ethosCaseRefGen(2022));
        assertEquals("6000001/2023", singleRefEnglandWalesRepository.ethosCaseRefGen(2023));
        assertEquals("6000002/2023", singleRefEnglandWalesRepository.ethosCaseRefGen(2023));
        assertEquals("6000004/2022", singleRefEnglandWalesRepository.ethosCaseRefGen(2022));
    }
}
