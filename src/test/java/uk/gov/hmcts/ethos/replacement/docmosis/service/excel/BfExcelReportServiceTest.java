package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.BFDateType;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.bfaction.BfActionReportData;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BfExcelReportServiceTest {
    @Mock
    BfExcelReportService service;
    BfActionReportData reportData;

    @BeforeEach
    void setUp() {
        reportData = new BfActionReportData();
        reportData.setReportPeriodDescription("Period description");
        reportData.setReportPrintedOnDescription("Printed On Description");
        reportData.setOffice("Office");
        reportData.setReportDate("2022-01-01");
        BFDateTypeItem bfDateTypeItem = new BFDateTypeItem();
        BFDateType bfDateType = new BFDateType();
        bfDateTypeItem.setId(UUID.randomUUID().toString());
        bfDateType.setBroughtForwardDateReason("reason");
        bfDateType.setBroughtForwardAction("action");
        bfDateType.setBroughtForwardDate("202-01-03");
        bfDateType.setCaseReference("111000/2023");
        bfDateType.setBroughtForwardDateCleared("cleared date");
        bfDateType.setBroughtForwardEnteredDate("entered date");
        bfDateTypeItem.setValue(bfDateType);
        reportData.setBfDateCollection(List.of(bfDateTypeItem));

        service = new BfExcelReportService();
    }

    @Test
    void shouldReturnReportExcelFileDocumentInfo() {
        assertNotNull(service.getReportExcelFile(reportData));
    }

    @Test
    void shouldReturnReportExcelFileEmptyByteArray() {
        assertNotNull(service.getReportExcelFile(
                new BfActionReportData()));
    }
}
