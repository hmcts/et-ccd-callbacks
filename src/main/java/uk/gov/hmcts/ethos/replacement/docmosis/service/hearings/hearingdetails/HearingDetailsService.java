package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.hearingdetails;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;

@Service
public class HearingDetailsService {

    private final HearingSelectionService hearingSelectionService;

    public HearingDetailsService(HearingSelectionService hearingSelectionService) {
        this.hearingSelectionService = hearingSelectionService;
    }

    public void initialiseHearingDetails(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(hearingSelectionService.getHearingDetailsSelection(caseData));
        caseData.setHearingDetailsHearing(dynamicFixedListType);
    }

    public void handleListingSelected(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        List<HearingDetailTypeItem> hearingDetailTypeItemList = new ArrayList<>();
        if (selectedHearing != null && isNotEmpty(selectedHearing.getHearingDateCollection())) {
            selectedHearing.getHearingDateCollection().forEach(dateListedTypeItem -> {
                if (ObjectUtils.isNotEmpty(selectedHearing.getHearingNotesDocument())) {
                    caseData.setDoesHearingNotesDocExist(YES);
                }
                caseData.setUploadHearingNotesDocument(selectedHearing.getHearingNotesDocument());
                hearingDetailTypeItemList.add(getHearingDetailTypeItem(dateListedTypeItem.getValue()));
            });
        }
        if (isNotEmpty(hearingDetailTypeItemList)) {
            caseData.setHearingDetailsCollection(hearingDetailTypeItemList);
        }
    }

    public void updateCase(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        // A collection of hearing update detail entries
        List<HearingDetailTypeItem> hearingDetailTypeItemsList = caseData.getHearingDetailsCollection();
        // The DateListedTypeItems from the currently selected hearing, to which the hearing updates get applied
        List<DateListedTypeItem> dateListedTypeItems = getDateListedItemFromSelectedHearing(caseData);

        if (isNotEmpty(hearingDetailTypeItemsList) && isNotEmpty(dateListedTypeItems)) {
            updatedMatchingHearing(dateListedTypeItems, hearingDetailTypeItemsList);
            setHearingNotesDocument(caseData);
        }
        buildFlagsImageFileName(caseDetails);
    }

    private void setHearingNotesDocument(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        if (selectedHearing != null && caseData.getUploadHearingNotesDocument() != null) {
            selectedHearing.setHearingNotesDocument(isNotEmpty(caseData.getRemoveHearingNotesDocument())
                    ? null
                    : caseData.getUploadHearingNotesDocument());
        }
        caseData.setUploadHearingNotesDocument(null);
        caseData.setRemoveHearingNotesDocument(null);
        caseData.setDoesHearingNotesDocExist(null);
    }

    private void updatedMatchingHearing(List<DateListedTypeItem> dateListedTypeItems,
                                        List<HearingDetailTypeItem> hearingDetailTypeItemsList) {
        dateListedTypeItems.stream().map(DateListedTypeItem::getValue)
            .filter(Objects::nonNull)
            .forEach(dateListedType -> hearingDetailTypeItemsList.stream()
                .map(HearingDetailTypeItem::getValue)
                .filter(hearingDetailType -> hearingDetailType != null
                     && hearingDetailType.getHearingDetailsDate() != null
                     && hearingDetailType.getHearingDetailsDate().equals(dateListedType.getListedDate()))
                .forEach(hearingDetailType -> updateDateListedTypeDetails(dateListedType, hearingDetailType)));
    }

    private List<DateListedTypeItem> getDateListedItemFromSelectedHearing(CaseData caseData) {
        return hearingSelectionService.getDateListedItemsFromSelectedHearing(caseData,
            caseData.getHearingDetailsHearing());
    }

    private HearingType getSelectedHearing(CaseData caseData) {
        return hearingSelectionService.getSelectedHearing(caseData, caseData.getHearingDetailsHearing());
    }

    private void updateDateListedTypeDetails(DateListedType dateListedType, HearingDetailType hearingDetailType) {
        dateListedType.setHearingStatus(hearingDetailType.getHearingDetailsStatus());
        if (HEARING_STATUS_POSTPONED.equals(dateListedType.getHearingStatus())) {
            dateListedType.setPostponedBy(hearingDetailType.getHearingDetailsPostponedBy());
            if (isNullOrEmpty(dateListedType.getPostponedDate())) {
                dateListedType.setPostponedDate(UtilHelper.formatCurrentDate2(LocalDate.now()));
            }
        } else {
            dateListedType.setPostponedBy(null);
            dateListedType.setPostponedDate(null);
        }
        dateListedType.setHearingCaseDisposed(hearingDetailType.getHearingDetailsCaseDisposed());
        dateListedType.setHearingPartHeard(hearingDetailType.getHearingDetailsPartHeard());
        dateListedType.setHearingReservedJudgement(
            hearingDetailType.getHearingDetailsReservedJudgment());
        dateListedType.setAttendeeClaimant(hearingDetailType.getHearingDetailsAttendeeClaimant());
        dateListedType.setAttendeeNonAttendees(
            hearingDetailType.getHearingDetailsAttendeeNonAttendees());
        dateListedType.setAttendeeRespNoRep(hearingDetailType.getHearingDetailsAttendeeRespNoRep());
        dateListedType.setAttendeeRespAndRep(hearingDetailType.getHearingDetailsAttendeeRespAndRep());
        dateListedType.setAttendeeRepOnly(hearingDetailType.getHearingDetailsAttendeeRepOnly());
        dateListedType.setHearingTimingStart(hearingDetailType.getHearingDetailsTimingStart());
        dateListedType.setHearingTimingBreak(hearingDetailType.getHearingDetailsTimingBreak());
        dateListedType.setHearingTimingResume(hearingDetailType.getHearingDetailsTimingResume());
        dateListedType.setHearingTimingFinish(hearingDetailType.getHearingDetailsTimingFinish());
        dateListedType.setHearingTimingDuration(hearingDetailType.getHearingDetailsTimingDuration());
        dateListedType.setHearingNotes2(hearingDetailType.getHearingDetailsHearingNotes2());
    }

    private HearingDetailTypeItem getHearingDetailTypeItem(DateListedType dateListedType) {
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsDate(dateListedType.getListedDate());
        hearingDetailType.setHearingDetailsStatus(dateListedType.getHearingStatus());
        hearingDetailType.setHearingDetailsPostponedBy(dateListedType.getPostponedBy());
        hearingDetailType.setHearingDetailsCaseDisposed(dateListedType.getHearingCaseDisposed());
        hearingDetailType.setHearingDetailsPartHeard(dateListedType.getHearingPartHeard());
        hearingDetailType.setHearingDetailsReservedJudgment(dateListedType.getHearingReservedJudgement());
        hearingDetailType.setHearingDetailsAttendeeClaimant(dateListedType.getAttendeeClaimant());
        hearingDetailType.setHearingDetailsAttendeeNonAttendees(dateListedType.getAttendeeNonAttendees());
        hearingDetailType.setHearingDetailsAttendeeRespNoRep(dateListedType.getAttendeeRespNoRep());
        hearingDetailType.setHearingDetailsAttendeeRespAndRep(dateListedType.getAttendeeRespAndRep());
        hearingDetailType.setHearingDetailsAttendeeRepOnly(dateListedType.getAttendeeRepOnly());
        hearingDetailType.setHearingDetailsTimingStart(dateListedType.getHearingTimingStart());
        hearingDetailType.setHearingDetailsTimingBreak(dateListedType.getHearingTimingBreak());
        hearingDetailType.setHearingDetailsTimingResume(dateListedType.getHearingTimingResume());
        hearingDetailType.setHearingDetailsTimingFinish(dateListedType.getHearingTimingFinish());
        hearingDetailType.setHearingDetailsTimingDuration(dateListedType.getHearingTimingDuration());
        hearingDetailType.setHearingDetailsHearingNotes2(dateListedType.getHearingNotes2());

        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        hearingDetailTypeItem.setValue(hearingDetailType);
        return hearingDetailTypeItem;
    }
}
