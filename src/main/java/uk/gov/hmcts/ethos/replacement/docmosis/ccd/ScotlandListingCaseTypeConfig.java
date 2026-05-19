package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandListingCaseTypeConfig extends ListingCaseTypeConfig<ScotlandListingData> {

    public ScotlandListingCaseTypeConfig() {
        super(
            "ET_Scotland_Listings",
            "Scotland - Reports (RET)",
            "Scotland - Hearings/Reports (RET)",
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true
        );
    }
}
