package uk.gov.hmcts.ethos.replacement.docmosis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScheduledTaskRunner;

import java.util.TimeZone;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EUROPE_LONDON;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.ethos", "uk.gov.hmcts.ecm.common",
    "uk.gov.hmcts.reform.document", "uk.gov.hmcts.reform.authorisation", "uk.gov.hmcts.reform.ccd.document"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.ethos.replacement"})
@EnableCaching
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, this is not a utility class
@Slf4j
public class DocmosisApplication implements CommandLineRunner {
    private static final String TASK_NAME = "TASK_NAME";

    @Autowired(required = false)
    ScheduledTaskRunner taskRunner;

    @SuppressWarnings("PMD.CloseResource") // Context is intentionally closed only when TASK_NAME env var is set
    public static void main(String[] args) {
        final var application = new SpringApplication(DocmosisApplication.class);
        final var instance = application.run(args);

        if (System.getenv(TASK_NAME) != null) {
            instance.close();
        }
    }

    @Override
    public void run(String... args) {
        if (System.getenv(TASK_NAME) != null && taskRunner != null) {
            taskRunner.run(System.getenv(TASK_NAME));
        }
    }

    public void setTaskRunner(ScheduledTaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(EUROPE_LONDON));
    }
}
