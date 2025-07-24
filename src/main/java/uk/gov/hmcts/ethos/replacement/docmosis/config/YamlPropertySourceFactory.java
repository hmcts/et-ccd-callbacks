package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.List;

/**
 * Factory to enable YAML files to be used with {@link PropertySource}.
 *
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources = loader.load(name != null
                ? name
                : resource.getResource().getFilename(),
                resource.getResource());
        
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("No property sources found in YAML resource: "
                                               + resource.getResource().getFilename());
        }
        
        return sources.getFirst();
    }
}
