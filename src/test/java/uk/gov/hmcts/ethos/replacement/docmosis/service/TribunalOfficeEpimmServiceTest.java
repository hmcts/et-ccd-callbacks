package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TribunalOfficeEpimmServiceTest {

    @InjectMocks
    private TribunalOfficesService tribunalOfficesService;
    @MockitoBean
    private PostcodeToOfficeService postcodeToOfficeService;

    @MockitoBean
    private TribunalOfficesConfiguration config;

    @Mock
    private Map<TribunalOffice, CourtLocations> mockMapping;

    @Mock
    private CourtLocations mockCourtLocation;
    private Map<TribunalOffice, CourtLocations> mapping;

    private TribunalOffice office;

    private static final String EPIMMSCODE = "123";

    @BeforeEach
    void setUp() {
        tribunalOfficesService = new TribunalOfficesService(config, postcodeToOfficeService);
        office = TribunalOffice.valueOfOfficeName("Manchester");

        CourtLocations courtLocation = new CourtLocations();
        courtLocation.setEpimmsId(EPIMMSCODE);

        mapping = new HashMap();
        mapping.put(office, courtLocation);
    }

    @Test
    void testGetEpimmsIdLocationCode() {
        when(config.getCourtLocations())
                .thenReturn(mapping);
        String actual = tribunalOfficesService.getEpimmsIdLocationCode(office);
        assertEquals(EPIMMSCODE, actual);
    }

    @Test
    void testGetEpimmCodeWhenConfigReturnsNull() {
        when(config.getCourtLocations()).thenReturn(mockMapping);
        when(mockMapping.get(office)).thenReturn(mockCourtLocation);
        when(mockCourtLocation.getEpimmsId()).thenReturn(null);
        String actual = tribunalOfficesService.getEpimmsIdLocationCode(office);
        String expected = "";
        assertEquals(expected, actual);
    }
}
