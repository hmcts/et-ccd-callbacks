package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.hearingdetails;

import static com.google.common.base.Strings.isNullOrEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

@Service
public class HearingDetailsService {

    private final HearingSelectionService hearingSelectionService;

    public HearingDetailsService(HearingSelectionService hearingSelectionService) {
        this.hearingSelectionService = hearingSelectionService;
    }

    public void initialiseHearingDetails(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(hearingSelectionService.getHearingSelection(caseData));
        caseData.setHearingDetailsHearing(dynamicFixedListType);
    }

    public void handleListingSelected(CaseData caseData) {

        HearingType selectedHearing = getSelectedHearing(caseData);
        List<HearingDetailTypeItem> hearingDetailTypeItemList = new ArrayList<>();
        if (selectedHearing != null && !CollectionUtils.isEmpty(selectedHearing.getHearingDateCollection())) {
            for (DateListedTypeItem dateListedTypeItem : selectedHearing.getHearingDateCollection()) {
                DateListedType dateListedType = dateListedTypeItem.getValue();
                HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
                HearingDetailType hearingDetailType = new HearingDetailType();
                hearingDetailType.setHearingDetailsDate(dateListedType.getListedDate());
                hearingDetailType.setHearingDetailsStatus(nonNull(dateListedType.getHearingStatus()));
                hearingDetailType.setHearingDetailsPostponedBy(nonNull(dateListedType.getPostponedBy()));
                hearingDetailType.setHearingDetailsCaseDisposed(nonNull(dateListedType.getHearingCaseDisposed()));
                hearingDetailType.setHearingDetailsPartHeard(nonNull(dateListedType.getHearingPartHeard()));
                hearingDetailType.setHearingDetailsReservedJudgment(nonNull(dateListedType.getHearingReservedJudgement()));
                hearingDetailType.setHearingDetailsAttendeeClaimant(nonNull(dateListedType.getAttendeeClaimant()));
                hearingDetailType.setHearingDetailsAttendeeNonAttendees(nonNull(dateListedType.getAttendeeNonAttendees()));
                hearingDetailType.setHearingDetailsAttendeeRespNoRep(nonNull(dateListedType.getAttendeeRespNoRep()));
                hearingDetailType.setHearingDetailsAttendeeRespAndRep(nonNull(dateListedType.getAttendeeRespAndRep()));
                hearingDetailType.setHearingDetailsAttendeeRepOnly(nonNull(dateListedType.getAttendeeRepOnly()));
                hearingDetailType.setHearingDetailsTimingStart(nonNull(dateListedType.getHearingTimingStart()));
                hearingDetailType.setHearingDetailsTimingBreak(dateListedType.getHearingTimingBreak());
                hearingDetailType.setHearingDetailsTimingResume(dateListedType.getHearingTimingResume());
                hearingDetailType.setHearingDetailsTimingFinish(nonNull(dateListedType.getHearingTimingFinish()));
                hearingDetailType.setHearingDetailsTimingDuration(nonNull(dateListedType.getHearingTimingDuration()));
                hearingDetailType.setHearingDetailsHearingNotes2(nonNull(dateListedType.getHearingNotes2()));
                hearingDetailTypeItem.setId(UUID.randomUUID().toString());
                hearingDetailTypeItem.setValue(hearingDetailType);
                hearingDetailTypeItemList.add(hearingDetailTypeItem);
            }
        }
        if (!CollectionUtils.isEmpty(hearingDetailTypeItemList)) {
            caseData.setHearingDetailsCollection(hearingDetailTypeItemList);
        }
    }

    private String nonNull(String value) {
        return isNullOrEmpty(value) ? " " : value;
    }

    public void updateCase(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        HearingType selectedHearing = getSelectedHearing(caseData);
        if (!CollectionUtils.isEmpty(selectedHearing.getHearingDateCollection())) {
            for (DateListedTypeItem dateListedTypeItem : selectedHearing.getHearingDateCollection()) {
                DateListedType dateListedType = dateListedTypeItem.getValue();
                for (HearingDetailTypeItem hearingDetailTypeItem : caseData.getHearingDetailsCollection()) {
                    HearingDetailType hearingDetailType = hearingDetailTypeItem.getValue();
                    if (hearingDetailType.getHearingDetailsDate().equals(dateListedType.getListedDate())) {
                        dateListedType.setHearingStatus(hearingDetailType.getHearingDetailsStatus());
                        dateListedType.setPostponedBy(hearingDetailType.getHearingDetailsPostponedBy());
                        dateListedType.setHearingCaseDisposed(hearingDetailType.getHearingDetailsCaseDisposed());
                        dateListedType.setHearingPartHeard(hearingDetailType.getHearingDetailsPartHeard());
                        dateListedType.setHearingReservedJudgement(hearingDetailType.getHearingDetailsReservedJudgment());
                        dateListedType.setAttendeeClaimant(hearingDetailType.getHearingDetailsAttendeeClaimant());
                        dateListedType.setAttendeeNonAttendees(hearingDetailType.getHearingDetailsAttendeeNonAttendees());
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
                }
            }
        }
        Helper.updatePostponedDate(caseData);
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
    }

    private HearingType getSelectedHearing(CaseData caseData) {
        return hearingSelectionService.getSelectedHearing(caseData, caseData.getHearingDetailsHearing());
    }

}
