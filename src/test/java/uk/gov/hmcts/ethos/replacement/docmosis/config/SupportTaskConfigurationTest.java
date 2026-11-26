package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SupportTaskService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    SupportTaskConfiguration.class,
    ConfigurationPropertiesAutoConfiguration.class
})
class SupportTaskConfigurationTest {
    @Autowired
    private SupportTaskConfiguration configuration;

    @Test
    void loadsRetrySettingsFromYaml() {
        assertThat(configuration.getClosureRetry().getMaxAttempts()).isEqualTo(3);
        assertThat(configuration.getClosureRetry().getInitialBackoffMs()).isEqualTo(200);
        assertThat(configuration.getClosureRetry().getMultiplier()).isEqualTo(2);
        assertThat(configuration.getClosureRetry().getMaxBackoffMs()).isEqualTo(1000);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RA0029", "RA0031", "RA0032", "RA0034", "RA0035", "RA0036", "RA0037", "RA0038"})
    void formerlyJudgeOrLegalOfficerReviewedFlagsUseAdminReview(String code) {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder().claimantExternalFlags(CaseFlagsType.builder()
                .details(ListTypeItem.from(FlagDetailType.builder().flagCode(code).status("Requested").build()))
                .build()).build());

        new SupportTaskService(configuration).prepareReviewSupportTasks(caseData);

        assertThat(caseData.getSupportTaskState().getAdminTaskRequired()).isEqualTo("Yes");
        assertThat(caseData.getSupportTaskState().getJudgeTaskRequired()).isNull();
        assertThat(caseData.getSupportTaskState().getLegalOfficerTaskRequired()).isNull();
    }

    @Test
    void loadsFlagCodesFromYaml() {
        assertThat(configuration.getReview().getJudgeFlagCodes()).isEmpty();
        assertThat(configuration.getReview().getLegalOfficerFlagCodes()).isEmpty();
        assertThat(configuration.getReview().getAdminPathFlags()).singleElement().satisfies(pathFlag -> {
            assertThat(pathFlag.getFlagCode()).isEqualTo("OT0001");
            assertThat(pathFlag.getPath()).containsExactly("Party", "Reasonable adjustment");
        });

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
        assertThat(configuration.getArrange().getPathFlags()).singleElement().satisfies(pathFlag -> {
            assertThat(pathFlag.getFlagCode()).isEqualTo("OT0001");
            assertThat(pathFlag.getPath()).containsExactly(
                    "Party", "Reasonable adjustment", "I need help with forms");
            assertThat(pathFlag.getTaskTitle()).isEqualTo("I need help with forms");
        });
    }
}
