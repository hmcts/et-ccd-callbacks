package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JudgeOverviewModelFactoryTest {

    private static final long CASE_REFERENCE = 1_701_234_567_890_123L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC);
    private final JudgeOverviewModelFactory factory = new JudgeOverviewModelFactory(CLOCK);

    @Test
    void createsBusyOverviewFromCaseData() {
        CaseData caseData = baseCase();
        caseData.setGenericTseApplicationCollection(List.of(application("3", "Postpone a hearing", "Open",
            "8 September 2026", "13 September 2026")));
        caseData.setReferralCollection(List.of(referral("4", "Postpone a hearing", "Awaiting instructions",
            "Yes", "9 Sep 2026")));
        caseData.setBfActions(List.of(bfAction("2026-09-01", "No")));
        caseData.setHearingCollection(List.of(hearing("Final Hearing", "Leeds", "EJ Okafor",
            "2026-09-21T10:00:00.000", "Listed")));

        JudgeOverviewModel model = factory.create(caseData, CASE_REFERENCE, CaseState.Accepted);

        assertThat(model.getCaption()).isEqualTo("6012345/2026 · 35 weeks in the system");
        assertThat(model.getHeading()).isEqualTo("Ms P Nair v Halcyon Logistics Ltd");
        assertThat(model.isWarning()).isTrue();
        assertThat(model.getAttentionItems()).hasSize(2);
        assertThat(model.getAttentionItems().getFirst().getStatus()).isEqualTo("Overdue");
        assertThat(model.getAttentionItems().getFirst().getRaised()).isEqualTo("8 Sept 2026");
        assertThat(model.getAttentionItems().getFirst().getDue()).isEqualTo("13 Sept 2026");
        assertThat(model.getAttentionItems().getLast().getStatus()).isEqualTo("Urgent");
        assertThat(model.getAttentionItems().getLast().getRaised()).isEqualTo("9 Sept 2026");
        assertThat(model.getOverdueBfActions()).isEqualTo("1 overdue BF action, oldest due 1 Sept 2026");
        assertThat(model.getNextHearing().getVenue()).isEqualTo("Leeds");
        assertThat(model.getQuickActions()).extracting(JudgeOverviewModel.Action::getLabel)
            .contains("Record a decision", "Reply to referral", "Draft and sign judgment or order");
        assertThat(model.getQuickActions()).extracting(JudgeOverviewModel.Action::getUrl)
            .contains(
                "/cases/case-details/1701234567890123/trigger/tseAdmin/tseAdmin1",
                "/cases/case-details/1701234567890123/trigger/replyToReferral/replyToReferral1"
        );
    }

    @Test
    void createsQuietSparseOverviewAndExcludesClosedItems() {
        CaseData caseData = baseCase();
        caseData.setGenericTseApplicationCollection(List.of(application("1", "Amend details", "Closed",
            "2026-09-08", "2026-09-13")));
        caseData.setReferralCollection(List.of(referral("1", "Orders", "Closed", "No", "2026-09-09")));
        caseData.setBfActions(List.of(bfAction("2026-09-01", "Yes")));

        JudgeOverviewModel model = factory.create(caseData, CASE_REFERENCE, CaseState.Closed);

        assertThat(model.isWarning()).isFalse();
        assertThat(model.getAttentionItems()).isEmpty();
        assertThat(model.isHasOverdueBfActions()).isFalse();
        assertThat(model.getTodayMessage()).contains("Nothing needs your attention today");
        assertThat(model.getQuickActions()).extracting(JudgeOverviewModel.Action::getLabel)
            .doesNotContain("Record a decision", "Reply to referral", "Draft and sign judgment or order");
    }

    @Test
    void readsScotlandHearingVenue() {
        CaseData caseData = baseCase();
        HearingTypeItem hearing = hearing("Preliminary Hearing", null, "EJ Fraser",
            "2026-10-01T10:00:00.000", "Listed");
        hearing.getValue().setHearingVenueScotland("Edinburgh");
        hearing.getValue().setHearingEdinburgh(new DynamicFixedListType("Edinburgh Tribunal Centre"));
        caseData.setHearingCollection(List.of(hearing));

        JudgeOverviewModel model = factory.create(caseData, CASE_REFERENCE, CaseState.Accepted);

        assertThat(model.getNextHearing().getVenue()).isEqualTo("Edinburgh Tribunal Centre");
    }

    private CaseData baseCase() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("6012345/2026");
        caseData.setClaimant("Ms P Nair");
        caseData.setRespondent("Halcyon Logistics Ltd");
        caseData.setReceiptDate("2026-01-12");
        caseData.setManagingOffice("Leeds");
        caseData.setPositionType("Awaiting instructions from Judge");
        caseData.setConciliationTrack("Open Track");
        return caseData;
    }

    private GenericTseApplicationTypeItem application(String number, String type, String status, String date,
                                                      String dueDate) {
        GenericTseApplicationType value = new GenericTseApplicationType();
        value.setNumber(number);
        value.setType(type);
        value.setStatus(status);
        value.setDate(date);
        value.setDueDate(dueDate);
        GenericTseApplicationTypeItem item = new GenericTseApplicationTypeItem();
        item.setValue(value);
        return item;
    }

    private ReferralTypeItem referral(String number, String subject, String status, String urgent, String date) {
        ReferralType value = new ReferralType();
        value.setReferralNumber(number);
        value.setReferralSubject(subject);
        value.setReferralStatus(status);
        value.setIsUrgent(urgent);
        value.setReferralDate(date);
        ReferralTypeItem item = new ReferralTypeItem();
        item.setValue(value);
        return item;
    }

    private BFActionTypeItem bfAction(String date, String cleared) {
        BFActionType value = new BFActionType();
        value.setBfDate(date);
        value.setCleared(cleared);
        BFActionTypeItem item = new BFActionTypeItem();
        item.setValue(value);
        return item;
    }

    private HearingTypeItem hearing(String type, String venue, String judge, String date, String status) {
        DateListedType listed = new DateListedType();
        listed.setListedDate(date);
        listed.setHearingStatus(status);
        DateListedTypeItem listedItem = new DateListedTypeItem();
        listedItem.setValue(listed);

        HearingType value = new HearingType();
        value.setHearingType(type);
        value.setJudge(new DynamicFixedListType(judge));
        value.setHearingDateCollection(List.of(listedItem));
        if (venue != null) {
            value.setHearingVenue(new DynamicFixedListType(venue));
        }

        HearingTypeItem item = new HearingTypeItem();
        item.setValue(value);
        return item;
    }
}
