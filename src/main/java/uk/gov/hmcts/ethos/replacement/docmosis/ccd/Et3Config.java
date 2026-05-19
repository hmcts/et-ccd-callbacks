package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class Et3Config<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_LABEL = "PageLabel";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean grantSubmitToApi;
    private final boolean grantEt3NotificationToAcas;

    protected Et3Config(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean grantSubmitToApi,
        boolean grantEt3NotificationToAcas
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.grantSubmitToApi = grantSubmitToApi;
        this.grantEt3NotificationToAcas = grantEt3NotificationToAcas;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        Event.EventBuilder<T, EtUserRole, EtState> submitEt3Form = et3Event(
            configBuilder,
            "SUBMIT_ET3_FORM",
            "Submit ET3 Form"
        )
            .publishToCamunda()
            .grant(Permission.CRUD, EtUserRole.DEFENDANT, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .grant(
                Permission.R,
                EtUserRole.CASEWORKER_EMPLOYMENT,
                regionalCaseworkerRole,
                EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE,
                regionalJudgeRole
            );

        if (grantSubmitToApi) {
            submitEt3Form.grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
        }

        et3Event(configBuilder, "UPDATE_ET3_FORM", "Update ET3 Form")
            .grant(Permission.CRUD, EtUserRole.CITIZEN);

        Event.EventBuilder<T, EtUserRole, EtState> et3Notification = et3NotificationFields(
            configBuilder.event("et3Notification")
                .forAllStates()
                .name("ET3 notification")
                .description("ET3 notification")
                .displayOrder(19)
                .caseEventColumn("PreConditionState(s)", "Accepted")
                .aboutToStartCallbackUrl("${ET_COS_URL}/et3Notification/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Notification/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/et3Notification/submitted")
        )
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        if (grantEt3NotificationToAcas) {
            et3Notification.grant(Permission.R, EtUserRole.ET_ACAS_API);
        }
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et3Event(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name
    ) {
        return configBuilder.event(eventId)
            .forStateTransition(EtState.ACCEPTED, EtState.ACCEPTED)
            .name(name)
            .description(name)
            .caseEventColumn("DisplayOrder", null);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et3NotificationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/et3Notification/midUploadDocuments")
            .pageLabel("Upload documents")
            .field(CaseData::getEt3NotificationDocCollection)
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("2")
            .pageLabel("Who are you sending this document to?")
            .field(CaseData::getHorizontalLine4)
            .readOnly()
            .showSummary()
            .caseEventColumn("PageShowCondition", "et3OtherTypeDocumentName != \"\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3OtherTypeDocumentName)
            .mandatory()
            .showCondition("horizontalLine4=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3OtherTypeDocumentNameLabel)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3SelectAllThatApply)
            .readOnly()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3NotificationDocRecipient)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("3")
            .pageLabel("Email Acas")
            .field(CaseData::getEt3EmailDocsToAcasTitle)
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3EmailLinkToAcas)
            .mandatory()
            .showCondition("et3EmailDocsToAcasTitle=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3EmailDocsToAcasLink)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3EmailDocsToAcasInstructions)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();
    }
}
