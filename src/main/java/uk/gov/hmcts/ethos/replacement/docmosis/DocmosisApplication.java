package uk.gov.hmcts.ethos.replacement.docmosis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@ComponentScan("uk.gov.hmcts.reform.authorisation")
@EnableFeignClients(basePackages = {"uk.gov.hmcts.ethos.replacement"
    }
)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, this is not a utility class
public class DocmosisApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocmosisApplication.class, args);
    }

}

