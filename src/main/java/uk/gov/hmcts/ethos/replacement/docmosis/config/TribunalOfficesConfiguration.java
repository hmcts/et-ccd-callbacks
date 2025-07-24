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
@ConfigurationProperties("tribunal-offices")
@PropertySource(value = "classpath:defaults.yml", factory = YamlPropertySourceFactory.class)
@Getter
public class TribunalOfficesConfiguration {
    private final Map<TribunalOffice, ContactDetails> contactDetails = new ConcurrentHashMap<>();

    private final Map<TribunalOffice, CourtLocations> courtLocations = new ConcurrentHashMap<>();
}
