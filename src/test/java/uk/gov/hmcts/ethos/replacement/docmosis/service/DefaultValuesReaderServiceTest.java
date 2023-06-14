package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultValuesReaderServiceTest {

    private CaseDefaultValuesConfiguration config;

    private TribunalOfficesService tribunalOfficesService;

    private DefaultValuesReaderService defaultValuesReaderService;

    @BeforeEach
    public void setup() {
        config = mock(CaseDefaultValuesConfiguration.class);
        tribunalOfficesService = mock(TribunalOfficesService.class);
        ConciliationTrackService conciliationTrackService = mock(ConciliationTrackService.class);
        defaultValuesReaderService = new DefaultValuesReaderService(config,
                tribunalOfficesService,
                conciliationTrackService);
    }

    @Test
    public void testGetDefaultValues() {
        // Arrange
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setAddress1("TestAddress1");
        contactDetails.setAddress2("TestAddress2");
        contactDetails.setAddress3("TestAddress3");
        contactDetails.setTown("TestTown");
        contactDetails.setPostcode("TestPostcode");
        contactDetails.setTelephone("TestTelephone");
        contactDetails.setFax("TestFax");
        contactDetails.setDx("TestDx");
        contactDetails.setEmail("TestEmail");
        contactDetails.setManagingOffice("TestManagingOffice");

        String officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(tribunalOfficesService.getTribunalContactDetails(officeName)).thenReturn(contactDetails);
        String caseType = Constants.MULTIPLE_CASE_TYPE;
        when(config.getCaseType()).thenReturn(caseType);
        String positionType = Constants.POSITION_TYPE_CASE_CLOSED;
        when(config.getPositionType()).thenReturn(positionType);

        // Act
        DefaultValues defaultValues = defaultValuesReaderService.getDefaultValues(officeName);

        // Assert
        assertEquals(positionType, defaultValues.getPositionType());
        assertEquals(caseType, defaultValues.getCaseType());
        assertEquals("TestAddress1", defaultValues.getTribunalCorrespondenceAddressLine1());
        assertEquals("TestAddress2", defaultValues.getTribunalCorrespondenceAddressLine2());
        assertEquals("TestAddress3", defaultValues.getTribunalCorrespondenceAddressLine3());
        assertEquals("TestTown", defaultValues.getTribunalCorrespondenceTown());
        assertEquals("TestPostcode", defaultValues.getTribunalCorrespondencePostCode());
        assertEquals("TestTelephone", defaultValues.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", defaultValues.getTribunalCorrespondenceFax());
        assertEquals("TestDx", defaultValues.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", defaultValues.getTribunalCorrespondenceEmail());
        assertEquals("TestManagingOffice", defaultValues.getManagingOffice());
    }

    @Test
    public void testGetListingDefaultValuesScottishOfficeAll() {
        // Arrange
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setAddress1("TestAddress1");
        contactDetails.setAddress2("TestAddress2");
        contactDetails.setAddress3("TestAddress3");
        contactDetails.setTown("TestTown");
        contactDetails.setPostcode("TestPostcode");
        contactDetails.setTelephone("TestTelephone");
        contactDetails.setFax("TestFax");
        contactDetails.setDx("TestDx");
        contactDetails.setEmail("TestEmail");
        contactDetails.setManagingOffice("Glasgow");

        String officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(tribunalOfficesService.getTribunalContactDetails(officeName)).thenReturn(contactDetails);
        String caseType = Constants.MULTIPLE_CASE_TYPE;
        when(config.getCaseType()).thenReturn(caseType);
        String positionType = Constants.POSITION_TYPE_CASE_CLOSED;
        when(config.getPositionType()).thenReturn(positionType);
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(Constants.SCOTLAND_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice("All");
        listingDetails.setCaseData(listingData);

        // Act
        DefaultValues defaultValues = defaultValuesReaderService.getListingDefaultValues(listingDetails);

        // Assert
        assertEquals(positionType, defaultValues.getPositionType());
        assertEquals(caseType, defaultValues.getCaseType());
        assertEquals("TestAddress1", defaultValues.getTribunalCorrespondenceAddressLine1());
        assertEquals("TestAddress2", defaultValues.getTribunalCorrespondenceAddressLine2());
        assertEquals("TestAddress3", defaultValues.getTribunalCorrespondenceAddressLine3());
        assertEquals("TestTown", defaultValues.getTribunalCorrespondenceTown());
        assertEquals("TestPostcode", defaultValues.getTribunalCorrespondencePostCode());
        assertEquals("TestTelephone", defaultValues.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", defaultValues.getTribunalCorrespondenceFax());
        assertEquals("TestDx", defaultValues.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", defaultValues.getTribunalCorrespondenceEmail());
        assertEquals("Glasgow", defaultValues.getManagingOffice());
    }

    @Test
    public void testGetClaimantTypeOfClaimant() {
        String claimantTypeOfClaimant = Constants.INDIVIDUAL_TYPE_CLAIMANT;
        when(config.getClaimantTypeOfClaimant()).thenReturn(claimantTypeOfClaimant);

        assertEquals(claimantTypeOfClaimant, defaultValuesReaderService.getClaimantTypeOfClaimant());
    }

    @Test
    public void testGetPositionType() {
        String positionType = Constants.POSITION_TYPE_CASE_CLOSED;
        when(config.getPositionType()).thenReturn(positionType);

        assertEquals(positionType, defaultValuesReaderService.getPositionType());
    }

    @Test
    public void testGetCaseDataWithNoValues() {
        DefaultValues defaultValues = createDefaultValues();
        CaseData caseData = new CaseData();

        defaultValuesReaderService.getCaseData(caseData, defaultValues);

        assertEquals(Constants.POSITION_TYPE_CASE_CLOSED, caseData.getPositionType());
        assertEquals(Constants.POSITION_TYPE_CASE_CLOSED, caseData.getCaseSource());
        assertEquals("TestManagingOffice", caseData.getManagingOffice());
        assertEquals(Constants.MULTIPLE_CASE_TYPE, caseData.getEcmCaseType());
        verifyAddress(caseData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", caseData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", caseData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", caseData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", caseData.getTribunalCorrespondenceEmail());
        assertNull(caseData.getClaimantWorkAddress());
    }

    @Test
    public void testGetCaseDataWithExistingValues() {
        DefaultValues defaultValues = createDefaultValues();
        CaseData caseData = createCaseWithValues();

        defaultValuesReaderService.getCaseData(caseData, defaultValues);

        assertEquals("ExistingPositionType", caseData.getPositionType());
        assertEquals("ExistingCaseSource", caseData.getCaseSource());
        assertEquals("TestManagingOffice", caseData.getManagingOffice());
        assertEquals("ExistingCaseType", caseData.getEcmCaseType());
        verifyAddress(caseData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", caseData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", caseData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", caseData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", caseData.getTribunalCorrespondenceEmail());
        assertNull(caseData.getClaimantWorkAddress());
    }

    @Test
    public void testGetCaseDataWithClaimantWorkAddress() {
        CaseData caseData = new CaseData();
        caseData.setClaimantWorkAddressQuestion(Constants.YES);
        caseData.setClaimantWorkAddressQRespondent(new DynamicFixedListType("Respondent 2"));
        caseData.setRespondentCollection(createRespondents());
        DefaultValues defaultValues = createDefaultValues();
        defaultValuesReaderService.getCaseData(caseData, defaultValues);

        assertEquals(Constants.POSITION_TYPE_CASE_CLOSED, caseData.getPositionType());
        assertEquals(Constants.POSITION_TYPE_CASE_CLOSED, caseData.getCaseSource());
        assertEquals("TestManagingOffice", caseData.getManagingOffice());
        assertEquals(Constants.MULTIPLE_CASE_TYPE, caseData.getEcmCaseType());
        verifyAddress(caseData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", caseData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", caseData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", caseData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", caseData.getTribunalCorrespondenceEmail());
        Address address = caseData.getClaimantWorkAddress().getClaimantWorkAddress();
        assertEquals("Respondent 2 AddressLine1", address.getAddressLine1());
    }

    @Test
    public void testGetListingData() {
        DefaultValues defaultValues = createDefaultValues();
        ListingData listingData = new ListingData();

        defaultValuesReaderService.getListingData(listingData, defaultValues);

        verifyAddress(listingData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", listingData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", listingData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", listingData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", listingData.getTribunalCorrespondenceEmail());
    }

    private DefaultValues createDefaultValues() {
        return DefaultValues.builder()
                .positionType(Constants.POSITION_TYPE_CASE_CLOSED)
                .caseType(Constants.MULTIPLE_CASE_TYPE)
                .tribunalCorrespondenceAddressLine1("TestAddress1")
                .tribunalCorrespondenceAddressLine2("TestAddress2")
                .tribunalCorrespondenceAddressLine3("TestAddress3")
                .tribunalCorrespondenceTown("TestTown")
                .tribunalCorrespondencePostCode("TestPostcode")
                .tribunalCorrespondenceTelephone("TestTelephone")
                .tribunalCorrespondenceFax("TestFax")
                .tribunalCorrespondenceDX("TestDX")
                .tribunalCorrespondenceEmail("TestEmail")
                .managingOffice("TestManagingOffice")
                .build();
    }

    private CaseData createCaseWithValues() {
        CaseData caseData = new CaseData();
        caseData.setPositionType("ExistingPositionType");
        caseData.setCaseSource("ExistingCaseSource");
        caseData.setManagingOffice("ExistingManagingOffice");
        caseData.setEcmCaseType("ExistingCaseType");

        return caseData;
    }

    private List<RespondentSumTypeItem> createRespondents() {
        List<RespondentSumTypeItem> respondents = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            RespondentSumType respondentSumType = new RespondentSumType();
            respondentSumType.setRespondentName("Respondent " + i);
            Address address = new Address();
            address.setAddressLine1(respondentSumType.getRespondentName() + " AddressLine1");
            respondentSumType.setRespondentAddress(address);
            RespondentSumTypeItem item = new RespondentSumTypeItem();
            item.setValue(respondentSumType);
            respondents.add(item);
        }

        return respondents;
    }

    private void verifyAddress(Address address) {
        assertEquals("TestAddress1", address.getAddressLine1());
        assertEquals("TestAddress2", address.getAddressLine2());
        assertEquals("TestAddress3", address.getAddressLine3());
        assertEquals("TestTown", address.getPostTown());
        assertEquals("TestPostcode", address.getPostCode());
    }
}
