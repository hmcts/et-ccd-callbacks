package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.EnumSet;
import java.util.Set;

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
    private final int caseTransferEcmDisplayOrder;
    private final Set<Permission> caseTransferEcmWaPermissions;
    private final boolean caseTransferEcmReasonShowsSummary;
    private final int amendSingleDisplayOrder;
    private final boolean includeSameCountryEccLinkedCaseTransfer;

    protected CaseTransferConfig(
        int caseTransferMultipleDisplayOrder,
        String caseTransferMultipleDescription,
        int processCaseTransferDisplayOrder,
        int returnCaseTransferDisplayOrder,
        int caseTransferEcmDisplayOrder,
        Set<Permission> caseTransferEcmWaPermissions,
        boolean caseTransferEcmReasonShowsSummary,
        int amendSingleDisplayOrder,
        boolean includeSameCountryEccLinkedCaseTransfer
    ) {
        this.caseTransferMultipleDisplayOrder = caseTransferMultipleDisplayOrder;
        this.caseTransferMultipleDescription = caseTransferMultipleDescription;
        this.processCaseTransferDisplayOrder = processCaseTransferDisplayOrder;
        this.returnCaseTransferDisplayOrder = returnCaseTransferDisplayOrder;
        this.caseTransferEcmDisplayOrder = caseTransferEcmDisplayOrder;
        this.caseTransferEcmWaPermissions = caseTransferEcmWaPermissions;
        this.caseTransferEcmReasonShowsSummary = caseTransferEcmReasonShowsSummary;
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

        caseTransferEcmFields(
            configBuilder.event("caseTransferECM")
                .forStateTransition(EnumSet.allOf(EtState.class), EtState.TRANSFERRED)
                .name("Case Transfer to ECM")
                .description("Transfer case to ECM system")
                .displayOrder(caseTransferEcmDisplayOrder)
                .caseEventColumn(
                    "PreConditionState(s)",
                    "Submitted;AWAITING_SUBMISSION_TO_HMCTS;Vetted;Accepted;Closed;Rejected"
                )
                .publishToCamunda()
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/caseTransfer/transferToEcm")
        )
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, regionalCaseworkerRole())
            .grant(caseTransferEcmWaPermissions, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

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

    protected abstract EtUserRole regionalCaseworkerRole();

    private Event.EventBuilder<T, EtUserRole, EtState> caseTransferEcmFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field(CaseData::getEcmOfficeCT)
            .mandatory()
            .showSummary()
            .caseEventColumn("Publish", null)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getReasonForCT)
            .mandatory()
            .caseEventColumn("Publish", null)
            .caseEventColumn("ShowSummaryChangeOption", caseTransferEcmReasonShowsSummary ? "Y" : null)
            .caseEventColumn("PageColumnNumber", caseTransferEcmReasonShowsSummary ? 1 : null)
            .done()
            .done();
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
