package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class CaseDataBuilder {

    private final CaseData caseData = new CaseData();

    public static CaseDataBuilder builder() {
        return new CaseDataBuilder();
    }

    public CaseDataBuilder withEthosCaseReference(String ethosCaseReference) {
        caseData.setEthosCaseReference(ethosCaseReference);
        return this;
    }

    public CaseDataBuilder withSingleCaseType() {
        caseData.setEcmCaseType(SINGLE_CASE_TYPE);
        return this;
    }

    public CaseDataBuilder withMultipleCaseType(String multipleReference) {
        caseData.setEcmCaseType(MULTIPLE_CASE_TYPE);
        caseData.setMultipleReference(multipleReference);
        return this;
    }

    public CaseDataBuilder withCurrentPosition(String currentPosition) {
        caseData.setCurrentPosition(currentPosition);
        return this;
    }

    public CaseDataBuilder withDateToPosition(String dateToPosition) {
        caseData.setDateToPosition(dateToPosition);
        return this;
    }

    public CaseDataBuilder withConciliationTrack(String conciliationTrack) {
        caseData.setConciliationTrack(conciliationTrack);
        return this;
    }

    public CaseDataBuilder withHearing(String hearingNumber, String hearingType, String judge) {
        if (caseData.getHearingCollection() == null) {
            caseData.setHearingCollection(new ArrayList<>());
        }

        var type = new HearingType();
        type.setHearingNumber(hearingNumber);
        type.setHearingType(hearingType);
        type.setJudge(new DynamicFixedListType(judge));

        var hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(type);
        caseData.getHearingCollection().add(hearingTypeItem);

        return this;
    }

    public CaseDataBuilder withHearingSession(int hearingIndex, String number, String listedDate, String hearingStatus,
                                              boolean disposed) {
        var hearing = caseData.getHearingCollection().get(hearingIndex);

        var dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        dateListedType.setHearingStatus(hearingStatus);
        dateListedType.setHearingCaseDisposed(disposed ? YES : NO);
        var dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);

        if (hearing.getValue().getHearingDateCollection() == null) {
            hearing.getValue().setHearingDateCollection(new ArrayList<>());
        }
        var hearingDates = new ArrayList<DateListedTypeItem>();
        hearing.getValue().getHearingDateCollection().add(dateListedTypeItem);

        return this;
    }

    public CaseDataBuilder withJudgment() {
        var judgementType = new JudgementType();
        var judgementTypeItem = new JudgementTypeItem();
        judgementTypeItem.setValue(judgementType);

        if (caseData.getJudgementCollection() == null) {
            caseData.setJudgementCollection(new ArrayList<>());
        }
        caseData.getJudgementCollection().add(judgementTypeItem);

        return this;
    }

    public CaseDataBuilder withPositionType(String positionType) {
        caseData.setPositionType(positionType);
        return this;
    }

    public CaseDataBuilder withManagingOffice(String managingOffice) {
        caseData.setManagingOffice(managingOffice);
        return this;
    }

    public CaseDataBuilder withEccCase(String ethosCaseReference) {
        if (caseData.getEccCases() == null) {
            caseData.setEccCases(new ArrayList<>());
        }
        var eccCases = caseData.getEccCases();
        var eccCase = new EccCounterClaimType();
        eccCase.setCounterClaim(ethosCaseReference);
        var eccCaseItem = new EccCounterClaimTypeItem();
        eccCaseItem.setValue(eccCase);
        eccCases.add(eccCaseItem);

        return this;
    }

    public CaseDataBuilder withCaseTransfer(String officeCT, String reasonForCT) {
        caseData.setOfficeCT(DynamicFixedListType.of(DynamicValueType.create(officeCT, officeCT)));
        caseData.setReasonForCT(reasonForCT);

        return this;
    }

    public CaseDataBuilder withCounterClaim(String counterClaim) {
        caseData.setCounterClaim(counterClaim);
        return this;
    }

    public CaseDataBuilder withBfAction(String cleared) {
        var bfAction = new BFActionType();
        bfAction.setCleared(cleared);
        var bfActionItem = new BFActionTypeItem();
        bfActionItem.setValue(bfAction);

        caseData.setBfActions(List.of(bfActionItem));
        return this;
    }

    public CaseData build() {
        return caseData;
    }

    public SubmitEvent buildAsSubmitEvent(String state) {
        var submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(state);

        return submitEvent;
    }

    public CaseDetails buildAsCaseDetails(String caseTypeId) {
        return buildAsCaseDetails(caseTypeId, null);
    }

    public CaseDetails buildAsCaseDetails(String caseTypeId, String jurisdiction) {
        var caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(caseTypeId);
        caseDetails.setJurisdiction(jurisdiction);
        caseDetails.setCaseData(caseData);

        return caseDetails;
    }
}

