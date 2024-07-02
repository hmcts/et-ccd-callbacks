package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BATCH_UPDATE_TYPE_2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BATCH_UPDATE_TYPE_3;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATING_STATE;

@ExtendWith(SpringExtension.class)
class MultipleUpdateServiceTest {

    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private MultipleBatchUpdate1Service multipleBatchUpdate1Service;
    @Mock
    private MultipleBatchUpdate2Service multipleBatchUpdate2Service;
    @Mock
    private MultipleBatchUpdate3Service multipleBatchUpdate3Service;

    @InjectMocks
    private MultipleUpdateService multipleUpdateService;

    private TreeMap<String, Object> multipleObjectsFlags;
    private MultipleDetails multipleDetails;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleObjectsFlags = MultipleUtil.getMultipleObjectsFlags();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
    }

    @Test
    void bulkUpdate1Logic() throws IOException {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        multipleUpdateService.bulkUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        assertEquals(UPDATING_STATE, multipleDetails.getCaseData().getState());
        verify(multipleBatchUpdate1Service, times(1)).batchUpdate1Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);
        verifyNoMoreInteractions(multipleBatchUpdate1Service);
    }

    @Test
    void bulkUpdate2Logic() throws IOException {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        multipleDetails.getCaseData().setBatchUpdateType(BATCH_UPDATE_TYPE_2);
        multipleUpdateService.bulkUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        assertEquals(UPDATING_STATE, multipleDetails.getCaseData().getState());
        verify(multipleBatchUpdate2Service, times(1)).batchUpdate2Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);
        verifyNoMoreInteractions(multipleBatchUpdate2Service);
    }

    @Test
    void bulkUpdate3Logic() throws IOException {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        multipleDetails.getCaseData().setBatchUpdateType(BATCH_UPDATE_TYPE_3);
        multipleUpdateService.bulkUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        assertEquals(UPDATING_STATE, multipleDetails.getCaseData().getState());
        verify(multipleBatchUpdate3Service, times(1)).batchUpdate3Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);
        verifyNoMoreInteractions(multipleBatchUpdate3Service);
    }

    @Test
    void bulkUpdateLogicEmptyAcceptedState() throws IOException {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(new TreeMap<>());
        multipleUpdateService.bulkUpdateLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        assertEquals(OPEN_STATE, multipleDetails.getCaseData().getState());
    }

}