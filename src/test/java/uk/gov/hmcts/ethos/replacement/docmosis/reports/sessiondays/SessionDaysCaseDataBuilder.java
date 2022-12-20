package uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysCaseData;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysSubmitEvent;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;

public class SessionDaysCaseDataBuilder {
    private final SessionDaysCaseData caseData = new SessionDaysCaseData();

    public void withNoHearings() {
        caseData.setHearingCollection(null);
    }

    public void withHearingData(String hearingStatus) {
        List<HearingTypeItem> hearings = new ArrayList<>();
        hearings.add(addHearingSession(hearingStatus, "ftcJudge"));
        hearings.add(addHearingSession(hearingStatus, "ptcJudge"));
        hearings.add(addHearingSession(hearingStatus, ""));
        hearings.add(addHearingSession(hearingStatus, "unknownJudge"));
        caseData.setHearingCollection(hearings);
    }

    private HearingTypeItem addHearingSession(String hearingStatus, String judge) {
        HearingTypeItem item = new HearingTypeItem();
        item.setId(UUID.randomUUID().toString());
        HearingType type = new HearingType();
        type.setHearingSitAlone("Sit Alone");
        type.setHearingFormat(Collections.singletonList("Telephone"));
        type.setHearingType(HEARING_TYPE_JUDICIAL_HEARING);
        if (StringUtils.isNotBlank(judge)) {
            type.setJudge(DynamicFixedListType.of(DynamicValueType.create(judge, judge)));
        }
        type.setHearingNumber("1");
        item.setValue(type);
        DateListedTypeItem dateItem = new DateListedTypeItem();
        dateItem.setId(UUID.randomUUID().toString());
        DateListedType dateType = new DateListedType();
        dateType.setHearingStatus(hearingStatus);
        dateType.setHearingClerk(DynamicFixedListType.of(DynamicValueType.create("Clerk A", "Clerk A")));
        dateType.setListedDate("2022-01-20T11:00:00.000");
        dateType.setHearingTimingStart("2022-01-20T11:00:00.000");
        dateType.setHearingTimingFinish("2022-01-20T17:00:00.000");
        dateType.setHearingTimingBreak("2022-01-20T13:00:00");
        dateType.setHearingTimingResume("2022-01-20T13:30:00.000");
        dateItem.setValue(dateType);
        item.getValue().setHearingDateCollection(Collections.singletonList(dateItem));
        return item;
    }

    public SessionDaysSubmitEvent buildAsSubmitEvent() {
        SessionDaysSubmitEvent submitEvent = new SessionDaysSubmitEvent();
        caseData.setEthosCaseReference("111");
        submitEvent.setCaseData(caseData);
        return submitEvent;
    }
}
