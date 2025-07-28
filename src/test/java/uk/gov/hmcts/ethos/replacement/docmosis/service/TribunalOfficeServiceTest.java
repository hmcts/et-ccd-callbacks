package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ecm.common.configuration.PostcodeToOfficeMappings;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.UNASSIGNED_OFFICE;

@SpringBootTest(classes = { TribunalOfficesService.class, PostcodeToOfficeService.class })
@EnableConfigurationProperties({ CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class,
    PostcodeToOfficeMappings.class})
class TribunalOfficeServiceTest {

    @Autowired
    TribunalOfficesService tribunalOfficesService;
    @Autowired
    PostcodeToOfficeService postcodeToOfficeService;

    private CaseData caseData;
    private ContactDetails contactDetails;
    private CourtLocations tribunalLocations;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        contactDetails = new ContactDetails();
        tribunalLocations = new CourtLocations();
    }

    @ParameterizedTest
    @MethodSource("tribunalContactDetails")
    void testGetsCorrectTribunalContactDetails(String managingOffice, String postcode, String epimmsId,
                                                String region) {
        contactDetails = tribunalOfficesService.getTribunalContactDetails(managingOffice);
        assertEquals(postcode, contactDetails.getPostcode());
    }

    @Test
    void testGetsCorrectTribunalContactDetailsNullValue() {
        contactDetails = tribunalOfficesService.getTribunalContactDetails(null);
        assertEquals("", contactDetails.getPostcode());
    }

    @ParameterizedTest
    @Disabled("Disabled due to null pointer exception")
    @MethodSource("tribunalContactDetails")
    void testGetsCorrectTribunalLocationDetails(String managingOffice, String postcode,
                                                String epimmsId, String region) {
        tribunalLocations = tribunalOfficesService.getTribunalLocations(managingOffice);
        assertEquals(epimmsId, tribunalLocations.getEpimmsId());
        assertEquals(region, tribunalLocations.getRegion());
    }

    @Test
    void shouldDefaultToUnassignedOffice() {
        caseData = new CaseData();
        tribunalOfficesService.addManagingOffice(caseData, null);
        assertEquals(UNASSIGNED_OFFICE, caseData.getManagingOffice());
    }

    @ParameterizedTest
    @MethodSource("managingOfficeMappings")
    void shouldSetManagingOfficeClaimantWorkAddress(String managingOffice, String postcode, String caseType) {
        Address address = new Address();
        address.setPostCode(postcode);
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(address);
        caseData.setClaimantWorkAddress(claimantWorkAddressType);
        tribunalOfficesService.addManagingOffice(caseData, caseType);
        assertEquals(managingOffice, caseData.getManagingOffice());
    }

    @ParameterizedTest
    @MethodSource("managingOfficeMappings")
    void shouldSetMangingOfficeRespondent(String managingOffice, String postcode, String caseType) {
        Address address = new Address();
        address.setPostCode(postcode);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentAddress(address);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        tribunalOfficesService.addManagingOffice(caseData, caseType);
        assertEquals(managingOffice, caseData.getManagingOffice());
    }

    @Test
    void shouldBeUnassignedWhenUnknownPostcode() {
        Address address = new Address();
        address.setPostCode("UNKNOWN");
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(address);
        caseData.setClaimantWorkAddress(claimantWorkAddressType);
        tribunalOfficesService.addManagingOffice(caseData, "ET_EnglandWales");
        assertEquals(UNASSIGNED_OFFICE, caseData.getManagingOffice());
    }

    private static Stream<Arguments> tribunalContactDetails() {
        return Stream.of(
                Arguments.of(TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA", "301017", "4"),
                Arguments.of(TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT", "366559", "11"),
                Arguments.of(TribunalOffice.ABERDEEN.getOfficeName(), "AB10 1SH", "219164", "11"),
                Arguments.of(TribunalOffice.DUNDEE.getOfficeName(), "DD1 4QB", "367564", "11"),
                Arguments.of(TribunalOffice.EDINBURGH.getOfficeName(), "EH3 7HF", "368308", "11"),
                Arguments.of(UNASSIGNED_OFFICE, "", "", ""));
    }

    private static Stream<Arguments> managingOfficeMappings() {
        return Stream.of(
                Arguments.of(TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA", "ET_EnglandWales"),
                Arguments.of(TribunalOffice.LEEDS.getOfficeName(), "LS16 6NB", "ET_EnglandWales"),
                Arguments.of(TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT", "ET_Scotland"),
                Arguments.of(TribunalOffice.GLASGOW.getOfficeName(), "DD1 1AB", "ET_Scotland"),
                Arguments.of(TribunalOffice.GLASGOW.getOfficeName(), "AB10 1SH", "ET_Scotland"),
                Arguments.of(UNASSIGNED_OFFICE, "", "", ""),
                Arguments.of(UNASSIGNED_OFFICE, "AB10 1SH", "ET_EnglandWales"),
                Arguments.of(UNASSIGNED_OFFICE, "M3 2JA", "ET_Scotland"));
    }
}
