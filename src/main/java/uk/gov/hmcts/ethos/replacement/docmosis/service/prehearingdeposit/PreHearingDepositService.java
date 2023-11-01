package uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.*;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PreHearingDepositService {
    private final UserService userService;
    private final ExcelReadingService excelReadingService;
    private final CcdClient ccdClient;

    @Transactional
    public void importPreHearingDepositData(
            ImportFile preHearingDepositImportFile, String userToken) throws IOException {
        String documentUrl = preHearingDepositImportFile.getFile().getBinaryUrl();
        UserDetails user = userService.getUserDetails(userToken);
        preHearingDepositImportFile.setUser(user.getName());
        preHearingDepositImportFile.setLastImported(LocalDateTime.now().toString());
        try (XSSFWorkbook workbook = excelReadingService.readWorkbook(userToken, documentUrl)) {
            XSSFSheet officeSheet = workbook.getSheetAt(0);
            for (Row row : officeSheet) {
                if (row.getRowNum() > 0) {
                    PreHearingDepositData preHearingDepositData = new PreHearingDepositData();
                    preHearingDepositData.setPreHearingDepositImportFile(preHearingDepositImportFile);
                    setPreHearingDepositDataWithExcelRowValues(row, preHearingDepositData);
                    CaseDetails preHearingDepositCaseDetails = new CaseDetails();
                    CaseData caseData = new CaseData();
                    caseData.setPreHearingDepositData(preHearingDepositData);
                    preHearingDepositCaseDetails.setCaseData(caseData);
                    preHearingDepositCaseDetails.setCaseTypeId("Pre_Hearing_Deposit");
                    preHearingDepositCaseDetails.setJurisdiction("EMPLOYMENT");
                    CCDRequest request = ccdClient.startCaseCreation(userToken, preHearingDepositCaseDetails);
                    ccdClient.submitCaseCreation(userToken, preHearingDepositCaseDetails, request);
                }
            }
        }
    }

    private void setPreHearingDepositDataWithExcelRowValues(
            Row row, PreHearingDepositData preHearingDepositData) {
        preHearingDepositData.setCaseNumber(row.getCell(0).getStringCellValue());
        preHearingDepositData.setClaimantOrRespondentName(row.getCell(1).getStringCellValue());
        preHearingDepositData.setDepositDue(row.getCell(2).getDateCellValue().toString());
        preHearingDepositData.setDateDepositReceived(row.getCell(3).getDateCellValue().toString());
        preHearingDepositData.setDepositAmount(String.valueOf(row.getCell(4).getNumericCellValue()));
        preHearingDepositData.setAmountRefunded(row.getCell(5).getStringCellValue());
        preHearingDepositData.setDepositRefundDate(row.getCell(6).getDateCellValue().toString());
        preHearingDepositData.setChequeOrPONumber(excelReadingService.getCellValue(row.getCell(7)));
        preHearingDepositData.setReceivedBy(row.getCell(8).getStringCellValue());
        preHearingDepositData.setDepositReceivedFrom(row.getCell(9).getStringCellValue());
        preHearingDepositData.setDepositComments(row.getCell(10).getStringCellValue());
        preHearingDepositData.setPhrNumber(String.valueOf(row.getCell(11).getNumericCellValue()));
        preHearingDepositData.setMr1Reference(row.getCell(12).getStringCellValue());
        if (row.getCell(13).getDateCellValue() != null) {
            preHearingDepositData.setBankingDate(row.getCell(13).getDateCellValue().toString());
        }
        if (row.getCell(14).getDateCellValue() != null) {
            preHearingDepositData.setJournalConfirmedReceipt(row.getCell(14).getDateCellValue().toString());
        }
        preHearingDepositData.setComments(row.getCell(15).getStringCellValue());
        preHearingDepositData.setStatus(row.getCell(16).getStringCellValue());
        if (row.getCell(17).getDateCellValue() != null) {
            preHearingDepositData.setDateSentForRefund(row.getCell(17).getDateCellValue().toString());
        }
        preHearingDepositData.setDepositAmount(String.valueOf(row.getCell(18).getNumericCellValue()));
        preHearingDepositData.setPayeeName(row.getCell(19).getStringCellValue());
        preHearingDepositData.setRefundReference(row.getCell(20).getStringCellValue());
        preHearingDepositData.setJournalConfirmedPaid(row.getCell(21).getDateCellValue().toString());
        if (!row.getCell(25).getCellType().equals(CellType.ERROR)) {
            preHearingDepositData.setRegionOffice(row.getCell(25).getStringCellValue());
        }
    }
}
