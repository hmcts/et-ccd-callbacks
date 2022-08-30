package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.venues;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.FileLocationFixedListSheetImporter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.FixedListSheetReader;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.FixedListSheetReaderException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.VenueFixedListSheetImporter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings({"PMD.LawOfDemeter"})
public class VenueImportService {

    private final ExcelReadingService excelReadingService;
    private final VenueFixedListSheetImporter venueFixedListSheetImporter;
    private final FileLocationFixedListSheetImporter fileLocationFixedListSheetImporter;
    private final UserService userService;

    public VenueImportService(ExcelReadingService excelReadingService,
                              VenueFixedListSheetImporter venueFixedListSheetImporter,
                              FileLocationFixedListSheetImporter fileLocationFixedListSheetImporter,
                              UserService userService) {
        this.excelReadingService = excelReadingService;
        this.venueFixedListSheetImporter = venueFixedListSheetImporter;
        this.fileLocationFixedListSheetImporter = fileLocationFixedListSheetImporter;
        this.userService = userService;
    }

    public void initImport(AdminData adminData) {
        if (adminData.getVenueImport() != null) {
            adminData.getVenueImport().setVenueImportFile(null);
            adminData.getVenueImport().setVenueImportOffice(null);
        }
    }

    @Transactional
    public void importVenues(AdminData adminData, String userToken) throws IOException, FixedListSheetReaderException {
        var workbook = getWorkbook(adminData, userToken);
        var tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getVenueImport().getVenueImportOffice());
        var sheetImporters = List.of(venueFixedListSheetImporter, fileLocationFixedListSheetImporter);
        var sheetReader = FixedListSheetReader.create(sheetImporters);

        var importOffices = new ArrayList<TribunalOffice>();
        if (TribunalOffice.SCOTLAND.equals(tribunalOffice)) {
            importOffices.addAll(TribunalOffice.SCOTLAND_OFFICES);
        } else {
            importOffices.add(tribunalOffice);
        }

        for (var office : importOffices) {
            sheetReader.handle(office, workbook);
        }
        workbook.close();

        var user = userService.getUserDetails(userToken);
        adminData.getVenueImport().getVenueImportFile().setUser(user.getName());
        adminData.getVenueImport().getVenueImportFile().setLastImported(LocalDateTime.now().toString());
    }

    private XSSFWorkbook getWorkbook(AdminData adminData, String userToken) throws IOException {
        var documentUrl = adminData.getVenueImport().getVenueImportFile().getFile().getBinaryUrl();
        return excelReadingService.readWorkbook(userToken, documentUrl);
    }
}
