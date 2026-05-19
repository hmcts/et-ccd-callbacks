package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class Et1ServingConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_LABEL = "PageLabel";
    private static final String PUBLISH = "Publish";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean repeatSendDocumentsPageLabel;

    protected Et1ServingConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean repeatSendDocumentsPageLabel
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.repeatSendDocumentsPageLabel = repeatSendDocumentsPageLabel;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        uploadDocumentForServingFields(
            configBuilder.event("uploadDocumentForServing")
                .forState(EtState.ACCEPTED)
                .name("ET1 serving")
                .description("Upload a Document")
                .displayOrder(15)
                .showCondition("managingOffice !=\"Unassigned\"")
                .caseEventColumn("PostConditionState", "*")
                .caseEventColumn(PUBLISH, "Y")
                .submittedCallbackUrl("${ET_COS_URL}/et1Serving/submitted")
        )
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> uploadDocumentForServingFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var sendDocByFirstClass = event.fields()
            .page("1")
            .pageLabel("Upload documents")
            .field(CaseData::getServingDocumentCollection)
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/midServingDocumentOtherTypeNames")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("servingDocumentCollection=\"dummy\"")
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, "Y")
            .done()
            .field(CaseData::getBfActions)
            .showCondition("otherTypeDocumentName=\"dummy\"")
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .page("2")
            .pageLabel("Who are you sending this document to?")
            .field(CaseData::getHorizontalLine)
            .readOnly()
            .showSummary()
            .caseEventColumn("PageShowCondition", "otherTypeDocumentName != \"\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getOtherTypeDocumentName)
            .mandatory()
            .showCondition("horizontalLine=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("otherTypeDocumentNameLabel")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("selectAllThatApply")
            .readOnly()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getServingDocumentRecipient)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .page("3")
            .pageLabel("Send documents")
            .field("printAndSendPaperDocuments")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendDocByFirstClass")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null);

        if (!repeatSendDocumentsPageLabel) {
            sendDocByFirstClass.caseEventColumn(PAGE_LABEL, null);
        }

        return sendDocByFirstClass
            .done()
            .field(CaseData::getClaimantAndRespondentAddresses)
            .readOnly()
            .showCondition("printAndSendPaperDocuments=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("claimantAndRespondentAddressesLabel")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .page("4")
            .pageLabel("Email Acas")
            .field("emailDocsToAcasLine")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("emailDocsToAcasTitle")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getEmailLinkToAcas)
            .mandatory()
            .showCondition("emailDocsToAcasTitle=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("emailDocsToAcasLink")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("emailDocsToAcasInstructions")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .done();
    }
}
