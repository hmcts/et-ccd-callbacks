package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReportData;

@RequiredArgsConstructor
@Slf4j
@Service
public class ClaimsByHearingVenueExcelReportDocumentInfoService {
    private final ClaimsByHearingVenueExcelReportCreationService excelReportCreationService;
    private final ExcelDocManagementService excelDocManagementService;
    private static final String CLAIMS_BY_HEARING_VENUE_FILE_NAME = "_Hearings_By_Venue_Report.xlsx";

    public DocumentInfo generateExcelReportDocumentInfo(ClaimsByHearingVenueReportData reportData, String caseTypeId,
                                                    String userToken) {
        byte[] excelBytes = excelReportCreationService.getReportExcelFile(reportData);
        String outPutFileName = caseTypeId + CLAIMS_BY_HEARING_VENUE_FILE_NAME;
        return excelDocManagementService.uploadExcelReportDocument(userToken, outPutFileName, excelBytes);
    }
}
