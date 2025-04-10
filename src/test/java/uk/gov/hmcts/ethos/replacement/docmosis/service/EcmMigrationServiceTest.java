package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class EcmMigrationServiceTest {

    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private AdminUserService adminUserService;
    private EcmMigrationService ecmMigrationService;
    private CaseDetails caseDetails;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        when(adminUserService.getAdminUserToken()).thenReturn("123");
        caseData = CaseDataBuilder.builder()
                .withManagingOffice(TribunalOffice.LEEDS.getOfficeName())
                .withEthosCaseReference("1800001/2025")
                .build();
        caseData.setFeeGroupReference("1111222233334444");
        caseData.setEcmFeeGroupReference("5555666677778888");
        caseData.setEcmCaseLink("<a href=\"/cases/case-details/5555666677778888\">1800001/2025</a>");
        caseData.setMigratedFromEcm(YES);
        caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1234567890123456");

        ecmMigrationService = new EcmMigrationService(adminUserService, ccdClient);
    }

    @Test
    void rollbackEcmMigration() throws IOException {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(SUBMITTED_STATE);
        when(ccdClient.retrieveCase("123", ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, "1234567890123456"))
                .thenReturn(submitEvent);

        CCDRequest ccdRequest = getEcmCcdRequest();
        when(ccdClient.startEventForEcmCase(
                "123",
                TribunalOffice.LEEDS.getOfficeName(),
                EMPLOYMENT,
                "5555666677778888",
                "rollbackMigrateCase"))
                .thenReturn(ccdRequest);
        when(ccdClient.submitEventForEcmCase(
                eq("123"),
                any(uk.gov.hmcts.ecm.common.model.ccd.CaseDetails.class),
                any(CCDRequest.class)))
                .thenReturn(new uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent());
        ecmMigrationService.rollbackEcmMigration(caseDetails);
        ArgumentCaptor<uk.gov.hmcts.ecm.common.model.ccd.CaseDetails> caseDetailsCaptor = ArgumentCaptor.forClass(
                uk.gov.hmcts.ecm.common.model.ccd.CaseDetails.class);
        verify(ccdClient, times(1)).retrieveCase("123", ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, "1234567890123456");
        verify(ccdClient, times(1)).startEventForEcmCase(
                "123",
                TribunalOffice.LEEDS.getOfficeName(),
                EMPLOYMENT,
                "5555666677778888",
                "rollbackMigrateCase");
        verify(ccdClient, times(1)).submitEventForEcmCase(
                eq("123"),
                caseDetailsCaptor.capture(),
                eq(ccdRequest));
        uk.gov.hmcts.ecm.common.model.ccd.CaseData ecmCaseData = caseDetailsCaptor.getValue().getCaseData();
        // Verify that the ecm case data has been updated
        assertNull(ecmCaseData.getReformCaseLink());
        assertNotNull(ecmCaseData.getStateAPI());

        // Verify that the reform case data has been updated
        assertNull(caseData.getEcmFeeGroupReference());
        assertNull(caseData.getEcmCaseLink());
        assertNull(caseData.getMigratedFromEcm());
    }

    private static CCDRequest getEcmCcdRequest() {
        var ecmCaseData = new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        ecmCaseData.setReformCaseLink("<a href=\"/cases/case-details/1234567890123456\">1800001/2025</a>");
        ecmCaseData.setEthosCaseReference("1800001/2025");
        ecmCaseData.setFeeGroupReference("1111222233334444");
        var ecmCaseDetails = new uk.gov.hmcts.ecm.common.model.ccd.CaseDetails();
        ecmCaseDetails.setCaseData(ecmCaseData);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(ecmCaseDetails);
        return ccdRequest;
    }

    @Test
    void shouldThrowErrorWhenCantFindEcmCaseId() {
        caseDetails.getCaseData().setEcmCaseLink("<a href=\"/cases/case-details/1234\">1800001/2025</a>");
        assertThrows(IllegalArgumentException.class, () -> ecmMigrationService.rollbackEcmMigration(caseDetails));
    }

    @Test
    void shouldThrowErrorWithNullEcmCaseId() {
        caseDetails.getCaseData().setEcmCaseLink(null);
        assertThrows(IllegalArgumentException.class, () -> ecmMigrationService.rollbackEcmMigration(caseDetails));
    }

    @Test
    void shouldThrowErrorWithEmptyManagingOffice() {
        caseDetails.getCaseData().setManagingOffice(null);
        assertThrows(IllegalArgumentException.class, () -> ecmMigrationService.rollbackEcmMigration(caseDetails));
    }
}