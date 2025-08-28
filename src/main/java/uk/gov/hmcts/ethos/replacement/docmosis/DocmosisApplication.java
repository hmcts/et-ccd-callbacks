package uk.gov.hmcts.ethos.replacement.docmosis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.ethos", "uk.gov.hmcts.ecm.common",
    "uk.gov.hmcts.reform.document", "uk.gov.hmcts.reform.authorisation", "uk.gov.hmcts.reform.ccd.document"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.ethos.replacement"})
@EnableScheduling
@EnableCaching
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, this is not a utility class
public class DocmosisApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocmosisApplication.class, args);
    }

}