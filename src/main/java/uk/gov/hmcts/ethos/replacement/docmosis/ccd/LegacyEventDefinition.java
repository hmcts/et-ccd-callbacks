package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

final class LegacyEventDefinition {

    private LegacyEventDefinition() {
    }

    record EventSpec(
        String id,
        String name,
        String description,
        Integer displayOrder,
        String preState,
        String postState,
        String enablingCondition,
        String aboutToStartUrl,
        String aboutToSubmitUrl,
        String submittedUrl,
        String endButtonLabel,
        String publish,
        boolean showSummary,
        List<RoleGrant> grants,
        List<EventFieldSpec> fields,
        List<ComplexSpec> complexFields
    ) {
    }

    record RoleGrant(Set<Permission> permissions, EtUserRole role) {
    }

    record ComplexSpec(
        String caseFieldId,
        String listElementCode,
        DisplayContext context,
        int fieldDisplayOrder,
        String eventElementLabel,
        String id,
        String publish,
        String eventHintText
    ) {
    }

    static EventSpec event(
        String id,
        String name,
        String description,
        Integer displayOrder,
        String preState,
        String postState,
        String enablingCondition,
        String aboutToStartUrl,
        String aboutToSubmitUrl,
        String submittedUrl,
        String endButtonLabel,
        String publish,
        boolean showSummary,
        List<RoleGrant> grants,
        List<EventFieldSpec> fields,
        List<ComplexSpec> complexFields
    ) {
        return new EventSpec(id, name, description, displayOrder, preState, postState, enablingCondition,
            aboutToStartUrl, aboutToSubmitUrl, submittedUrl, endButtonLabel, publish, showSummary, grants, fields,
            complexFields);
    }

    static RoleGrant grant(Set<Permission> permissions, EtUserRole role) {
        return new RoleGrant(permissions, role);
    }

    static RoleGrant grant(Permission permission, EtUserRole role) {
        return grant(Set.of(permission), role);
    }

    static ComplexSpec complex(
        String caseFieldId,
        String listElementCode,
        DisplayContext context,
        int fieldDisplayOrder,
        String eventElementLabel,
        String id,
        String publish,
        String eventHintText
    ) {
        return new ComplexSpec(caseFieldId, listElementCode, context, fieldDisplayOrder, eventElementLabel, id,
            publish, eventHintText);
    }

    static <T> void addTo(ConfigBuilder<T, EtState, EtUserRole> configBuilder, EventSpec spec) {
        EventBuilder<T, EtUserRole, EtState> event = eventBuilder(configBuilder, spec)
            .name(spec.name());
        if (spec.description() == null) {
            event.caseEventColumn("Description", null);
        } else {
            event.description(spec.description());
        }
        if (spec.displayOrder() == null) {
            event.caseEventColumn("DisplayOrder", null);
        } else {
            event.displayOrder(spec.displayOrder());
        }
        if (spec.enablingCondition() != null) {
            event.caseEventColumn("EventEnablingCondition", spec.enablingCondition());
        }
        if (spec.aboutToStartUrl() != null) {
            event.aboutToStartCallbackUrl(spec.aboutToStartUrl());
        }
        if (spec.aboutToSubmitUrl() != null) {
            event.aboutToSubmitCallbackUrl(spec.aboutToSubmitUrl());
        }
        if (spec.submittedUrl() != null) {
            event.submittedCallbackUrl(spec.submittedUrl());
        }
        if (spec.endButtonLabel() != null) {
            event.endButtonLabel(spec.endButtonLabel());
        }
        if (spec.publish() != null) {
            event.caseEventColumn("Publish", spec.publish());
        }
        if (spec.showSummary()) {
            event.showSummary();
        }
        if (spec.preState() != null) {
            event.caseEventColumn("PreConditionState(s)", spec.preState());
        }
        event.caseEventColumn("PostConditionState", spec.postState());
        spec.grants().forEach(grant -> event.grant(grant.permissions(), grant.role()));
        spec.complexFields().forEach(complex -> event.caseEventToComplexType(
            complex.caseFieldId(),
            complex.listElementCode(),
            complex.context(),
            complex.fieldDisplayOrder(),
            complex.eventElementLabel(),
            null,
            complex.id(),
            complex.publish(),
            complex.eventHintText()
        ));

        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields = event.fields();
        spec.fields().forEach(field -> field.addTo(fields).done());
        fields.done();
    }

    private static <T> EventBuilder<T, EtUserRole, EtState> eventBuilder(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        EventSpec spec
    ) {
        if (spec.preState() == null) {
            return configBuilder.event(spec.id()).initialState(state(spec.postState()));
        }
        if ("*".equals(spec.preState()) && "*".equals(spec.postState())) {
            return configBuilder.event(spec.id()).forAllStates();
        }
        if ("*".equals(spec.preState())) {
            return configBuilder.event(spec.id()).forStateTransition(EnumSet.allOf(EtState.class),
                stateOrOpen(spec.postState()));
        }
        if ("*".equals(spec.postState()) || spec.postState().contains("(")) {
            return configBuilder.event(spec.id()).forStateTransition(state(spec.preState()),
                EnumSet.allOf(EtState.class));
        }
        return configBuilder.event(spec.id()).forStateTransition(state(spec.preState()), state(spec.postState()));
    }

    private static EtState stateOrOpen(String value) {
        return value.contains("(") ? EtState.OPEN : state(value);
    }

    private static EtState state(String value) {
        return switch (value) {
            case "Submitted" -> EtState.SUBMITTED;
            case "SubmittedReport" -> EtState.SUBMITTED_REPORT;
            case "Open" -> EtState.OPEN;
            case "Closed" -> EtState.CLOSED;
            case "Transferred" -> EtState.TRANSFERRED;
            default -> throw new IllegalArgumentException("Unsupported legacy state " + value);
        };
    }
}
