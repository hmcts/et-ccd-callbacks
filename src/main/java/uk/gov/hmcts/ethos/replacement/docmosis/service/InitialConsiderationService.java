package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;

@Service
public class InitialConsiderationService {
    private static final String HORIZONTAL_RULE = "<hr>";

    private static final String completeICTitle = "<h3>What happens next</h3>";
    private static final String completeICText =
            "<p>A tribunal caseworker will act on any instructions set out in your initial consideration to progress the case. " +
                    "You can <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\">view the initial " +
                    "consideration document in the Documents tab (opens in new tab).</a></p>";

    public String getCompletionText(){
        return new StringBuilder().append(HORIZONTAL_RULE).append(completeICTitle).append(completeICText).toString();
    }
}
