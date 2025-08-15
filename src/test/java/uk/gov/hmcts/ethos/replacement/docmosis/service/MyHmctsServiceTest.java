package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MyHmctsServiceTest {

    private MyHmctsService myHmctsService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private UserIdamService userIdamService;

    private static final String DUMMY_USER_TOKEN = "dummyUserToken";
    private static final String TEST_USER_ID = "12345";
    private static final String TEST_ADDRESS_LINE_1 = "Test address line 1";
    private static final String TEST_ADDRESS_LINE_2 = "Test address line 2";
    private static final String TEST_ADDRESS_LINE_3 = "Test address line 3";
    private static final String TEST_COUNTRY = "Test country";
    private static final String TEST_TOWN_CITY = "Test town city";
    private static final String TEST_POST_CODE = "Test post code";
    private static final String TEST_COUNTY = "Test county";
    private static final String EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND = "Organisation details not found";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        myHmctsService = new MyHmctsService(authTokenGenerator, organisationClient, userIdamService);
    }

    @Test
    @SneakyThrows
    void theGetOrganisationAddress() {

        UserDetails userDetails = new UserDetails();
        userDetails.setUid(TEST_USER_ID);
        when(userIdamService.getUserDetails(DUMMY_USER_TOKEN)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(DUMMY_USER_TOKEN);
        // 1: When retrieving organisation details, we expect the organisation client to return a response
        // with contact information.
        when(organisationClient.retrieveOrganisationDetailsByUserId(DUMMY_USER_TOKEN, DUMMY_USER_TOKEN, TEST_USER_ID))
                .thenReturn(ResponseEntity.ok(OrganisationsResponse.builder().contactInformation(
                        List.of(OrganisationAddress.builder()
                                .addressLine1(TEST_ADDRESS_LINE_1)
                                .addressLine2(TEST_ADDRESS_LINE_2)
                                .addressLine3(TEST_ADDRESS_LINE_3)
                                .country(TEST_COUNTRY)
                                .townCity(TEST_TOWN_CITY)
                                .postCode(TEST_POST_CODE)
                                .county(TEST_COUNTY).build())).build()));
        OrganisationAddress organisationAddress = myHmctsService.getOrganisationAddress(DUMMY_USER_TOKEN);
        assertThat(organisationAddress.getAddressLine1()).isEqualTo(TEST_ADDRESS_LINE_1);
        assertThat(organisationAddress.getAddressLine2()).isEqualTo(TEST_ADDRESS_LINE_2);
        assertThat(organisationAddress.getAddressLine3()).isEqualTo(TEST_ADDRESS_LINE_3);
        assertThat(organisationAddress.getCountry()).isEqualTo(TEST_COUNTRY);
        assertThat(organisationAddress.getTownCity()).isEqualTo(TEST_TOWN_CITY);
        assertThat(organisationAddress.getPostCode()).isEqualTo(TEST_POST_CODE);
        assertThat(organisationAddress.getCounty()).isEqualTo(TEST_COUNTY);
        // 2: When retrieving organisation details, we expect the organisation client to return null.
        when(organisationClient.retrieveOrganisationDetailsByUserId(DUMMY_USER_TOKEN, DUMMY_USER_TOKEN, TEST_USER_ID))
                .thenReturn(null);
        GenericServiceException exceptionNullResponse = assertThrows(GenericServiceException.class,
                () -> myHmctsService.getOrganisationAddress(DUMMY_USER_TOKEN));
        assertThat(exceptionNullResponse.getMessage()).isEqualTo(EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND);
        // 3: When retrieving organisation details, we expect the organisation client to return a response with
        // empty response body.
        when(organisationClient.retrieveOrganisationDetailsByUserId(DUMMY_USER_TOKEN, DUMMY_USER_TOKEN, TEST_USER_ID))
                .thenReturn(ResponseEntity.ok(null));
        GenericServiceException exceptionNullBody = assertThrows(GenericServiceException.class,
                () -> myHmctsService.getOrganisationAddress(DUMMY_USER_TOKEN));
        assertThat(exceptionNullBody.getMessage()).isEqualTo(EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND);
        // 3: When retrieving organisation details, we expect the organisation client to return a response with
        // empty contact information.
        when(organisationClient.retrieveOrganisationDetailsByUserId(DUMMY_USER_TOKEN, DUMMY_USER_TOKEN, TEST_USER_ID))
                .thenReturn(ResponseEntity.ok(OrganisationsResponse.builder().build()));
        GenericServiceException exceptionEmptyContactInformation = assertThrows(GenericServiceException.class,
                () -> myHmctsService.getOrganisationAddress(DUMMY_USER_TOKEN));
        assertThat(exceptionEmptyContactInformation.getMessage()).isEqualTo(EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND);
        // 4: When retrieving organisation details, we expect the organisation client to return a response with
        // empty contact information list is empty.
        when(organisationClient.retrieveOrganisationDetailsByUserId(DUMMY_USER_TOKEN, DUMMY_USER_TOKEN, TEST_USER_ID))
                .thenReturn(ResponseEntity.ok(
                        OrganisationsResponse.builder().contactInformation(new ArrayList<>()).build()));
        GenericServiceException exceptionEmptyOrganisationAddressList = assertThrows(GenericServiceException.class,
                () -> myHmctsService.getOrganisationAddress(DUMMY_USER_TOKEN));
        assertThat(exceptionEmptyOrganisationAddressList.getMessage())
                .isEqualTo(EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND);
    }
}