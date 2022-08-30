package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.config.YamlPropertySourceFactory;

import java.util.EnumMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "fixed-list-mappings")
@PropertySource(value = "classpath:fixedListMappings.yml", factory = YamlPropertySourceFactory.class)
@Getter
class FixedListMappings {
    private final EnumMap<TribunalOffice, Map<String, String>> rooms = new EnumMap<>(TribunalOffice.class);
}
