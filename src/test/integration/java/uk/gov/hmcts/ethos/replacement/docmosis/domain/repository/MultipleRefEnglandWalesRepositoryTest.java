package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class MultipleRefEnglandWalesRepositoryTest {

    @Autowired
    MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;

    @BeforeAll
    static void setUp() {
        EtCosPostgresqlContainer.getInstance().start();
    }

    @Test
    public void testGenerateRefs() {
        assertEquals("6000001", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
        assertEquals("6000002", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
        assertEquals("6000003", multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen());
    }
}
