package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SuppressWarnings({"squid:S5961", "PMD.ExcessiveImports", "PMD.GodClass", "PMD.TooManyMethods",
    "PMD.FieldNamingConventions", "PMD.CyclomaticComplexity"})
class PseRespondToTribunalServiceTest {
    private PseRespondToTribunalService pseRespondToTribService;
    private CaseData caseData;

    private static final String RESPONSE = "Some Response";
    private static final String YES = "Yes";
    private static final String NO = "No";

    private static final String EXPECTED_TABLE_MARKDOWN = "|Hearing, case management order or request | |\r\n"
        + "|--|--|\r\n"
        + "|Notification | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Hearing | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Date sent | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Sent by | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Case management order or request? | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Response due | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Party or parties to respond | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Additional information | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Description | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Document | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Case management order made by | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Name | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Sent to | [ToDo: Dependency on RET-2949]|\r\n"
        + "\r\n"
        + "\r\n"
        + "|Response [ToDo: Dependency on RET-2928] | |\r\n"
        + "|--|--|\r\n"
        + "|Response from | [ToDo: Dependency on RET-2928]|\r\n"
        + "|Response date | [ToDo: Dependency on RET-2928]|\r\n"
        + "|What's your response to the tribunal? | [ToDo: Dependency on RET-2928]|\r\n"
        + "|Supporting material | [ToDo: Dependency on RET-2928]|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | [ToDo: Dependency on RET-2928]|\r\n"
        + "\r\n";

    @BeforeEach
    void setUp() {
        pseRespondToTribService = new PseRespondToTribunalService();

        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void initialOrdReqDetailsTableMarkUp_hasOrderRequests() {
        assertThat(pseRespondToTribService.initialOrdReqDetailsTableMarkUp(caseData), is(EXPECTED_TABLE_MARKDOWN));
    }

    @ParameterizedTest
    @MethodSource("inputList")
    void validateInput_CountErrors(String responseText, String supportingMaterial, int expectedErrorCount) {
        caseData.setPseRespondentOrdReqResponseText(responseText);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(supportingMaterial);

        List<String> errors = pseRespondToTribService.validateInput(caseData);

        assertEquals(expectedErrorCount, errors.size());
    }

    private static Stream<Arguments> inputList() {
        return Stream.of(
            Arguments.of(null, null, 1),
            Arguments.of(null, NO, 1),
            Arguments.of(null, YES, 0),
            Arguments.of(RESPONSE, null, 0),
            Arguments.of(RESPONSE, NO, 0),
            Arguments.of(RESPONSE, YES, 0)
        );
    }
}
