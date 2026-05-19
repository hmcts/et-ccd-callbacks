package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

@Component
public class EnglandWalesSingleFieldEventsConfig extends SingleFieldEventsConfig<EnglandWalesCaseData> {

    public EnglandWalesSingleFieldEventsConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            52,
            "Add telephone note",
            2,
            1,
            42,
            46,
            "Judgment, Order, Notification",
            "View all judgments, orders and notifications"
        );
    }

    @Override
    public void configure(ConfigBuilder<EnglandWalesCaseData, EtState, EtUserRole> configBuilder) {
        super.configure(configBuilder);

        regionalCaseworkerEvent(
            configBuilder.event("assignCase").forStateTransition(EtState.SUBMITTED, EtState.SUBMITTED)
        )
            .name("Assign Case")
            .description("Assign case to a tribunal office")
            .displayOrder(7)
            .showCondition("managingOffice =\"Unassigned\"")
            .publishToCamunda()
            .aboutToStartCallbackUrl("${ET_COS_URL}/caseTransfer/initTransferToEnglandWales")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/caseTransfer/assignCase")
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .fields()
            .page("1")
            .field(CaseData::getAssignOffice)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .caseEventColumn("Publish", null)
            .done()
            .done();
    }

    @Override
    protected int addAmendJurisdictionDisplayOrder() {
        return 18;
    }

    @Override
    protected int adrDocumentsDisplayOrder() {
        return 57;
    }

    @Override
    protected int piiDocumentsDisplayOrder() {
        return 58;
    }

    @Override
    protected int appealDocumentsDisplayOrder() {
        return 59;
    }

    @Override
    protected int pseRespondentRespondToTribunalDisplayOrder() {
        return 60;
    }

    @Override
    protected boolean pseRespondentRespondToTribunalUnavailableWarningShowsSummary() {
        return false;
    }

    @Override
    protected String generateCorrespondenceTypeFieldId() {
        return "correspondenceType";
    }

    @Override
    protected String addressLabelsSelectionPageShowCondition() {
        return "correspondenceType.topLevel_Documents=\"EM-TRB-LET-ENG-00544\" "
            + "AND correspondenceType.part_0_Documents=\"0.1\"";
    }

    @Override
    protected String addressLabelsPageShowCondition() {
        return "correspondenceType.topLevel_Documents=\"EM-TRB-LET-ENG-00544\"";
    }

    @Override
    protected boolean includeEt1ReppedCreateCaseTriageErrorPage() {
        return true;
    }
}
