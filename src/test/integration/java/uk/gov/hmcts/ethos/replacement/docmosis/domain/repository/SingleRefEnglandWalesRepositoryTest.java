package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class SingleRefEnglandWalesRepositoryTest {

    @Autowired
    SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;

    @BeforeAll
    static void setUp() {
        EtCosPostgresqlContainer.getInstance().start();
    }

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
