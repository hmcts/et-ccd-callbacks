package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandCaseCreationConfig extends CaseCreationConfig<ScotlandCaseData> {

    public ScotlandCaseCreationConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true
        );
    }

    @Override
    protected Display updateDraftTriageDisplay() {
        return Display.COMPLEX;
    }
}
