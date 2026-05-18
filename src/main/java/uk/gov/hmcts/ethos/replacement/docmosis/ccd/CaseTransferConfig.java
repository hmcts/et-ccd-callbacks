package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.EnumSet;

public abstract class CaseTransferConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String TRANSFER_POST_CONDITION_STATES =
        "Accepted(preAcceptCase.caseAccepted=\"Yes\" AND positionType!=\"Case closed\"):1;"
            + "Rejected(preAcceptCase.caseAccepted=\"No\" AND positionType!=\"Case closed\"):2;"
            + "Closed(positionType=\"Case closed\"):3;Submitted";

    private static final String CLOSED_OR_ANY_POST_CONDITION_STATES = "Closed(positionType=\"Case closed\"):1;*";

    private final int caseTransferMultipleDisplayOrder;
    private final String caseTransferMultipleDescription;
    private final int processCaseTransferDisplayOrder;
    private final int returnCaseTransferDisplayOrder;
    private final int amendSingleDisplayOrder;
    private final boolean includeSameCountryEccLinkedCaseTransfer;

    protected CaseTransferConfig(
        int caseTransferMultipleDisplayOrder,
        String caseTransferMultipleDescription,
        int processCaseTransferDisplayOrder,
        int returnCaseTransferDisplayOrder,
        int amendSingleDisplayOrder,
        boolean includeSameCountryEccLinkedCaseTransfer
    ) {
        this.caseTransferMultipleDisplayOrder = caseTransferMultipleDisplayOrder;
        this.caseTransferMultipleDescription = caseTransferMultipleDescription;
        this.processCaseTransferDisplayOrder = processCaseTransferDisplayOrder;
        this.returnCaseTransferDisplayOrder = returnCaseTransferDisplayOrder;
        this.amendSingleDisplayOrder = amendSingleDisplayOrder;
        this.includeSameCountryEccLinkedCaseTransfer = includeSameCountryEccLinkedCaseTransfer;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        if (includeSameCountryEccLinkedCaseTransfer) {
            apiEvent(
                configBuilder,
                "caseTransferSameCountryEccLinkedCase",
                "Case Transfer (Eng/Wales)",
                "Transfer ECC linked case to another office within England/Wales (API)",
                34
            )
                .showSummary()
                .showCondition("caseType !=\"Multiple\"")
                .caseEventColumn("PreConditionState(s)", "Submitted;Vetted;Accepted")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/caseTransfer/transferSameCountryEccLinkedCase")
                .endButtonLabel("Transfer Case")
                .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
        }

        configBuilder.event("caseTransferMultiple")
            .forStateTransition(EnumSet.allOf(EtState.class), EtState.TRANSFERRED)
            .name("Case Transfer (Multiples)")
            .description(caseTransferMultipleDescription)
            .displayOrder(caseTransferMultipleDisplayOrder)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/postDefaultValues")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        apiEvent(
            configBuilder,
            "processCaseTransfer",
            "Process Case Transfer",
            "Process Case Transfer",
            processCaseTransferDisplayOrder
        )
            .caseEventColumn("PreConditionState(s)", null)
            .caseEventColumn("PostConditionState", TRANSFER_POST_CONDITION_STATES)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/postDefaultValues")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("returnCaseTransfer")
            .forState(EtState.TRANSFERRED)
            .name("Return Case Transfer")
            .description("Return Case Transfer")
            .displayOrder(returnCaseTransferDisplayOrder)
            .caseEventColumn("PostConditionState", TRANSFER_POST_CONDITION_STATES)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/postDefaultValues")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        apiEvent(
            configBuilder,
            "amendSingle",
            "Amend Case (Multiple) API ONLY",
            "Amend case via Multiples",
            amendSingleDisplayOrder
        )
            .caseEventColumn("PostConditionState", CLOSED_OR_ANY_POST_CONDITION_STATES)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> apiEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        String description,
        int displayOrder
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(description)
            .displayOrder(displayOrder);
    }
}
