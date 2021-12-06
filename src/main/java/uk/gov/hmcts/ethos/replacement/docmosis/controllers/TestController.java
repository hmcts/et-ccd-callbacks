package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TornadoConfiguration;

@RestController("/test")
@Slf4j
public class TestController {

    public TestController(TornadoConfiguration tornadoConfiguration) {
        log.info("Tornado key " + tornadoConfiguration.getAccessKey());
        log.info("Tornado URL " + tornadoConfiguration.getUrl());
        log.info("Create Updates " + tornadoConfiguration.getCreateUpdates());
    }
}
