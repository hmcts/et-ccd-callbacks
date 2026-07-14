package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.AdminCourtWorker;

import static uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole.EMPLOYMENT_API;

@Component
public class AdminStaffEvents implements CCDConfig<AdminData, AdminState, AdminRole> {

    @Override
    public String groupingKey() {
        return AdminDefinition.CASE_TYPE;
    }

    @Override
    public void configure(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        configureCourtWorkerEvents(builder);
        configureJudgeEvents(builder);
    }

    private void configureCourtWorkerEvents(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        configureAddCourtWorker(builder);
        configureUpdateCourtWorker(builder);
        configureDeleteCourtWorker(builder);
    }

    private void configureAddCourtWorker(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.event("addCourtWorker")
            .forAllStates()
            .postStateWildcard()
            .name("Add Court Worker")
            .displayOrder(4)
            .endButtonLabel("Add Court Worker")
            .omitShowSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToStart,
                "${ET_COS_URL}/admin/staff/initAddCourtWorker"
            )
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/admin/staff/addCourtWorker")
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageDisplayOrder()
            .omitPageColumnNumber()
            .complex(AdminData::getAdminCourtWorker, false, "Add Court Worker")
            .eventToComplexTypeId("addCourtWorker")
            .mandatoryWithLabel(AdminCourtWorker::getTribunalOffice, "Tribunal Office")
            .mandatoryWithLabel(AdminCourtWorker::getCourtWorkerType, "Court Worker Type")
            .mandatoryWithLabel(AdminCourtWorker::getCourtWorkerCode, "Court Worker Code")
            .mandatoryWithLabel(AdminCourtWorker::getCourtWorkerName, "Court Worker Name")
            .done()
            .done();
    }

    private void configureUpdateCourtWorker(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        var fields = builder.event("updateCourtWorker")
            .forAllStates()
            .postStateWildcard()
            .name("Update Court Worker")
            .displayOrder(5)
            .omitEndButtonLabel()
            .omitShowSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/staff/updateCourtWorker"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageDisplayOrder()
            .omitPageColumnNumber()
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/midEventCourtWorkerSelectOffice"
            )
            .mandatoryNoSummary(AdminData::getCourtWorkerOffice)
            .mandatoryNoSummary(AdminData::getCourtWorkerType)
            .page("2")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/midEventCourtWorkerSelectCourtWorker"
            )
            .mandatoryNoSummary(AdminData::getCourtWorkerSelectList)
            .page("3")
            .readonlyNoSummary(AdminData::getCourtWorkerCode)
            .mandatoryNoSummary(AdminData::getCourtWorkerName);
        fields.done();
    }

    private void configureDeleteCourtWorker(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        var fields = builder.event("deleteCourtWorker")
            .forAllStates()
            .postStateWildcard()
            .name("Delete Court Worker")
            .displayOrder(6)
            .endButtonLabel("Delete Court Worker")
            .showSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/staff/deleteCourtWorker"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageDisplayOrder()
            .omitPageColumnNumber()
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/midEventCourtWorkerSelectOffice"
            )
            .mandatoryNoSummary(AdminData::getCourtWorkerOffice)
            .mandatoryNoSummary(AdminData::getCourtWorkerType)
            .page("2")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/midEventCourtWorkerSelectCourtWorker"
            )
            .mandatoryNoSummary(AdminData::getCourtWorkerSelectList)
            .page("3")
            .readonlyNoSummary(AdminData::getCourtWorkerCode)
            .readonlyNoSummary(AdminData::getCourtWorkerName);
        fields.done();
    }

    private void configureJudgeEvents(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        configureAddJudge(builder);
        configureUpdateJudge(builder);
        configureDeleteJudge(builder);
    }

    private void configureAddJudge(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.event("addJudge")
            .forAllStates()
            .postStateWildcard()
            .name("Add Judge")
            .displayOrder(7)
            .omitEndButtonLabel()
            .omitShowSummary()
            .omitPublish()
            .externalCallbackUrl(Webhook.AboutToStart, "${ET_COS_URL}/admin/staff/initAddJudge")
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/admin/staff/addJudge")
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .mandatory(AdminData::getTribunalOffice)
            .mandatory(AdminData::getJudgeCode)
            .mandatory(AdminData::getJudgeName)
            .mandatory(AdminData::getEmploymentStatus)
            .done();
    }

    private void configureUpdateJudge(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        var fields = builder.event("updateJudge")
            .forAllStates()
            .postStateWildcard()
            .name("Update Judge")
            .displayOrder(8)
            .omitEndButtonLabel()
            .omitShowSummary()
            .omitPublish()
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/admin/staff/updateJudge")
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageColumnNumber()
            .page("1")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/updateJudgeMidEventSelectOffice"
            )
            .mandatoryNoSummary(AdminData::getTribunalOffice)
            .page("2")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/updateJudgeMidEventSelectJudge"
            )
            .mandatoryNoSummary(AdminData::getJudgeSelectList)
            .page("3")
            .readonlyNoSummary(AdminData::getJudgeCode)
            .mandatoryNoSummary(AdminData::getJudgeName)
            .mandatoryNoSummary(AdminData::getEmploymentStatus);
        fields.done();
    }

    private void configureDeleteJudge(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        var fields = builder.event("deleteJudge")
            .forAllStates()
            .postStateWildcard()
            .name("Delete Judge")
            .displayOrder(9)
            .endButtonLabel("Delete Judge")
            .showSummary()
            .omitPublish()
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/admin/staff/deleteJudge")
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageColumnNumber()
            .page("1")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/deleteJudgeMidEventSelectOffice"
            )
            .mandatory(AdminData::getTribunalOffice)
            .page("2")
            .externalMidEventCallbackUrl(
                "${ET_COS_URL}/admin/staff/deleteJudgeMidEventSelectJudge"
            )
            .mandatoryWithLabel(AdminData::getJudgeSelectList, "Judge Name");
        fields.done();
    }
}
