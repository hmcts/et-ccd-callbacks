package uk.gov.hmcts.ethos.replacement.docmosis.service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.BDDAssumptions.*;
import static org.assertj.core.api.FactoryBasedNavigableListAssert.*;
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties(value = TribunalOfficesConfiguration.class)
@TestPropertySource("classpath:defaults.yml")
@ContextConfiguration(classes = TribunalOfficesConfiguration.class)
public class TribunalOfficeServiceMockData {
    @Mock
    private TribunalOfficesConfiguration mockTribunalOfficesConfiguration;
    @InjectMocks
    private TribunalOfficesService tribunalOfficesService;

    private TribunalOfficesConfiguration tribunalOfficesConfiguration;

    @Test
    void shouldNotBeNull() {
        assertThat(mockTribunalOfficesConfiguration).isNotNull();
        assertThat(tribunalOfficesService).isNotNull();
    }

    @Test
    void shouldReturnCorrectOfficeWhenPostcodeIsValid() {

        TribunalOffice office = TribunalOffice.valueOfOfficeName("Manchester");
        CourtLocations courtLocation = new CourtLocations();

       // Map<CourtLocations> mockData = new ConcurrentHashMap<>();
        mockData.put(courtLocation);
                given(mockTribunalOfficesConfiguration.getCourtLocations()).returns(Map.of(courtLocation));

        String result = tribunalOfficesService.setCaseManagementLocationCode();

        Map<TribunalOffice, CourtLocations> courtLocations
        assertThat(result).contains(TribunalOffice.EDINBURGH);
    }
}
