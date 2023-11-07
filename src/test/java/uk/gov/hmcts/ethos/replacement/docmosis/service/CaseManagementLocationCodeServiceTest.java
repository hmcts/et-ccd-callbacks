package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

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
        caseManagementLocationCodeService = new CaseManagementLocationCodeService(
                tribunalOfficesService);
    }

    @Test
    void testSetCaseManagementLocationCode() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Manchester");
        TribunalOffice office = TribunalOffice.valueOfOfficeName("Manchester");
        when(tribunalOfficesService.getEpimmsIdLocationCode(office))
                .thenReturn("123");
        caseManagementLocationCodeService.setCaseManagementLocationCode(caseData);
        assertEquals("123", caseData.getCaseManagementLocationCode());
    }

    @Test
    void testSetCaseManagementLocationCodeInvalidOffice() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("invalid");
        caseManagementLocationCodeService.setCaseManagementLocationCode(caseData);
        assertEquals("", caseData.getCaseManagementLocationCode());
    }

    @Test
    void testSetCaseManagementLocationCodeWhenEpimmCodeIsNull() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Manchester");
        TribunalOffice office = TribunalOffice.valueOfOfficeName("Manchester");
        when(tribunalOfficesService.getEpimmsIdLocationCode(office))
                .thenReturn(null);
        caseManagementLocationCodeService.setCaseManagementLocationCode(caseData);
        assertEquals("", caseData.getCaseManagementLocationCode());
    }

    @Test
    void testSetCaseManagementLocationCodeWhenEpimmCodeIsEmpty() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Manchester");
        TribunalOffice office = TribunalOffice.valueOfOfficeName("Manchester");
        when(tribunalOfficesService.getEpimmsIdLocationCode(office))
                .thenReturn("");
        caseManagementLocationCodeService.setCaseManagementLocationCode(caseData);
        assertEquals("", caseData.getCaseManagementLocationCode());
    }
}