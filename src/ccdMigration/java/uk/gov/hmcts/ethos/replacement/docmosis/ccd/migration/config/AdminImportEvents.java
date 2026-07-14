package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.VenueImport;

import static uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole.EMPLOYMENT_API;

@Component
public class AdminImportEvents implements CCDConfig<AdminData, AdminState, AdminRole> {

    @Override
    public String groupingKey() {
        return AdminDefinition.CASE_TYPE;
    }

    @Override
    public void configure(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        configureStaffImport(builder);
        configureVenueImport(builder);
        configurePreHearingDepositImport(builder);
    }

    private void configureStaffImport(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.event("importStaffData")
            .forAllStates()
            .postStateWildcard()
            .name("Import Staff Data")
            .description("Import staff data from Excel file")
            .displayOrder(2)
            .endButtonLabel("Import")
            .omitShowSummary()
            .showEventNotes()
            .omitPublish()
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/admin/staff/import")
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageDisplayOrder()
            .omitPageColumnNumber()
            .complex(AdminData::getStaffImportFile, false, "Import Staff Data")
            .eventToComplexTypeId("importStaffDataFile")
            .mandatoryWithLabel(ImportFile::getFile, "File")
            .done()
            .done();
    }

    private void configureVenueImport(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.event("importVenueData")
            .forAllStates()
            .postStateWildcard()
            .name("Import Venue Data")
            .description("Import venue data from ECM CCD config file")
            .displayOrder(3)
            .endButtonLabel("Import")
            .omitShowSummary()
            .showEventNotes()
            .omitPublish()
            .externalCallbackUrl(Webhook.AboutToStart, "${ET_COS_URL}/admin/venue/initImport")
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/admin/venue/import")
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageDisplayOrder()
            .omitPageColumnNumber()
            .complex(AdminData::getVenueImport, false, "Import Venue Data")
            .eventToComplexTypeId("importVenueDataFile")
            .complex(VenueImport::getVenueImportFile, false)
            .mandatoryWithLabel(ImportFile::getFile, "File")
            .done()
            .mandatoryWithLabel(VenueImport::getVenueImportOffice, "Tribunal Office")
            .done()
            .done();
    }

    private void configurePreHearingDepositImport(
        ConfigBuilder<AdminData, AdminState, AdminRole> builder
    ) {
        builder.event("importPHRDepositsFromExcelFile")
            .forAllStates()
            .postStateWildcard()
            .name("Import PHR Deposits")
            .displayOrder(13)
            .endButtonLabel("Import PHR Deposits")
            .showSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/preHearingDeposit/importPHRDeposits"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageColumnNumber()
            .complex(AdminData::getPreHearingDepositImportFile, false)
            .eventToComplexTypeId("importPreHearingDepositData")
            .mandatoryWithLabel(ImportFile::getFile, "File")
            .done()
            .done();
    }
}
