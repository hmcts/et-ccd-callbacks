package uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.OfficeSheet;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.SimpleSheetHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

@Service
public class PreHearingDepositService {
    private final UserService userService;
    private final ExcelReadingService excelReadingService;

    public PreHearingDepositService(UserService userService,
                                    ExcelReadingService excelReadingService) {
        this.userService = userService;
        this.excelReadingService = excelReadingService;
    }

    @Transactional
    public void importData(PreHearingDepositData data, String userToken) throws IOException {
        String documentUrl = data.getPreHearingDepositImportFile().getFile().getBinaryUrl();
        UserDetails user = userService.getUserDetails(userToken);
        try (XSSFWorkbook workbook = excelReadingService.readWorkbook(userToken, documentUrl)) {
            setPreHearingDepositData(workbook, user);
        }
    }

    private void setPreHearingDepositData(XSSFWorkbook workbook, UserDetails user) {
        SimpleSheetHandler sheetHandler = new SimpleSheetHandler();
        Iterator<OfficeSheet> iterator = sheetHandler.sheets(workbook);

        while (iterator.hasNext()) {
            OfficeSheet officeSheet = iterator.next();
            for (Row row : officeSheet.getSheet()) {
                PreHearingDepositData preHearingDepositData = new PreHearingDepositData();
                preHearingDepositData.getPreHearingDepositImportFile().setUser(user.getName());
                preHearingDepositData.getPreHearingDepositImportFile().setLastImported(LocalDateTime.now().toString());
                preHearingDepositData.setCaseNumber(row.getCell(1).getStringCellValue());
                preHearingDepositData.setClaimantOrRespondentName(row.getCell(2).getStringCellValue());
                preHearingDepositData.setDepositDue(row.getCell(3).getStringCellValue());
                preHearingDepositData.setDateDepositReceived(row.getCell(4).getStringCellValue());
                preHearingDepositData.setDepositAmount(row.getCell(5).getStringCellValue());
                preHearingDepositData.setAmountRefunded(row.getCell(6).getStringCellValue());
                preHearingDepositData.setDepositRefundDate(row.getCell(7).getStringCellValue());
                preHearingDepositData.setChequeOrPONumber(row.getCell(8).getStringCellValue());
                preHearingDepositData.setReceivedBy(row.getCell(9).getStringCellValue());
                preHearingDepositData.setDepositReceivedFrom(row.getCell(10).getStringCellValue());
                preHearingDepositData.setDepositComments(row.getCell(11).getStringCellValue());
                preHearingDepositData.setPHRNumber(row.getCell(12).getStringCellValue());
                preHearingDepositData.setMr1Reference(row.getCell(13).getStringCellValue());
                preHearingDepositData.setBankingDate(row.getCell(14).getStringCellValue());
                preHearingDepositData.setJournalConfirmedReceipt(row.getCell(15).getStringCellValue());
                preHearingDepositData.setComments(row.getCell(16).getStringCellValue());
                preHearingDepositData.setStatus(row.getCell(17).getStringCellValue());
                preHearingDepositData.setDateSentForRefund(row.getCell(18).getStringCellValue());
                preHearingDepositData.setDepositAmount(row.getCell(19).getStringCellValue());
                preHearingDepositData.setPayeeName(row.getCell(20).getStringCellValue());
                preHearingDepositData.setRefundReference(row.getCell(21).getStringCellValue());
                preHearingDepositData.setJournalConfirmedPaid(row.getCell(22).getStringCellValue());
                preHearingDepositData.setRegionOffice(row.getCell(26).getStringCellValue());
            }
        }
    }
}
