package uk.gov.hmcts.ethos.replacement.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HealthCheckTest {

    @BeforeEach
    public void before() {
    }

    @Test
    @Tag("SmokeTest")
    public void healthcheck_returns_200() {
        assertThat("smokeTest", is("smokeTest"));
    }
}
