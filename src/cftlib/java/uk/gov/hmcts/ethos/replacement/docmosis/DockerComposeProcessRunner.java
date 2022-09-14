package uk.gov.hmcts.ethos.replacement.docmosis;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;

/**
 * Utility class to handle execution of docker compose up in the cftlib environment.
 * This is to enable the startup of ET specific services.
 */
final class DockerComposeProcessRunner {

    private DockerComposeProcessRunner() {
        // Utility class
    }

    /**
     * Starts docker compose.
     */
    @SneakyThrows
    static void start()  {
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "compose", "up", "-d");
        URL dir = DockerComposeProcessRunner.class.getClassLoader().getResource("compose");
        processBuilder.directory(new File(dir.toURI()))
                .start();
    }

}
