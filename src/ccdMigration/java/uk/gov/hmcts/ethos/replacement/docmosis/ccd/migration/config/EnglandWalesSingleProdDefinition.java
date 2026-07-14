package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SingleRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;

@Component
@Profile("prod")
public class EnglandWalesSingleProdDefinition
        implements CCDConfig<CaseData, CaseState, SingleRole> {
    @Override
    public String groupingKey() {
        return "ET_EnglandWales";
    }

    @Override
    public void configure(ConfigBuilder<CaseData, CaseState, SingleRole> builder) {
        SingleDefinitionSupport.configure(
                builder, SingleDefinitionSupport.Variant.PROD_ENGLAND_WALES);
    }
}
