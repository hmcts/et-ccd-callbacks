package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HighLevelDataSetupConfigurationTest {

    private static final String AAT_DEFINITION_STORE_URL =
        "http://ccd-definition-store-api-aat.service.core-compute-aat.internal";
    private static final String PRODUCTION_DEFINITION_STORE_URL =
        "http://ccd-definition-store-api-prod.service.core-compute-prod.internal";

    @Test
    void shouldAllowAatDefinitionsForAatDefinitionStore() {
        assertThatCode(() -> HighLevelDataSetupConfiguration.validateDefinitionStoreTarget(
            CcdEnvironment.AAT,
            AAT_DEFINITION_STORE_URL
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowProductionDefinitionsForProductionDefinitionStore() {
        assertThatCode(() -> HighLevelDataSetupConfiguration.validateDefinitionStoreTarget(
            CcdEnvironment.PROD,
            PRODUCTION_DEFINITION_STORE_URL
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectAatDefinitionsForProductionDefinitionStore() {
        assertThatThrownBy(() -> HighLevelDataSetupConfiguration.validateDefinitionStoreTarget(
            CcdEnvironment.AAT,
            PRODUCTION_DEFINITION_STORE_URL
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Refusing to load AAT definitions");
    }

    @Test
    void shouldRejectProductionDefinitionsForAatDefinitionStore() {
        assertThatThrownBy(() -> HighLevelDataSetupConfiguration.validateDefinitionStoreTarget(
            CcdEnvironment.PROD,
            AAT_DEFINITION_STORE_URL
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Refusing to load PROD definitions");
    }
}
