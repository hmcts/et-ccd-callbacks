package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandCaseLinksConfig extends CaseLinksConfig<ScotlandCaseData> {

    public ScotlandCaseLinksConfig() {
        super(
            ScotlandCaseData::getCaseLinks,
            ScotlandCaseData::getLinkedCasesComponentLauncher,
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND
        );
    }
}
