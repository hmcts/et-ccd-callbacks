package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.ccd.sdk.CCDDefinitionGenerator;
import java.io.File;

public final class EtMigrationDefinitionGenerator {

    private EtMigrationDefinitionGenerator() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the generated JSON output directory");
        }

        for (String profile : new String[] {"cftlib", "prod"}) {
            try (AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext()) {
                context.getEnvironment().setActiveProfiles(profile);
                context.register(DefinitionConfiguration.class);
                context.refresh();
                context.getBean(CCDDefinitionGenerator.class)
                        .generateAllCaseTypesToJSON(new File(args[0], profile));
            }
        }
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                "uk.gov.hmcts.ccd.sdk.generator",
                "uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config"
            })
    @Import(CCDDefinitionGenerator.class)
    static class DefinitionConfiguration {
    }
}
