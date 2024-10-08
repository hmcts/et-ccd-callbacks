package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper.SELECT_ALL;

@ExtendWith(SpringExtension.class)
class MultipleDynamicListFlagsServiceTest {

    @Mock
    private ExcelReadingService excelReadingService;
    @InjectMocks
    private MultipleDynamicListFlagsService multipleDynamicListFlagsService;

    private MultipleDetails multipleDetails;
    private SortedMap<String, Object> multipleObjectsDLFlags;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleObjectsDLFlags = MultipleUtil.getMultipleObjectsDLFlags();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
    }

    @Test
    void populateDynamicListFlagsLogic() {

        List<String> errors = new ArrayList<>();

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsDLFlags);

        multipleDynamicListFlagsService.populateDynamicListFlagsLogic(userToken,
                multipleDetails,
                errors);

        assertEquals(2, multipleDetails.getCaseData().getFlag1().getListItems().size());
        assertEquals("AA", multipleDetails.getCaseData().getFlag1().getListItems().get(1).getCode());
        assertEquals(SELECT_ALL, multipleDetails.getCaseData().getFlag1().getValue().getCode());
        assertEquals(3, multipleDetails.getCaseData().getFlag2().getListItems().size());
        assertEquals("BB", multipleDetails.getCaseData().getFlag2().getListItems().get(1).getCode());
        assertEquals(SELECT_ALL, multipleDetails.getCaseData().getFlag3().getValue().getCode());
        assertEquals(SELECT_ALL, multipleDetails.getCaseData().getFlag4().getListItems().get(0).getCode());
    }

}