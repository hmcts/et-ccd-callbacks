package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConfigurationProperties(prefix = "tribunal-offices")
@PropertySource(value = "classpath:defaults.yml", factory = YamlPropertySourceFactory.class)
@Getter
public class TribunalOfficesConfiguration {
    private Map<TribunalOffice, ContactDetails> contactDetails = new ConcurrentHashMap<>();
    private Map<TribunalOffice, CourtLocations> courtLocations = new ConcurrentHashMap<>();
}
