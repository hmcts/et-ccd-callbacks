package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MultipleDataBuilder;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATING_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferDifferentCountryService.CASE_TRANSFERRED_POSITION_TYPE;

@ExtendWith(SpringExtension.class)
class MultipleTransferDifferentCountryServiceTest {

    @InjectMocks
    MultipleTransferDifferentCountryService multipleTransferDifferentCountryService;

    @Mock
    ExcelReadingService excelReadingService;

    @Mock
    CaseTransferEventService caseTransferEventService;

    @Captor
    ArgumentCaptor<CaseTransferEventParams> caseTransferEventParamsArgumentCaptor;

    @Test
    void testTransferMultiple() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String multipleReference = "110001";
        String excelFileBinaryUrl = "test-url";
        String officeCT = TribunalOffice.GLASGOW.getOfficeName();
        String reasonForCT = "test transfer";
        String caseId = "1234";
        String jurisdiction = "EMPLOYMENT";
        String userToken = "my-test-token";
        MultipleDetails multipleDetails = MultipleDataBuilder.builder()
                .withMultipleReference(multipleReference)
                .withManagingOffice(managingOffice)
                .withCaseImporterFile(excelFileBinaryUrl)
                .withCaseTransfer(officeCT, reasonForCT)
                .buildAsMultipleDetails(caseId, ENGLANDWALES_BULK_CASE_TYPE_ID, jurisdiction);

        List<String> ethosCaseReferences = List.of("110001/2022", "110002/2022", "110003/2022");
        SortedMap<String, Object> multipleObjects = createMultipleObjects(ethosCaseReferences);
        when(excelReadingService.readExcel(userToken, excelFileBinaryUrl, Collections.emptyList(),
                multipleDetails.getCaseData(), FilterExcelType.ALL)).thenReturn(multipleObjects);

        List<String> errors = multipleTransferDifferentCountryService.transferMultiple(multipleDetails, userToken);

        assertTrue(errors.isEmpty());
        assertEquals(UPDATING_STATE, multipleDetails.getCaseData().getState());
        assertEquals(managingOffice, multipleDetails.getCaseData().getManagingOffice());

        verify(caseTransferEventService, times(1)).transfer(caseTransferEventParamsArgumentCaptor.capture());

        CaseTransferEventParams actualParams = caseTransferEventParamsArgumentCaptor.getValue();
        assertEquals(userToken, actualParams.getUserToken());
        assertEquals(ENGLANDWALES_BULK_CASE_TYPE_ID, actualParams.getCaseTypeId());
        assertEquals(jurisdiction, actualParams.getJurisdiction());
        assertEquals(ethosCaseReferences, actualParams.getEthosCaseReferences());
        assertEquals(multipleReference, actualParams.getSourceEthosCaseReference());
        assertEquals(officeCT, actualParams.getNewManagingOffice());
        assertEquals(CASE_TRANSFERRED_POSITION_TYPE, actualParams.getPositionType());
        assertEquals(reasonForCT, actualParams.getReason());
        assertEquals(multipleReference, actualParams.getMultipleReference());
        assertTrue(actualParams.isConfirmationRequired());
        String expectedMultipleReferenceLink = "<a target=\"_blank\" href=\"null/cases/case-details/" + caseId + "\">"
                + multipleReference + "</a>";
        assertEquals(expectedMultipleReferenceLink, actualParams.getMultipleReferenceLink());
        assertFalse(actualParams.isTransferSameCountry());
    }

    @Test
    void testTransferMultipleNoCases() {
        String excelFileBinaryUrl = "test-url";
        String caseId = "1234";
        String jurisdiction = "EMPLOYMENT";
        String userToken = "my-test-token";
        MultipleDetails multipleDetails = MultipleDataBuilder.builder()
                .withCaseImporterFile(excelFileBinaryUrl)
                .buildAsMultipleDetails(caseId, ENGLANDWALES_BULK_CASE_TYPE_ID, jurisdiction);
        TreeMap<String, Object> multipleObjects = new TreeMap<>();
        when(excelReadingService.readExcel(userToken, excelFileBinaryUrl, Collections.emptyList(),
                multipleDetails.getCaseData(), FilterExcelType.ALL)).thenReturn(multipleObjects);

        List<String> errors = multipleTransferDifferentCountryService.transferMultiple(multipleDetails, userToken);
        assertEquals(1, errors.size());
        assertEquals("No cases in the multiple", errors.get(0));
        assertNotEquals(UPDATING_STATE, multipleDetails.getCaseData().getState());

        verify(caseTransferEventService, never()).transfer(any(CaseTransferEventParams.class));
    }

    @Test
    void testTransferMultipleTransferErrors() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String multipleReference = "110001";
        String excelFileBinaryUrl = "test-url";
        String officeCT = TribunalOffice.GLASGOW.getOfficeName();
        String reasonForCT = "test transfer";
        String caseId = "1234";
        String jurisdiction = "EMPLOYMENT";
        String userToken = "my-test-token";
        MultipleDetails multipleDetails = MultipleDataBuilder.builder()
                .withMultipleReference(multipleReference)
                .withManagingOffice(managingOffice)
                .withCaseImporterFile(excelFileBinaryUrl)
                .withCaseTransfer(officeCT, reasonForCT)
                .buildAsMultipleDetails(caseId, ENGLANDWALES_BULK_CASE_TYPE_ID, jurisdiction);

        List<String> ethosCaseReferences = List.of("110001/2022", "110002/2022", "110003/2022");
        SortedMap<String, Object> multipleObjects = createMultipleObjects(ethosCaseReferences);
        when(excelReadingService.readExcel(userToken, excelFileBinaryUrl, Collections.emptyList(),
                multipleDetails.getCaseData(), FilterExcelType.ALL)).thenReturn(multipleObjects);

        List<String> transferErrors = List.of("Transfer Error 1", "Transfer Error2");
        when(caseTransferEventService.transfer(any(CaseTransferEventParams.class))).thenReturn(transferErrors);

        List<String> errors = multipleTransferDifferentCountryService.transferMultiple(multipleDetails, userToken);

        assertEquals(transferErrors, errors);

        assertEquals(UPDATING_STATE, multipleDetails.getCaseData().getState());
        assertEquals(managingOffice, multipleDetails.getCaseData().getManagingOffice());

        verify(caseTransferEventService, times(1)).transfer(caseTransferEventParamsArgumentCaptor.capture());

        CaseTransferEventParams actualParams = caseTransferEventParamsArgumentCaptor.getValue();
        assertEquals(userToken, actualParams.getUserToken());
        assertEquals(ENGLANDWALES_BULK_CASE_TYPE_ID, actualParams.getCaseTypeId());
        assertEquals(jurisdiction, actualParams.getJurisdiction());
        assertEquals(ethosCaseReferences, actualParams.getEthosCaseReferences());
        assertEquals(multipleReference, actualParams.getSourceEthosCaseReference());
        assertEquals(officeCT, actualParams.getNewManagingOffice());
        assertEquals(CASE_TRANSFERRED_POSITION_TYPE, actualParams.getPositionType());
        assertEquals(reasonForCT, actualParams.getReason());
        assertEquals(multipleReference, actualParams.getMultipleReference());
        assertTrue(actualParams.isConfirmationRequired());
        String expectedMultipleReferenceLink = "<a target=\"_blank\" href=\"null/cases/case-details/" + caseId + "\">"
                + multipleReference + "</a>";
        assertEquals(expectedMultipleReferenceLink, actualParams.getMultipleReferenceLink());
        assertFalse(actualParams.isTransferSameCountry());
    }

    private SortedMap<String, Object> createMultipleObjects(List<String> ethosCaseReferences) {
        TreeMap<String, Object> multipleObjects = new TreeMap<>();
        ethosCaseReferences.forEach(caseRef -> multipleObjects.put(caseRef, new Object()));
        return multipleObjects;
    }
}
