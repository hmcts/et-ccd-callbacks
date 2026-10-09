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

    @Test
    @SneakyThrows
    void theLoadStagedContactDetails_prefillsStagingFieldsAndLeavesEt3Untouched() {
        CaseData caseData = new CaseData();
        Address address = createAddress();
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder()
                .id(RESPONDENT_REPRESENTATIVE_ID)
                .value(RepresentedTypeR.builder()
                        .respondentId(RESPONDENT_ID)
                        .representativeAddress(address)
                        .representativePhoneNumber(REPRESENTATIVE_PHONE_NUMBER)
                        .build())
                .build()));
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        caseData.setRespondentCollection(List.of(respondent));
        when(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(VALID_USER_TOKEN,
                SUBMISSION_REFERENCE)).thenReturn(List.of(ROLE_SOLICITOR_A));

        amendRepresentativeContactService.loadStagedContactDetails(VALID_USER_TOKEN, caseData, SUBMISSION_REFERENCE);

        assertThat(caseData.getRespRepPhoneNumber()).isEqualTo(REPRESENTATIVE_PHONE_NUMBER);
        assertThat(caseData.getRespRepAddress()).isEqualTo(address);
        // the live ET3 response fields must not be used as staging by this event
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        assertThat(caseData.getEt3ResponseAddress()).isNull();
    }

    @Test
    @SneakyThrows
    void theSaveStagedContactDetails_persistsStagingThenClearsItAndLeavesEt3Untouched() {
        CaseData caseData = new CaseData();
        Address stagedAddress = createAddress();
        Address et3Address = new Address();
        et3Address.setAddressLine1("10 Downing Street");
        caseData.setRespRepPhoneNumber(REPRESENTATIVE_PHONE_NUMBER);
        caseData.setRespRepAddress(stagedAddress);
        caseData.setMyHmctsAddressText("Org House");
        caseData.setRepresentativeContactChangeOption(StringUtils.EMPTY);
        caseData.setEt3ResponsePhone("01234567890");
        caseData.setEt3ResponseAddress(et3Address);

        RepresentedTypeR representativeValue = RepresentedTypeR.builder().respondentId(RESPONDENT_ID).build();
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder()
                .id(RESPONDENT_REPRESENTATIVE_ID).value(representativeValue).build()));
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        caseData.setRespondentCollection(List.of(respondent));
        when(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(VALID_USER_TOKEN,
                SUBMISSION_REFERENCE)).thenReturn(List.of(ROLE_SOLICITOR_A));

        amendRepresentativeContactService.saveStagedContactDetails(VALID_USER_TOKEN, caseData, SUBMISSION_REFERENCE);

        assertThat(representativeValue.getRepresentativePhoneNumber()).isEqualTo(REPRESENTATIVE_PHONE_NUMBER);
        assertThat(representativeValue.getRepresentativeAddress()).isEqualTo(stagedAddress);
        // staged and Check your answers fields are cleared once persisted
        assertThat(caseData.getRespRepPhoneNumber()).isNull();
        assertThat(caseData.getRespRepAddress()).isNull();
        assertThat(caseData.getRepresentativeContactChangeOption()).isNull();
        assertThat(caseData.getMyHmctsAddressText()).isNull();
        // the live ET3 response fields are left exactly as they were
        assertThat(caseData.getEt3ResponsePhone()).isEqualTo("01234567890");
        assertThat(caseData.getEt3ResponseAddress()).isEqualTo(et3Address);
    }

    @Test
    @SneakyThrows
    void theSaveStagedContactDetails_withMyHmctsOption_usesOrganisationAddress() {
        CaseData caseData = new CaseData();
        caseData.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS);
        // CCD clears the hidden phone staging field for the MyHMCTS option
        caseData.setRespRepPhoneNumber(null);
        when(myHmctsService.getUserOrganisationAddress(VALID_USER_TOKEN)).thenReturn(OrganisationAddress.builder()
                .addressLine1(ADDRESS_LINE_1).addressLine2(ADDRESS_LINE_2).addressLine3(ADDRESS_LINE_3)
                .country(COUNTRY).county(COUNTY).postCode(POSTAL_CODE).townCity(TOWN_CITY).build());

        RepresentedTypeR representativeValue = RepresentedTypeR.builder()
                .respondentId(RESPONDENT_ID)
                .representativePhoneNumber(REPRESENTATIVE_PHONE_NUMBER)
                .build();
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder()
                .id(RESPONDENT_REPRESENTATIVE_ID).value(representativeValue).build()));
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        caseData.setRespondentCollection(List.of(respondent));
        when(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(VALID_USER_TOKEN,
                SUBMISSION_REFERENCE)).thenReturn(List.of(ROLE_SOLICITOR_A));

        amendRepresentativeContactService.saveStagedContactDetails(VALID_USER_TOKEN, caseData, SUBMISSION_REFERENCE);

        assertThat(representativeValue.getRepresentativeAddress()).isEqualTo(createAddress());
        // existing phone must not be wiped when the staging phone field is hidden/null
        assertThat(representativeValue.getRepresentativePhoneNumber()).isEqualTo(REPRESENTATIVE_PHONE_NUMBER);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getRespRepAddress()).isNull();
    }

    @Test
    @SneakyThrows
    void theSetStagedMyHmctsContactAddress_writesStagingFieldOnly() {
        CaseData caseData = new CaseData();
        when(myHmctsService.getUserOrganisationAddress(VALID_USER_TOKEN)).thenReturn(OrganisationAddress.builder()
                .addressLine1(ADDRESS_LINE_1).addressLine2(ADDRESS_LINE_2).addressLine3(ADDRESS_LINE_3)
                .country(COUNTRY).county(COUNTY).postCode(POSTAL_CODE).townCity(TOWN_CITY).build());

        amendRepresentativeContactService.setStagedMyHmctsContactAddress(VALID_USER_TOKEN, caseData);

        assertThat(caseData.getRespRepAddress()).isEqualTo(createAddress());
        assertThat(caseData.getMyHmctsAddressText()).isNotNull();
        assertThat(caseData.getEt3ResponseAddress()).isNull();
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
