package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesCaseLinksConfig extends CaseLinksConfig<EnglandWalesCaseData> {

    public EnglandWalesCaseLinksConfig() {
        super(
            EnglandWalesCaseData::getCaseLinks,
            EnglandWalesCaseData::getLinkedCasesComponentLauncher,
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES
        );
    }
}
