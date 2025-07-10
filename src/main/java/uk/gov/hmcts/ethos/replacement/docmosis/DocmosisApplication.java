package uk.gov.hmcts.ethos.replacement.docmosis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.ccd.document.am.config.CaseDocumentManagementClientAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.ethos", "uk.gov.hmcts.ecm.common",
    "uk.gov.hmcts.reform.document", "uk.gov.hmcts.reform.authorisation"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.ethos.replacement"})
@EnableScheduling
@Import(CaseDocumentManagementClientAutoConfiguration.class)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, this is not a utility class
public class DocmosisApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocmosisApplication.class, args);
    }

}