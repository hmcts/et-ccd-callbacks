package uk.gov.hmcts.ethos.replacement.docmosis;

import com.ibm.icu.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DocmosisApplicationTest {

    @Test
    void doesNotRunTaskRunnerWhenTaskRunnerIsNull() {
        System.setProperty("TASK_NAME", "TASK_NAME");
        DocmosisApplication application = new DocmosisApplication();
        application.taskRunner = null;
        application.run();

        // No exception should be thrown, and no task should be run
        System.clearProperty("TASK_NAME");
    }

    @Test
    void setsDefaultTimeZoneToEuropeLondonOnInit() {
        DocmosisApplication application = new DocmosisApplication();
        application.init();

        assert (TimeZone.getDefault().getID().equalsIgnoreCase("Europe/London"));
    }
}
