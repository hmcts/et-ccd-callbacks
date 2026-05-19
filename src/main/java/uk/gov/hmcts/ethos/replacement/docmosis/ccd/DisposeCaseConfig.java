package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.TTL;

import java.util.EnumSet;

public abstract class DisposeCaseConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_DISPLAY_ORDER = "PageDisplayOrder";
    private static final String PAGE_FIELD_DISPLAY_ORDER = "PageFieldDisplayOrder";
    private static final String RETAIN_HIDDEN_VALUE = "RetainHiddenValue";
    private static final String TTL_CONDITION =
        "additionalCaseInfo.additional_live_appeal=\"Yes\" OR additionalCaseInfo.interventionRequired=\"Yes\"";
    private static final String SUSPEND_DELETION_LABEL = "Do you want to suspend case deletion?";
    private static final String SUSPEND_DELETION_HINT =
        "By default, a closed case will be deleted after 365 days. If you want to retain the case, select 'Yes'. "
            + "If you want to delete the case, select 'No'.";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final int displayOrder;
    private final String suspendedDefaultValue;
    private final String aboutToStartCallbackUrl;

    protected DisposeCaseConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        int displayOrder,
        String suspendedDefaultValue,
        String aboutToStartCallbackUrl
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.displayOrder = displayOrder;
        this.suspendedDefaultValue = suspendedDefaultValue;
        this.aboutToStartCallbackUrl = aboutToStartCallbackUrl;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        Event.EventBuilder<T, EtUserRole, EtState> event = configBuilder.event("disposeCase")
            .forStateTransition(EnumSet.of(EtState.ACCEPTED, EtState.REJECTED), EtState.CLOSED)
            .name("Close Case")
            .description("Close Case")
            .displayOrder(displayOrder)
            .showSummary()
            .caseEventColumn("Publish", "Y")
            .significantEvent("${ET_ENV_RETENTION_SIGNIFICANT_EVENT}")
            .ttlIncrement("${ET_ENV_DISPOSE_CASE_TTL_INCREMENT}");

        if (aboutToStartCallbackUrl != null) {
            event.aboutToStartCallbackUrl(aboutToStartCallbackUrl);
        }

        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields = event.fields();
        addFields(fields);
        fields.done()
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    protected abstract void addFields(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields
    );

    protected void addField(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        String fieldId,
        FieldKind kind,
        int page,
        int pageDisplayOrder,
        int pageFieldDisplayOrder,
        String showCondition,
        String retainHiddenValue
    ) {
        Field.FieldBuilder<?, EtState, T, Event.EventBuilder<T, EtUserRole, EtState>> field =
            fields.page(String.valueOf(page)).field(fieldId);
        applyContext(field, kind);
        field.showSummary()
            .showCondition(showCondition)
            .caseEventColumn(PAGE_DISPLAY_ORDER, pageDisplayOrder)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, pageFieldDisplayOrder)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1);

        if (retainHiddenValue != null) {
            field.caseEventColumn(RETAIN_HIDDEN_VALUE, retainHiddenValue);
        }
        field.done();
    }

    protected void addTtlField(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        int page,
        int pageDisplayOrder,
        int pageFieldDisplayOrder
    ) {
        fields.page(String.valueOf(page)).field("TTL")
            .showSummary()
            .showCondition(TTL_CONDITION)
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn(PAGE_DISPLAY_ORDER, pageDisplayOrder)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, pageFieldDisplayOrder)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .complex(TTL.class)
            .field(TTL::getSuspended)
            .mandatory()
            .showSummary()
            .defaultValue(suspendedDefaultValue)
            .caseEventFieldLabel(SUSPEND_DELETION_LABEL)
            .caseEventFieldHint(SUSPEND_DELETION_HINT)
            .caseEventColumn("ID", "disposeCaseTTL")
            .caseEventColumn("FieldDisplayOrder", 1)
            .caseEventColumn("LiveFrom", null)
            .done()
            .done();
    }

    private void applyContext(
        Field.FieldBuilder<?, EtState, T, Event.EventBuilder<T, EtUserRole, EtState>> field,
        FieldKind kind
    ) {
        switch (kind) {
            case MANDATORY -> field.mandatory();
            case OPTIONAL -> field.optional();
            case READ_ONLY -> field.readOnly();
            default -> throw new IllegalArgumentException("Unsupported field kind " + kind);
        }
    }

    protected enum FieldKind {
        MANDATORY,
        OPTIONAL,
        READ_ONLY
    }
}
