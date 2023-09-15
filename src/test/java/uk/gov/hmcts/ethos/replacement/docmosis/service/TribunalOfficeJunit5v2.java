package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

@ExtendWith(SpringExtension.class)

@EnableConfigurationProperties(value = TribunalOfficesConfiguration.class)
@TestPropertySource("classpath:defaults.yml")

public class TribunalOfficeJunit5v2 {

    @Qualifier(value = "tribunalOfficesConfiguration")
    private TribunalOfficesConfiguration config;

    @BeforeEach
    void setup() {
        this.config = new TribunalOfficesConfiguration();
    }
    @Test
    void givenUserDefinedPOJO_whenBindingPropertiesFile_thenAllFieldsAreSet() {
        assertEquals("Manchester", config.getContactDetails().get("Manchester"));

    }
}
