package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandSingleFieldEventsConfig extends SingleFieldEventsConfig<ScotlandCaseData> {

    public ScotlandSingleFieldEventsConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            50,
            "Add telephone Note",
            1,
            2,
            40,
            44,
            "Judgement, Order, Notification",
            "View all judgement, orders and notifications"
        );
    }

    @Override
    protected int addAmendJurisdictionDisplayOrder() {
        return 21;
    }

    @Override
    protected int adrDocumentsDisplayOrder() {
        return 53;
    }

    @Override
    protected int piiDocumentsDisplayOrder() {
        return 54;
    }

    @Override
    protected int appealDocumentsDisplayOrder() {
        return 55;
    }

    @Override
    protected int pseRespondentRespondToTribunalDisplayOrder() {
        return 3;
    }

    @Override
    protected boolean pseRespondentRespondToTribunalUnavailableWarningShowsSummary() {
        return true;
    }

    @Override
    protected String generateCorrespondenceTypeFieldId() {
        return "correspondenceScotType";
    }

    @Override
    protected String addressLabelsSelectionPageShowCondition() {
        return "correspondenceScotType.topLevel_Scot_Documents=\"EM-TRB-LET-ENG-00544\" "
            + "AND correspondenceScotType.part_0_Scot_Documents=\"0.1\"";
    }

    @Override
    protected String addressLabelsPageShowCondition() {
        return "correspondenceScotType.topLevel_Scot_Documents=\"EM-TRB-LET-ENG-00544\"";
    }

    @Override
    protected boolean includeEt1ReppedCreateCaseTriageErrorPage() {
        return false;
    }
}
