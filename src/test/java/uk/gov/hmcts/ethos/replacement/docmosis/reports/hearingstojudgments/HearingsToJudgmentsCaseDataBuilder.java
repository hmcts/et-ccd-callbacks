package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.reports.hearingstojudgments.HearingsToJudgmentsCaseData;
import uk.gov.hmcts.ecm.common.model.reports.hearingstojudgments.HearingsToJudgmentsSubmitEvent;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
class HearingsToJudgmentsCaseDataBuilder {

    private final HearingsToJudgmentsCaseData caseData = new HearingsToJudgmentsCaseData();

    public HearingsToJudgmentsCaseDataBuilder withEthosCaseReference(String ethosCaseReference) {
        caseData.setEthosCaseReference(ethosCaseReference);
        return this;
    }

    public HearingsToJudgmentsCaseDataBuilder withManagingOffice(String managingOffice) {
        caseData.setManagingOffice(managingOffice);
        return this;
    }

    public HearingsToJudgmentsCaseDataBuilder withHearing(String listedDate,
                                                          String hearingNumber,
                                                          String hearingStatus,
                                                          String hearingType,
                                                          String disposed) {
        return withHearing(listedDate, hearingStatus, hearingType, disposed, hearingNumber, null, null);
    }

    public HearingsToJudgmentsCaseDataBuilder withHearing(String listedDate, String hearingStatus, String hearingType,
                                                          String disposed, String hearingNumber, String judge,
                                                          String hearingReserved) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        dateListedType.setHearingStatus(hearingStatus);
        dateListedType.setHearingReservedJudgement(hearingReserved);
        dateListedType.setHearingCaseDisposed(disposed);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);

        List<DateListedTypeItem> hearingDates = new ArrayList<>();
        hearingDates.add(dateListedTypeItem);

        HearingType type = new HearingType();
        type.setHearingNumber(hearingNumber);
        type.setHearingType(hearingType);
        type.setJudge(DynamicFixedListType.of(DynamicValueType.create(judge, judge)));
        type.setHearingDateCollection(hearingDates);

        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(type);

        if (caseData.getHearingCollection() == null) {
            caseData.setHearingCollection(new ArrayList<>());
        }
        caseData.getHearingCollection().add(hearingTypeItem);

        return this;
    }

    public HearingsToJudgmentsCaseDataBuilder withJudgment(String judgmentHearingDate, String dateJudgmentMade,
                                                           String dateJudgmentSent) {
        JudgementType judgementType = new JudgementType();
        judgementType.setJudgmentHearingDate(judgmentHearingDate);
        judgementType.setDateJudgmentSent(dateJudgmentSent);
        judgementType.setDateJudgmentMade(dateJudgmentMade);
        JudgementTypeItem judgementTypeItem = new JudgementTypeItem();
        judgementTypeItem.setValue(judgementType);

        if (caseData.getJudgementCollection() == null) {
            caseData.setJudgementCollection(new ArrayList<>());
        }
        caseData.getJudgementCollection().add(judgementTypeItem);

        return this;
    }

    public HearingsToJudgmentsCaseData build() {
        return caseData;
    }

    public HearingsToJudgmentsSubmitEvent buildAsSubmitEvent(String state) {
        HearingsToJudgmentsSubmitEvent submitEvent = new HearingsToJudgmentsSubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(state);

        return submitEvent;
    }
}
