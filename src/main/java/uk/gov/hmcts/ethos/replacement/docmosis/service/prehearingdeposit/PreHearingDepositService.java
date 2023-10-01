package uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            setPreHearingDepositData(workbook, user, data);
        }
    }

    private void setPreHearingDepositData(XSSFWorkbook workbook, UserDetails user, PreHearingDepositData data) {
        XSSFSheet officeSheet = workbook.getSheetAt(0);
        data.getPreHearingDepositImportFile().setUser(user.getName());
        data.getPreHearingDepositImportFile().setLastImported(LocalDateTime.now().toString());
        List<PreHearingDepositTypeItem> preHearingDepositTypeItems = new ArrayList<>();
        for (Row row : officeSheet) {
            if (row.getRowNum() > 0) {
                PreHearingDepositTypeItem preHearingDepositTypeItem = new PreHearingDepositTypeItem();
                preHearingDepositTypeItem.setId(UUID.randomUUID().toString());
                PreHearingDepositType preHearingDepositType = new PreHearingDepositType();
                preHearingDepositType.setCaseNumber(row.getCell(0).getStringCellValue());
                preHearingDepositType.setClaimantOrRespondentName(row.getCell(1).getStringCellValue());
                preHearingDepositType.setDepositDue(row.getCell(2).getDateCellValue().toString());
                preHearingDepositType.setDateDepositReceived(row.getCell(3).getDateCellValue().toString());
                preHearingDepositType.setDepositAmount(String.valueOf(row.getCell(4).getNumericCellValue()));
                preHearingDepositType.setAmountRefunded(row.getCell(5).getStringCellValue());
                preHearingDepositType.setDepositRefundDate(row.getCell(6).getDateCellValue().toString());
                preHearingDepositType.setChequeOrPONumber(excelReadingService.getCellValue(row.getCell(7)));
                preHearingDepositType.setReceivedBy(row.getCell(8).getStringCellValue());
                preHearingDepositType.setDepositReceivedFrom(row.getCell(9).getStringCellValue());
                preHearingDepositType.setDepositComments(row.getCell(10).getStringCellValue());
                preHearingDepositType.setPhrNumber(String.valueOf(row.getCell(11).getNumericCellValue()));
                preHearingDepositType.setMr1Reference(row.getCell(12).getStringCellValue());
                if (row.getCell(13).getDateCellValue() != null) {
                    preHearingDepositType.setBankingDate(row.getCell(13).getDateCellValue().toString());
                }
                if (row.getCell(14).getDateCellValue() != null) {
                    preHearingDepositType.setJournalConfirmedReceipt(row.getCell(14).getDateCellValue().toString());
                }
                preHearingDepositType.setComments(row.getCell(15).getStringCellValue());
                preHearingDepositType.setStatus(row.getCell(16).getStringCellValue());
                if (row.getCell(17).getDateCellValue() != null) {
                    preHearingDepositType.setDateSentForRefund(row.getCell(17).getDateCellValue().toString());
                }
                preHearingDepositType.setDepositAmount(String.valueOf(row.getCell(18).getNumericCellValue()));
                preHearingDepositType.setPayeeName(row.getCell(19).getStringCellValue());
                preHearingDepositType.setRefundReference(row.getCell(20).getStringCellValue());
                preHearingDepositType.setJournalConfirmedPaid(row.getCell(21).getDateCellValue().toString());
                if (!row.getCell(25).getCellType().equals(CellType.ERROR)) {
                    preHearingDepositType.setRegionOffice(row.getCell(25).getStringCellValue());
                }
                preHearingDepositTypeItem.setValue(preHearingDepositType);
                preHearingDepositTypeItems.add(preHearingDepositTypeItem);
            }
        }
        if (CollectionUtils.isNotEmpty(preHearingDepositTypeItems)) {
            data.setPreHearingDepositDataCollection(preHearingDepositTypeItems);
        }
    }
}
