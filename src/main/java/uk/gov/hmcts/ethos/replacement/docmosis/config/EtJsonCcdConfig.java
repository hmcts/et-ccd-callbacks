package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.sdk.api.TypedCCDConfig;
import uk.gov.hmcts.ccd.sdk.json.JsonCaseTypeFactory;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.AdminCaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.ListingCaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.MultipleCaseState;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

@Configuration
@ComponentScan("uk.gov.hmcts.ccd.sdk")
public class EtJsonCcdConfig {

    private static final String ADMIN_JSON_ROOT =
        "file:ccd-definitions/jurisdictions/admin/json";
    private static final String ENGLAND_WALES_JSON_ROOT =
        "file:ccd-definitions/jurisdictions/england-wales/json";
    private static final String SCOTLAND_JSON_ROOT =
        "file:ccd-definitions/jurisdictions/scotland/json";

    @Bean
    public TypedCCDConfig<AdminData, AdminCaseState, ?> etAdminJsonCcdConfig(JsonCaseTypeFactory factory) {
        return factory.build(AdminData.class, AdminCaseState.class, ADMIN_CASE_TYPE_ID, ADMIN_JSON_ROOT);
    }

    @Bean
    public TypedCCDConfig<CaseData, CaseState, ?> etEnglandWalesJsonCcdConfig(JsonCaseTypeFactory factory) {
        return factory.build(CaseData.class, CaseState.class, ENGLANDWALES_CASE_TYPE_ID, ENGLAND_WALES_JSON_ROOT);
    }

    @Bean
    public TypedCCDConfig<CaseData, CaseState, ?> etScotlandJsonCcdConfig(JsonCaseTypeFactory factory) {
        return factory.build(CaseData.class, CaseState.class, SCOTLAND_CASE_TYPE_ID, SCOTLAND_JSON_ROOT);
    }

    @Bean
    public TypedCCDConfig<ListingData, ListingCaseState, ?> etEnglandWalesListingJsonCcdConfig(
        JsonCaseTypeFactory factory
    ) {
        return factory.build(
            ListingData.class,
            ListingCaseState.class,
            ENGLANDWALES_LISTING_CASE_TYPE_ID,
            ENGLAND_WALES_JSON_ROOT
        );
    }

    @Bean
    public TypedCCDConfig<ListingData, ListingCaseState, ?> etScotlandListingJsonCcdConfig(
        JsonCaseTypeFactory factory
    ) {
        return factory.build(ListingData.class, ListingCaseState.class, SCOTLAND_LISTING_CASE_TYPE_ID,
            SCOTLAND_JSON_ROOT);
    }

    @Bean
    public TypedCCDConfig<MultipleData, MultipleCaseState, ?> etEnglandWalesMultipleJsonCcdConfig(
        JsonCaseTypeFactory factory
    ) {
        return factory.build(
            MultipleData.class,
            MultipleCaseState.class,
            ENGLANDWALES_BULK_CASE_TYPE_ID,
            ENGLAND_WALES_JSON_ROOT
        );
    }

    @Bean
    public TypedCCDConfig<MultipleData, MultipleCaseState, ?> etScotlandMultipleJsonCcdConfig(
        JsonCaseTypeFactory factory
    ) {
        return factory.build(MultipleData.class, MultipleCaseState.class, SCOTLAND_BULK_CASE_TYPE_ID,
            SCOTLAND_JSON_ROOT);
    }
}
