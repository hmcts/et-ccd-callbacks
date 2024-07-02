package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADD_CASES_TO_MULTIPLE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LEAD_CASE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REMOVE_CASES_FROM_MULTIPLE_AMENDMENT;

@ExtendWith(SpringExtension.class)
class MultipleAmendServiceTest {

    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private ExcelDocManagementService excelDocManagementService;
    @Mock
    private MultipleAmendLeadCaseService multipleAmendLeadCaseService;
    @Mock
    private MultipleAmendCaseIdsService multipleAmendCaseIdsService;
    @InjectMocks
    private MultipleAmendService multipleAmendService;

    private SortedMap<String, Object> multipleObjects;
    private MultipleDetails multipleDetails;
    private String userToken;
    private List<String> errors;

    @BeforeEach
    public void setUp() {
        userToken = "authString";
        errors = new ArrayList<>();

        multipleObjects = MultipleUtil.getMultipleObjectsAll();

        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
    }

    @Test
    void bulkAmendMultipleLogic_AllTypes() {
        multipleDetails.getCaseData().setTypeOfAmendmentMSL(List.of(
                LEAD_CASE_AMENDMENT,
                ADD_CASES_TO_MULTIPLE_AMENDMENT,
                REMOVE_CASES_FROM_MULTIPLE_AMENDMENT));

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        when(multipleAmendLeadCaseService.bulkAmendLeadCaseLogic(anyString(), any(), anyList(), any()))
                .thenReturn(getMultipleObjectsListFromLeadService());
        when(multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(anyString(), any(), anyList(), any()))
                .thenReturn(getMultipleObjectsListFromAmendService());

        multipleAmendService.bulkAmendMultipleLogic(userToken, multipleDetails, errors);

        assertNull(multipleDetails.getCaseData().getAmendLeadCase());
        assertNull(multipleDetails.getCaseData().getCaseIdCollection());
        assertNull(multipleDetails.getCaseData().getTypeOfAmendmentMSL());
        assertNull(multipleDetails.getCaseData().getAltCaseIdCollection());

        verify(multipleAmendLeadCaseService, times(1))
                .bulkAmendLeadCaseLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, times(1))
                .bulkAmendCaseIdsLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, times(1))
                .bulkRemoveCaseIdsLogic(any(), any(), any(), any());
        verify(excelDocManagementService, times(1))
                .generateAndUploadExcel(getMultipleObjectsListFromAmendService(), userToken, multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkAmendMultipleLogic_WithErrors() {
        errors.add("Oh No!");
        multipleDetails.getCaseData().setTypeOfAmendmentMSL(List.of(LEAD_CASE_AMENDMENT));

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        when(multipleAmendLeadCaseService.bulkAmendLeadCaseLogic(anyString(), any(), anyList(), any()))
                .thenReturn(getMultipleObjectsListFromLeadService());

        multipleAmendService.bulkAmendMultipleLogic(userToken, multipleDetails, errors);

        assertNull(multipleDetails.getCaseData().getAmendLeadCase());
        assertNull(multipleDetails.getCaseData().getCaseIdCollection());
        assertNull(multipleDetails.getCaseData().getTypeOfAmendmentMSL());
        assertNull(multipleDetails.getCaseData().getAltCaseIdCollection());

        verify(multipleAmendLeadCaseService, times(1))
                .bulkAmendLeadCaseLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, never())
                .bulkAmendCaseIdsLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, never())
                .bulkRemoveCaseIdsLogic(any(), any(), any(), any());
        verify(excelDocManagementService, never())
                .generateAndUploadExcel(getMultipleObjectsListFromLeadService(), userToken, multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkAmendMultipleLogic_LeadCase() {
        multipleDetails.getCaseData().setTypeOfAmendmentMSL(List.of(LEAD_CASE_AMENDMENT));

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        when(multipleAmendLeadCaseService.bulkAmendLeadCaseLogic(anyString(), any(), anyList(), any()))
                .thenReturn(getMultipleObjectsListFromLeadService());

        multipleAmendService.bulkAmendMultipleLogic(userToken, multipleDetails, errors);

        assertNull(multipleDetails.getCaseData().getAmendLeadCase());
        assertNull(multipleDetails.getCaseData().getCaseIdCollection());
        assertNull(multipleDetails.getCaseData().getTypeOfAmendmentMSL());
        assertNull(multipleDetails.getCaseData().getAltCaseIdCollection());

        verify(multipleAmendLeadCaseService, times(1))
                .bulkAmendLeadCaseLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, never())
                .bulkAmendCaseIdsLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, never())
                .bulkRemoveCaseIdsLogic(any(), any(), any(), any());
        verify(excelDocManagementService, times(1))
                .generateAndUploadExcel(getMultipleObjectsListFromLeadService(), userToken, multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkAmendMultipleLogic_AddCases() {
        multipleDetails.getCaseData().setTypeOfAmendmentMSL(List.of(ADD_CASES_TO_MULTIPLE_AMENDMENT));

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        when(multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(anyString(), any(), anyList(), any()))
                .thenReturn(getMultipleObjectsListFromAmendService());

        multipleAmendService.bulkAmendMultipleLogic(userToken, multipleDetails, errors);

        assertNull(multipleDetails.getCaseData().getAmendLeadCase());
        assertNull(multipleDetails.getCaseData().getCaseIdCollection());
        assertNull(multipleDetails.getCaseData().getTypeOfAmendmentMSL());
        assertNull(multipleDetails.getCaseData().getAltCaseIdCollection());

        verify(multipleAmendLeadCaseService, never())
                .bulkAmendLeadCaseLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, times(1))
                .bulkAmendCaseIdsLogic(any(), any(), any(), any());
        verify(multipleAmendCaseIdsService, never())
                .bulkRemoveCaseIdsLogic(any(), any(), any(), any());
        verify(excelDocManagementService, times(1))
                .generateAndUploadExcel(getMultipleObjectsListFromAmendService(), userToken, multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    private List<Object> getMultipleObjectsListFromLeadService() {
        return new ArrayList<>(getMultipleObjectsList());
    }

    private List<MultipleObject> getMultipleObjectsListFromAmendService() {
        return new ArrayList<>(getMultipleObjectsList());
    }

    private List<MultipleObject> getMultipleObjectsList() {
        return Arrays.asList(
                MultipleObject.builder()
                        .subMultiple("245000")
                        .ethosCaseRef("245000/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("")
                        .ethosCaseRef("245001/2020")
                        .flag1("")
                        .flag2("")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("245003")
                        .ethosCaseRef("245003/2020")
                        .flag1("AA")
                        .flag2("EE")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("245002")
                        .ethosCaseRef("245004/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .ethosCaseRef("245005/2020")
                        .subMultiple("SubMultiple")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build());
    }
}