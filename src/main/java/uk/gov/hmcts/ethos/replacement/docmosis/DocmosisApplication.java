package uk.gov.hmcts.ethos.replacement.docmosis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScheduledTaskRunner;

import java.util.TimeZone;
import javax.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.ethos", "uk.gov.hmcts.ecm.common"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.ethos.replacement"})
@EnableScheduling
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, this is not a utility class
@Slf4j
public class DocmosisApplication implements CommandLineRunner {

    @Autowired(required = false)
    ScheduledTaskRunner taskRunner;

    public static void main(String[] args) {
        final var application = new SpringApplication(DocmosisApplication.class);
        final var instance = application.run(args);

        if (System.getenv("TASK_NAME") != null) {
            instance.close();
        }
    }

    @Override
    public void run(String... args) {
        if (System.getenv("TASK_NAME") != null && taskRunner != null) {
            taskRunner.run(System.getenv("TASK_NAME"));
        }
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
    }
}
