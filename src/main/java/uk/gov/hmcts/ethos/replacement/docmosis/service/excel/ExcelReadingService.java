package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.CONSTRAINT_KEY;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_2;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_3;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_4;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_5;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_6;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.SHEET_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper.SELECT_ALL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper.NOT_ALLOCATED;

@Slf4j
@Service("excelReadingService")
public class ExcelReadingService {

    private static final String ERROR_SHEET_NAME_NOT_FOUND = "Worksheet name not found";
    private static final String ERROR_DOCUMENT_NOT_VALID = "Document uploaded not valid";

    private final ExcelDocManagementService excelDocManagementService;
    private final CcdClient ccdClient;

    @Autowired
    public ExcelReadingService(ExcelDocManagementService excelDocManagementService, CcdClient ccdClient) {
        this.excelDocManagementService = excelDocManagementService;
        this.ccdClient = ccdClient;
    }

    public XSSFWorkbook readWorkbook(String userToken, String documentBinaryUrl) throws IOException {
        ZipSecureFile.setMinInflateRatio(0);
        InputStream excelInputStream =
                excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl);
        return new XSSFWorkbook(excelInputStream);
    }

    public SortedMap<String, Object> readExcel(String userToken, String documentBinaryUrl, List<String> errors,
                                               MultipleData multipleData, FilterExcelType filter) {
        SortedMap<String, Object> multipleObjects = new TreeMap<>();

        try {
            XSSFSheet datatypeSheet = checkExcelErrors(userToken, documentBinaryUrl, errors);
            if (errors.isEmpty()) {
                populateMultipleObjects(multipleObjects, datatypeSheet, multipleData, filter);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading the excel for multiple reference"
                    + multipleData.getMultipleReference(), e);
        }

        return multipleObjects;

    }

    /**
     * Given a case number and subMultiple name, case is found and its subMultiple property is populated.
     * @param userToken used for IDAM Authentication
     * @param multipleDetails multipleDetails to get caseTypeId, jurisdiction
     * @param ethosRef case number
     * @param subMultiple subMultiple name to be assigned to single case
     */
    public void setSubMultipleFieldInSingleCaseData(String userToken,
                                                    MultipleDetails multipleDetails,
                                                    String ethosRef,
                                                    String subMultiple) throws IOException {
        List<SubmitEvent> submitEvents = ccdClient.retrieveCasesElasticSearch(userToken,
                UtilHelper.getCaseTypeId(multipleDetails.getCaseTypeId()), List.of(ethosRef));
        submitEvents.getFirst().getCaseData().setSubMultipleName(Strings.isNullOrEmpty(subMultiple)
                ? " " : subMultiple);
        CCDRequest returnedRequest = ccdClient.startEventForCase(userToken,
                UtilHelper.getCaseTypeId(multipleDetails.getCaseTypeId()),
                multipleDetails.getJurisdiction(), String.valueOf(submitEvents.getFirst().getCaseId()));
        ccdClient.submitEventForCase(userToken,
                submitEvents.getFirst().getCaseData(),
                UtilHelper.getCaseTypeId(multipleDetails.getCaseTypeId()),
                multipleDetails.getJurisdiction(), returnedRequest,
                String.valueOf(submitEvents.getFirst().getCaseId()));
    }

    public XSSFSheet checkExcelErrors(String userToken, String documentBinaryUrl, List<String> errors)
            throws IOException {

        try (XSSFWorkbook workbook = readWorkbook(userToken, documentBinaryUrl)) {
            XSSFSheet datatypeSheet = workbook.getSheet(SHEET_NAME);
            if (datatypeSheet == null) {
                errors.add(ERROR_SHEET_NAME_NOT_FOUND);
            } else if (!datatypeSheet.validateSheetPassword(CONSTRAINT_KEY)) {
                errors.add(ERROR_DOCUMENT_NOT_VALID);
            }
            return datatypeSheet;
        }
    }

    private void setSubMultipleObjects(SortedMap<String, Object> multipleObjects, String ethosCaseRef,
                                       String subMultiple) {

        if (multipleObjects.containsKey(subMultiple)) {
            List<String> list = (List<String>) multipleObjects.get(subMultiple);
            list.add(ethosCaseRef);
            multipleObjects.put(subMultiple, list);

        } else {
            multipleObjects.put(subMultiple, new ArrayList<>(Collections.singletonList(ethosCaseRef)));
        }

    }

    private void setFlagObjects(SortedMap<String, Object> multipleObjects, String subMultiple,
                                String flag1, String flag2, String flag3, String flag4) {

        populateTreeMapWithSet(multipleObjects, HEADER_2, subMultiple);
        populateTreeMapWithSet(multipleObjects, HEADER_3, flag1);
        populateTreeMapWithSet(multipleObjects, HEADER_4, flag2);
        populateTreeMapWithSet(multipleObjects, HEADER_5, flag3);
        populateTreeMapWithSet(multipleObjects, HEADER_6, flag4);

    }

    private void populateTreeMapWithSet(SortedMap<String, Object> multipleObjects,
                                        String key,
                                        String value) {

        if (multipleObjects.containsKey(key)) {
            HashSet<String> set = (HashSet<String>) multipleObjects.get(key);
            set.add(value);
            multipleObjects.put(key, set);

        } else {
            multipleObjects.put(key, new HashSet<>(Collections.singletonList(value)));
        }

    }

    private void filterSubMultiple(Row currentRow, MultipleData multipleData,
                                   SortedMap<String, Object> multipleObjects) {
        if (isMultipleInFlagsAndBelongsSubMultiple(currentRow, multipleData)) {
            setSubMultipleObjects(multipleObjects,
                    getCellValue(currentRow.getCell(0)),
                    getCellValue(currentRow.getCell(1)));
        } else {
            if (isMultipleInFlags(currentRow, multipleData)) {
                setSubMultipleObjects(multipleObjects,
                        getCellValue(currentRow.getCell(0)),
                        NOT_ALLOCATED);
            }
        }
    }

    private void populateMultipleObjects(SortedMap<String, Object> multipleObjects,
                                         XSSFSheet datatypeSheet,
                                         MultipleData multipleData,
                                         FilterExcelType filter) {

        for (Row currentRow : datatypeSheet) {

            if (currentRow.getRowNum() == 0) {
                continue;
            }

            if (filter.equals(FilterExcelType.SUB_MULTIPLE)) {
                filterSubMultiple(currentRow, multipleData, multipleObjects);

            } else if (filter.equals(FilterExcelType.FLAGS)) {
                if (isMultipleInFlags(currentRow, multipleData)) {
                    multipleObjects.put(
                            getCellValue(currentRow.getCell(0)),
                            getCellValue(currentRow.getCell(0)));
                }

            } else if (filter.equals(FilterExcelType.DL_FLAGS)) {
                setFlagObjects(multipleObjects,
                        getCellValue(currentRow.getCell(1)),
                        getCellValue(currentRow.getCell(2)),
                        getCellValue(currentRow.getCell(3)),
                        getCellValue(currentRow.getCell(4)),
                        getCellValue(currentRow.getCell(5)));

            } else {
                multipleObjects.put(
                        getCellValue(currentRow.getCell(0)),
                        getMultipleObject(currentRow));

            }
        }
    }

    /**
     * Reads every data row of the first sheet in the uploaded spreadsheet and returns one
     * {@link AdditionalClaimant} per non-blank row.  The header row (row 0) is processed via
     * {@link #buildHeaderMap(Row)} so columns may appear in any order, matching the normalisation
     * logic used by the frontend validator.
     *
     * @param userToken         IDAM token used to download the document
     * @param documentBinaryUrl CCD document binary URL for the uploaded spreadsheet
     * @param errors            list to which any read errors are appended
     * @return list of {@link AdditionalClaimant} parsed from the spreadsheet data rows
     */
    public List<AdditionalClaimant> readClaimantsFromSpreadsheet(String userToken,
                                                                 String documentBinaryUrl,
                                                                 List<String> errors) {
        List<AdditionalClaimant> claimants = new ArrayList<>();
        try (XSSFWorkbook workbook = readWorkbook(userToken, documentBinaryUrl)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                errors.add("No worksheet found in additional claimant spreadsheet");
                return claimants;
            }
            Map<String, Integer> headerMap = null;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    headerMap = buildHeaderMap(row);
                    continue;
                }
                if (headerMap == null) {
                    break;
                }
                AdditionalClaimant claimant = rowToAdditionalClaimant(row, headerMap);
                if (claimant != null) {
                    claimants.add(claimant);
                }
            }
        } catch (IOException e) {
            errors.add("Error reading additional claimant spreadsheet: " + e.getMessage());
            log.error("Error reading additional claimant spreadsheet", e);
        }
        return claimants;
    }

    /**
     * Builds a map of normalised column-header strings to their zero-based column indices.
     * Normalisation trims, lowercases and removes all whitespace, matching the frontend
     * {@code buildHeaderMap} function.  Both English and Welsh header variants are recognised.
     *
     * @param headerRow the first row of the spreadsheet
     * @return map of field key (e.g. {@code "firstName"}) to column index
     */
    public Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String normalised = normaliseHeader(getCellValue(cell));
            int idx = cell.getColumnIndex();
            switch (normalised) {
                case "title", "teitl"                                              -> map.put("title", idx);
                case "firstname", "first_name", "enwcyntaf"                       -> map.put("firstName", idx);
                case "lastname", "last_name", "cyfenw"                            -> map.put("lastName", idx);
                case "dateofbirth", "dob", "dyddiadgeni"                          -> map.put("dob", idx);
                case "email", "e-mail", "e-bost", "ebost"                         -> map.put("email", idx);
                case "addressline1", "llinellcyfeiriad1"                          -> map.put("address1", idx);
                case "addressline2", "llinellcyfeiriad2"                          -> map.put("address2", idx);
                case "town", "city", "townorcity", "trefneuddinas", "dinas", "tref" -> map.put("town", idx);
                case "country", "gwlad"                                            -> map.put("country", idx);
                case "postcode", "codpost"                                         -> map.put("postcode", idx);
                default -> { /* unknown column - skip */ }
            }
        }
        return map;
    }

    private String normaliseHeader(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private AdditionalClaimant rowToAdditionalClaimant(Row row, Map<String, Integer> headerMap) {
        String firstName = getCellByIndex(row, headerMap.get("firstName"));
        String lastName  = getCellByIndex(row, headerMap.get("lastName"));
        // Skip entirely blank rows
        if (firstName.isBlank() && lastName.isBlank()) {
            return null;
        }

        Address address = new Address();
        address.setAddressLine1(getCellByIndex(row, headerMap.get("address1")));
        address.setAddressLine2(getCellByIndex(row, headerMap.get("address2")));
        address.setPostTown(getCellByIndex(row, headerMap.get("town")));
        address.setCountry(getCellByIndex(row, headerMap.get("country")));
        address.setPostCode(getCellByIndex(row, headerMap.get("postcode")));

        return AdditionalClaimant.builder()
                .title(getCellByIndex(row, headerMap.get("title")))
                .firstName(firstName)
                .lastName(lastName)
                .dob(getDateCellByIndex(row, headerMap.get("dob")))
                .email(getCellByIndex(row, headerMap.get("email")))
                .address(address)
                .build();
    }

    private String getCellByIndex(Row row, Integer index) {
        if (index == null) {
            return "";
        }
        Cell cell = row.getCell(index);
        return cell == null ? "" : getCellValue(cell);
    }

    /**
     * Reads a cell that may contain a date value and returns it in {@code YYYY-MM-DD} format.
     * Excel stores dates as formatted numeric cells; this method detects them via
     * {@link DateUtil#isCellDateFormatted(Cell)} and extracts the local date directly.
     * Plain string cells (e.g. {@code "01/01/1990"} or {@code "1990-01-01"}) are returned
     * as-is so that {@code CreateMultiplesService.convertDobToIso} can normalise them.
     */
    private String getDateCellByIndex(Row row, Integer index) {
        if (index == null) {
            return "";
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue()
                    .toLocalDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return getCellValue(cell);
    }

    public String getCellValue(Cell currentCell) {

        if (currentCell.getCellType() == CellType.STRING) {

            return currentCell.getStringCellValue();

        } else if (currentCell.getCellType() == CellType.NUMERIC) {

            return NumberToTextConverter.toText(currentCell.getNumericCellValue());

        } else {

            return "";

        }

    }

    private MultipleObject getMultipleObject(Row currentRow) {

        return MultipleObject.builder()
                .ethosCaseRef(getCellValue(currentRow.getCell(0)))
                .subMultiple(getCellValue(currentRow.getCell(1)))
                .flag1(getCellValue(currentRow.getCell(2)))
                .flag2(getCellValue(currentRow.getCell(3)))
                .flag3(getCellValue(currentRow.getCell(4)))
                .flag4(getCellValue(currentRow.getCell(5)))
                .build();
    }

    private boolean isMultipleInFlags(Row currentRow, MultipleData multipleData) {

        return isFilterPassed(currentRow.getCell(1), multipleData.getSubMultiple())
                && isFilterPassed(currentRow.getCell(2), multipleData.getFlag1())
                && isFilterPassed(currentRow.getCell(3), multipleData.getFlag2())
                && isFilterPassed(currentRow.getCell(4), multipleData.getFlag3())
                && isFilterPassed(currentRow.getCell(5), multipleData.getFlag4());
    }

    private boolean isMultipleInFlagsAndBelongsSubMultiple(Row currentRow, MultipleData multipleData) {

        return !getCellValue(currentRow.getCell(1)).isEmpty()
                && isFilterPassed(currentRow.getCell(2), multipleData.getFlag1())
                && isFilterPassed(currentRow.getCell(3), multipleData.getFlag2())
                && isFilterPassed(currentRow.getCell(4), multipleData.getFlag3())
                && isFilterPassed(currentRow.getCell(5), multipleData.getFlag4());
    }

    private boolean isFilterPassed(Cell cell, DynamicFixedListType flag) {

        if (flag != null) {

            return flag.getValue().getCode().equals(SELECT_ALL)
                    || getCellValue(cell).equals(flag.getValue().getCode());

        } else {

            return getCellValue(cell).isEmpty();
        }

    }

}
