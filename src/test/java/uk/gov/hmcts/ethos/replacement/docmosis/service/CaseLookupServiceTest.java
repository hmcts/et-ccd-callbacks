package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE;

@ExtendWith(SpringExtension.class)
class CaseLookupServiceTest {
    private CaseLookupService caseLookupService;
    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() throws IOException {
        caseLookupService = new CaseLookupService(adminUserService, ccdClient);
        SubmitEvent returned = new SubmitEvent();

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdID("123");
        returned.setCaseData(caseData);

        when(adminUserService.getAdminUserToken()).thenReturn("123");
        when(ccdClient.retrieveCase(any(), any(), any(), any())).thenReturn(returned);
    }

    @Test
    void getLeadCaseFromMultipleAsAdmin_success() throws IOException {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID + MULTIPLE);
        MultipleData multipleData = new MultipleData();
        multipleData.setLeadCase("<a href=\"/cases/case-details/1706874768326533\">6000001/2024</a>");
        multipleDetails.setCaseData(multipleData);

        CaseData caseData = caseLookupService.getLeadCaseFromMultipleAsAdmin(multipleDetails);
        assertEquals("123", caseData.getCcdID());
    }

    @Test
    void getLeadCaseFromMultipleAsAdmin_malformedLeadCase() {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID + MULTIPLE);
        MultipleData multipleData = new MultipleData();
        multipleData.setLeadCase("malformed data");
        multipleDetails.setCaseData(multipleData);

        assertThrows(Exception.class, () -> caseLookupService.getLeadCaseFromMultipleAsAdmin(multipleDetails));
    }

    @Test
    void getCaseDataAsAdmin_success() throws IOException {
        CaseData caseData = caseLookupService.getCaseDataAsAdmin(ENGLANDWALES_CASE_TYPE_ID, "1234567812345678");
        assertEquals("123", caseData.getCcdID());
    }

    @Test
    void getCaseDataAsAdmin_noCase() throws IOException {
        when(ccdClient.retrieveCase(any(), any(), any(), any())).thenThrow(new IOException("Case not found"));

        assertThrows(IOException.class, () ->
                caseLookupService.getCaseDataAsAdmin(ENGLANDWALES_CASE_TYPE_ID, "1234567812345678")
        );
    }
}