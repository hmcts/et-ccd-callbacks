package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import com.google.common.base.Strings;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

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
        return withHearing(hearingNumber, hearingType, judge, null);
    }

    public CaseDataBuilder withHearing(String hearingNumber, String hearingType, String judge, String venue) {
        if (caseData.getHearingCollection() == null) {
            caseData.setHearingCollection(new ArrayList<>());
        }

        HearingTypeItem hearingTypeItem = createHearing(hearingNumber, hearingType, judge, venue);
        caseData.getHearingCollection().add(hearingTypeItem);

        return this;
    }

    public CaseDataBuilder withHearingScotland(String hearingNumber, String hearingType, String judge,
                                               TribunalOffice tribunalOffice, String venue) {
        if (caseData.getHearingCollection() == null) {
            caseData.setHearingCollection(new ArrayList<>());
        }

        HearingTypeItem hearingTypeItem = createHearing(hearingNumber, hearingType, judge, null);
        HearingType hearing = hearingTypeItem.getValue();
        hearing.setHearingVenueScotland(tribunalOffice.getOfficeName());

        DynamicFixedListType dynamicFixedListType = DynamicFixedListType.of(DynamicValueType.create(venue, venue));

        switch (tribunalOffice) {
            case ABERDEEN:
                hearing.setHearingAberdeen(dynamicFixedListType);
                break;
            case DUNDEE:
                hearing.setHearingDundee(dynamicFixedListType);
                break;
            case EDINBURGH:
                hearing.setHearingEdinburgh(dynamicFixedListType);
            case GLASGOW:
                hearing.setHearingGlasgow(dynamicFixedListType);
                break;
            default:
                throw new IllegalArgumentException("Unexpected tribunal office " + tribunalOffice);
        }

        caseData.getHearingCollection().add(hearingTypeItem);

        return this;
    }

    private HearingTypeItem createHearing(String hearingNumber, String hearingType, String judge, String venue) {
        var type = new HearingType();
        type.setHearingNumber(hearingNumber);
        type.setHearingType(hearingType);
        type.setJudge(new DynamicFixedListType(judge));
        if (venue != null) {
            type.setHearingVenue(DynamicFixedListType.of(DynamicValueType.create(venue, venue)));
        }

        var hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(type);

        return hearingTypeItem;
    }

    public CaseDataBuilder withHearingSession(int hearingIndex, String number, String listedDate, String hearingStatus,
                                              boolean disposed) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        dateListedType.setHearingStatus(hearingStatus);
        dateListedType.setHearingCaseDisposed(disposed ? YES : NO);

        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);

        HearingTypeItem hearing = caseData.getHearingCollection().get(hearingIndex);
        if (hearing.getValue().getHearingDateCollection() == null) {
            hearing.getValue().setHearingDateCollection(new ArrayList<>());
        }

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

    public CaseDataBuilder withEcmOfficeCT(String ecmOfficeCT, String reasonForCT) {
        caseData.setEcmOfficeCT(ecmOfficeCT);
        caseData.setReasonForCT(reasonForCT);
        return this;
    }

    public CaseDataBuilder withClaimServedDate(String claimServedDate) {
        caseData.setClaimServedDate(claimServedDate);
        return this;
    }

    public CaseDataBuilder withClaimantIndType(String claimantFirstNames, String claimantLastName) {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames(claimantFirstNames);
        claimantIndType.setClaimantLastName(claimantLastName);
        caseData.setClaimantIndType(claimantIndType);
        return this;
    }

    public CaseDataBuilder withClaimantType(String addressLine1, String addressLine2, String addressLine3,
                                            String postTown, String postCode, String country) {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(
                createAddress(addressLine1, addressLine2, addressLine3, postTown, null, postCode, country));
        caseData.setClaimantType(claimantType);
        return this;
    }

    public CaseDataBuilder withClaimantWorkAddress(String addressLine1, String addressLine2, String addressLine3,
                                                   String postTown, String postCode, String country) {
        ClaimantWorkAddressType claimantWorkAddress = new ClaimantWorkAddressType();
        claimantWorkAddress.setClaimantWorkAddress(
            createAddress(addressLine1, addressLine2, addressLine3, postTown, null, postCode, country)
        );
        caseData.setClaimantWorkAddress(claimantWorkAddress);
        return this;
    }

    public CaseDataBuilder withRespondent(RespondentSumType respondent) {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondent);

        if (caseData.getRespondentCollection() == null) {
            caseData.setRespondentCollection(new ArrayList<>());
        }

        caseData.getRespondentCollection().add(respondentSumTypeItem);
        return this;
    }

    public CaseDataBuilder withRespondent(String respondent, String responseReceived, String receivedDate,
                                          boolean extension) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(respondent);
        respondentSumType.setResponseReceived(responseReceived);
        respondentSumType.setResponseReceivedDate(receivedDate);
        if (extension) {
            respondentSumType.setExtensionRequested(YES);
            respondentSumType.setExtensionGranted(YES);
            respondentSumType.setExtensionDate("2022-03-01");
        }

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        if (caseData.getRespondentCollection() == null) {
            caseData.setRespondentCollection(new ArrayList<>());
        }
        caseData.getRespondentCollection().add(respondentSumTypeItem);
        return this;
    }

    public CaseDataBuilder withRespondentWithAddress(String respondentName, String addressLine1, String addressLine2,
                                                     String addressLine3, String postTown, String postCode,
                                                     String country, String responseAcas) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(respondentName);

        respondentSumType.setRespondentAddress(
                createAddress(addressLine1, addressLine2, addressLine3, postTown, null, postCode, country));

        if (!Strings.isNullOrEmpty(responseAcas)) {
            respondentSumType.setRespondentACAS(responseAcas);
        }

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        if (caseData.getRespondentCollection() == null) {
            caseData.setRespondentCollection(new ArrayList<>());
        }
        caseData.getRespondentCollection().add(respondentSumTypeItem);
        return this;
    }

    private Address createAddress(String addressLine1, String addressLine2, String addressLine3,
                                  String postTown, String county, String postCode, String country) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        if (!Strings.isNullOrEmpty(addressLine2)) {
            address.setAddressLine2(addressLine2);
        }
        if (!Strings.isNullOrEmpty(addressLine3)) {
            address.setAddressLine3(addressLine3);
        }
        address.setPostTown(postTown);
        if (!Strings.isNullOrEmpty(county)) {
            address.setCounty(county);
        }
        address.setCounty(county);
        address.setPostCode(postCode);
        address.setCountry(country);
        return address;
    }

    public CaseDataBuilder withChooseEt3Respondent(String respondentName) {
        DynamicValueType respondent = DynamicValueType.create(respondentName, respondentName);
        DynamicFixedListType respondentList = new DynamicFixedListType();
        respondentList.setListItems(List.of(respondent));
        respondentList.setValue(respondent);
        caseData.setEt3ChooseRespondent(respondentList);
        return this;
    }
}

