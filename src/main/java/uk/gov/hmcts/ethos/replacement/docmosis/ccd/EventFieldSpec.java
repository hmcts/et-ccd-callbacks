package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.Field.FieldBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

record EventFieldSpec(
    String id,
    DisplayContext context,
    int page,
    Integer pageDisplayOrder,
    int pageFieldDisplayOrder,
    String showCondition,
    String pageShowCondition,
    String pageLabel,
    String label,
    String midEventCallbackUrl,
    String showSummaryChangeOption,
    Integer pageColumnNumber,
    String retainHiddenValue,
    String publish
) {

    static EventFieldSpec field(
        String id,
        DisplayContext context,
        int page,
        int pageDisplayOrder,
        int pageFieldDisplayOrder
    ) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, null, null, null, null,
            null, null, null, null, null);
    }

    EventFieldSpec show(String value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, value, pageShowCondition,
            pageLabel, label, midEventCallbackUrl, showSummaryChangeOption, pageColumnNumber, retainHiddenValue,
            publish);
    }

    EventFieldSpec pageShow(String value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition, value,
            pageLabel, label, midEventCallbackUrl, showSummaryChangeOption, pageColumnNumber, retainHiddenValue,
            publish);
    }

    EventFieldSpec pageLabel(String value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
            pageShowCondition, value, label, midEventCallbackUrl, showSummaryChangeOption, pageColumnNumber,
            retainHiddenValue, publish);
    }

    EventFieldSpec label(String value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
            pageShowCondition, pageLabel, value, midEventCallbackUrl, showSummaryChangeOption, pageColumnNumber,
            retainHiddenValue, publish);
    }

    EventFieldSpec mid(String value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
            pageShowCondition, pageLabel, label, value, showSummaryChangeOption, pageColumnNumber, retainHiddenValue,
            publish);
    }

    EventFieldSpec summary(String value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
            pageShowCondition, pageLabel, label, midEventCallbackUrl, value, pageColumnNumber, retainHiddenValue,
            publish);
    }

    EventFieldSpec noPageDisplayOrder() {
        return new EventFieldSpec(id, context, page, null, pageFieldDisplayOrder, showCondition, pageShowCondition,
            pageLabel, label, midEventCallbackUrl, showSummaryChangeOption, pageColumnNumber, retainHiddenValue,
            publish);
    }

    EventFieldSpec pageColumn(int value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
            pageShowCondition, pageLabel, label, midEventCallbackUrl, showSummaryChangeOption, value,
            retainHiddenValue, publish);
    }

    EventFieldSpec retainHidden() {
        return retainHidden("Yes");
    }

    EventFieldSpec retainHidden(String value) {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
            pageShowCondition, pageLabel, label, midEventCallbackUrl, showSummaryChangeOption, pageColumnNumber,
            value, publish);
    }

    EventFieldSpec publishColumn() {
        return new EventFieldSpec(id, context, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
            pageShowCondition, pageLabel, label, midEventCallbackUrl, showSummaryChangeOption, pageColumnNumber,
            retainHiddenValue, "Y");
    }

    <T extends CaseData> FieldBuilder<?, EtState, T, EventBuilder<T, EtUserRole, EtState>> addTo(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields
    ) {
        FieldBuilder<?, EtState, T, EventBuilder<T, EtUserRole, EtState>> field =
            fields.page(String.valueOf(page)).field(id);
        applyContext(field);
        field.showCondition(showCondition)
            .caseEventColumn("PageFieldDisplayOrder", pageFieldDisplayOrder);
        field.caseEventColumn("PageDisplayOrder", pageDisplayOrder);
        applyColumn(field, "PageShowCondition", pageShowCondition);
        applyColumn(field, "PageLabel", pageLabel);
        applyColumn(field, "Label", label);
        applyColumn(field, "CallBackURLMidEvent", midEventCallbackUrl);
        applyColumn(field, "ShowSummaryChangeOption", showSummaryChangeOption);
        field.caseEventColumn("PageColumnNumber", pageColumnNumber);
        applyColumn(field, "RetainHiddenValue", retainHiddenValue);
        applyColumn(field, "Publish", publish);
        return field;
    }

    private <T extends CaseData> void applyContext(
        FieldBuilder<?, EtState, T, EventBuilder<T, EtUserRole, EtState>> field
    ) {
        switch (context) {
            case Mandatory -> field.mandatory();
            case Optional -> field.optional();
            case ReadOnly -> field.readOnly();
            case Complex -> field.caseEventColumn("DisplayContext", "COMPLEX");
            default -> throw new IllegalArgumentException("Unsupported display context " + context);
        }
    }

    private <T extends CaseData> void applyColumn(
        FieldBuilder<?, EtState, T, EventBuilder<T, EtUserRole, EtState>> field,
        String column,
        Object value
    ) {
        if (value != null) {
            field.caseEventColumn(column, value);
        }
    }
}
