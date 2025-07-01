package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class DefaultValuesReaderServiceTest {

    private CaseDefaultValuesConfiguration config;

    private TribunalOfficesService tribunalOfficesService;

    private DefaultValuesReaderService defaultValuesReaderService;
    @MockitoBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @BeforeEach
    public void setup() {
        config = mock(CaseDefaultValuesConfiguration.class);
        tribunalOfficesService = mock(TribunalOfficesService.class);
        ConciliationTrackService conciliationTrackService = mock(ConciliationTrackService.class);
        defaultValuesReaderService = new DefaultValuesReaderService(config,
                tribunalOfficesService,
                conciliationTrackService, caseManagementForCaseWorkerService);
    }

    @Test
    void testGetDefaultValues() {
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
    void testGetListingDefaultValuesScottishOfficeAll() {
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
    void testGetClaimantTypeOfClaimant() {
        String claimantTypeOfClaimant = Constants.INDIVIDUAL_TYPE_CLAIMANT;
        when(config.getClaimantTypeOfClaimant()).thenReturn(claimantTypeOfClaimant);

        assertEquals(claimantTypeOfClaimant, defaultValuesReaderService.getClaimantTypeOfClaimant());
    }

    @Test
    void testGetPositionType() {
        String positionType = Constants.POSITION_TYPE_CASE_CLOSED;
        when(config.getPositionType()).thenReturn(positionType);

        assertEquals(positionType, defaultValuesReaderService.getPositionType());
    }

    @Test
    void testSetCaseDataWithNoValues() {
        DefaultValues defaultValues = createDefaultValues();
        CaseData caseData = new CaseData();

        defaultValuesReaderService.setCaseData(caseData, defaultValues);

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
    void testSetCaseDataWithExistingValues() {
        DefaultValues defaultValues = createDefaultValues();
        CaseData caseData = createCaseWithValues();

        defaultValuesReaderService.setCaseData(caseData, defaultValues);

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
    void testSetCaseDataWithClaimantWorkAddress() {
        CaseData caseData = new CaseData();
        caseData.setClaimantWorkAddressQuestion(Constants.YES);
        caseData.setClaimantWorkAddressQRespondent(new DynamicFixedListType("Respondent 2"));
        caseData.setRespondentCollection(createRespondents());
        DefaultValues defaultValues = createDefaultValues();
        defaultValuesReaderService.setCaseData(caseData, defaultValues);

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
    void testGetListingData() {
        DefaultValues defaultValues = createDefaultValues();
        ListingData listingData = new ListingData();

        defaultValuesReaderService.getListingData(listingData, defaultValues);

        verifyAddress(listingData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", listingData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", listingData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", listingData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", listingData.getTribunalCorrespondenceEmail());
    }

    @ParameterizedTest
    @MethodSource("setPositionTypeArguments")
    void setPositionType(String positionType, String expectedPosition) {
        CaseData caseData = new CaseDataBuilder()
                .withPositionType(positionType)
                .withCaseSource(Constants.ET1_ONLINE_CASE_SOURCE)
                .build();
        defaultValuesReaderService.setPositionAndOffice(Constants.ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertEquals(expectedPosition, caseData.getPositionType());
    }

    private static Stream<Arguments> setPositionTypeArguments() {
        return Stream.of(
                Arguments.of(null, "ET1 Online submission"),
                Arguments.of(Constants.MANUALLY_CREATED_POSITION, Constants.MANUALLY_CREATED_POSITION)
        );
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

    @ParameterizedTest
    @MethodSource
    void setSubmissionReference(String submissionReference, String caseId) {
        CaseData caseData = new CaseData();
        caseData.setFeeGroupReference(submissionReference);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(caseId);
        caseDetails.setCaseData(caseData);

        defaultValuesReaderService.setSubmissionReference(caseDetails);
        assertEquals(caseData.getFeeGroupReference(), caseId);
    }

    public static Stream<Arguments> setSubmissionReference() {
        return Stream.of(
                Arguments.of(null, "1234567890123456"),
                Arguments.of("", "1234567890123456"),
                Arguments.of("1111222233334444", "1111222233334444")
        );
    }
}
