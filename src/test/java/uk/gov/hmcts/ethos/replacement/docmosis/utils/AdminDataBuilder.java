package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;

public class AdminDataBuilder {

    private final AdminData adminData = new AdminData();

    public static AdminDataBuilder builder() {
        return new AdminDataBuilder();
    }

    public AdminDataBuilder withStaffImportFile(String documentBinaryUrl) {
        if (adminData.getStaffImportFile() == null) {
            adminData.setStaffImportFile(new ImportFile());
        }
        if (adminData.getStaffImportFile().getFile() == null) {
            adminData.getStaffImportFile().setFile(new Document());
        }
        adminData.getStaffImportFile().getFile().setBinaryUrl(documentBinaryUrl);

        return this;
    }

    public CCDRequest buildAsCCDRequest() {
        var ccdRequest = new CCDRequest();
        var caseDetails = new CaseDetails();
        caseDetails.setAdminData(adminData);
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
