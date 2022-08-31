package uk.gov.hmcts.ethos.replacement.docmosis;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;

/**
 * Utility class to handle startup of dm-store service in the cftlib environment.
 */
final class DmStore {

    private DmStore() {
        // Utility class
    }

    /**
     * Starts the dm-store service.
     */
    @SneakyThrows
    static void start()  {
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "compose", "-f", "dm-store.yml", "up", "-d");
        URL dir = DmStore.class.getClassLoader().getResource("compose");
        processBuilder.directory(new File(dir.toURI()))
                .start();
    }

}
