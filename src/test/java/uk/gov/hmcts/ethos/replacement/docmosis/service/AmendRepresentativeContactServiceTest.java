package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.AmendRepresentativeContactService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRepresentativeService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AmendRepresentativeContactServiceTest {

    @MockitoBean
    private UserIdamService  userIdamService;
    @MockitoBean
    private MyHmctsService myHmctsService;
    @MockitoBean
    private CcdCaseAssignment ccdCaseAssignment;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private NocRepresentativeService nocRepresentativeService;

    @InjectMocks
    private AmendRepresentativeContactService amendRepresentativeContactService;

    private static final String SUBMISSION_REFERENCE = "1234567890123456";
    private static final String VALID_USER_TOKEN = "validUserToken";
    private static final String REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS = "Use MyHMCTS details";
    private static final String ROLE_CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String ROLE_SOLICITOR_A = "[SOLICITORA]";
    private static final String ADDRESS_LINE_1 = "addressLine1";
    private static final String ADDRESS_LINE_2 = "addressLine2";
    private static final String ADDRESS_LINE_3 = "addressLine3";
    private static final String POSTAL_CODE = "postalCode";
    private static final String COUNTRY = "country";
    private static final String COUNTY = "county";
    private static final String TOWN_CITY = "townCity";
    private static final String REPRESENTATIVE_PHONE_NUMBER = "07444518903";
    private static final String RESPONDENT_ID = "respondentId";
    private static final String RESPONDENT_REPRESENTATIVE_ID = "respondentRepresentativeId";
    private static final String RESPONDENT_NAME = "RespondentName";

    @BeforeEach
    void setUp() {
        amendRepresentativeContactService = new AmendRepresentativeContactService(myHmctsService,
                nocRepresentativeService);
    }

    @Test
    @SneakyThrows
    void theUpdateRepresentativeContactDetails() {
        CaseData caseData = new CaseData();
        // when my hmcts address is selected and role is claimant solicitor should set representative my hmcts contact
        // address and phone number to claimant representative
        caseData.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS);
        when(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(VALID_USER_TOKEN,
                SUBMISSION_REFERENCE)).thenReturn(List.of(ROLE_CLAIMANT_SOLICITOR));
        OrganisationAddress organisationAddress = OrganisationAddress.builder()
                .addressLine1(ADDRESS_LINE_1).addressLine2(ADDRESS_LINE_2).addressLine3(ADDRESS_LINE_3)
                .country(COUNTRY).county(COUNTY).postCode(POSTAL_CODE).townCity(TOWN_CITY).build();
        when(myHmctsService.getUserOrganisationAddress(VALID_USER_TOKEN)).thenReturn(organisationAddress);
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().build());
        caseData.setEt3ResponsePhone(REPRESENTATIVE_PHONE_NUMBER);
        amendRepresentativeContactService.updateRepresentativeContactDetails(VALID_USER_TOKEN, caseData,
                SUBMISSION_REFERENCE);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeAddress().getPostCode())
                .isEqualTo(POSTAL_CODE);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeAddress().getAddressLine1())
                .isEqualTo(ADDRESS_LINE_1);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeAddress().getAddressLine2())
                .isEqualTo(ADDRESS_LINE_2);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeAddress().getAddressLine3())
                .isEqualTo(ADDRESS_LINE_3);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeAddress().getCountry())
                .isEqualTo(COUNTRY);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeAddress().getCounty())
                .isEqualTo(COUNTY);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeAddress().getPostTown())
                .isEqualTo(TOWN_CITY);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativePhoneNumber())
                .isEqualTo(REPRESENTATIVE_PHONE_NUMBER);
        // when address is not my hmcts and role is respondent representative role should set address and phone number
        // entered through et3 form to respondent representative
        RepresentedTypeR respondentRepresentativeValue = RepresentedTypeR.builder().respondentId(RESPONDENT_ID).build();
        RepresentedTypeRItem respondentRepresentative = new RepresentedTypeRItem();
        respondentRepresentative.setValue(respondentRepresentativeValue);
        respondentRepresentative.setId(RESPONDENT_REPRESENTATIVE_ID);
        caseData.setRepCollection(List.of(respondentRepresentative));
        RespondentSumType respondentValue = RespondentSumType.builder().respondentName(RESPONDENT_NAME).build();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(respondentValue);
        respondent.setId(RESPONDENT_ID);
        caseData.setRespondentCollection(List.of(respondent));
        when(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(VALID_USER_TOKEN,
                SUBMISSION_REFERENCE)).thenReturn(List.of(ROLE_SOLICITOR_A));
        caseData.setRepresentativeContactChangeOption(StringUtils.EMPTY);
        caseData.setEt3ResponsePhone(REPRESENTATIVE_PHONE_NUMBER);
        Address address = createAddress();
        amendRepresentativeContactService.updateRepresentativeContactDetails(VALID_USER_TOKEN, caseData,
                SUBMISSION_REFERENCE);
        assertThat(respondentRepresentativeValue.getRepresentativePhoneNumber()).isEqualTo(REPRESENTATIVE_PHONE_NUMBER);
        assertThat(respondentRepresentativeValue.getRepresentativeAddress()).isEqualTo(address);
    }

    @Test
    @SneakyThrows
    void theSetEt3ResponseContactAddress() {
        // when roles contain claimant solicitor should add claimant solicitor data to et3 response
        CaseData caseData = new CaseData();
        Address address = createAddress();
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().representativeAddress(address)
                .representativePhoneNumber(REPRESENTATIVE_PHONE_NUMBER).build();
        caseData.setRepresentativeClaimantType(claimantRepresentative);
        when(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(VALID_USER_TOKEN,
                SUBMISSION_REFERENCE)).thenReturn(List.of(ROLE_CLAIMANT_SOLICITOR));
        amendRepresentativeContactService
                .setEt3ResponseContactAddress(VALID_USER_TOKEN, caseData, SUBMISSION_REFERENCE);
        assertThat(caseData.getEt3ResponsePhone()).isEqualTo(REPRESENTATIVE_PHONE_NUMBER);
        assertThat(caseData.getEt3ResponseAddress()).isEqualTo(address);
        // when roles contain respondent solicitor role should add respondent solicitor data to et3 response
        RepresentedTypeR representativeValue = RepresentedTypeR.builder()
                .respondentId(RESPONDENT_ID).role(ROLE_SOLICITOR_A).representativeAddress(address)
                .representativePhoneNumber(REPRESENTATIVE_PHONE_NUMBER).build();
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(RESPONDENT_REPRESENTATIVE_ID)
                .value(representativeValue).build();
        caseData.setRepCollection(List.of(representative));
        RespondentSumType respondentValue = RespondentSumType.builder().respondentName(RESPONDENT_NAME).build();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(respondentValue);
        respondent.setId(RESPONDENT_ID);
        when(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(VALID_USER_TOKEN,
                SUBMISSION_REFERENCE)).thenReturn(List.of(ROLE_SOLICITOR_A));
        amendRepresentativeContactService.setEt3ResponseContactAddress(VALID_USER_TOKEN, caseData,
                SUBMISSION_REFERENCE);
        assertThat(caseData.getEt3ResponsePhone()).isEqualTo(REPRESENTATIVE_PHONE_NUMBER);
        assertThat(caseData.getEt3ResponseAddress()).isEqualTo(address);
    }

    private static Address createAddress() {
        Address address = new Address();
        address.setAddressLine1(ADDRESS_LINE_1);
        address.setAddressLine2(ADDRESS_LINE_2);
        address.setAddressLine3(ADDRESS_LINE_3);
        address.setCountry(COUNTRY);
        address.setCounty(COUNTY);
        address.setPostTown(TOWN_CITY);
        address.setPostCode(POSTAL_CODE);
        return address;
    }
}
