package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseType;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Jurisdiction;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositState;

import java.time.LocalDate;

import static uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositRole.EMPLOYMENT_API;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositState.Open;

@Component
public class PreHearingDepositDefinition implements
    CCDConfig<PreHearingDepositData, PreHearingDepositState, PreHearingDepositRole> {

    private static final String CASE_TYPE = "Pre_Hearing_Deposit";
    private static final String REFUND_REQUESTED = "depositRefund=\"YES\"";

    @Override
    public String groupingKey() {
        return CASE_TYPE;
    }

    @Override
    public void configure(
        ConfigBuilder<PreHearingDepositData, PreHearingDepositState, PreHearingDepositRole> builder
    ) {
        builder.caseType(CaseType.builder()
            .id(CASE_TYPE)
            .name("Pre-Hearing Deposit")
            .description("Pre-Hearing Deposit")
            .liveFrom(LocalDate.of(2023, 9, 28))
            .retriesTimeoutUrlPrintEvent(20)
            .build());
        builder.jurisdiction(Jurisdiction.builder()
            .id("EMPLOYMENT")
            .name("Employment")
            .description("Employment")
            .shuttered(true)
            .build());
        builder.omitDefaultLiveFrom();
        builder.omitCaseHistory();

        configureCreateEvent(builder);
        configureTabs(builder);
        configureSearch(builder);
    }

    private void configureCreateEvent(
        ConfigBuilder<PreHearingDepositData, PreHearingDepositState, PreHearingDepositRole> builder
    ) {
        builder.event("initiateCase")
            .initialState(Open)
            .name("Create Pre-Hearing Deposit")
            .description("Initiate Pre-Hearing Deposit Case")
            .displayOrder(1)
            .endButtonLabel("Create PHR Deposit")
            .omitShowSummary()
            .omitPublish()
            .externalCallbackUrl(
                Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/preHearingDeposit/createPHRDeposit"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API)
            .fields()
            .omitPageColumnNumber()
            .optionalNoSummary(PreHearingDepositData::getCaseNumber)
            .optionalNoSummary(PreHearingDepositData::getClaimantOrRespondentName)
            .optionalNoSummary(PreHearingDepositData::getDepositOrderedAgainst)
            .optionalNoSummary(PreHearingDepositData::getDepositRequestedBy)
            .optionalNoSummary(PreHearingDepositData::getDepositCovers)
            .optionalNoSummary(PreHearingDepositData::getDepositOrderSent)
            .optionalNoSummary(PreHearingDepositData::getDepositTimeExtension)
            .optionalNoSummary(PreHearingDepositData::getDepositTimeExtensionDue)
            .optionalNoSummary(PreHearingDepositData::getDepositReceived)
            .optionalNoSummary(PreHearingDepositData::getDepositAmount)
            .optionalNoSummary(PreHearingDepositData::getDateDepositReceived)
            .optionalNoSummary(PreHearingDepositData::getDepositDue)
            .optionalNoSummary(PreHearingDepositData::getDepositRefund)
            .optionalNoSummary(PreHearingDepositData::getDateSentForRefund, REFUND_REQUESTED, null)
            .optionalNoSummary(PreHearingDepositData::getAmountRefunded, REFUND_REQUESTED, null)
            .optionalNoSummary(PreHearingDepositData::getDepositRefundDate, REFUND_REQUESTED, null)
            .optionalNoSummary(PreHearingDepositData::getDepositRefundedTo, REFUND_REQUESTED, null)
            .optionalNoSummary(PreHearingDepositData::getChequeOrPONumber)
            .optionalNoSummary(PreHearingDepositData::getReceivedBy)
            .optionalNoSummary(PreHearingDepositData::getDepositReceivedFrom)
            .optionalNoSummary(PreHearingDepositData::getDepositComments)
            .optionalNoSummary(PreHearingDepositData::getComments)
            .optionalNoSummary(PreHearingDepositData::getPhrNumber)
            .optionalNoSummary(PreHearingDepositData::getMr1Reference)
            .optionalNoSummary(PreHearingDepositData::getBankingDate)
            .optionalNoSummary(PreHearingDepositData::getJournalConfirmedReceipt)
            .optionalNoSummary(PreHearingDepositData::getStatus)
            .optionalNoSummary(PreHearingDepositData::getPayeeName)
            .optionalNoSummary(PreHearingDepositData::getRefundReference)
            .optionalNoSummary(PreHearingDepositData::getJournalConfirmedPaid)
            .optionalNoSummary(PreHearingDepositData::getRegionOffice)
            .optionalNoSummary(PreHearingDepositData::getNotes)
            .optionalNoSummary(PreHearingDepositData::getDocumentUpload)
            .done();
    }

    private void configureTabs(
        ConfigBuilder<PreHearingDepositData, PreHearingDepositState, PreHearingDepositRole> builder
    ) {
        builder.tab("preHearingDepositData", "Pre-Hearing Deposit")
            .withoutChannel()
            .field(
                PreHearingDepositData::getCaseNumber,
                "claimantOrRespondentName=\"dummy_claimant_respondent_name_1983_\""
            )
            .field(PreHearingDepositData::getClaimantOrRespondentName)
            .field(PreHearingDepositData::getDepositAmount)
            .field(PreHearingDepositData::getDepositOrderedAgainst)
            .field(PreHearingDepositData::getDepositRequestedBy)
            .field(PreHearingDepositData::getDepositCovers)
            .field(PreHearingDepositData::getDepositOrderSent)
            .field(PreHearingDepositData::getDepositDue)
            .field(PreHearingDepositData::getDepositTimeExtension)
            .field(PreHearingDepositData::getDepositTimeExtensionDue)
            .field(PreHearingDepositData::getDepositReceived)
            .field(PreHearingDepositData::getDateDepositReceived)
            .field(PreHearingDepositData::getDepositRefund)
            .field(PreHearingDepositData::getAmountRefunded)
            .field(PreHearingDepositData::getDepositRefundDate)
            .field(PreHearingDepositData::getChequeOrPONumber)
            .field(PreHearingDepositData::getReceivedBy)
            .field(PreHearingDepositData::getDepositRefundedTo)
            .field(PreHearingDepositData::getNotes)
            .field(PreHearingDepositData::getDocumentUpload)
            .field(PreHearingDepositData::getDepositReceivedFrom)
            .field(PreHearingDepositData::getDepositComments)
            .field(PreHearingDepositData::getComments)
            .field(PreHearingDepositData::getPhrNumber)
            .field(PreHearingDepositData::getMr1Reference)
            .field(PreHearingDepositData::getBankingDate)
            .field(PreHearingDepositData::getJournalConfirmedReceipt)
            .field(PreHearingDepositData::getStatus)
            .field(PreHearingDepositData::getDateSentForRefund)
            .field(PreHearingDepositData::getRefundReference)
            .field(PreHearingDepositData::getRegionOffice)
            .field(PreHearingDepositData::getPayeeName)
            .field(PreHearingDepositData::getJournalConfirmedPaid);

        builder.tab("preHearingDepositDataHistory", "History")
            .withoutChannel()
            .field(PreHearingDepositData::getPreHearingDepositImportFile);
    }

    private void configureSearch(
        ConfigBuilder<PreHearingDepositData, PreHearingDepositState, PreHearingDepositRole> builder
    ) {
        builder.searchInputFields()
            .field(PreHearingDepositData::getCaseNumber, "Case number");
        builder.searchResultFields()
            .field(PreHearingDepositData::getCaseNumber, "Case number");
        builder.workBasketInputFields()
            .field(PreHearingDepositData::getCaseNumber, "Case number");
        builder.workBasketResultFields()
            .field(PreHearingDepositData::getCaseReferenceNumber, "Case reference number")
            .field(PreHearingDepositData::getCaseNumber, "Case number")
            .field(PreHearingDepositData::getClaimantOrRespondentName, "Claimant or respondent name")
            .field(PreHearingDepositData::getDepositDue, "Deposit due")
            .field(PreHearingDepositData::getDepositAmount, "Deposit Amount")
            .field(PreHearingDepositData::getStatus, "Deposit state");
    }
}
