package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.Field.FieldBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

record EventComplexFieldSpec<C>(
    TypedPropertyGetter<C, ?> getter,
    DisplayContext context,
    int fieldDisplayOrder,
    String label,
    String retainHiddenValue,
    String eventTypeId,
    String publish
) {

    static <C> EventComplexFieldSpec<C> complexField(
        TypedPropertyGetter<C, ?> getter,
        DisplayContext context,
        int fieldDisplayOrder
    ) {
        return new EventComplexFieldSpec<>(
            getter, context, fieldDisplayOrder, null, null, "InitialConsideration", null);
    }

    EventComplexFieldSpec<C> label(String value) {
        return new EventComplexFieldSpec<>(
            getter, context, fieldDisplayOrder, value, retainHiddenValue, eventTypeId, publish);
    }

    EventComplexFieldSpec<C> retainHidden(String value) {
        return new EventComplexFieldSpec<>(
            getter, context, fieldDisplayOrder, label, value, eventTypeId, publish);
    }

    EventComplexFieldSpec<C> eventTypeId(String value) {
        return new EventComplexFieldSpec<>(
            getter, context, fieldDisplayOrder, label, retainHiddenValue, value, publish);
    }

    EventComplexFieldSpec<C> publishColumn() {
        return new EventComplexFieldSpec<>(
            getter, context, fieldDisplayOrder, label, retainHiddenValue, eventTypeId, "Y");
    }

    <T extends CaseData, P> void addTo(
        FieldCollectionBuilder<C, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            fields
    ) {
        FieldBuilder<?, EtState, C, ?> field = fields.field(getter);
        applyContext(field);
        field.caseEventColumn("FieldDisplayOrder", fieldDisplayOrder)
            .caseEventColumn("ID", eventTypeId)
            .caseEventColumn("LiveFrom", null);
        if (label != null) {
            field.caseEventFieldLabel(label);
        }
        if (retainHiddenValue != null) {
            field.caseEventColumn("RetainHiddenValue", retainHiddenValue);
        }
        if (publish != null) {
            field.caseEventColumn("Publish", publish);
        }
    }

    private void applyContext(FieldBuilder<?, EtState, C, ?> field) {
        switch (context) {
            case Mandatory -> field.mandatory();
            case Optional -> field.optional();
            case ReadOnly -> field.readOnly();
            default -> throw new IllegalArgumentException("Unsupported complex display context " + context);
        }
    }
}
