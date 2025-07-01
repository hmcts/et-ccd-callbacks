package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.utils.ResourceUtils.generateCaseDetails;

@ExtendWith(SpringExtension.class)
class CaseManagementLocationServiceTest {
    @InjectMocks
    private CaseManagementLocationService caseManagementLocationService;

    @MockitoBean
    private TribunalOfficesService tribunalOfficesService;

    @BeforeEach
    void setUp() {
        when(tribunalOfficesService.getTribunalOffice(any()))
                .thenReturn(TribunalOffice.valueOfOfficeName("Edinburgh"));
        when(tribunalOfficesService.getTribunalLocations(any())).thenReturn(getEdinburghCourtLocations());
        caseManagementLocationService = new CaseManagementLocationService(tribunalOfficesService);
    }

    private static CourtLocations getEdinburghCourtLocations() {
        CourtLocations edinburghLocation = new CourtLocations();
        edinburghLocation.setEpimmsId("301017");
        edinburghLocation.setRegion("North West");
        edinburghLocation.setRegionId("4");
        return edinburghLocation;
    }

    @ParameterizedTest
    @MethodSource("testSetCaseManagementLocationCode")
    void testSetCaseManagementLocationCode(String epimmsCode, String expected) {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Manchester");
        TribunalOffice office = TribunalOffice.valueOfOfficeName("Manchester");
        when(tribunalOfficesService.getEpimmsIdLocationCode(office)).thenReturn(epimmsCode);
        caseManagementLocationService.setCaseManagementLocationCode(caseData);
        assertEquals(expected, caseData.getCaseManagementLocationCode());
    }

    private static Stream<Arguments> testSetCaseManagementLocationCode() {
        return Stream.of(
                Arguments.of("123", "123"),
                Arguments.of("", ""),
                Arguments.of(null, "")
        );
    }

    @Test
    void testSetCaseManagementLocationCodeInvalidOffice() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("invalid");
        caseManagementLocationService.setCaseManagementLocationCode(caseData);
        assertEquals("", caseData.getCaseManagementLocationCode());
    }

    @Test
    void caseDataDefaultsCaseManagementLocation() throws Exception {
        CCDRequest scotlandCcdRequest1 = new CCDRequest();
        CaseDetails caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        scotlandCcdRequest1.setCaseDetails(caseDetailsScot1);

        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementLocationService.setCaseManagementLocation(caseData);
        assertEquals(CaseLocation.builder()
                        .baseLocation("301017")
                        .region("4")
                        .build(),
                caseData.getCaseManagementLocation());
    }

    @Test
    void caseDataDefaultsCaseManagementLocation_nullManagingOfficeName() throws Exception {
        CCDRequest scotlandCcdRequest1 = new CCDRequest();
        CaseDetails caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        scotlandCcdRequest1.setCaseDetails(caseDetailsScot1);

        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.setManagingOffice(null);
        caseManagementLocationService.setCaseManagementLocation(caseData);
        assertNull(caseData.getCaseManagementLocation());
    }

    @Test
    void caseDataDefaultsCaseManagementLocation_nullMTribunalOffice() throws Exception {
        CCDRequest scotlandCcdRequest1 = new CCDRequest();
        CaseDetails caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        scotlandCcdRequest1.setCaseDetails(caseDetailsScot1);
        CourtLocations blankCourtLocation = new CourtLocations();
        blankCourtLocation.setEpimmsId("");
        when(tribunalOfficesService.getTribunalLocations(any())).thenReturn(blankCourtLocation);

        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementLocationService.setCaseManagementLocation(caseData);
        assertNull(caseData.getCaseManagementLocation());
    }
}
