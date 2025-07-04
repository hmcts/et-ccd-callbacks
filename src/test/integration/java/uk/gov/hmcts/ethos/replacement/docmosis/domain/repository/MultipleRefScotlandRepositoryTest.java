package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class MultipleRefScotlandRepositoryTest {

    @Autowired
    MultipleRefScotlandRepository multipleRefScotlandRepository;

    @BeforeAll
    static void setUp() {
        EtCosPostgresqlContainer.getInstance().start();
    }

    @Test
    public void testGenerateRefs() {
        assertEquals("8000001", multipleRefScotlandRepository.ethosMultipleCaseRefGen());
        assertEquals("8000002", multipleRefScotlandRepository.ethosMultipleCaseRefGen());
        assertEquals("8000003", multipleRefScotlandRepository.ethosMultipleCaseRefGen());
    }
}
