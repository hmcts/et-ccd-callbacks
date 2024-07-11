package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SubMultipleReferenceService;

import java.util.ArrayList;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.AMEND_ACTION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.DELETE_ACTION;

@ExtendWith(SpringExtension.class)
class SubMultipleUpdateServiceTest {

    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private SubMultipleReferenceService subMultipleReferenceService;
    @Mock
    private ExcelDocManagementService excelDocManagementService;

    @InjectMocks
    private SubMultipleUpdateService subMultipleUpdateService;

    private SortedMap<String, Object> multipleObjectsAll;
    private MultipleDetails multipleDetails;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleObjectsAll = MultipleUtil.getMultipleObjectsAll();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
    }

    @Test
    void subMultipleUpdateLogicCreate() {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsAll);

        assertEquals(2, multipleDetails.getCaseData().getSubMultipleCollection().size());

        subMultipleUpdateService.subMultipleUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());

        assertEquals(3, multipleDetails.getCaseData().getSubMultipleCollection().size());
        assertEquals("NewSubMultiple",
                multipleDetails.getCaseData().getSubMultipleCollection().get(2).getValue().getSubMultipleName());

        verify(subMultipleReferenceService, times(1)).createReference(
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getMultipleReference(),
                1);
        verifyNoMoreInteractions(subMultipleReferenceService);
        verify(excelDocManagementService, times(1)).generateAndUploadExcel(
                anyList(),
                anyString(),
                any());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void subMultipleUpdateLogicCreateEmptySubMultiples() {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsAll);

        multipleDetails.getCaseData().setSubMultipleCollection(null);

        subMultipleUpdateService.subMultipleUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());

        assertEquals(1, multipleDetails.getCaseData().getSubMultipleCollection().size());
        assertEquals("NewSubMultiple",
                multipleDetails.getCaseData().getSubMultipleCollection().get(0).getValue().getSubMultipleName());

        verify(subMultipleReferenceService, times(1)).createReference(
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getMultipleReference(),
                1);
        verifyNoMoreInteractions(subMultipleReferenceService);
        verify(excelDocManagementService, times(1)).generateAndUploadExcel(
                anyList(),
                anyString(),
                any());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void subMultipleUpdateLogicAmend() {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsAll);

        multipleDetails.getCaseData().getSubMultipleAction().setActionType(AMEND_ACTION);

        assertEquals(2, multipleDetails.getCaseData().getSubMultipleCollection().size());
        assertEquals(multipleDetails.getCaseData().getSubMultipleAction().getAmendSubMultipleNameExisting(),
                multipleDetails.getCaseData().getSubMultipleCollection().get(0).getValue().getSubMultipleName());

        subMultipleUpdateService.subMultipleUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());

        assertEquals(2, multipleDetails.getCaseData().getSubMultipleCollection().size());
        assertEquals("SubMultipleAmended",
                multipleDetails.getCaseData().getSubMultipleCollection().get(0).getValue().getSubMultipleName());

        verify(excelDocManagementService, times(1)).generateAndUploadExcel(
                anyList(),
                anyString(),
                any());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void subMultipleUpdateLogicDelete() {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsAll);

        multipleDetails.getCaseData().getSubMultipleAction().setActionType(DELETE_ACTION);

        assertEquals(2, multipleDetails.getCaseData().getSubMultipleCollection().size());

        subMultipleUpdateService.subMultipleUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());

        assertEquals(1, multipleDetails.getCaseData().getSubMultipleCollection().size());

        verify(excelDocManagementService, times(1)).generateAndUploadExcel(
                anyList(),
                anyString(),
                any());
        verifyNoMoreInteractions(excelDocManagementService);
    }

}