package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class SingleRefScotlandRepositoryTest {

    @Autowired
    SingleRefScotlandRepository singleRefScotlandRepository;

    @BeforeAll
    static void setUp() {
        EtCosPostgresqlContainer.getInstance().start();
    }

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
