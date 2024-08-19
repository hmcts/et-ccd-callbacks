package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ExcelCreationServiceTest {

    @InjectMocks
    private ExcelCreationService excelCreationService;

    @Mock
    SingleCasesReadingService singleCasesReadingService;

    String leadLink = "<a target=\"_blank\" href=\"https://www-ccd.perftest.platform.hmcts.net/v2/case/1604313560561842\">245000/2020</a>";

    private SortedMap<String, Object> multipleObjects;

    @BeforeEach
    public void setUp() {
        multipleObjects = MultipleUtil.getMultipleObjectsAll();
        when(singleCasesReadingService.retrieveSingleCase(any(), any(), any(), any()))
                .thenReturn(MultipleUtil.getSubmitEvents().get(0));
    }

    @Test
    void writeExcelObjects() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(multipleObjects.values()),
                new ArrayList<>(Arrays.asList("245000/1", "245000/1")),
                leadLink, "userToken", "caseTypeId"));
    }

    @Test
    void writeExcelObjectsEmptySubMultiples() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(multipleObjects.values()),
                new ArrayList<>(),
                leadLink, "userToken", "caseTypeId"));
    }

    @Test
    void writeExcelString() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(Arrays.asList("245000/2020", "245001/2020", "245002/2020")),
                new ArrayList<>(),
                leadLink, "userToken", "caseTypeId"));
    }

    @Test
    void writeExcelStringEmpty() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(),
                new ArrayList<>(),
                leadLink, "userToken", "caseTypeId"));
    }
}