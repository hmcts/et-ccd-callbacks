package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.AmendRepresentativeContactService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRepresentativeService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_USER_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.SYSTEM_ERROR;

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
    private static final String INVALID_CASE_ID = "invalidCaseId";
    private static final String VALID_CASE_ID = "validCaseId";
    private static final String VALID_CASE_ID_RETURNS_INVALID_USER = "validCaseIdReturnsInvalidUser";
    private static final String USER_ID = "userId";

    private static final String EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND =
            "Case data not found for submission reference, " + SUBMISSION_REFERENCE + ".";

    @BeforeEach
    void setUp() {
        amendRepresentativeContactService = new AmendRepresentativeContactService(myHmctsService,
                nocRepresentativeService);
    }

    @ParameterizedTest
    @MethodSource("generateTestUpdateRepresentativeContactDetails")
    @SneakyThrows
    void theUpdateRepresentativeContactDetails(String userToken, CaseData caseData) {
        // When both user token and case data are empty
        if (StringUtils.isBlank(userToken) && ObjectUtils.isEmpty(caseData)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService
                            .updateRepresentativeContactDetails(userToken, caseData, SUBMISSION_REFERENCE));
            assertThat(gex.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND);
            return;
        }
        // When only case data is empty
        if (ObjectUtils.isEmpty(caseData)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService
                            .updateRepresentativeContactDetails(userToken, caseData, SUBMISSION_REFERENCE));
            assertThat(gex.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND);
            return;
        }
        // When only user token is empty
        if (ObjectUtils.isEmpty(userToken)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.updateRepresentativeContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(ERROR_INVALID_USER_TOKEN);
            return;
        }
        // when et3ResponseService.getRepresentedRespondentIndexes(INVALID_USER_TOKEN, INVALID_CASE_ID)
        // gives case roles not found exception.
        if (VALID_USER_TOKEN.equals(userToken) && INVALID_CASE_ID.equals(caseData.getCcdID())) {
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(INVALID_CASE_ID)).thenReturn(getInvalidCaseUserAssignmentData());
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.updateRepresentativeContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(SYSTEM_ERROR);
        }
        // TODO add missing tests
    }

    private static Stream<Arguments> generateTestUpdateRepresentativeContactDetails() {
        CaseData validCaseData = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withEt3RepresentingRespondent("Antonio Vazquez")
                .withSubmitEt3Respondent("Antonio Vazquez")
                .build();
        validCaseData.setCcdID(VALID_CASE_ID);
        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        validCaseData.setEt3ResponseAddress(address);
        validCaseData.setEt3ResponsePhone("1234567890");

        CaseData invalidCaseData = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withEt3RepresentingRespondent("Antonio Vazquez")
                .withSubmitEt3Respondent("Antonio Vazquez")
                .build();
        invalidCaseData.setCcdID(INVALID_CASE_ID);

        CaseData notRepresentedCaseData = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withEt3RepresentingRespondent("Antonio Vazquez")
                .withSubmitEt3Respondent("Antonio Vazquez")
                .build();
        invalidCaseData.setCcdID(VALID_CASE_ID_RETURNS_INVALID_USER);
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(VALID_USER_TOKEN, null),
                Arguments.of(null, validCaseData),
                Arguments.of(VALID_USER_TOKEN, invalidCaseData),
                Arguments.of(VALID_USER_TOKEN, notRepresentedCaseData),
                Arguments.of(VALID_USER_TOKEN, validCaseData)
        );
    }

    private static @NotNull CaseUserAssignmentData getInvalidCaseUserAssignmentData() {
        CaseUserAssignment validRespondentSolicitorCaseUserAssignment = new CaseUserAssignment();
        validRespondentSolicitorCaseUserAssignment.setCaseId(VALID_CASE_ID);
        validRespondentSolicitorCaseUserAssignment.setUserId(USER_ID);
        validRespondentSolicitorCaseUserAssignment.setCaseRole("[CLAIMANTSOLICITOR]");
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(validRespondentSolicitorCaseUserAssignment));
        return caseUserAssignmentData;
    }

}
