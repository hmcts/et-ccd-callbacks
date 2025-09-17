package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.lang3.ObjectUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;

public final class HearingsHelper {

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
    private static final String TWO_JUDGES = "Two Judges";
    public static final String BREAK_TIME_VALIDATION_MESSAGE =
            "%s break time must be after the start time and "
        + "before resume time.";
    public static final String RESUME_TIME_VALIDATION_MESSAGE =
            "%s resume time must be after the break time and before finish time.";

    private HearingsHelper() {
    }

    public static String findHearingNumber(CaseData caseData, String hearingDate) {
        if (isEmpty(caseData.getHearingCollection())) {
            return null;
        }
        for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
            for (DateListedTypeItem dateListedTypeItem : hearingTypeItem.getValue().getHearingDateCollection()) {
                String listedDate = dateListedTypeItem.getValue().getListedDate().substring(0, 10);
                if (listedDate.equals(hearingDate)) {
                    return hearingTypeItem.getValue().getHearingNumber();
                }
            }
        }
        return null;
    }

    public static List<String> hearingMidEventValidation(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getHearingCollection() != null) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                findHearingNumberErrors(errors, hearingTypeItem);
                if (!errors.isEmpty()) {
                    return errors;
                }
                findHearingDayErrors(errors, hearingTypeItem);
                if (!errors.isEmpty()) {
                    return errors;
                }
            }
        }
        return errors;
    }

    private static void findHearingNumberErrors(List<String> errors, HearingTypeItem hearingTypeItem) {
        if (isNullOrEmpty(hearingTypeItem.getValue().getHearingNumber())) {
            errors.add(HEARING_CREATION_NUMBER_ERROR);
        }
    }

    private static void findHearingDayErrors(List<String> errors, HearingTypeItem hearingTypeItem) {
        if (isEmpty(hearingTypeItem.getValue().getHearingDateCollection())) {
            return;
        }
        for (DateListedTypeItem dateListedTypeItem : hearingTypeItem.getValue().getHearingDateCollection()) {
            if (isNullOrEmpty(dateListedTypeItem.getValue().getListedDate())) {
                errors.add(HEARING_CREATION_DAY_ERROR);
            }
        }
    }

    public static List<String> hearingTimeValidation(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        String hearingNumber = "Hearing ";
        if (caseData.getHearingDetailsHearing() != null) {
            hearingNumber = caseData.getHearingDetailsHearing().getValue().getLabel().split(",")[0];
        }

        if (isEmpty(caseData.getHearingDetailsCollection())) {
            return errors;
        }

        for (HearingDetailTypeItem hearingDetailTypeItem : caseData.getHearingDetailsCollection()) {
            HearingDetailType hearingDetailType = hearingDetailTypeItem.getValue();
            if (HEARING_STATUS_HEARD.equals(hearingDetailType.getHearingDetailsStatus())) {
                checkHearingTimes(errors, hearingDetailType, hearingNumber);
                checkIfDateInFuture(errors, hearingDetailType);
            }
        }

        return errors;
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

    private static void checkHearingTimes(List<String> errors, HearingDetailType hearingDetailType,
                                          String hearingNumber) {
        if (isNullOrEmpty(hearingDetailType.getHearingDetailsTimingStart())
            || isNullOrEmpty(hearingDetailType.getHearingDetailsTimingFinish())) {
            return;
        }
        LocalDateTime startTime = LocalDateTime.parse(hearingDetailType.getHearingDetailsTimingStart());
        LocalDateTime finishTime = LocalDateTime.parse(hearingDetailType.getHearingDetailsTimingFinish());
        if (!finishTime.isAfter(startTime)) {
            errors.add(String.format(HEARING_FINISH_INVALID, hearingNumber));
        }

        validateBreakResumeTimes(errors, hearingDetailType, hearingNumber, startTime, finishTime);

    }

    private static void validateBreakResumeTimes(List<String> errors, HearingDetailType hearingDetailType,
                                                 String hearingNumber, LocalDateTime startTime,
                                                 LocalDateTime finishTime) {
        String timingBreak = hearingDetailType.getHearingDetailsTimingBreak();
        LocalDateTime breakTime = isNullOrEmpty(timingBreak) ? null :
                LocalDateTime.parse(timingBreak);

        String timingResume = hearingDetailType.getHearingDetailsTimingResume();
        LocalDateTime resumeTime = isNullOrEmpty(timingResume) ? null :
                LocalDateTime.parse(timingResume);

        if (breakTime == null || resumeTime == null) {
            return;
        }

        LocalTime invalidTime = LocalTime.of(0, 0, 0, 0);
        if (invalidTime.equals(breakTime.toLocalTime()) || invalidTime.equals(resumeTime.toLocalTime())) {
            errors.add(String.format(HEARING_BREAK_RESUME_INVALID, hearingNumber));
            return;
        }

        if (!breakTime.isAfter(startTime) || !breakTime.isBefore(resumeTime)) {
            errors.add(String.format(BREAK_TIME_VALIDATION_MESSAGE, hearingNumber));
        }
        if (!resumeTime.isAfter(breakTime) || !resumeTime.isBefore(finishTime)) {
            errors.add(String.format(RESUME_TIME_VALIDATION_MESSAGE, hearingNumber));
        }

    }

    /**
     * This finds the hearing by using the listed date and comparing it to the date provided. It streams through each
     * hearing and then checks whether the date listed is equal to the date provided.
     *
     * @param caseData    used to query the hearing collection
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
        if (isEmpty(hearingCollection)) {
            return null;
        }

        List<DateListedTypeItem> earliestDatePerHearing = hearingCollection.stream()
            .map(HearingsHelper::mapEarliest)
            .filter(Objects::nonNull)
            .toList();

        if (earliestDatePerHearing.isEmpty()) {
            return null;
        }

        return Collections.min(earliestDatePerHearing, Comparator.comparing(c -> c.getValue().getListedDate()))
                .getValue().getListedDate();
    }

    public static DateListedTypeItem mapEarliest(HearingTypeItem hearingTypeItem) {
        HearingType hearingType = hearingTypeItem.getValue();
        List<DateListedTypeItem> futureHearings = filterFutureHearings(hearingType.getHearingDateCollection());
        if (futureHearings.isEmpty()) {
            return null;
        }
        return Collections.min(futureHearings, Comparator.comparing(c -> c.getValue().getListedDate()));
    }

    private static List<DateListedTypeItem> filterFutureHearings(List<DateListedTypeItem> hearingDateCollection) {
        return hearingDateCollection.stream()
            .filter(d -> isDateInFuture(d.getValue().getListedDate(), LocalDateTime.now())
                    && HEARING_STATUS_LISTED.equals(d.getValue().getHearingStatus()))
            .toList();
    }

    public static String getHearingVenue(HearingType hearing) {
        if (hearing.getHearingVenueScotland() != null) {
            return hearing.getHearingVenueScotland();
        }
        if (ObjectUtils.isEmpty(hearing.getHearingVenue())) {
            return EMPTY_STRING;
        }
        return defaultIfEmpty(hearing.getHearingVenue().getSelectedLabel(), EMPTY_STRING);
    }

    public static List<String> validateTwoJudges(CaseData caseData) {
        if (TWO_JUDGES.equals(caseData.getAllocateHearingSitAlone())
            && ObjectUtils.isNotEmpty(caseData.getAllocateHearingJudge())
            && ObjectUtils.isNotEmpty(caseData.getAllocateHearingAdditionalJudge())
            && !isNullOrEmpty(caseData.getAllocateHearingJudge().getSelectedCode())
            && !isNullOrEmpty(caseData.getAllocateHearingAdditionalJudge().getSelectedCode())
            && caseData.getAllocateHearingAdditionalJudge().getSelectedCode()
                    .equals(caseData.getAllocateHearingJudge().getSelectedCode())) {
            return List.of("Please choose a different judge for the second judge as the same judge has been selected "
                           + "for both judges");
        }

        return new ArrayList<>();
    }
}
