package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesDigitalCaseFileConfig extends DigitalCaseFileConfig<EnglandWalesCaseData> {

    public EnglandWalesDigitalCaseFileConfig() {
        super(
            EnglandWalesCaseData::getUploadOrRemoveDcf,
            EnglandWalesCaseData::getDigitalCaseFile,
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES
        );
    }
}
