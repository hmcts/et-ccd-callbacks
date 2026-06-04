package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.json.JsonBackedCCDConfig;
import uk.gov.hmcts.ccd.sdk.json.JsonBackedCCDConfigFactory;
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

    private static final String ENGLAND_WALES_JSON_ROOT =
        "file:ccd-definitions/jurisdictions/england-wales/json";
    private static final String SCOTLAND_JSON_ROOT =
        "file:ccd-definitions/jurisdictions/scotland/json";

    @Bean
    public CCDConfig<AdminData, AdminCaseState, PlaceholderRole> etAdminJsonCcdConfig(
        JsonBackedCCDConfigFactory factory
    ) {
        return new JsonBackedCCDConfig<>(
          factory,
          ADMIN_CASE_TYPE_ID,
          "file:ccd-definitions/jurisdictions/admin/json"
        ) {
        };
    }

    @Bean
    public CCDConfig<CaseData, CaseState, PlaceholderRole> etEnglandWalesJsonCcdConfig(
        JsonBackedCCDConfigFactory factory
    ) {
        return new JsonBackedCCDConfig<>(
          factory,
          ENGLANDWALES_CASE_TYPE_ID,
          ENGLAND_WALES_JSON_ROOT
        ) {
        };
    }

    @Bean
    public CCDConfig<CaseData, CaseState, PlaceholderRole> etScotlandJsonCcdConfig(
        JsonBackedCCDConfigFactory factory
    ) {
        return new JsonBackedCCDConfig<>(
          factory,
          SCOTLAND_CASE_TYPE_ID,
          SCOTLAND_JSON_ROOT
        ) {
        };
    }

    @Bean
    public CCDConfig<ListingData, ListingCaseState, PlaceholderRole> etEnglandWalesListingJsonCcdConfig(
        JsonBackedCCDConfigFactory factory
    ) {
        return new JsonBackedCCDConfig<>(
          factory,
          ENGLANDWALES_LISTING_CASE_TYPE_ID,
          ENGLAND_WALES_JSON_ROOT
        ) {
        };
    }

    @Bean
    public CCDConfig<ListingData, ListingCaseState, PlaceholderRole> etScotlandListingJsonCcdConfig(
        JsonBackedCCDConfigFactory factory
    ) {
        return new JsonBackedCCDConfig<>(
          factory,
          SCOTLAND_LISTING_CASE_TYPE_ID,
          SCOTLAND_JSON_ROOT
        ) {
        };
    }

    @Bean
    public CCDConfig<MultipleData, MultipleCaseState, PlaceholderRole> etEnglandWalesMultipleJsonCcdConfig(
        JsonBackedCCDConfigFactory factory
    ) {
        return new JsonBackedCCDConfig<>(
          factory,
          ENGLANDWALES_BULK_CASE_TYPE_ID,
          ENGLAND_WALES_JSON_ROOT
        ) {
        };
    }

    @Bean
    public CCDConfig<MultipleData, MultipleCaseState, PlaceholderRole> etScotlandMultipleJsonCcdConfig(
        JsonBackedCCDConfigFactory factory
    ) {
        return new JsonBackedCCDConfig<>(
          factory,
          SCOTLAND_BULK_CASE_TYPE_ID,
          SCOTLAND_JSON_ROOT
        ) {
        };
    }

  /**
   * If you someday wish to represent your case in Java instead of json, you can define case roles using enums.
   * This is an inert placeholder.
   */
  public enum PlaceholderRole implements HasRole {
        DUMMY_STATE;

        @Override
        public String getRole() {
            return toString();
        }

        @Override
        public String getCaseTypePermissions() {
            return "CRUD";
        }
    }
}
