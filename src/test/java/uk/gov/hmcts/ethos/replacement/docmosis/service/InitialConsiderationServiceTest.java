package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InitialConsiderationServiceTest {
    private static String EXPECTED_COMPLETION_TEXT = "<hr><h3>What happens next</h3><p>A tribunal caseworker will act on any instructions set " +
            "out in your initial consideration to progress the case. You can <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" " +
            "target=\"_blank\">view the initial consideration document in the Documents tab (opens in new tab).</a></p>";

    private InitialConsiderationService initialConsiderationService;

    @BeforeEach
    public void setup(){
        initialConsiderationService = new InitialConsiderationService();
    }

    @Test
    public void getCompletionTextTest(){
        assertEquals(EXPECTED_COMPLETION_TEXT, initialConsiderationService.getCompletionText());
    }
}
