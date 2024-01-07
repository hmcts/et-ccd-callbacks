package uk.gov.hmcts.ethos.replacement.docmosis;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import static java.io.File.separator;

/**
 * Utility class to handle execution of docker compose up in the cftlib environment.
 * This is to enable the startup of ET specific services.
 */
@Slf4j
final class DockerComposeProcessRunner {

    private DockerComposeProcessRunner() {
        // Utility class
    }

    /**
     * Starts docker compose.
     */
    @SneakyThrows
    static void start()  {
        String strComposeFiles = "docker-compose.yml," + System.getenv("CFTLIB_EXTRA_COMPOSE_FILES");

        String[] composeFiles = strComposeFiles.split(",");
        URL compose = Thread.currentThread().getContextClassLoader().getResource("compose");
        String basePath = Objects.requireNonNull(compose).getPath();

        for (String file : composeFiles) {
            String path = String.join(separator, basePath, file);

            Process process = new ProcessBuilder("docker", "compose", "-f", path, "up", "-d").inheritIO().start();

            int code = process.waitFor();

            if (code != 0) {
                log.error("****** Failed to start services in {} ******", file);
                log.info("Exit value: {}", code);
                return;
            }

            log.info("Successfully started services in {}", file);
        }
    }
}
