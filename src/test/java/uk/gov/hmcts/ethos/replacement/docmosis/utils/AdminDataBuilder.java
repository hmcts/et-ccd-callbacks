package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.AdminCourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.VenueImport;

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

    public AdminDataBuilder withVenueImport(TribunalOffice tribunalOffice, String documentBinaryUrl) {
        if (adminData.getVenueImport() == null) {
            adminData.setVenueImport(new VenueImport());
            adminData.getVenueImport().setVenueImportFile(new ImportFile());
            adminData.getVenueImport().getVenueImportFile().setFile(new Document());
        }
        adminData.getVenueImport().getVenueImportFile().getFile().setBinaryUrl(documentBinaryUrl);
        adminData.getVenueImport().setVenueImportOffice(tribunalOffice.getOfficeName());

        return this;
    }

    public AdminDataBuilder withJudgeData(String judgeCode, String judgeName, String tribunalOffice, String employmentStatus) {
        if (adminData.getJudgeCode() == null) {
            adminData.setJudgeCode(judgeCode);
        }
        if (adminData.getJudgeName() == null) {
            adminData.setJudgeName(judgeName);
        }
        if (adminData.getTribunalOffice() == null) {
            adminData.setTribunalOffice(tribunalOffice);
        }
        if (adminData.getEmploymentStatus() == null) {
            adminData.setEmploymentStatus(employmentStatus);
        }
  
        return this;
    }

    public CCDRequest buildAsCCDRequest() {
        var ccdRequest = new CCDRequest();
        var caseDetails = new CaseDetails();
        caseDetails.setAdminData(adminData);
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }

    public AdminDataBuilder withEmployeeMember(String tribunalOffice, String employeeMemberCode, String employeeMemberName) {
        var employeeMember = new AdminCourtWorker();
        employeeMember.setTribunalOffice(tribunalOffice);
        employeeMember.setCourtWorkerCode(employeeMemberCode);
        employeeMember.setCourtWorkerName(employeeMemberName);

        adminData.setAdminCourtWorker(employeeMember);
        return this;
    }
}
