package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.labels.LabelPayloadEvent;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DynamicListHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO_CASES_SEARCHED;

@ExtendWith(SpringExtension.class)
class MultipleLetterServiceTest {

    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private TornadoService tornadoService;
    @Mock
    private EventValidationService eventValidationService;
    @InjectMocks
    private MultipleLetterService multipleLetterService;
    @Mock
    private MultipleDynamicListFlagsService multipleDynamicListFlagsService;

    private SortedMap<String, Object> multipleObjectsFlags;
    private MultipleDetails multipleDetails;
    private String userToken;
    private List<SubmitEvent> submitEvents;
    private List<LabelPayloadEvent> labelPayloadEvents;
    private List<String> errors;

    @BeforeEach
    public void setUp() {
        multipleObjectsFlags = MultipleUtil.getMultipleObjectsFlags();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        multipleDetails.setCaseTypeId("Leeds_Multiple");
        submitEvents = MultipleUtil.getSubmitEvents();
        labelPayloadEvents = MultipleUtil.getLabelPayloadEvents();
        userToken = "authString";
        errors = new ArrayList<>();
    }

    @Test
    void bulkLetterLogic() throws IOException {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));
        when(tornadoService.documentGeneration(anyString(), any(), anyString(), any(), any(), any()))
                .thenReturn(new DocumentInfo());
        multipleLetterService.bulkLetterLogic(userToken,
                multipleDetails,
                errors,
                false);
        verify(singleCasesReadingService, times(1)).retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource());
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkLetterLogicException() throws IOException {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));
        when(tornadoService.documentGeneration(anyString(), any(), anyString(), any(), any(), any()))
                .thenThrow(new IOException());

        assertThrows(Exception.class, () ->
                multipleLetterService.bulkLetterLogic(userToken,
                        multipleDetails,
                        errors,
                        false)
        );

        verify(singleCasesReadingService, times(1)).retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource());
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkLetterLogicWithoutCases() {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(new TreeMap<>());
        multipleLetterService.bulkLetterLogic(userToken,
                multipleDetails,
                errors,
                false);
        assertEquals(NO_CASES_SEARCHED, errors.get(0));
    }

    @Test
    void bulkLetterLogicForLabels() throws IOException {
        CorrespondenceType correspondenceType = new CorrespondenceType();
        correspondenceType.setTopLevelDocuments(ADDRESS_LABELS_TEMPLATE);
        multipleDetails.getCaseData().setCorrespondenceType(correspondenceType);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(labelPayloadEvents);
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));
        when(tornadoService.documentGeneration(anyString(), any(), anyString(), any(), any(), any()))
                .thenReturn(new DocumentInfo());
        multipleLetterService.bulkLetterLogic(userToken,
                multipleDetails,
                errors,
                false);
        verify(singleCasesReadingService, times(1)).retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verify(singleCasesReadingService, times(1)).retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource());
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkLetterLogicForLabelsScotland() throws IOException {
        CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(ADDRESS_LABELS_TEMPLATE);
        multipleDetails.getCaseData().setCorrespondenceScotType(correspondenceScotType);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(labelPayloadEvents);
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));
        when(tornadoService.documentGeneration(anyString(), any(), anyString(), any(), any(), any()))
                .thenReturn(new DocumentInfo());
        multipleLetterService.bulkLetterLogic(userToken,
                multipleDetails,
                errors,
                false);
        verify(singleCasesReadingService, times(1)).retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verify(singleCasesReadingService, times(1)).retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource());
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkLetterLogicForLabelsValidation() {
        CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(ADDRESS_LABELS_TEMPLATE);
        multipleDetails.getCaseData().setCorrespondenceScotType(correspondenceScotType);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(labelPayloadEvents);
        multipleLetterService.bulkLetterLogic(userToken,
                multipleDetails,
                errors,
                true);
        verify(singleCasesReadingService, times(1)).retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void dynamicMultipleLetters() {
        MultipleUtil.addHearingToCaseData(submitEvents.get(0).getCaseData());
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));
        multipleLetterService.dynamicMultipleLetters(userToken, multipleDetails, errors);
        verify(singleCasesReadingService, times(1)).retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleObjectsFlags.firstKey(),
                multipleDetails.getCaseData().getMultipleSource());
        assertEquals(1, multipleDetails.getCaseData().getCorrespondenceType().getDynamicHearingNumber()
                .getListItems().size());
        DynamicValueType hearingFromCase = DynamicListHelper.createDynamicHearingList(
                submitEvents.get(0).getCaseData()).get(0);
        assertEquals(hearingFromCase, multipleDetails.getCaseData().getCorrespondenceType().getDynamicHearingNumber()
                .getListItems().get(0));
    }

}
