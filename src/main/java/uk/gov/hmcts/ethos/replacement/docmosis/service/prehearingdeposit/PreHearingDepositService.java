package uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.service.UserService;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.GenericTypeCaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PreHearingDepositService {
    private final UserService userService;
    private final ExcelReadingService excelReadingService;
    private final CcdClient ccdClient;
    private static final String PRE_HEARING_CASE_CREATION_EVENT_DESCRIPTION =
            "Pre-Hearing Deposit is Crated By Excel File";
    private static final String PRE_HEARING_CASE_CREATION_EVENT_SUMMARY = "Pre-Hearing Deposit Bulk Case Creation";
    private static final String PRE_HEARING_CASE_TYPE_ID = "Pre_Hearing_Deposit";
    private static final String JURISDICTION_EMPLOYMENT = "EMPLOYMENT";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final int CURRENCY_FACTOR = 100;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final int FIRST_SHEET_NUMBER = 0;
    private static final int FIRST_ROW_NUMBER = 0;
    private static final int CASE_NUMBER_COLUMN_NO = 0;
    private static final int CLAIMANT_OR_RESPONDENT_COLUMN_NO = 1;
    private static final int DEPOSIT_DUE_DATE_COLUMN_NO_1 = 2;
    private static final int DEPOSIT_RECEIVED_DATE_COLUMN_NO = 3;
    private static final int DEPOSIT_AMOUNT_COLUMN_NO = 4;
    private static final int DEPOSIT_REFUNDED_COLUMN_NO = 5;
    private static final int DEPOSIT_REFUND_DATE_COLUMN_NO = 6;
    private static final int CHEQUE_OR_PO_NUMBER_COLUMN_NO = 7;
    private static final int RECEIVED_BY_COLUMN_NO = 8;
    private static final int DEPOSIT_RECEIVED_FROM_COLUMN_NO = 9;
    private static final int DEPOSIT_COMMENTS_COLUMN_NO = 10;
    private static final int PHR_NUMBER_COLUMN_NO = 11;
    private static final int MR_1_REFERENCE_COLUMN_NO = 12;
    private static final int BANKING_DATE_COLUMN_NO = 13;
    private static final int JOURNAL_CONFIRMED_RECEIPT_DATE_COLUMN_NO = 14;
    private static final int COMMENTS_COLUMN_NO = 15;
    private static final int STATUS_COLUMN_NO = 16;
    private static final int DATE_SENT_FOR_REFUND_COLUMN_NO = 17;
    private static final int AMOUNT_REFUNDED_COLUMN_NO = 18;
    private static final int PAYEE_NAME_COLUMN_NO = 19;
    private static final int REFUND_REFERENCE_COLUMN_NO = 20;
    private static final int JOURNAL_CONFIRMED_PAID_DATE_COLUMN_NO = 21;
    private static final int REGION_OFFICE_COLUMN_NO = 25;

    @Transactional
    public void importPreHearingDepositData(
            ImportFile preHearingDepositImportFile, String userToken) throws IOException {
        String documentUrl = preHearingDepositImportFile.getFile().getBinaryUrl();
        UserDetails user = userService.getUserDetails(userToken);
        preHearingDepositImportFile.setUser(user.getName());
        preHearingDepositImportFile.setLastImported(LocalDateTime.now().toString());
        try (XSSFWorkbook workbook = excelReadingService.readWorkbook(userToken, documentUrl)) {
            XSSFSheet officeSheet = workbook.getSheetAt(FIRST_SHEET_NUMBER);
            for (Row row : officeSheet) {
                if (row.getRowNum() > FIRST_ROW_NUMBER) {
                    GenericTypeCaseDetails<PreHearingDepositData> preHearingDepositCaseDetails =
                        generatePreHearingDepositCaseDetails(preHearingDepositImportFile, row);
                    CCDRequest request = ccdClient.startGenericTypeCaseCreation(
                            userToken, preHearingDepositCaseDetails);
                    ccdClient.submitGenericTypeCaseCreation(userToken, preHearingDepositCaseDetails, request,
                            PRE_HEARING_CASE_CREATION_EVENT_SUMMARY, PRE_HEARING_CASE_CREATION_EVENT_DESCRIPTION);
                }
            }
        }
    }

    private GenericTypeCaseDetails<PreHearingDepositData> generatePreHearingDepositCaseDetails(
            ImportFile importFile, Row row) {
        GenericTypeCaseDetails<PreHearingDepositData> preHearingDepositCaseDetails = new GenericTypeCaseDetails<>();
        preHearingDepositCaseDetails.setCaseTypeId(PRE_HEARING_CASE_TYPE_ID);
        preHearingDepositCaseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        PreHearingDepositData preHearingDepositData = new PreHearingDepositData();
        preHearingDepositData.setPreHearingDepositImportFile(importFile);
        preHearingDepositData.setCaseNumber(row.getCell(CASE_NUMBER_COLUMN_NO).getStringCellValue());
        preHearingDepositData.setClaimantOrRespondentName(
                row.getCell(CLAIMANT_OR_RESPONDENT_COLUMN_NO).getStringCellValue());
        setDateValue(preHearingDepositData, row.getCell(DEPOSIT_DUE_DATE_COLUMN_NO_1).getDateCellValue());
        setDateValue(preHearingDepositData, row.getCell(DEPOSIT_RECEIVED_DATE_COLUMN_NO).getDateCellValue());
        setCurrencyValue(preHearingDepositData, row.getCell(DEPOSIT_AMOUNT_COLUMN_NO).getNumericCellValue());
        setCurrencyValue(preHearingDepositData, row.getCell(AMOUNT_REFUNDED_COLUMN_NO).getNumericCellValue());
        setYesOrNoValue(preHearingDepositData, row.getCell(DEPOSIT_REFUNDED_COLUMN_NO).getStringCellValue());
        setDateValue(preHearingDepositData, row.getCell(DEPOSIT_REFUND_DATE_COLUMN_NO).getDateCellValue());
        preHearingDepositData.setChequeOrPONumber(excelReadingService.getCellValue(
                row.getCell(CHEQUE_OR_PO_NUMBER_COLUMN_NO)));
        preHearingDepositData.setReceivedBy(row.getCell(RECEIVED_BY_COLUMN_NO).getStringCellValue());
        preHearingDepositData.setDepositReceivedFrom(row.getCell(DEPOSIT_RECEIVED_FROM_COLUMN_NO).getStringCellValue());
        preHearingDepositData.setDepositComments(row.getCell(DEPOSIT_COMMENTS_COLUMN_NO).getStringCellValue());
        if (ObjectUtils.isNotEmpty(row.getCell(PHR_NUMBER_COLUMN_NO).getNumericCellValue())) {
            preHearingDepositData.setPhrNumber(
                    NumberToTextConverter.toText(row.getCell(PHR_NUMBER_COLUMN_NO).getNumericCellValue()));
        }

        preHearingDepositData.setMr1Reference(row.getCell(MR_1_REFERENCE_COLUMN_NO).getStringCellValue());
        setDateValue(preHearingDepositData, row.getCell(BANKING_DATE_COLUMN_NO).getDateCellValue());
        setDateValue(preHearingDepositData, row.getCell(JOURNAL_CONFIRMED_RECEIPT_DATE_COLUMN_NO).getDateCellValue());

        preHearingDepositData.setComments(row.getCell(COMMENTS_COLUMN_NO).getStringCellValue());
        preHearingDepositData.setStatus(row.getCell(STATUS_COLUMN_NO).getStringCellValue());
        setDateValue(preHearingDepositData, row.getCell(DATE_SENT_FOR_REFUND_COLUMN_NO).getDateCellValue());

        preHearingDepositData.setPayeeName(row.getCell(PAYEE_NAME_COLUMN_NO).getStringCellValue());
        preHearingDepositData.setRefundReference(row.getCell(REFUND_REFERENCE_COLUMN_NO).getStringCellValue());
        setDateValue(preHearingDepositData, row.getCell(JOURNAL_CONFIRMED_PAID_DATE_COLUMN_NO).getDateCellValue());
        if (ObjectUtils.isNotEmpty(row.getCell(REGION_OFFICE_COLUMN_NO))
                && !row.getCell(REGION_OFFICE_COLUMN_NO).getCellType().equals(CellType.ERROR)) {
            preHearingDepositData.setRegionOffice(row.getCell(REGION_OFFICE_COLUMN_NO).getStringCellValue());
        }
        preHearingDepositCaseDetails.setCaseData(preHearingDepositData);
        return preHearingDepositCaseDetails;
    }

    private void setDateValue(PreHearingDepositData preHearingDepositData, Date date) {
        if (ObjectUtils.isNotEmpty(date)) {
            preHearingDepositData.setDepositDue(dateFormatter.format(date));
        }
    }

    private void setCurrencyValue(PreHearingDepositData preHearingDepositData, double doubleValue) {
        if (ObjectUtils.isNotEmpty(doubleValue)) {
            preHearingDepositData.setDepositAmount(NumberToTextConverter.toText(doubleValue * CURRENCY_FACTOR));
        }
    }

    private void setYesOrNoValue(PreHearingDepositData preHearingDepositData, String yesOrNo) {
        if (YES.equalsIgnoreCase(yesOrNo)) {
            preHearingDepositData.setDepositRefund(YES);
        } else {
            preHearingDepositData.setDepositRefund(NO);
        }
    }
}
