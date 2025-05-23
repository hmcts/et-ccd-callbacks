package uk.gov.hmcts.ethos.utils;

import com.google.common.base.Strings;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DynamicListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
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

    public CaseDataBuilder withHearing(String hearingNumber, String hearingType, String judge,
                                       List<String> hearingFormat, String hearingLengthNum,
                                       String hearingLengthType, String hearingSitAlone) {
        return withHearing(hearingNumber, hearingType, judge, null, hearingFormat, hearingLengthNum,
                hearingLengthType, hearingSitAlone);
    }

    public CaseDataBuilder withHearing(String hearingNumber, String hearingType, String judge, String venue,
                                       List<String> hearingFormat, String hearingLengthNum, String hearingLengthType,
                                       String hearingSitAlone) {
        if (caseData.getHearingCollection() == null) {
            caseData.setHearingCollection(new ArrayList<>());
        }

        HearingTypeItem hearingTypeItem = createHearing(hearingNumber, hearingType, judge, venue, hearingFormat,
                hearingLengthNum, hearingLengthType, hearingSitAlone);
        caseData.getHearingCollection().add(hearingTypeItem);

        return this;
    }

    public CaseDataBuilder withHearingScotland(String hearingNumber, String hearingType, String judge,
                                               TribunalOffice tribunalOffice, String venue) {
        if (caseData.getHearingCollection() == null) {
            caseData.setHearingCollection(new ArrayList<>());
        }

        HearingTypeItem hearingTypeItem = createHearing(hearingNumber, hearingType, judge, null, null, null,
                null, null);
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
                break;
            case GLASGOW:
                hearing.setHearingGlasgow(dynamicFixedListType);
                break;
            default:
                throw new IllegalArgumentException("Unexpected tribunal office " + tribunalOffice);
        }

        caseData.getHearingCollection().add(hearingTypeItem);

        return this;
    }

    private HearingTypeItem createHearing(String hearingNumber, String hearingType, String judge, String venue,
                                          List<String> hearingFormat, String hearingLengthNum, String hearingLengthType,
                                          String hearingSitAlone) {
        HearingType type = new HearingType();
        type.setHearingNumber(hearingNumber);
        type.setHearingType(hearingType);
        type.setJudge(new DynamicFixedListType(judge));
        if (venue != null) {
            type.setHearingVenue(DynamicFixedListType.of(DynamicValueType.create(venue, venue)));
        }
        type.setHearingFormat(hearingFormat);
        type.setHearingEstLengthNum(hearingLengthNum);
        type.setHearingEstLengthNumType(hearingLengthType);
        type.setHearingSitAlone(hearingSitAlone);

        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(type);

        return hearingTypeItem;
    }

    public CaseDataBuilder withHearingSession(int hearingIndex, String listedDate, String hearingStatus,
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
        JudgementType judgementType = new JudgementType();
        JudgementTypeItem judgementTypeItem = new JudgementTypeItem();
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
        List<EccCounterClaimTypeItem> eccCases = caseData.getEccCases();
        EccCounterClaimType eccCase = new EccCounterClaimType();
        eccCase.setCounterClaim(ethosCaseReference);
        EccCounterClaimTypeItem eccCaseItem = new EccCounterClaimTypeItem();
        eccCaseItem.setValue(eccCase);
        eccCases.add(eccCaseItem);

        return this;
    }

    public CaseDataBuilder withClaimantRepresentedQuestion(String claimantRepresentedQuestion) {
        caseData.setClaimantRepresentedQuestion(claimantRepresentedQuestion);
        return this;
    }

    public CaseDataBuilder withClaimant(String claimant) {
        caseData.setClaimant(claimant);
        return this;
    }

    public CaseDataBuilder withClaimantHearingPreference(String preference) {
        ClaimantHearingPreference claimantHearingPreference = new ClaimantHearingPreference();
        claimantHearingPreference.setContactLanguage(preference);
        caseData.setClaimantHearingPreference(claimantHearingPreference);
        return this;
    }

    public CaseDataBuilder withCaseTransfer(String officeCT, String reasonForCT) {
        caseData.setOfficeCT(DynamicFixedListType.of(DynamicValueType.create(officeCT, officeCT)));
        caseData.setReasonForCT(reasonForCT);

        return this;
    }

    public CaseDataBuilder withCaseLinks(ListTypeItem<CaseLink> caseLinks) {
        caseData.setCaseLinks(caseLinks);
        return this;
    }

    public CaseDataBuilder withCounterClaim(String counterClaim) {
        caseData.setCounterClaim(counterClaim);
        return this;
    }

    public CaseDataBuilder withBfAction(String cleared) {
        BFActionType bfAction = new BFActionType();
        bfAction.setCleared(cleared);
        BFActionTypeItem bfActionItem = new BFActionTypeItem();
        bfActionItem.setValue(bfAction);

        caseData.setBfActions(List.of(bfActionItem));
        return this;
    }

    public CaseData build() {
        return caseData;
    }

    public SubmitEvent buildAsSubmitEvent(String state) {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(state);

        return submitEvent;
    }

    public CaseDetails buildAsCaseDetails(String caseTypeId) {
        return buildAsCaseDetails(caseTypeId, null);
    }

    public CaseDetails buildAsCaseDetails(String caseTypeId, String jurisdiction) {
        CaseDetails caseDetails = new CaseDetails();
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

    /**
     * Add a ClaimantIndType with a first name, last name, title and preferred title.
     */
    public CaseDataBuilder withClaimantIndType(String firstName, String lastName, String title, String preferredTitle) {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames(firstName);
        claimantIndType.setClaimantLastName(lastName);
        claimantIndType.setClaimantTitle(title);
        claimantIndType.setClaimantPreferredTitle(preferredTitle);
        caseData.setClaimantIndType(claimantIndType);
        return this;
    }

    /**
     * Add a RepresentativeClaimantType with a name and email.
     */
    public CaseDataBuilder withRepresentativeClaimantType(String name, String email) {
        RepresentedTypeC rep = new RepresentedTypeC();
        rep.setNameOfRepresentative(name);
        rep.setRepresentativeEmailAddress(email);
        rep.setMyHmctsOrganisation(Organisation.builder()
                .organisationName("ClaimantOrg")
                .organisationID(UUID.randomUUID().toString())
                .build());
        caseData.setRepresentativeClaimantType(rep);
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

    /**
     * Add a ClaimantType with an email.
     */
    public CaseDataBuilder withClaimantType(String email) {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(email);
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

    public CaseDataBuilder withRespondent(String respondentName, String responseReceived, String receivedDate,
                                          String respondentEmail, boolean extension) {
        withRespondent(respondentName, responseReceived, receivedDate, extension);
        RespondentSumTypeItem respondentSumTypeItem = caseData.getRespondentCollection()
                .get(caseData.getRespondentCollection().size() - 1);
        respondentSumTypeItem.getValue().setRespondentEmail(respondentEmail);
        return this;

    }

    public CaseDataBuilder withRespondent(String respondentName, String responseReceived, String receivedDate,
                                          boolean extension) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(respondentName);
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
            respondentSumType.setRespondentAcas(responseAcas);
        }

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        if (caseData.getRespondentCollection() == null) {
            caseData.setRespondentCollection(new ArrayList<>());
        }
        caseData.getRespondentCollection().add(respondentSumTypeItem);
        return this;
    }

    /**
     * Add a respondent with an address and email.
     */
    public CaseDataBuilder withRespondentWithAddress(String respondentName, String addressLine1, String addressLine2,
                                                     String addressLine3, String postTown, String postCode,
                                                     String country, String responseAcas, String email) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(respondentName);
        respondentSumType.setRespondentEmail(email);
        respondentSumType.setRespondentAddress(
                createAddress(addressLine1, addressLine2, addressLine3, postTown, null, postCode, country));

        if (!Strings.isNullOrEmpty(responseAcas)) {
            respondentSumType.setRespondentAcas(responseAcas);
        }

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        if (caseData.getRespondentCollection() == null) {
            caseData.setRespondentCollection(new ArrayList<>());
        }
        caseData.getRespondentCollection().add(respondentSumTypeItem);
        return this;
    }

    /**
     * Add a respondent representative with names and an email.
     */
    public CaseDataBuilder withRespondentRepresentative(String respondentName, String repName, String email) {
        RepresentedTypeR item = RepresentedTypeR.builder()
                .respondentId("1234")
                .respRepName(respondentName)
                .nameOfRepresentative(repName)
                .representativeEmailAddress(email)
                .myHmctsYesNo(YES)
                .respondentOrganisation(Organisation.builder()
                        .organisationID("1234")
                        .organisationName("Respondent Org")
                        .build())
                .build();
        RepresentedTypeRItem itemType = new RepresentedTypeRItem();
        itemType.setValue(item);
        if (CollectionUtils.isEmpty(caseData.getRepCollection())) {
            caseData.setRepCollection(new ArrayList<>());
        }
        caseData.getRepCollection().add(itemType);
        return this;
    }

    /**
     * Add two respondent representatives with org ids and an emails.
     */
    public CaseDataBuilder withTwoRespondentRepresentative(String org1Id, String org2Id,
                                                           String rep1Email, String rep2Email) {

        RepresentedTypeR rep1 = RepresentedTypeR.builder()
                .respondentOrganisation(Organisation.builder().organisationID(org1Id).build())
                .respRepName("res1 name")
                .nameOfRepresentative("rep1 name")
                .representativeEmailAddress(rep1Email).build();
        RepresentedTypeR rep2 = RepresentedTypeR.builder()
                .respondentOrganisation(Organisation.builder().organisationID(org2Id).build())
                .respRepName("res2 name")
                .nameOfRepresentative("rep2 name")
                .representativeEmailAddress(rep2Email).build();
        RepresentedTypeRItem itemType1 = new RepresentedTypeRItem();
        itemType1.setValue(rep1);
        RepresentedTypeRItem itemType2 = new RepresentedTypeRItem();
        itemType2.setValue(rep2);
        if (CollectionUtils.isEmpty(caseData.getRepCollection())) {
            caseData.setRepCollection(new ArrayList<>());
        }
        caseData.getRepCollection().add(itemType1);
        caseData.getRepCollection().add(itemType2);
        return this;
    }

    /**
     * Creates an Address object from its properties.
     */
    public Address createAddress(String addressLine1, String addressLine2, String addressLine3,
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

    public CaseDataBuilder withChangeOrganisationRequestField(Organisation organisationToAdd,
                                                              Organisation organisationToRemove,
                                                              LocalDateTime requestTimestamp,
                                                              ChangeOrganisationApprovalStatus approvalStatus) {
        DynamicValueType caseRoleIDValue = DynamicValueType.create("[SOLICITORA]", "Respondent Solicitor");

        DynamicFixedListType caseRoleIDList = new DynamicFixedListType();
        caseRoleIDList.setValue(caseRoleIDValue);
        caseRoleIDList.setListItems(List.of(caseRoleIDValue));

        ChangeOrganisationRequest cor = ChangeOrganisationRequest.builder()
                .organisationToAdd(organisationToAdd)
                .organisationToRemove(organisationToRemove)
                .caseRoleId(caseRoleIDList)
                .requestTimestamp(requestTimestamp)
                .approvalStatus(approvalStatus)
                .build();

        caseData.setChangeOrganisationRequestField(cor);
        return this;
    }

    public CaseDataBuilder withAssignOffice(String selectedOffice) {
        List<DynamicValueType> tribunalOffices = TribunalOffice.ENGLANDWALES_OFFICES.stream()
                .map(tribunalOffice ->
                        DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                .toList();
        caseData.setAssignOffice(DynamicFixedListType.from(tribunalOffices));
        caseData.getAssignOffice().setValue(DynamicValueType.create(selectedOffice, selectedOffice));
        return this;
    }

    public CaseDataBuilder withEt3RepresentingRespondent(String respondentName) {
        DynamicValueType respondent = DynamicValueType.create(respondentName, respondentName);
        DynamicFixedListType dynamicFixedListType = DynamicFixedListType.from(List.of(respondent));
        dynamicFixedListType.setValue(respondent);
        DynamicListType dynamicListType = new DynamicListType();
        dynamicListType.setDynamicList(dynamicFixedListType);
        DynamicListTypeItem dynamicListTypeItem = new DynamicListTypeItem();
        dynamicListTypeItem.setValue(dynamicListType);
        caseData.setEt3RepresentingRespondent(List.of(dynamicListTypeItem));
        return this;
    }

    public CaseDataBuilder withSubmitEt3Respondent(String respondent) {
        caseData.setSubmitEt3Respondent(DynamicFixedListType.of(DynamicValueType.create(respondent, respondent)));
        caseData.getSubmitEt3Respondent().setValue(DynamicValueType.create(respondent, respondent));
        return this;
    }

    public CaseDataBuilder withDocumentCollection(String docType) {
        if (caseData.getDocumentCollection() == null) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename("test.pdf");
        uploadedDocumentType.setDocumentBinaryUrl("http://dummy.link/documents/11111111-1111-1111-1111-111111111111/binary");
        uploadedDocumentType.setDocumentUrl("http://dummy.link/documents/11111111-1111-1111-1111-111111111111");
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(docType);
        documentType.setUploadedDocument(uploadedDocumentType);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        documentTypeItem.setId(UUID.randomUUID().toString());
        caseData.getDocumentCollection().add(documentTypeItem);
        return this;
    }

    public CaseDataBuilder withGenericTseApplicationTypeItem(String tseApplicant, String tseDate) {
        GenericTseApplicationTypeItem item = new GenericTseApplicationTypeItem();
        GenericTseApplicationType genericTseApplicationType = new GenericTseApplicationType();
        genericTseApplicationType.setApplicant(tseApplicant);
        genericTseApplicationType.setDate(tseDate);
        item.setValue(genericTseApplicationType);
        caseData.setGenericTseApplicationCollection(List.of(item));
        return this;
    }

    public CaseDataBuilder withCaseSource(String caseSource) {
        caseData.setCaseSource(caseSource);
        return this;
    }

    public static Address createGenericAddress() {
        Address address = new Address();
        address.setAddressLine1("Line 1");
        address.setAddressLine2("Line 2");
        address.setAddressLine3("Line 3");
        address.setPostTown("Town");
        address.setPostCode("Postcode");
        address.setCountry("Country");
        return address;
    }

    public CaseDataBuilder withNotification(String title, String type) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            caseData.setSendNotificationCollection(new ArrayList<>());
        }
        SendNotificationType sendNotificationType = new SendNotificationType();
        sendNotificationType.setNumber(String.valueOf(caseData.getSendNotificationCollection().size() + 1));
        sendNotificationType.setDate(LocalDateTime.now().toString());
        sendNotificationType.setSendNotificationTitle(title);
        sendNotificationType.setSendNotificationSubject(List.of(type));
        sendNotificationType.setSendNotificationNotify(BOTH_PARTIES);
        sendNotificationType.setSendNotificationWhoCaseOrder("Judge");

        SendNotificationTypeItem sendNotificationTypeItem = new SendNotificationTypeItem();
        sendNotificationTypeItem.setValue(sendNotificationType);
        sendNotificationTypeItem.setId(UUID.randomUUID().toString());
        caseData.getSendNotificationCollection().add(sendNotificationTypeItem);
        return this;
    }
}