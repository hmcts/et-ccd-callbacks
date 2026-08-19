package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReportDetail;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ClaimsByHearingVenueExcelReportCreationServiceTest {
    @Mock
    ClaimsByHearingVenueExcelReportCreationService service;
    ClaimsByHearingVenueReportData reportData;

    @BeforeEach
    void setUp() {
        reportData = new ClaimsByHearingVenueReportData();
        ClaimsByHearingVenueReportDetail detailEntry = new ClaimsByHearingVenueReportDetail();
        detailEntry.setCaseReference("245000/2021");
        detailEntry.setRespondentET3Postcode("TE5 TE1");
        reportData.getReportDetails().add(detailEntry);
        service = new ClaimsByHearingVenueExcelReportCreationService();
    }

    @Test
    void shouldReturnReportExcelFileDocumentInfo() {
        assertNotNull(service.getReportExcelFile(reportData));
    }

    @Test
    void shouldReturnReportExcelFileEmptyByteArray() {
        assertNotNull(service.getReportExcelFile(
                new ClaimsByHearingVenueReportData()));
    }
}
