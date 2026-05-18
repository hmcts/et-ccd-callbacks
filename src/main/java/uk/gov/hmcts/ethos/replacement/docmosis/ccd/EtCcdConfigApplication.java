package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.ccd.sdk",
        "uk.gov.hmcts.ethos.replacement.docmosis.ccd"
    },
    exclude = {
        DataSourceAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    }
)
public class EtCcdConfigApplication {
}
