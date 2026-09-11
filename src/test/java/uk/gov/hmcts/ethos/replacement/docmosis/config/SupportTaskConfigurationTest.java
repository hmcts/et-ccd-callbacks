package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    SupportTaskConfiguration.class,
    ConfigurationPropertiesAutoConfiguration.class
})
class SupportTaskConfigurationTest {
    @Autowired
    private SupportTaskConfiguration configuration;

    @Test
    void loadsFlagCodesFromYaml() {
        assertThat(configuration.getReview().getAdminFlagCodes())
                .containsExactlyInAnyOrder(
                        "RA0021", "RA0033", "RA0039", "RA0041");
        assertThat(configuration.getReview().getJudgeFlagCodes())
                .containsExactlyInAnyOrder("RA0029", "RA0031", "RA0032", "RA0037", "RA0038");
        assertThat(configuration.getReview().getLegalOfficerFlagCodes())
                .containsExactlyInAnyOrder("RA0034", "RA0035", "RA0036");

        assertThat(configuration.getArrange().getFlagTitles())
                .containsEntry("RA0017", "Guidance on how to complete forms")
                .containsEntry("RA0018", "Support filling in forms")
                .containsEntry("RA0019", "Step free / wheelchair access")
                .containsEntry("RA0020", "Use of venue wheelchair")
                .containsEntry("RA0021", "Parking space close to the venue")
                .containsEntry("RA0022", "Accessible toilet")
                .containsEntry("RA0024", "A different type of chair")
                .containsEntry("RA0030", "Appropriate lighting")
                .containsEntry("RA0038", "Intermediary")
                .containsEntry("RA0039", "Speech to text reporter (palantypist)")
                .containsEntry("RA0041", "Lip speaker")
                .containsEntry("RA0042", "Sign language interpreter")
                .containsEntry("RA0043", "Hearing loop (hearing enhancement system)")
                .containsEntry("RA0044", "Infrared receiver (hearing enhancement system)")
                .containsEntry("RA0045", "Induction loop (hearing enhancement system)")
                .containsEntry("RA0046", "Visit to court or tribunal before the hearing")
                .hasSize(16);
    }
}
