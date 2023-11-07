package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CaseManagementLocationCodeServiceTest {
    @InjectMocks
    private CaseManagementLocationCodeService caseManagementLocationCodeService;

    @MockBean
    private TribunalOfficesService tribunalOfficesService;

    @BeforeEach
    void setUp() {
        caseManagementLocationCodeService = new CaseManagementLocationCodeService(tribunalOfficesService);
    }

    @ParameterizedTest
    @MethodSource
    void testSetCaseManagementLocationCode(String epimmsCode, String expected) {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Manchester");
        TribunalOffice office = TribunalOffice.valueOfOfficeName("Manchester");
        when(tribunalOfficesService.getEpimmsIdLocationCode(office)).thenReturn(epimmsCode);
        caseManagementLocationCodeService.setCaseManagementLocationCode(caseData);
        assertEquals(expected, caseData.getCaseManagementLocationCode());
    }

    private static Stream<Arguments> testSetCaseManagementLocationCode() {
        return Stream.of(
                Arguments.of("123", "123"),
                Arguments.of("", ""),
                Arguments.of("", null)
        );
    }

    @Test
    void testSetCaseManagementLocationCodeInvalidOffice() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("invalid");
        caseManagementLocationCodeService.setCaseManagementLocationCode(caseData);
        assertEquals("", caseData.getCaseManagementLocationCode());
    }
}