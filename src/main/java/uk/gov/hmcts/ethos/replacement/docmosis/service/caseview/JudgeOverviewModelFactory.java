package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewModel.Action;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewModel.AttentionItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewModel.Fact;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewModel.Hearing;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewModel.Tag;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Component
public class JudgeOverviewModelFactory {

    private static final String CLOSED = "Closed";
    private static final String AWAITING_INSTRUCTIONS = "Awaiting instructions";
    private static final String YES = "Yes";
    private static final String TAG_GREY = "govuk-tag govuk-tag--grey";
    private static final String TAG_ORANGE = "govuk-tag govuk-tag--orange";
    private static final String TAG_RED = "govuk-tag govuk-tag--red";
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.UK);
    private static final List<DateTimeFormatter> CASE_DATE_FORMATS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH)
    );
    private static final int URGENT_DAYS = 7;

    private final Clock clock;

    public JudgeOverviewModelFactory() {
        this(Clock.systemDefaultZone());
    }

    JudgeOverviewModelFactory(Clock clock) {
        this.clock = clock;
    }

    public JudgeOverviewModel create(CaseData caseData, long caseReference, CaseState state) {
        LocalDate today = LocalDate.now(clock);
        List<AttentionItem> attentionItems = createAttentionItems(caseData, caseReference, today);
        List<BFActionType> overdueBfActions = findOverdueBfActions(caseData, today);
        Optional<NextHearing> nextHearing = findNextHearing(caseData, today);
        boolean hearingIsImminent = nextHearing
            .map(hearing -> !hearing.date().isAfter(today.plusDays(URGENT_DAYS)))
            .orElse(false);
        boolean warning = !attentionItems.isEmpty() || !overdueBfActions.isEmpty() || hearingIsImminent;

        return new JudgeOverviewModel(
            createCaption(caseData, today),
            createHeading(caseData),
            createTags(caseData, state),
            warning,
            createTodayMessage(attentionItems, overdueBfActions, nextHearing, hearingIsImminent),
            attentionItems,
            !attentionItems.isEmpty(),
            !overdueBfActions.isEmpty(),
            createBfSummary(overdueBfActions),
            nextHearing.map(this::toHearing).orElseGet(this::emptyHearing),
            createKeyFacts(caseData, caseReference),
            createQuickActions(caseData, caseReference, state)
        );
    }

    private List<AttentionItem> createAttentionItems(CaseData caseData, long caseReference, LocalDate today) {
        List<AttentionItem> items = new ArrayList<>();
        emptyIfNull(caseData.getGenericTseApplicationCollection()).stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(GenericTseApplicationTypeItem::getValue)
            .filter(application -> !CLOSED.equalsIgnoreCase(application.getStatus()))
            .map(application -> applicationAttentionItem(application, caseReference, today))
            .forEach(items::add);

        emptyIfNull(caseData.getReferralCollection()).stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(ReferralTypeItem::getValue)
            .filter(referral -> AWAITING_INSTRUCTIONS.equalsIgnoreCase(referral.getReferralStatus()))
            .map(referral -> referralAttentionItem(referral, caseReference))
            .forEach(items::add);

        return items;
    }

    private AttentionItem applicationAttentionItem(GenericTseApplicationType application, long caseReference,
                                                    LocalDate today) {
        Optional<LocalDate> dueDate = parseDate(application.getDueDate());
        String status = "Open";
        String cssClass = TAG_GREY;
        if (dueDate.isPresent() && dueDate.get().isBefore(today)) {
            status = "Overdue";
            cssClass = TAG_RED;
        } else if (dueDate.isPresent() && !dueDate.get().isAfter(today.plusDays(URGENT_DAYS))) {
            status = "Due soon";
            cssClass = TAG_ORANGE;
        }

        return new AttentionItem(
            cssClass,
            status,
            joinNonBlank(" · ", prefix("Application ", application.getNumber()), application.getType()),
            formatDate(application.getDate()),
            dueDate.map(this::formatDate).orElse("No due date"),
            eventUrl(caseReference, "tseAdmin"),
            "Record a decision"
        );
    }

    private AttentionItem referralAttentionItem(ReferralType referral, long caseReference) {
        boolean urgent = YES.equalsIgnoreCase(referral.getIsUrgent());
        return new AttentionItem(
            urgent ? TAG_RED : TAG_GREY,
            urgent ? "Urgent" : "Open",
            joinNonBlank(" · ", prefix("Referral ", referral.getReferralNumber()), referral.getReferralSubject()),
            formatDate(referral.getReferralDate()),
            StringUtils.isBlank(referral.getReferralHearingDate())
                ? "No due date"
                : formatDate(referral.getReferralHearingDate()),
            eventUrl(caseReference, "replyToReferral"),
            "Reply to referral"
        );
    }

    private List<BFActionType> findOverdueBfActions(CaseData caseData, LocalDate today) {
        return emptyIfNull(caseData.getBfActions()).stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(BFActionTypeItem::getValue)
            .filter(action -> !YES.equalsIgnoreCase(action.getCleared()))
            .filter(action -> parseDate(action.getBfDate()).filter(date -> date.isBefore(today)).isPresent())
            .toList();
    }

    private String createBfSummary(List<BFActionType> actions) {
        if (actions.isEmpty()) {
            return "";
        }
        LocalDate oldestDate = actions.stream()
            .map(BFActionType::getBfDate)
            .map(this::parseDate)
            .flatMap(Optional::stream)
            .min(LocalDate::compareTo)
            .orElseThrow();
        String itemWord = actions.size() == 1 ? "action" : "actions";
        return "%d overdue BF %s, oldest due %s".formatted(actions.size(), itemWord, formatDate(oldestDate));
    }

    private Optional<NextHearing> findNextHearing(CaseData caseData, LocalDate today) {
        return emptyIfNull(caseData.getHearingCollection()).stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(HearingTypeItem::getValue)
            .flatMap(hearing -> emptyIfNull(hearing.getHearingDateCollection()).stream()
                .filter(item -> item != null && item.getValue() != null)
                .map(DateListedTypeItem::getValue)
                .map(dateListed -> toNextHearing(hearing, dateListed)))
            .flatMap(Optional::stream)
            .filter(hearing -> !hearing.date().isBefore(today))
            .filter(hearing -> !isInactiveHearing(hearing.status()))
            .min(Comparator.comparing(NextHearing::date));
    }

    private Optional<NextHearing> toNextHearing(HearingType hearing, DateListedType dateListed) {
        return parseDate(dateListed.getListedDate()).map(date -> new NextHearing(
            date,
            defaultText(hearing.getHearingType(), "Hearing"),
            firstNonBlank(
                selectedLabel(dateListed.getHearingVenueDay()),
                selectedLabel(hearing.getHearingVenue()),
                selectedLabel(dateListed.getHearingGlasgow()),
                selectedLabel(dateListed.getHearingAberdeen()),
                selectedLabel(dateListed.getHearingDundee()),
                selectedLabel(dateListed.getHearingEdinburgh()),
                selectedLabel(hearing.getHearingGlasgow()),
                selectedLabel(hearing.getHearingAberdeen()),
                selectedLabel(hearing.getHearingDundee()),
                selectedLabel(hearing.getHearingEdinburgh()),
                hearing.getHearingVenueScotland()
            ),
            selectedLabel(hearing.getJudge()),
            dateListed.getHearingStatus()
        ));
    }

    private boolean isInactiveHearing(String status) {
        return List.of("Postponed", "Vacated", "Settled", "Withdrawn", "Heard").stream()
            .anyMatch(value -> value.equalsIgnoreCase(status));
    }

    private Hearing toHearing(NextHearing hearing) {
        return new Hearing(
            true,
            hearing.type(),
            formatDate(hearing.date()),
            defaultText(hearing.venue(), "Not specified"),
            defaultText(hearing.judge(), "Not allocated")
        );
    }

    private Hearing emptyHearing() {
        return new Hearing(false, "", "", "", "");
    }

    private String createCaption(CaseData caseData, LocalDate today) {
        List<String> parts = new ArrayList<>();
        addIfNotBlank(parts, caseData.getEthosCaseReference());
        parseDate(caseData.getReceiptDate()).ifPresent(receiptDate -> {
            long weeks = Math.max(0, ChronoUnit.WEEKS.between(receiptDate, today));
            parts.add("%d %s in the system".formatted(weeks, weeks == 1 ? "week" : "weeks"));
        });
        return String.join(" · ", parts);
    }

    private String createHeading(CaseData caseData) {
        String claimant = defaultText(caseData.getClaimant(), "Claimant");
        String respondent = defaultText(caseData.getRespondent(), "Respondent");
        return claimant + " v " + respondent;
    }

    private List<Tag> createTags(CaseData caseData, CaseState state) {
        List<Tag> tags = new ArrayList<>();
        if (state != null) {
            tags.add(new Tag("govuk-tag govuk-tag--blue", displayState(state)));
        }
        addTag(tags, caseData.getPositionType());
        addTag(tags, caseData.getConciliationTrack());
        return tags;
    }

    private void addTag(List<Tag> tags, String text) {
        if (StringUtils.isNotBlank(text)) {
            tags.add(new Tag(TAG_GREY, text));
        }
    }

    private String createTodayMessage(List<AttentionItem> attentionItems, List<BFActionType> overdueBfActions,
                                      Optional<NextHearing> nextHearing, boolean hearingIsImminent) {
        List<String> warnings = new ArrayList<>();
        if (!attentionItems.isEmpty()) {
            warnings.add("%d open %s".formatted(attentionItems.size(),
                attentionItems.size() == 1 ? "item needs attention" : "items need attention"));
        }
        if (!overdueBfActions.isEmpty()) {
            warnings.add("%d BF %s overdue".formatted(overdueBfActions.size(),
                overdueBfActions.size() == 1 ? "action is" : "actions are"));
        }
        if (hearingIsImminent) {
            nextHearing.ifPresent(hearing -> warnings.add(
                "%s starts %s".formatted(hearing.type(), relativeDate(hearing.date(), LocalDate.now(clock)))));
        }
        if (!warnings.isEmpty()) {
            return StringUtils.capitalize(String.join("; ", warnings)) + ".";
        }
        return nextHearing
            .map(hearing -> "Nothing needs your attention today. Next hearing: %s on %s."
                .formatted(hearing.type(), formatDate(hearing.date())))
            .orElse("Nothing needs your attention today. No future hearing is listed.");
    }

    private List<Fact> createKeyFacts(CaseData caseData, long caseReference) {
        List<Fact> facts = new ArrayList<>();
        addFact(facts, "ET case number", caseData.getEthosCaseReference());
        addFact(facts, "CCD reference", formatCaseReference(caseReference));
        addFact(facts, "Managing office", firstNonBlank(caseData.getManagingOffice(), caseData.getAllocatedOffice()));
        if (StringUtils.isNotBlank(caseData.getReceiptDate())) {
            addFact(facts, "Received", formatDate(caseData.getReceiptDate()));
        }
        addFact(facts, "Current position", caseData.getPositionType());
        return facts;
    }

    private void addFact(List<Fact> facts, String label, String value) {
        if (StringUtils.isNotBlank(value)) {
            facts.add(new Fact(label, value));
        }
    }

    private List<Action> createQuickActions(CaseData caseData, long caseReference, CaseState state) {
        List<Action> actions = new ArrayList<>();
        if (hasOpenApplications(caseData)) {
            actions.add(new Action(eventUrl(caseReference, "tseAdmin"), "Record a decision"));
        }
        if (hasOpenReferrals(caseData)) {
            actions.add(new Action(eventUrl(caseReference, "replyToReferral"), "Reply to referral"));
        }
        actions.add(new Action(eventUrl(caseReference, "createReferral"), "Create referral"));
        if (state != CaseState.Closed) {
            actions.add(new Action(eventUrl(caseReference, "draftAndSignJudgement"),
                "Draft and sign judgment or order"));
        }
        actions.add(new Action(eventUrl(caseReference, "sendNotification"), "Send a notification"));
        return actions;
    }

    private boolean hasOpenApplications(CaseData caseData) {
        return emptyIfNull(caseData.getGenericTseApplicationCollection()).stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(GenericTseApplicationTypeItem::getValue)
            .anyMatch(application -> !CLOSED.equalsIgnoreCase(application.getStatus()));
    }

    private boolean hasOpenReferrals(CaseData caseData) {
        return emptyIfNull(caseData.getReferralCollection()).stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(ReferralTypeItem::getValue)
            .anyMatch(referral -> AWAITING_INSTRUCTIONS.equalsIgnoreCase(referral.getReferralStatus()));
    }

    private Optional<LocalDate> parseDate(String value) {
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }
        for (DateTimeFormatter formatter : CASE_DATE_FORMATS) {
            try {
                return Optional.of(LocalDate.parse(value, formatter));
            } catch (DateTimeParseException ignored) {
                // Try the next date format used by ET case data.
            }
        }
        try {
            return Optional.of(LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate());
        } catch (DateTimeParseException invalidDate) {
            return Optional.empty();
        }
    }

    private String formatDate(String value) {
        return parseDate(value).map(this::formatDate).orElse(defaultText(value, "Not provided"));
    }

    private String formatDate(LocalDate value) {
        return DISPLAY_DATE.format(value);
    }

    private String relativeDate(LocalDate date, LocalDate today) {
        long days = ChronoUnit.DAYS.between(today, date);
        if (days == 0) {
            return "today";
        }
        if (days == 1) {
            return "tomorrow";
        }
        return "in " + days + " days";
    }

    private String selectedLabel(DynamicFixedListType value) {
        return value == null ? null : value.getSelectedLabel();
    }

    private String eventUrl(long caseReference, String eventId) {
        return "/cases/case-details/%d/trigger/%s/%s1".formatted(caseReference, eventId, eventId);
    }

    private String formatCaseReference(long caseReference) {
        String value = Long.toString(caseReference);
        if (value.length() != 16) {
            return value;
        }
        return "%s-%s-%s-%s".formatted(
            value.substring(0, 4),
            value.substring(4, 8),
            value.substring(8, 12),
            value.substring(12)
        );
    }

    private String displayState(CaseState state) {
        return state.name().replace('_', ' ');
    }

    private String prefix(String prefix, String value) {
        return StringUtils.isBlank(value) ? "" : prefix + value;
    }

    private String joinNonBlank(String delimiter, String... values) {
        return String.join(delimiter, Arrays.stream(values).filter(StringUtils::isNotBlank).toList());
    }

    private String firstNonBlank(String... values) {
        return Arrays.stream(values).filter(StringUtils::isNotBlank).findFirst().orElse(null);
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.defaultIfBlank(value, fallback);
    }

    private void addIfNotBlank(List<String> values, String value) {
        if (StringUtils.isNotBlank(value)) {
            values.add(value);
        }
    }

    private record NextHearing(LocalDate date, String type, String venue, String judge, String status) {
    }
}
