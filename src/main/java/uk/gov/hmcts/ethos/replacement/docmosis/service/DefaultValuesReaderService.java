package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@Service("defaultValuesReaderService")
public class DefaultValuesReaderService {

    private final CaseDefaultValuesConfiguration config;
    private final TribunalOfficesService tribunalOfficesService;
    private final ConciliationTrackService conciliationTrackService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    public static final String ALL_OFFICES = "All";
    private static final String ET1_ONLINE_SUBMISSION_POSITION_TYPE = "ET1 Online submission";

    public DefaultValuesReaderService(CaseDefaultValuesConfiguration config,
                                      TribunalOfficesService tribunalOfficesService,
                                      ConciliationTrackService conciliationTrackService,
                                      CaseManagementForCaseWorkerService caseManagementForCaseWorkerService) {
        this.config = config;
        this.tribunalOfficesService = tribunalOfficesService;
        this.conciliationTrackService = conciliationTrackService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    public DefaultValues getListingDefaultValues(ListingDetails listingDetails) {
        String managingOffice = listingDetails.getCaseData().getManagingOffice();
        if (listingDetails.getCaseTypeId().equals(SCOTLAND_LISTING_CASE_TYPE_ID)
                &&  ALL_OFFICES.equals(managingOffice)) {
            managingOffice = TribunalOffice.GLASGOW.getOfficeName();
        }
        return getDefaultValues(managingOffice);
    }

    public DefaultValues getDefaultValues(String managingOffice) {
        ContactDetails contactDetails = tribunalOfficesService.getTribunalContactDetails(managingOffice);
        return createDefaultValues(contactDetails);
    }

    public String getClaimantTypeOfClaimant() {
        return config.getClaimantTypeOfClaimant();
    }

    public String getPositionType() {
        return config.getPositionType();
    }

    private void setPositionType(CaseData caseData, DefaultValues defaultValues) {
        if (caseData.getPositionType() == null) {
            caseData.setPositionType(defaultValues.getPositionType());
        }
    }

    public void setCaseData(CaseData caseData, DefaultValues defaultValues) {
        setPositionType(caseData, defaultValues);
        conciliationTrackService.populateConciliationTrackForJurisdiction(caseData);
        if (caseData.getCaseSource() == null || caseData.getCaseSource().trim().equals("")) {
            caseData.setCaseSource(defaultValues.getPositionType());
        }
        if (defaultValues.getManagingOffice() != null) {
            caseData.setManagingOffice(defaultValues.getManagingOffice());
        }
        if (caseData.getEcmCaseType() == null) {
            caseData.setEcmCaseType(defaultValues.getCaseType());
        }
        caseData.setTribunalCorrespondenceAddress(getTribunalCorrespondenceAddress(defaultValues));
        caseData.setTribunalCorrespondenceTelephone(defaultValues.getTribunalCorrespondenceTelephone());
        caseData.setTribunalCorrespondenceFax(defaultValues.getTribunalCorrespondenceFax());
        caseData.setTribunalCorrespondenceDX(defaultValues.getTribunalCorrespondenceDX());
        caseData.setTribunalCorrespondenceEmail(defaultValues.getTribunalCorrespondenceEmail());

        log.info("Adding claimant work address if from respondent");
        if (caseData.getClaimantWorkAddressQuestion() != null && caseData.getClaimantWorkAddressQuestion().equals(YES)
                && caseData.getClaimantWorkAddressQRespondent() != null) {
            ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
            String respondentName = caseData.getClaimantWorkAddressQRespondent().getValue().getCode();
            if (caseData.getRespondentCollection() != null) {
                Optional<RespondentSumTypeItem> respondentChosen =
                        caseData.getRespondentCollection().stream().filter(respondentSumTypeItem ->
                                respondentSumTypeItem.getValue().getRespondentName()
                                        .equals(respondentName)).findFirst();
                respondentChosen.ifPresent(respondentSumTypeItem ->
                        claimantWorkAddressType.setClaimantWorkAddress(
                                respondentSumTypeItem.getValue().getRespondentAddress()));
            }
            caseData.setClaimantWorkAddressQRespondent(null);
            caseData.setClaimantWorkAddress(claimantWorkAddressType);
        }
    }

    public ListingData getListingData(ListingData listingData, DefaultValues defaultValues) {
        listingData.setTribunalCorrespondenceAddress(getTribunalCorrespondenceAddress(defaultValues));
        listingData.setTribunalCorrespondenceTelephone(defaultValues.getTribunalCorrespondenceTelephone());
        listingData.setTribunalCorrespondenceFax(defaultValues.getTribunalCorrespondenceFax());
        listingData.setTribunalCorrespondenceDX(defaultValues.getTribunalCorrespondenceDX());
        listingData.setTribunalCorrespondenceEmail(defaultValues.getTribunalCorrespondenceEmail());
        return listingData;
    }

    private DefaultValues createDefaultValues(ContactDetails contactDetails) {
        return DefaultValues.builder()
                .positionType(config.getPositionType())
                .caseType(config.getCaseType())
                .tribunalCorrespondenceAddressLine1(contactDetails.getAddress1())
                .tribunalCorrespondenceAddressLine2(contactDetails.getAddress2())
                .tribunalCorrespondenceAddressLine3(contactDetails.getAddress3())
                .tribunalCorrespondenceTown(contactDetails.getTown())
                .tribunalCorrespondencePostCode(contactDetails.getPostcode())
                .tribunalCorrespondenceTelephone(contactDetails.getTelephone())
                .tribunalCorrespondenceFax(contactDetails.getFax())
                .tribunalCorrespondenceDX(contactDetails.getDx())
                .tribunalCorrespondenceEmail(contactDetails.getEmail())
                .managingOffice(contactDetails.getManagingOffice())
                .build();
    }

    private Address getTribunalCorrespondenceAddress(DefaultValues defaultValues) {
        Address address = new Address();
        address.setAddressLine1(
                Optional.ofNullable(defaultValues.getTribunalCorrespondenceAddressLine1()).orElse(""));
        address.setAddressLine2(
                Optional.ofNullable(defaultValues.getTribunalCorrespondenceAddressLine2()).orElse(""));
        address.setAddressLine3(
                Optional.ofNullable(defaultValues.getTribunalCorrespondenceAddressLine3()).orElse(""));
        address.setPostTown(
                Optional.ofNullable(defaultValues.getTribunalCorrespondenceTown()).orElse(""));
        address.setPostCode(
                Optional.ofNullable(defaultValues.getTribunalCorrespondencePostCode()).orElse(""));
        return address;
    }

    public void setPositionAndOffice(String caseTypeId, CaseData caseData) {
        if (ET1_ONLINE_CASE_SOURCE.equals(caseData.getCaseSource()) && isNullOrEmpty(caseData.getPositionType())) {
            caseData.setPositionType(ET1_ONLINE_SUBMISSION_POSITION_TYPE);
            caseManagementForCaseWorkerService.setScotlandAllocatedOffice(caseTypeId, caseData);
        }
    }

    public void setSubmissionReference(CaseDetails caseDetails) {
        if (isNullOrEmpty(caseDetails.getCaseData().getFeeGroupReference())) {
            caseDetails.getCaseData().setFeeGroupReference(caseDetails.getCaseId());
        }
    }

}
