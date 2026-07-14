package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.et.common.model.ccd.MultipleRole;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.MultipleCaseState;

@Component
@Profile("cftlib")
public class ScotlandMultipleCftlibDefinition
        implements CCDConfig<MultipleData, MultipleCaseState, MultipleRole> {

    @Override
    public String groupingKey() {
        return "ET_Scotland_Multiple";
    }

    @Override
    public void configure(ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder) {
        MultipleDefinitionSupport.configure(
                builder, MultipleDefinitionSupport.Variant.CFTLIB_SCOTLAND);
    }
}
