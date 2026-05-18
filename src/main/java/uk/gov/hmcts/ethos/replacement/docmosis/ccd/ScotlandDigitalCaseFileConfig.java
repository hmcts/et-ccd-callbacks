package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandDigitalCaseFileConfig extends DigitalCaseFileConfig<ScotlandCaseData> {

    public ScotlandDigitalCaseFileConfig() {
        super(
            ScotlandCaseData::getUploadOrRemoveDcf,
            ScotlandCaseData::getDigitalCaseFile,
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND
        );
    }
}
