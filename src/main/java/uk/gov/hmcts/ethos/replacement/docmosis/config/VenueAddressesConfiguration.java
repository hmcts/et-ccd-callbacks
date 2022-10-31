package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConfigurationProperties("venue-addresses")
@PropertySource(value = "classpath:venueAddressValues.yml", factory = YamlPropertySourceFactory.class)
@Getter
public class VenueAddressesConfiguration {
    private Map<TribunalOffice, List<VenueAddress>> tribunalOffices = new ConcurrentHashMap<>();
}
