package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.TooManyFields", "PMD.AvoidDuplicateLiterals",
    "PMD.UnnecessaryAnnotationValueElement", "PMD.ExcessivePublicCount", "PMD.ExcessiveClassLength",
    "PMD.GodClass", "PMD.ConfusingTernary", "PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal",
    "PMD.ImplicitSwitchFallThrough", "PMD.ConsecutiveAppendsShouldReuse", "PMD.LawOfDemeter",
    "PMD.CognitiveComplexity", "PMD.AvoidDeeplyNestedIfStmts", "PMD.CyclomaticComplexity"})
public class HearingsHelper {

    public static final String HEARING_CREATION_NUMBER_ERROR = "A new hearing can only "
            + "be added from the List Hearing menu item";
    public static final String HEARING_CREATION_DAY_ERROR = "A new day for a hearing can "
            + "only be added from the List Hearing menu item";
    public static final String HEARING_FINISH_INVALID = "The finish time for a hearing cannot be the "
            + "same or before the start time for %s";
    public static final String HEARING_START_FUTURE = "Start time can't be in future";
    public static final String HEARING_FINISH_FUTURE = "Finish time can't be in future";
    public static final String HEARING_BREAK_FUTURE = "Break time can't be in future";
    public static final String HEARING_RESUME_FUTURE = "Resume time can't be in future";
    public static final String HEARING_BREAK_RESUME_INVALID = "%s contains a hearing with break and "
            + "resume times of 00:00:00. If the hearing had a break then please update the times. If there was no "
            + "break, please remove the hearing date and times from the break and resume fields before continuing.";

    private HearingsHelper() {
    }

    public static String findHearingNumber(CaseData caseData, String hearingDate) {
        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                for (DateListedTypeItem dateListedTypeItem : hearingTypeItem.getValue().getHearingDateCollection()) {
                    String listedDate = dateListedTypeItem.getValue().getListedDate().substring(0, 10);
                    if (listedDate.equals(hearingDate)) {
                        return hearingTypeItem.getValue().getHearingNumber();
                    }
                }
            }
        }
        return null;
    }

    static boolean isHearingStatusPostponed(DateListedType dateListedType) {
        return dateListedType.getHearingStatus() != null
                && dateListedType.getHearingStatus().equals(HEARING_STATUS_POSTPONED);
    }

    public static List<String> hearingMidEventValidation(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getHearingCollection() != null) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                if (hearingTypeItem.getValue().getHearingNumber() == null
                        || hearingTypeItem.getValue().getHearingNumber().isEmpty()) {
                    errors.add(HEARING_CREATION_NUMBER_ERROR);
                    return errors;
                }
                if (hearingTypeItem.getValue().getHearingDateCollection() != null) {
                    for (DateListedTypeItem dateListedTypeItem
                            : hearingTypeItem.getValue().getHearingDateCollection()) {
                        if (dateListedTypeItem.getValue().getListedDate() == null
                                || dateListedTypeItem.getValue().getListedDate().isEmpty()) {
                            errors.add(HEARING_CREATION_DAY_ERROR);
                            return  errors;
                        }
                    }
                }
            }
        }
        return errors;
    }

    public static void updatePostponedDate(CaseData caseData) {
        if (caseData.getHearingCollection() != null) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                if (hearingTypeItem.getValue().getHearingDateCollection() != null) {
                    for (DateListedTypeItem dateListedTypeItem
                            : hearingTypeItem.getValue().getHearingDateCollection()) {
                        DateListedType dateListedType = dateListedTypeItem.getValue();
                        if (isHearingStatusPostponed(dateListedType) && dateListedType.getPostponedDate() == null) {
                            dateListedType.setPostponedDate(UtilHelper.formatCurrentDate2(LocalDate.now()));
                        }
                        if (dateListedType.getPostponedDate() != null
                                &&
                                (!isHearingStatusPostponed(dateListedType)
                                        || dateListedType.getHearingStatus() == null)) {
                            dateListedType.setPostponedDate(null);
                        }
                    }
                }
            }
        }

    }

    public static List<String> hearingTimeValidation(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        String hearingNumber = "Hearing ";
        if (caseData.getHearingDetailsHearing() != null) {
            hearingNumber = caseData.getHearingDetailsHearing().getValue().getLabel().split(",")[0];
        }

        if (CollectionUtils.isNotEmpty(caseData.getHearingDetailsCollection())) {
            for (HearingDetailTypeItem hearingDetailTypeItem : caseData.getHearingDetailsCollection()) {
                HearingDetailType hearingDetailType = hearingDetailTypeItem.getValue();
                if (HEARING_STATUS_HEARD.equals(hearingDetailType.getHearingDetailsStatus())) {
                    checkStartFinishTimes(errors, hearingDetailType,
                            hearingNumber);
                    checkIfDateInFuture(errors, hearingDetailType);
                    checkBreakResumeTimes(errors, hearingDetailType,
                            hearingNumber);
                }
            }
        }

        return errors;
    }

    private static void checkBreakResumeTimes(List<String> errors, HearingDetailType hearingDetailType,
                                              String hearingNumber) {
        LocalTime breakTime = !isNullOrEmpty(hearingDetailType.getHearingDetailsTimingBreak())
                ? LocalDateTime.parse(hearingDetailType.getHearingDetailsTimingBreak()).toLocalTime() : null;
        LocalTime resumeTime = !isNullOrEmpty(hearingDetailType.getHearingDetailsTimingResume())
                ? LocalDateTime.parse(hearingDetailType.getHearingDetailsTimingResume()).toLocalTime() : null;
        LocalTime invalidTime = LocalTime.of(0, 0, 0, 0);
        if (invalidTime.equals(breakTime) || invalidTime.equals(resumeTime)) {
            errors.add(String.format(HEARING_BREAK_RESUME_INVALID, hearingNumber));
        }
    }

    private static void checkIfDateInFuture(List<String> errors, HearingDetailType hearingDetailType) {
        if (isDateInFuture(hearingDetailType.getHearingDetailsTimingStart(), LocalDateTime.now())) {
            errors.add(HEARING_START_FUTURE);
        }
        if (isDateInFuture(hearingDetailType.getHearingDetailsTimingResume(), LocalDateTime.now())) {
            errors.add(HEARING_RESUME_FUTURE);
        }
        if (isDateInFuture(hearingDetailType.getHearingDetailsTimingBreak(), LocalDateTime.now())) {
            errors.add(HEARING_BREAK_FUTURE);
        }
        if (isDateInFuture(hearingDetailType.getHearingDetailsTimingFinish(), LocalDateTime.now())) {
            errors.add(HEARING_FINISH_FUTURE);
        }
    }

    public static boolean isDateInFuture(String date, LocalDateTime now) {
        //Azure times are always in UTC and users enter Europe/London Times,
        // so respective zonedDateTimes should be compared.
        return !isNullOrEmpty(date) && LocalDateTime.parse(date).atZone(ZoneId.of("Europe/London"))
                .isAfter(now.atZone(ZoneId.of("UTC")));
    }

    private static void checkStartFinishTimes(List<String> errors, HearingDetailType hearingDetailType,
                                              String hearingNumber) {
        if (!isNullOrEmpty(hearingDetailType.getHearingDetailsTimingStart())
                && !isNullOrEmpty(hearingDetailType.getHearingDetailsTimingFinish())) {
            LocalDateTime startTime = LocalDateTime.parse(hearingDetailType.getHearingDetailsTimingStart());
            LocalDateTime finishTime = LocalDateTime.parse(hearingDetailType.getHearingDetailsTimingFinish());
            if (!finishTime.isAfter(startTime)) {
                errors.add(String.format(HEARING_FINISH_INVALID, hearingNumber));
            }
        }

    }

    /**
     * This finds the hearing by using the listed date and comparing it to the date provided. It streams through each
     * hearing and then checks whether the date listed is equal to the date provided.
     * @param caseData used to query the hearing collection
     * @param hearingDate date given to find in the collection
     * @return hearingItem which contains the hearing data
     */
    public static HearingType findHearingByListedDate(CaseData caseData, String hearingDate) {
        Optional<HearingTypeItem> hearingTypeItem =
                caseData.getHearingCollection().stream()
                .filter(h -> hearingContainsDate(h.getValue().getHearingDateCollection(), hearingDate))
                .findFirst();
        if (hearingTypeItem.isEmpty()) {
            throw new NotFoundException("Failed to find hearing");
        }
        return hearingTypeItem.get().getValue();
    }

    private static boolean hearingContainsDate(List<DateListedTypeItem> hearingDateCollection, String hearingDate) {
        return hearingDateCollection.stream()
                .anyMatch(dateListedTypeItem -> hearingDate.equals(dateListedTypeItem.getValue().getListedDate()));
    }

    public static String getEarliestFutureHearingDate(List<HearingTypeItem> hearingCollection) {
        if (CollectionUtils.isEmpty(hearingCollection)) {
            return null;
        }

        List<DateListedTypeItem> earliestDatePerHearing = hearingCollection.stream()
            .map(HearingsHelper::mapEarliest)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (earliestDatePerHearing.isEmpty()) {
            return null;
        }

        return Collections.min(earliestDatePerHearing, Comparator.comparing(c -> c.getValue().getListedDate()))
            .getValue().getListedDate();
    }

    private static DateListedTypeItem mapEarliest(HearingTypeItem hearingTypeItem) {
        List<DateListedTypeItem> futureHearings = filterFutureHearings(hearingTypeItem.getValue()
            .getHearingDateCollection());
        if (futureHearings.isEmpty()) {
            return null;
        }
        return Collections.min(futureHearings, Comparator.comparing(c -> c.getValue().getListedDate()));
    }

    private static List<DateListedTypeItem> filterFutureHearings(List<DateListedTypeItem> hearingDateCollection) {
        return hearingDateCollection.stream()
            .filter(d -> isDateInFuture(d.getValue().getListedDate(), LocalDateTime.now())
                    && HEARING_STATUS_LISTED.equals(d.getValue().getHearingStatus()))
            .collect(Collectors.toList());
    }
}
