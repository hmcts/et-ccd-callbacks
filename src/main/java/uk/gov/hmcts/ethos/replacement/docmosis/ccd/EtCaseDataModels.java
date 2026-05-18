package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;

import java.util.Map;

public final class EtCaseDataModels {

    public static final Map<String, Class<?>> CASE_TYPES = Map.of(
        "ET_Admin", AdminData.class,
        "Pre_Hearing_Deposit", PreHearingDepositData.class,
        "ET_EnglandWales", CaseData.class,
        "ET_Scotland", CaseData.class,
        "ET_EnglandWales_Multiple", MultipleData.class,
        "ET_Scotland_Multiple", MultipleData.class,
        "ET_EnglandWales_Listings", ListingData.class,
        "ET_Scotland_Listings", ListingData.class
    );

    private EtCaseDataModels() {
    }
}
