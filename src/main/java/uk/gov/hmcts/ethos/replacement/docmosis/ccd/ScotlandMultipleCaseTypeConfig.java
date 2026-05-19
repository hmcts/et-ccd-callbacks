package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandMultipleCaseTypeConfig extends MultipleCaseTypeConfig<ScotlandMultipleData> {

    public ScotlandMultipleCaseTypeConfig() {
        super(
            "ET_Scotland_Multiple",
            "Scotland - Multiples (RET)",
            "Scotland - Multiples (RET)",
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND
        );
    }
}
