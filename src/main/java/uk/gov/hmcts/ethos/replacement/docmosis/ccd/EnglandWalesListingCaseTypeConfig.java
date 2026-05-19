package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesListingCaseTypeConfig extends ListingCaseTypeConfig<EnglandWalesListingData> {

    public EnglandWalesListingCaseTypeConfig() {
        super(
            "ET_EnglandWales_Listings",
            "Eng/Wales - Hearings/Reports",
            "England/Wales - Hearings/Reports",
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES
        );
    }
}
