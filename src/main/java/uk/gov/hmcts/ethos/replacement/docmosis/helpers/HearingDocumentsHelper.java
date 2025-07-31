package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.formatCurrentDate;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.getHearingVenue;

public final class HearingDocumentsHelper {

    public static final String HEARING_DOCUMENT_NO_HEARING_ERROR =
        "There are no hearings for this case. List a hearing before uploading a hearing document.";

    private HearingDocumentsHelper() {
        // Helper class should not have public constructors
    }

    /**
     * Populates the hearing details for future and past hearings in the case data.
     *
     * @param caseData the case data containing hearing information
     * @return a list of messages if there are no hearings, otherwise an empty list
     */
    public static List<String> populateHearingDetails(CaseData caseData) {
        if (isEmpty(caseData.getHearingCollection())) {
            return List.of(HEARING_DOCUMENT_NO_HEARING_ERROR);
        } else {
            caseData.setUploadHearingDocumentsSelectFutureHearing(
                    DynamicFixedListType.from(caseData.getHearingCollection().stream()
                            .map(HearingTypeItem::getValue)
                            .map(HearingDocumentsHelper::createFutureHearingDynamicValue)
                            .filter(Objects::nonNull)
                            .toList()
                    )
            );
            caseData.setUploadHearingDocumentsSelectPastHearing(
                    DynamicFixedListType.from(caseData.getHearingCollection().stream()
                            .map(HearingTypeItem::getValue)
                            .map(HearingDocumentsHelper::createPastHearingDynamicValue)
                            .filter(Objects::nonNull)
                            .toList()
                    ));
            return new ArrayList<>();
        }
    }

    private static DynamicValueType createPastHearingDynamicValue(HearingType hearingType) {
        if (isEmpty(hearingType.getHearingDateCollection())) {
            return null;
        }

        if (hearingType.getHearingDateCollection().stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(item -> LocalDateTime.parse(item.getValue().getListedDate()))
            .anyMatch(listedDate -> listedDate.isBefore(LocalDateTime.now()))) {
            return getHearingDynamicValueType(hearingType);
        }
        return null;
    }

    private static DynamicValueType getHearingDynamicValueType(HearingType hearingType) {
        String label = String.format("Hearing %s - %s - %s - %s",
                hearingType.getHearingNumber(),
                hearingType.getHearingType(),
                getHearingVenue(hearingType),
                getHearingDateRange(hearingType));
        return DynamicValueType.create(hearingType.getHearingNumber(), label);
    }

    private static DynamicValueType createFutureHearingDynamicValue(HearingType hearingType) {
        if (isEmpty(hearingType.getHearingDateCollection())) {
            return null;
        }
        return hearingType.getHearingDateCollection().stream()
            .filter(item -> item != null && item.getValue() != null)
            .anyMatch(item ->
                LocalDateTime.parse(item.getValue().getListedDate()).isAfter(LocalDateTime.now())
                && HEARING_STATUS_LISTED.equals(item.getValue().getHearingStatus()))
            ? getHearingDynamicValueType(hearingType)
            : null;
    }

    private static String getHearingDateRange(HearingType hearingType) {
        if (isEmpty(hearingType.getHearingDateCollection())) {
            return "";
        }
        List<LocalDate> hearingDates = hearingType.getHearingDateCollection().stream()
            .filter(item -> item != null && item.getValue() != null)
            .map(item -> LocalDateTime.parse(item.getValue().getListedDate()).toLocalDate())
            .sorted(LocalDate::compareTo)
            .toList();
        if (hearingDates.isEmpty()) {
            return "";
        }
        if (hearingDates.size() == 1) {
            return formatCurrentDate(hearingDates.getFirst());
        }
        return formatCurrentDate(hearingDates.getFirst()) + " - " + formatCurrentDate(hearingDates.getLast());

    }

    /**
     * Gets the selected hearing document based on the case data.
     *
     * @param caseData the case data containing the hearing selection
     * @return the selected hearing document
     */
    public static DynamicValueType getSelectedHearing(CaseData caseData) {
        switch (caseData.getUploadHearingDocumentsSelectPastOrFutureHearing()) {
            case "Past" -> {
                return caseData.getUploadHearingDocumentsSelectPastHearing().getValue();
            }
            case "Future" -> {
                return caseData.getUploadHearingDocumentsSelectFutureHearing().getValue();
            }
            default ->
                throw new IllegalArgumentException("Invalid hearing selection: {}"
                                                   + caseData.getUploadHearingDocumentsSelectPastOrFutureHearing());

        }
    }
}
