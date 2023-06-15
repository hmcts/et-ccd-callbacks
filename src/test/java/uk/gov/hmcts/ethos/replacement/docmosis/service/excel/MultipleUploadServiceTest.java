package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.TESTING_FILE_NAME_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.TESTING_FILE_NAME_WITH_TWO;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.TESTING_FILE_NAME_WRONG_COLUMN_ROW;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.getDataTypeSheet;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.setDocumentCollection;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUploadService.ERROR_SHEET_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUploadService.ERROR_SHEET_NUMBER_COLUMNS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUploadService.ERROR_SHEET_NUMBER_ROWS;

@ExtendWith(SpringExtension.class)
public class MultipleUploadServiceTest {

    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private MultipleBatchUpdate2Service multipleBatchUpdate2Service;
    @Mock
    private ExcelDocManagementService excelDocManagementService;
    @InjectMocks
    private MultipleUploadService multipleUploadService;

    private MultipleDetails multipleDetails;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
    }

    @Test
    public void bulkUploadLogic() throws IOException {

        List<String> errors = new ArrayList<>();

        when(excelReadingService.checkExcelErrors(
                userToken,
                MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                new ArrayList<>()))
                .thenReturn(getDataTypeSheet(TESTING_FILE_NAME_WITH_TWO));

        multipleUploadService.bulkUploadLogic(userToken,
                multipleDetails,
                errors);

        assertEquals(0, errors.size());
    }

    @Test
    public void bulkUploadLogicWrongColumnRow() throws IOException {

        List<String> errors = new ArrayList<>();

        when(excelReadingService.checkExcelErrors(
                userToken,
                MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                new ArrayList<>()))
                .thenReturn(getDataTypeSheet(TESTING_FILE_NAME_WRONG_COLUMN_ROW));

        multipleUploadService.bulkUploadLogic(userToken,
                multipleDetails,
                errors);

        assertEquals(ERROR_SHEET_NUMBER_ROWS + multipleDetails.getCaseData().getCaseIdCollection().size(),
                errors.get(0));
        assertEquals(ERROR_SHEET_NUMBER_COLUMNS + MultiplesHelper.HEADERS.size(),
                errors.get(1));
    }

    @Test
    public void bulkUploadLogicEmptySheet() throws IOException {
        List<String> errors = new ArrayList<>();

        setDocumentCollection(multipleDetails.getCaseData());
        when(excelReadingService.checkExcelErrors(
                userToken,
                MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                new ArrayList<>()))
                .thenReturn(getDataTypeSheet(TESTING_FILE_NAME_EMPTY));

        multipleUploadService.bulkUploadLogic(userToken,
                multipleDetails,
                errors);

        assertEquals(ERROR_SHEET_EMPTY, errors.get(0));
    }

    @Test
    public void bulkUploadLogicException() {
        assertThrows(Exception.class, () -> {
            when(excelReadingService.checkExcelErrors(
                    userToken,
                    MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                    new ArrayList<>()))
                    .thenThrow(new IOException());
            multipleUploadService.bulkUploadLogic(userToken,
                    multipleDetails,
                    new ArrayList<>());
            verify(excelReadingService, times(1)).checkExcelErrors(
                    userToken,
                    MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                    new ArrayList<>());
            verifyNoMoreInteractions(excelReadingService);

        });
    }

}