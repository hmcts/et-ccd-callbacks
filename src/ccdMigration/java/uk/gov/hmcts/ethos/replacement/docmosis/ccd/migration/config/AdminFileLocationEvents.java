package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminState;

import static uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole.EMPLOYMENT_API;

@Component
public class AdminFileLocationEvents implements CCDConfig<AdminData, AdminState, AdminRole> {

    @Override
    public String groupingKey() {
        return AdminDefinition.CASE_TYPE;
    }

    @Override
    public void configure(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        configureAddFileLocation(builder);
        configureUpdateFileLocation(builder);
        configureDeleteFileLocation(builder);
    }

    private void configureAddFileLocation(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.event("addFileLocation")
            .forAllStates()
            .postStateWildcard()
            .name("Add File Location")
            .displayOrder(10)
            .endButtonLabel("Add File Location")
            .omitShowSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToStart,
                "${ET_COS_URL}/admin/filelocation/initAdminData"
            )
            .externalCallbackUrl(
                Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/filelocation/addFileLocation"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .mandatory(AdminData::getTribunalOffice)
            .mandatory(AdminData::getFileLocationCode)
            .mandatory(AdminData::getFileLocationName)
            .done();
    }

    private void configureUpdateFileLocation(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        var fields = builder.event("updateFileLocation")
            .forAllStates()
            .postStateWildcard()
            .name("Update File Location")
            .displayOrder(11)
            .endButtonLabel("Update File Location")
            .omitShowSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToStart,
                "${ET_COS_URL}/admin/filelocation/initAdminData"
            )
            .externalCallbackUrl(
                Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/filelocation/updateFileLocation"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageColumnNumber()
            .page("1")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/filelocation/midEventSelectTribunalOffice"
            )
            .mandatoryNoSummary(AdminData::getTribunalOffice)
            .page("2")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/filelocation/midEventSelectFileLocation"
            )
            .mandatoryNoSummary(AdminData::getFileLocationList)
            .page("3")
            .readonlyNoSummary(AdminData::getFileLocationCode)
            .mandatoryNoSummary(AdminData::getFileLocationName);
        fields.done();
    }

    private void configureDeleteFileLocation(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        var fields = builder.event("deleteFileLocation")
            .forAllStates()
            .postStateWildcard()
            .name("Delete File Location")
            .displayOrder(12)
            .endButtonLabel("Delete File Location")
            .showSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToStart,
                "${ET_COS_URL}/admin/filelocation/initAdminData"
            )
            .externalCallbackUrl(
                Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/filelocation/deleteFileLocation"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageColumnNumber()
            .page("1")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/filelocation/midEventSelectTribunalOffice"
            )
            .mandatoryNoSummary(AdminData::getTribunalOffice)
            .page("2")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/filelocation/midEventSelectFileLocation"
            )
            .mandatoryNoSummary(AdminData::getFileLocationList)
            .page("3")
            .readonlyNoSummary(AdminData::getFileLocationCode)
            .readonlyNoSummary(AdminData::getFileLocationName);
        fields.done();
    }
}
