package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.*;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ContextConfiguration;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration
@EnableConfigurationProperties(value = TribunalOfficesConfiguration.class)
@TestPropertySource("classpath:defaults.yml")
//@Import(TribunalOfficesConfiguration.class)
@ContextConfiguration(classes = TribunalOfficesConfiguration.class)

@ExtendWith(SpringExtension.class)

public class TribunalOfficeServiceService2Test {

    @Autowired
   // @Qualifier("tribunal-offices")
    private TribunalOfficesConfiguration tribunalOfficesConfiguration;

    private TribunalOfficesService tribunalOfficesService;

    private static final String INVALID_POSTCODE = "ABC123";
    private static final String EDINBURGH_POSTCODE_FIRST_PART = "EH";
    private static final String EDINBURGH_POSTCODE = EDINBURGH_POSTCODE_FIRST_PART + "3 7HF";
    private static final String UNKNOWN_POSTCODE = "BT9 6DJ";
    private static final String PETERBOROUGH_POSTCODE  = "PE11DP"; // Should return Watford
    private static final String SPALDING_POSTCODE = "PE111AE"; // Should return Midlands East
    private static final String EDINBURGH = "Edinburgh";
    private static final String MIDLANDS_EAST = "Midlands East";
    private static final String WATFORD = "Watford";
    @ParameterizedTest
    @MethodSource("TEST_CASES")
    void testGetEPIM(TribunalOffice managingOffice, String epim) {

        tribunalOfficesConfiguration = new TribunalOfficesConfiguration();
        System.out.println(tribunalOfficesConfiguration.toString());
        tribunalOfficesService = new TribunalOfficesService(tribunalOfficesConfiguration);

        String code = tribunalOfficesService.tribunalOfficeToEpimmsId(managingOffice);
        assertEquals(code, epim);
    }

    private static Stream<Arguments> TEST_CASES() {
        return Stream.of(
                Arguments.of(TribunalOffice.MANCHESTER, "M3 2JA")
        );
    }

//    @Test
//    void shouldReturnCorrectOfficeWhenPostcodeIsValid()  {
//
//        Map<String, String> mockData = Map.of(EDINBURGH_POSTCODE_FIRST_PART, EDINBURGH);
//        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);
//
//        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(EDINBURGH_POSTCODE);
//        assertThat(result).contains(TribunalOffice.EDINBURGH);
//    }
//
//    @Test
//    void shouldReturnUnknownOfficeWhenPostcodeIsValidButNotKnown() {
//
//        Map<String, String> mockData = Collections.emptyMap();
//        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);
//
//        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(UNKNOWN_POSTCODE);
//        assertThat(result).isEmpty();
//    }
}

