package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_ADDITION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.LawOfDemeter"})
class NocNotificationServiceTest {
    private static final String TRIBUNAL_EMAIL = "tribunal@email.com";
    private static final String NEW_ORG_ADMIN_EMAIL = "orgadmin1@test.com";
    private static final String OLD_ORG_ADMIN_EMAIL = "orgadmin2@test.com";
    private static final String NEW_ORG_ID = "new_organisation_id";
    private static final String OLD_ORG_ID = "old_organisation_id";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String REPRESENTATIVE_ID = "Representative ID";
    private static final String CLAIMANT_EMAIL = "claimant@hmcts.org";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL = "claimant_representative@hmcts.org";
    private static final String RESPONDENT_REPRESENTATIVE_EMAIL = "claimant_representative@hmcts.org";
    private static final String TRIBUNAL_CORRESPONDENCE_EMAIL = "tribunal_correspondence@hmcts.org";
    private static final String RESPONDENT_ID = "Respondent ID";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String CASE_ID = "1234567890123456";
    private static final String ETHOS_CASE_REFERENCE = "6000001/2026";
    private static final String CITIZEN_CASE_LINK = "http://localhost:3001/citizen-hub/" + CASE_ID;
    private static final String EXUI_CASE_LINK = "http://localhost:3455/cases/case-details/" + CASE_ID;
    private static final String FIELD_NAME_CLAIMANT_TEMPLATE_ID = "claimantTemplateId";
    private static final String CLAIMANT_TEMPLATE_ID = "3d0c5784-0055-4863-9c03-7d37d9b2ad8d";
    private static final String FIELD_NAME_PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID =
            "previousRespondentSolicitorTemplateId";
    private static final String PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID = "fe52b39f-852c-43ca-a42a-b9a27c43b130";
    private static final String FIELD_NAME_NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID = "newRespondentSolicitorTemplateId";
    private static final String NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID = "8fe52f24-40b2-4986-8f86-4dd1af311cbd";
    private static final String FIELD_NAME_TRIBUNAL_TEMPLATE_ID = "tribunalTemplateId";
    private static final String TRIBUNAL_TEMPLATE_ID = "1d5efcbd-1971-4ebe-bfe8-72ba36b5abac";
    private static final String FIELD_NAME_RESPONDENT_TEMPLATE_ID = "respondentTemplateId";
    private static final String RESPONDENT_TEMPLATE_ID = "a3539d79-65c0-491c-b578-b58cf321f83e";
    private static final String ADMIN_USER_TOKEN = "adminUserToken";
    private static final String AUTH_TOKEN = "authToken";
    private static final String ORGANISATION_ADMIN_EMAIL = "organisation_admin@hmcts.org";
    private static final String RESPONDENT_EMAIL = "respondent@hmcts.org";

    private static final String EXCEPTION_RESPONDENT_EMAIL_SEND =
            "Dummy exception occurred while sending email to respondent";

    private static final String EXPECTED_WARNING_FAILED_TO_SEND_EMAIL_RESPONDENT =
            "Failed to send noc notification email to respondent, case id: " + CASE_ID + ", error: Dummy exception "
                    + "occurred while sending email to respondent";
    private static final String EXPECTED_WARNING_INVALID_RESPONDENT = "Invalid respondent while sending Notice of "
            + "Change (NoC) respondent representative removal notification for case " + CASE_ID + ".";
    private static final String EXPECTED_WARNING_MISSING_EMAIL_ADDRESS = "Missing respondent email address while "
            + "sending Notice of Change (NoC) respondent representative removal notification for case "
            + CASE_ID + ".";
    private static final String EXPECTED_WARNING_INVALID_CASE_DETAILS_WITHOUT_CASE_ID = "Invalid case details while "
            + "sending Notice of Change (NoC) respondent representative removal notification for case .";
    private static final String EXPECTED_WARNING_INVALID_CASE_DETAILS_WITH_CASE_ID = "Invalid case details while "
            + "sending Notice of Change (NoC) respondent representative removal notification for case " + CASE_ID + ".";
    private static final String EXPECTED_WARNING_NO_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITHOUT_CASEID =
            "Invalid case details. Unable to notify claimant for respondent representative update. Case id: ";
    private static final String EXPECTED_WARN_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITH_CASEID =
            "Invalid case details. Unable to notify claimant for respondent representative update. Case id: "
                    + CASE_ID;
    private static final String EXPECTED_WARNING_RESPONDENT_NAME_MISSING_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE =
            "Respondent name is missing. Unable to notify claimant for respondent representative update. Case id: "
                    + CASE_ID;
    private static final String EXPECTED_WARNING_CLAIMANT_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE =
            "Claimant email not found. Unable to notify claimant for respondent representative update. Case id: "
                    + CASE_ID;
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT =
            "Failed to send noc notification email to claimant, case id: " + CASE_ID + ", error: Dummy exception "
                    + "occurred while sending email to respondent";
    private static final String EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE =
            "Invalid case details. Unable to notify organisation for respondent representative update. Case id: , "
                    + "NOC type: Removal";
    private static final String
            EXPECTED_WARNING_INVALID_REPRESENTATIVE_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE = "Invalid case "
            + "details. Unable to notify organisation for respondent representative update. Case id: " + CASE_ID
            + ", NOC type: Removal";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION =
            "Failed to send NOC notification email to organisation admin, case id: " + CASE_ID + ", error: Dummy "
                    + "exception occurred while sending email to respondent";
    private static final String
            EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE_WITHOUT_CASE_ID =
            "Invalid case details. Unable to notify tribunal for respondent representative update. Case id: , "
                    + "NOC type: Removal";
    private static final String
            EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE_WITH_CASE_ID =
            "Invalid case details. Unable to notify tribunal for respondent representative update. Case id: "
                    + "1234567890123456, NOC type: Removal";
    private static final String EXPECTED_WARNING_TRIBUNAL_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE =
            "Tribunal email not found. Unable to notify organisation for respondent representative update. "
                + "Case id: 1234567890123456,  NOC type: Removal";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_TRIBUNAL =
            "Failed to send email to tribunal, case id: " + CASE_ID + ", error: Dummy exception occurred while "
                    + "sending email to respondent";

    private static final String EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_NEW_REPRESENTATIVE_WITHOUT_CASE_ID =
            "Invalid case details. Unable to notify new representative. Case id: ";
    public static final String EXPECTED_WARNING_INVALID_PARTY_NAME_TO_NOTIFY_NEW_REPRESENTATIVE =
            "Invalid party name. Unable to notify new representative. Case id: " + CASE_ID;
    public static final String EXPECTED_WARNING_INVALID_REP_EMAIL_NOTIFY_NEW_REPRESENTATIVE =
            "Invalid representative email. Unable to notify new representative. Case id: " + CASE_ID;
    public static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_NEW_REPRESENTATIVE =
            "Failed to send email to new representative, case id: " + CASE_ID + ", error: Dummy exception occurred "
                    + "while sending email to respondent";
    public static final String
            EXPECTED_WARNING_INVALID_CASE_DETAILS_CLAIMANT_NOT_NOTIFIED_OF_REMOVAL_OF_REPRESENTATIVE_WITHOUT_CASEID =
            "Invalid case details. Unable to notify claimant for removal of representative update. Case id: .";
    public static final String
            EXPECTED_WARNING_INVALID_CASE_DETAILS_CLAIMANT_NOT_NOTIFIED_OF_REMOVAL_OF_REPRESENTATIVE_WITH_CASEID =
            "Invalid case details. Unable to notify claimant for removal of representative update. Case id: "
                    + "1234567890123456.";
    public static final String
            EXPECTED_WARNING_INVALID_CLAIMANT_EMAIL_CLAIMANT_NOT_NOTIFIED_FOR_REMOVAL_OF_REPRESENTATIVE =
            "Invalid claimant email. Unable to notify claimant for removal of representative update. Case id: "
                    + "1234567890123456. Exception: Could not find claimant email address.";
    public static final String EXPECTED_WARNING_FAILED_TO_SEND_REMOVAL_OF_REPRESENTATIVE_CLAIMANT =
            "Failed to send email to claimant for removal of representative, case id: 1234567890123456, error: "
                    + "Dummy exception occurred while sending email to respondent";

    @InjectMocks
    private NocNotificationService nocNotificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseAccessService caseAccessService;
    @Mock
    private EmailNotificationService emailNotificationService;
    private CaseDetails caseDetailsBefore;
    private CaseDetails caseDetailsNew;

    @Mock
    private NocRespondentHelper nocRespondentHelper;

    private RespondentSumTypeItem respondentSumTypeItem;
    private CaseDetails validCaseDetails;

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(NocNotificationService.class);
        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId(RESPONDENT_ID);
        RespondentSumType respondentSumType = RespondentSumType.builder().respondentName(RESPONDENT_NAME)
                .respondentEmail(RESPONDENT_EMAIL).build();
        respondentSumTypeItem.setValue(respondentSumType);
        validCaseDetails = new CaseDetails();
        validCaseDetails.setCaseId(CASE_ID);
        validCaseDetails.setCaseData(new CaseData());
        validCaseDetails.getCaseData().setEthosCaseReference(ETHOS_CASE_REFERENCE);
        validCaseDetails.getCaseData().setClaimant(CLAIMANT_NAME);
        validCaseDetails.getCaseData().setRespondentCollection(List.of(respondentSumTypeItem));
        validCaseDetails.getCaseData().setRepresentativeClaimantType(RepresentedTypeC.builder()
                .representativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL).build());
        validCaseDetails.getCaseData().setClaimant(CLAIMANT_NAME);
        validCaseDetails.getCaseData().setEthosCaseReference(ETHOS_CASE_REFERENCE);
        validCaseDetails.getCaseData().getRepresentativeClaimantType().setMyHmctsOrganisation(Organisation.builder()
                .build());
        Organisation organisationToAdd = Organisation.builder()
            .organisationID(NEW_ORG_ID)
            .organisationName("New Organisation").build();
        Organisation organisationToRemove = Organisation.builder()
            .organisationID(OLD_ORG_ID)
            .organisationName("Old Organisation").build();

        caseDetailsNew = CaseDataBuilder.builder()
            .withEthosCaseReference("12345/6789")
            .withClaimantType("claimant@unrepresented.com")
            .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
            .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
            .withRespondentWithAddress("Respondent Unrepresented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null, "respondent@unrepresented.com")
            .withRespondentWithAddress("Respondent Represented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withTwoRespondentRepresentative(NEW_ORG_ID, OLD_ORG_ID, NEW_ORG_ADMIN_EMAIL, OLD_ORG_ADMIN_EMAIL)
            .withRespondent("Respondent", YES, "2022-03-01", "res@rep.com", false)
            .withChangeOrganisationRequestField(
                organisationToAdd,
                organisationToRemove, null,
                null)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetailsBefore = CaseDataBuilder.builder()
            .withEthosCaseReference("12345/6789")
            .withClaimantType("claimant@unrepresented.com")
            .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
            .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
            .withRespondentWithAddress("Respondent Unrepresented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null, "respondent@unrepresented.com")
            .withRespondentWithAddress("Respondent Represented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withTwoRespondentRepresentative(NEW_ORG_ID, OLD_ORG_ID, NEW_ORG_ADMIN_EMAIL, OLD_ORG_ADMIN_EMAIL)
            .withRespondent("Respondent", YES, "2022-03-01",
                "res@rep.com", false)
            .withChangeOrganisationRequestField(
                organisationToAdd,
                organisationToRemove, null,
                null)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetailsBefore.setCaseId("1682497607486678");
        caseDetailsNew.setCaseId("1682497607486678");
        caseDetailsBefore.getCaseData().setClaimant("Claimant LastName");
        caseDetailsNew.getCaseData().setClaimant("Claimant LastName");
        caseDetailsBefore.getCaseData().setTribunalCorrespondenceEmail("respondent@unrepresented.com");

        when(authTokenGenerator.generate()).thenReturn("authToken");
        when(adminUserService.getAdminUserToken()).thenReturn("adminUserToken");
    }

    @Test
    void theSendRespondentRepresentationUpdateNotifications() {
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_RESPONDENT_TEMPLATE_ID, RESPONDENT_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_CLAIMANT_TEMPLATE_ID, CLAIMANT_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID,
                PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_TRIBUNAL_TEMPLATE_ID, TRIBUNAL_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID,
                NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID);
        // when revoked representatives are empty should not send any email
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        nocNotificationService.sendRespondentRepresentationUpdateNotifications(validCaseDetails, representatives,
                NOC_TYPE_REMOVAL);
        verify(emailService, times(LoggerTestUtils.INTEGER_ZERO)).sendEmail(anyString(), anyString(), anyMap());
        // when noc type is empty should not send any email
        nocNotificationService.sendRespondentRepresentationUpdateNotifications(validCaseDetails, representatives,
                StringUtils.EMPTY);
        verify(emailService, times(LoggerTestUtils.INTEGER_ZERO)).sendEmail(anyString(), anyString(), anyMap());
        // when respondent of representative not found should not send email
        representatives.add(RepresentedTypeRItem.builder().build());
        nocNotificationService.sendRespondentRepresentationUpdateNotifications(validCaseDetails, representatives,
                NOC_TYPE_REMOVAL);
        verify(emailService, times(LoggerTestUtils.INTEGER_ZERO)).sendEmail(anyString(), anyString(), anyMap());
        // when noc type is removal should not send e-mail to claimant representative
        representatives.getFirst().setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID)
                .respRepName(RESPONDENT_NAME).respondentOrganisation(Organisation.builder()
                        .organisationID(OLD_ORG_ID).build()).build());
        representatives.getFirst().setId(REPRESENTATIVE_ID);
        validCaseDetails.getCaseData().setClaimantRepresentedQuestion(YES);
        validCaseDetails.getCaseData().setRepresentativeClaimantType(RepresentedTypeC.builder().myHmctsOrganisation(
                Organisation.builder().build()).representativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL).build());
        validCaseDetails.getCaseData().setTribunalCorrespondenceEmail(TRIBUNAL_CORRESPONDENCE_EMAIL);
        when(emailService.getCitizenCaseLink(CASE_ID)).thenReturn(CITIZEN_CASE_LINK);
        when(emailService.getExuiCaseLink(CASE_ID)).thenReturn(EXUI_CASE_LINK);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        when(organisationClient.getOrganisationById(ADMIN_USER_TOKEN, AUTH_TOKEN, OLD_ORG_ID)).thenReturn(
                new ResponseEntity<>(RetrieveOrgByIdResponse.builder().superUser(RetrieveOrgByIdResponse.SuperUser
                        .builder().email(ORGANISATION_ADMIN_EMAIL).build()).build(), HttpStatus.OK));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyMap());
        nocNotificationService.sendRespondentRepresentationUpdateNotifications(validCaseDetails, representatives,
                NOC_TYPE_REMOVAL);
        verify(emailService, times(LoggerTestUtils.INTEGER_FOUR)).sendEmail(anyString(), anyString(), anyMap());
        // when noc type is addition should send e-mail to claimant representative
        representatives.getFirst().getValue().setRepresentativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL);
        nocNotificationService.sendRespondentRepresentationUpdateNotifications(validCaseDetails, representatives,
                NOC_TYPE_ADDITION);
        verify(emailService, times(LoggerTestUtils.INTEGER_NINE)).sendEmail(anyString(), anyString(), anyMap());
    }

    @Test
    void theNotifyRespondentOfRepresentativeUpdate() {
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_RESPONDENT_TEMPLATE_ID, RESPONDENT_TEMPLATE_ID);
        // when case details is empty should not send email and log invalid case details warning
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(null, respondent);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(eq(RESPONDENT_TEMPLATE_ID),
                eq(RESPONDENT_EMAIL), anyMap());
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_WITHOUT_CASE_ID);
        // when case details not have case data should not send email and log invalid case details warning
        CaseDetails caseDetails = new CaseDetails();
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_WITHOUT_CASE_ID);
        // when case detail not have ethos case reference  should not send email and log invalid case details warning
        caseDetails.setCaseId(CASE_ID);
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_WITH_CASE_ID);
        // when case detail not have claimant  should not send email and log invalid case details warning
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(ETHOS_CASE_REFERENCE);
        caseDetails.setCaseData(caseData);
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FOUR,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_WITH_CASE_ID);
        // when case details not have respondent collection should not send email and log invalid case details warning
        caseData.setClaimant(CLAIMANT_NAME);
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FIVE,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_WITH_CASE_ID);
        // when respondent is empty should not send email and log respondent email not found warning
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, null);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_SIX, EXPECTED_WARNING_INVALID_RESPONDENT);
        // when respondent not has email should not send email and log respondent email not found warning
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(eq(RESPONDENT_TEMPLATE_ID),
                eq(RESPONDENT_EMAIL), anyMap());
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_SEVEN, EXPECTED_WARNING_MISSING_EMAIL_ADDRESS);
        // when not able to send email should log failed to send email warning
        respondent.getValue().setRespondentEmail(RESPONDENT_EMAIL);
        doThrow(new RuntimeException(EXCEPTION_RESPONDENT_EMAIL_SEND)).when(emailService).sendEmail(
                eq(RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(RESPONDENT_TEMPLATE_ID),
                eq(RESPONDENT_EMAIL), anyMap());
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_EIGHT,
                EXPECTED_WARNING_FAILED_TO_SEND_EMAIL_RESPONDENT);
        // successfully sends notification email to respondent
        respondent.getValue().setRespondentEmail(RESPONDENT_EMAIL);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_RESPONDENT_TEMPLATE_ID, RESPONDENT_TEMPLATE_ID);
        doNothing().when(emailService).sendEmail(eq(RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
        nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        verify(emailService, times(NumberUtils.INTEGER_TWO)).sendEmail(eq(RESPONDENT_TEMPLATE_ID),
                eq(RESPONDENT_EMAIL), anyMap());
    }

    @Test
    void theNotifyClaimantOfRespondentRepresentativeUpdate() {
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_CLAIMANT_TEMPLATE_ID, CLAIMANT_TEMPLATE_ID);
        // when case details is empty should not send email to claimant and log notification warning
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(null, RESPONDENT_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_NO_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITHOUT_CASEID);
        // when case details not have case id should not send email to claimant and log notification warning
        CaseDetails caseDetails = new CaseDetails();
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_NO_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITHOUT_CASEID);
        // when case details not have case data should not send email to claimant and log notification warning
        caseDetails.setCaseId(CASE_ID);
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_WARN_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITH_CASEID);
        // when case data not has ethos case reference should not send email to claimant and log notification warning
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FOUR,
                EXPECTED_WARN_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITH_CASEID);
        // when case data not has claimant should not send email to claimant and log notification warning
        caseData.setEthosCaseReference(ETHOS_CASE_REFERENCE);
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FIVE,
                EXPECTED_WARN_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITH_CASEID);
        // when case data respondent collection is empty should not send email to claimant and log notification warning
        caseData.setClaimant(CLAIMANT_NAME);
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_SIX,
                EXPECTED_WARN_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE_WITH_CASEID);
        // when respondent name is empty should not send email to claimant and log notification warning
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, StringUtils.EMPTY);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_SEVEN,
                EXPECTED_WARNING_RESPONDENT_NAME_MISSING_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE);
        // when claim is created by caseworker should not send notification email
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        verify(emailService, times(NumberUtils.INTEGER_ZERO))
                .sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        // when claimant or his/her representative not has email address should not send email to claimant and log
        // notification warning
        caseData.setEt1OnlineSubmission(YES);
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_EIGHT,
                EXPECTED_WARNING_CLAIMANT_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE);
        // when not able to send email to claimant should log exception
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(CLAIMANT_EMAIL);
        caseData.setClaimantType(claimantType);
        when(emailService.getExuiCaseLink(CASE_ID)).thenReturn(EXUI_CASE_LINK);
        doThrow(new RuntimeException(EXCEPTION_RESPONDENT_EMAIL_SEND)).when(emailService)
                .sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        verify(emailService, times(NumberUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_NINE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT);
        // successfully sends respondent representative update notification email to claimant
        doNothing().when(emailService)
                .sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        nocNotificationService.notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, RESPONDENT_NAME);
        verify(emailService, times(NumberUtils.INTEGER_TWO))
                .sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
    }

    @Test
    @SneakyThrows
    void theNotifyOrganisationOfRespondentRepresentativeUpdate() {
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID,
                PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID,
                NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID);
        // when case details not valid should log invalid case details warning
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        nocNotificationService.notifyOrganisationOfRespondentRepresentativeUpdate(null, representative,
                RESPONDENT_NAME, NOC_TYPE_REMOVAL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE);
        // when can not notify organisation should log invalid representative
        nocNotificationService.notifyOrganisationOfRespondentRepresentativeUpdate(validCaseDetails, representative,
                RESPONDENT_NAME, NOC_TYPE_REMOVAL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_INVALID_REPRESENTATIVE_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE);
        // when organisation response is empty should log invalid parameters
        representative.setId(REPRESENTATIVE_ID);
        representative.setValue(RepresentedTypeR.builder().respondentOrganisation(Organisation.builder()
                .organisationID(OLD_ORG_ID).build()).build());
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        when(organisationClient.getOrganisationById(ADMIN_USER_TOKEN, AUTH_TOKEN, OLD_ORG_ID)).thenReturn(null);
        nocNotificationService.notifyOrganisationOfRespondentRepresentativeUpdate(validCaseDetails, representative,
                RESPONDENT_NAME, NOC_TYPE_REMOVAL);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        // when send email throws exception should log that exception
        RetrieveOrgByIdResponse orgByIdResponse = RetrieveOrgByIdResponse.builder().superUser(RetrieveOrgByIdResponse
                .SuperUser.builder().email(ORGANISATION_ADMIN_EMAIL).build()).build();
        ResponseEntity<RetrieveOrgByIdResponse> orgResponse = new ResponseEntity<>(orgByIdResponse, HttpStatus.OK);
        when(organisationClient.getOrganisationById(ADMIN_USER_TOKEN, AUTH_TOKEN, OLD_ORG_ID)).thenReturn(orgResponse);
        doThrow(new RuntimeException(EXCEPTION_RESPONDENT_EMAIL_SEND)).when(emailService).sendEmail(
                eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID), eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        nocNotificationService.notifyOrganisationOfRespondentRepresentativeUpdate(validCaseDetails, representative,
                RESPONDENT_NAME, NOC_TYPE_REMOVAL);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION);
        // Successfully send removal of old representative notification
        doNothing().when(emailService).sendEmail(eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        nocNotificationService.notifyOrganisationOfRespondentRepresentativeUpdate(validCaseDetails, representative,
                RESPONDENT_NAME, NOC_TYPE_REMOVAL);
        verify(emailService, times(NumberUtils.INTEGER_TWO)).sendEmail(eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        // Successfully send addition of old representative notification
        when(emailService.getCitizenCaseLink(CASE_ID)).thenReturn(CITIZEN_CASE_LINK);
        doNothing().when(emailService).sendEmail(eq(NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        nocNotificationService.notifyOrganisationOfRespondentRepresentativeUpdate(validCaseDetails, representative,
                RESPONDENT_NAME, NOC_TYPE_ADDITION);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
    }

    @Test
    void theSendTribunalEmail() {
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_TRIBUNAL_TEMPLATE_ID, TRIBUNAL_TEMPLATE_ID);
        // when case details are empty should log invalid case details warning
        nocNotificationService.notifyTribunalOfRespondentRepresentativeUpdate(null, NOC_TYPE_REMOVAL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE_WITHOUT_CASE_ID);
        // when case details are invalid should log invalid case details warning
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        nocNotificationService.notifyTribunalOfRespondentRepresentativeUpdate(caseDetails, NOC_TYPE_REMOVAL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE_WITH_CASE_ID);
        // when tribunal email not found should log tribunal email not found warning
        nocNotificationService.notifyTribunalOfRespondentRepresentativeUpdate(validCaseDetails, NOC_TYPE_REMOVAL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_WARNING_TRIBUNAL_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE);
        // when not able to send email to tribunal should log exception
        validCaseDetails.getCaseData().setTribunalCorrespondenceEmail(TRIBUNAL_CORRESPONDENCE_EMAIL);
        doThrow(new RuntimeException(EXCEPTION_RESPONDENT_EMAIL_SEND)).when(emailService)
                .sendEmail(eq(TRIBUNAL_TEMPLATE_ID), eq(TRIBUNAL_CORRESPONDENCE_EMAIL), anyMap());
        nocNotificationService.notifyTribunalOfRespondentRepresentativeUpdate(validCaseDetails, NOC_TYPE_REMOVAL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FOUR,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_TRIBUNAL);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(TRIBUNAL_TEMPLATE_ID),
                eq(TRIBUNAL_CORRESPONDENCE_EMAIL), anyMap());
        // should successfully send email
        doNothing().when(emailService).sendEmail(eq(TRIBUNAL_TEMPLATE_ID), eq(TRIBUNAL_CORRESPONDENCE_EMAIL), anyMap());
        nocNotificationService.notifyTribunalOfRespondentRepresentativeUpdate(validCaseDetails, NOC_TYPE_REMOVAL);
        verify(emailService, times(NumberUtils.INTEGER_TWO)).sendEmail(eq(TRIBUNAL_TEMPLATE_ID),
                eq(TRIBUNAL_CORRESPONDENCE_EMAIL), anyMap());
    }

    @Test
    void theNotifyRepresentativeOfNewAssignment() {
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID,
                NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID);
        // when case details is empty should log invalid case details warning
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        nocNotificationService.notifyRepresentativeOfNewAssignment(null, RESPONDENT_NAME, representative);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_NEW_REPRESENTATIVE_WITHOUT_CASE_ID);
        // when case details is invalid should log invalid case details warning
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        nocNotificationService.notifyRepresentativeOfNewAssignment(caseDetails, RESPONDENT_NAME, representative);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_NEW_REPRESENTATIVE_WITHOUT_CASE_ID);
        // when party name is empty should log invalid party name warning
        nocNotificationService.notifyRepresentativeOfNewAssignment(validCaseDetails, StringUtils.EMPTY, representative);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_WARNING_INVALID_PARTY_NAME_TO_NOTIFY_NEW_REPRESENTATIVE);
        // when representative email is invalid should log invalid representative warning
        nocNotificationService.notifyRepresentativeOfNewAssignment(validCaseDetails, RESPONDENT_NAME, representative);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FOUR,
                EXPECTED_WARNING_INVALID_REP_EMAIL_NOTIFY_NEW_REPRESENTATIVE);
        // when send email throws exception should log that exception
        representative.setValue(RepresentedTypeR.builder().representativeEmailAddress(RESPONDENT_REPRESENTATIVE_EMAIL)
                .build());
        when(emailService.getExuiCaseLink(CASE_ID)).thenReturn(EXUI_CASE_LINK);
        doThrow(new RuntimeException(EXCEPTION_RESPONDENT_EMAIL_SEND)).when(emailService)
                .sendEmail(eq(NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID), eq(RESPONDENT_REPRESENTATIVE_EMAIL), anyMap());
        nocNotificationService.notifyRepresentativeOfNewAssignment(validCaseDetails, RESPONDENT_NAME, representative);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FIVE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_NEW_REPRESENTATIVE);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(
                eq(NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID), eq(RESPONDENT_REPRESENTATIVE_EMAIL), anyMap());
        // should send email successfully
        doNothing().when(emailService).sendEmail(eq(NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(RESPONDENT_REPRESENTATIVE_EMAIL), anyMap());
        nocNotificationService.notifyRepresentativeOfNewAssignment(validCaseDetails, RESPONDENT_NAME, representative);
        verify(emailService, times(NumberUtils.INTEGER_TWO)).sendEmail(
                eq(NEW_RESPONDENT_SOLICITOR_TEMPLATE_ID), eq(RESPONDENT_REPRESENTATIVE_EMAIL), anyMap());
    }

    @Test
    void theNotifyClaimantOfRepresentationRemoval() {
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_CLAIMANT_TEMPLATE_ID, CLAIMANT_TEMPLATE_ID);
        // when case details is empty should log invalid case details warning
        nocNotificationService.notifyClaimantOfRepresentationRemoval(null);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_CLAIMANT_NOT_NOTIFIED_OF_REMOVAL_OF_REPRESENTATIVE_WITHOUT_CASEID
        );
        // when case details is not valid should log invalid case details warning
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        nocNotificationService.notifyClaimantOfRepresentationRemoval(caseDetails);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_INVALID_CASE_DETAILS_CLAIMANT_NOT_NOTIFIED_OF_REMOVAL_OF_REPRESENTATIVE_WITH_CASEID);
        // when claimant not has email should log invalid claimant email warning
        nocNotificationService.notifyClaimantOfRepresentationRemoval(validCaseDetails);
        when(emailService.getCitizenCaseLink(CASE_ID)).thenReturn(CITIZEN_CASE_LINK);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_WARNING_INVALID_CLAIMANT_EMAIL_CLAIMANT_NOT_NOTIFIED_FOR_REMOVAL_OF_REPRESENTATIVE);
        // when send email throws exception should throw that exception
        validCaseDetails.getCaseData().setClaimantType(new ClaimantType());
        validCaseDetails.getCaseData().getClaimantType().setClaimantEmailAddress(CLAIMANT_EMAIL);
        doThrow(new RuntimeException(EXCEPTION_RESPONDENT_EMAIL_SEND)).when(emailService)
                .sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        nocNotificationService.notifyClaimantOfRepresentationRemoval(validCaseDetails);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL),
                anyMap());
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FOUR,
                EXPECTED_WARNING_FAILED_TO_SEND_REMOVAL_OF_REPRESENTATIVE_CLAIMANT);
        // should send email successfully
        doNothing().when(emailService).sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        nocNotificationService.notifyClaimantOfRepresentationRemoval(validCaseDetails);
        verify(emailService, times(NumberUtils.INTEGER_TWO)).sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL),
                anyMap());
    }

    @Test
    void sendNotificationsShouldSendFiveNotifications() {
        RetrieveOrgByIdResponse.SuperUser oldSuperUser = RetrieveOrgByIdResponse.SuperUser.builder()
                .email(OLD_ORG_ADMIN_EMAIL).build();
        RetrieveOrgByIdResponse retrieveOrgByIdResponse1 = RetrieveOrgByIdResponse.builder()
                .superUser(oldSuperUser).build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(OLD_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse1));

        RetrieveOrgByIdResponse.SuperUser newSuperUser = RetrieveOrgByIdResponse.SuperUser.builder()
                .email(NEW_ORG_ADMIN_EMAIL).build();
        RetrieveOrgByIdResponse retrieveOrgByIdResponse2 = RetrieveOrgByIdResponse.builder()
                .superUser(newSuperUser).build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(NEW_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse2));

        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent");
        respondentSumType.setRespondentEmail("res@rep.com");
        when(nocRespondentHelper.getRespondent(any(), any())).thenReturn(respondentSumType);
        when(emailService.getCitizenCaseLink(any())).thenReturn("http://domain/citizen-hub/1234");
        when(emailService.getExuiCaseLink(anyString())).thenReturn("linkToExui");

        nocNotificationService.sendNotificationOfChangeEmails(
                caseDetailsBefore,
                caseDetailsNew,
                caseDetailsBefore.getCaseData().getChangeOrganisationRequestField());

        // Claimant Representative
        verify(emailService, times(0)).sendEmail(any(), eq("claimant@represented.com"), any());
        //New Representative
        verify(emailService, times(1)).sendEmail(any(), eq(NEW_ORG_ADMIN_EMAIL), any());
        //Old Representative
        verify(emailService, times(1)).sendEmail(any(), eq(OLD_ORG_ADMIN_EMAIL), any());
        // Tribunal
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        // Respondent
        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), any());
    }

    @Test
    void sendNotificationsShouldSendFiveNotifications_NoSuperUserEmail() {
        RetrieveOrgByIdResponse retrieveOrgByIdResponse1 = RetrieveOrgByIdResponse.builder().build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(OLD_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse1));

        RetrieveOrgByIdResponse retrieveOrgByIdResponse2 = RetrieveOrgByIdResponse.builder().build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(NEW_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse2));

        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent");
        respondentSumType.setRespondentEmail("res@rep.com");
        when(nocRespondentHelper.getRespondent(any(), any())).thenReturn(respondentSumType);
        when(emailService.getCitizenCaseLink(any())).thenReturn("http://domain/citizen-hub/1234");
        when(emailService.getExuiCaseLink(anyString())).thenReturn("linkToExui");

        nocNotificationService.sendNotificationOfChangeEmails(
                caseDetailsBefore,
                caseDetailsNew,
                caseDetailsBefore.getCaseData().getChangeOrganisationRequestField());

        // Claimant Representative
        verify(emailService, times(0)).sendEmail(any(), eq("claimant@represented.com"), any());
        //New Representative
        verify(emailService, never()).sendEmail(any(), eq(NEW_ORG_ADMIN_EMAIL), any());
        //Old Representative
        verify(emailService, never()).sendEmail(any(), eq(OLD_ORG_ADMIN_EMAIL), any());
        // Tribunal
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        // Respondent
        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), any());
    }

    @Test
    void handleMissingEmails() {
        reset(emailService);

        when(organisationClient.getOrganisationById(anyString(), anyString(), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        CaseData oldCaseData = caseDetailsBefore.getCaseData();

        oldCaseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        oldCaseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        oldCaseData.getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);
        oldCaseData.getRepCollection().get(1).getValue().setRepresentativeEmailAddress(null);
        oldCaseData.setTribunalCorrespondenceEmail(null);

        caseDetailsNew.getCaseData().getRepCollection().getFirst().getValue().setRepresentativeEmailAddress(null);

        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent");
        respondentSumType.setRespondentEmail(null);

        when(nocRespondentHelper.getRespondent(any(), any())).thenReturn(respondentSumType);
        when(emailService.getExuiCaseLink(anyString())).thenReturn("linkToExui");
        nocNotificationService.sendNotificationOfChangeEmails(
                caseDetailsBefore,
                caseDetailsNew,
                caseDetailsBefore.getCaseData().getChangeOrganisationRequestField());

        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }

    @Test
    void handleMissingRepresentativeClaimantType() {
        when(emailService.getExuiCaseLink(anyString())).thenReturn("exuiLink");
        when(emailService.getCitizenCaseLink(any())).thenReturn("citizenLink");

        RetrieveOrgByIdResponse oldOrgByIdResponse = RetrieveOrgByIdResponse.builder()
                .superUser(RetrieveOrgByIdResponse.SuperUser.builder()
                        .email(OLD_ORG_ADMIN_EMAIL)
                        .build())
                .build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(OLD_ORG_ID)))
                .thenReturn(ResponseEntity.ok(oldOrgByIdResponse));

        RetrieveOrgByIdResponse newOrgByIdResponse = RetrieveOrgByIdResponse.builder()
                .superUser(RetrieveOrgByIdResponse.SuperUser.builder()
                        .email(NEW_ORG_ADMIN_EMAIL)
                        .build())
                .build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(NEW_ORG_ID)))
                .thenReturn(ResponseEntity.ok(newOrgByIdResponse));

        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        DynamicFixedListType caseRoleId = new DynamicFixedListType();
        caseRoleId.setValue(dynamicValueType);

        caseDetailsBefore.getCaseData().getChangeOrganisationRequestField().setCaseRoleId(caseRoleId);
        caseDetailsBefore.getCaseData().setTribunalCorrespondenceEmail(TRIBUNAL_EMAIL);

        caseDetailsNew.getCaseData().setRepresentativeClaimantType(null);

        nocNotificationService.sendNotificationOfChangeEmails(
                caseDetailsBefore,
                caseDetailsNew,
                caseDetailsBefore.getCaseData().getChangeOrganisationRequestField()
        );

        verify(emailNotificationService, times(0))
                .getRespondentsAndRepsEmailAddresses(any(), any());
        verify(emailService, times(3)).sendEmail(any(), any(), any());
        verify(emailService, times(1))
                .sendEmail(any(), eq(OLD_ORG_ADMIN_EMAIL), any());
        verify(emailService, times(1))
                .sendEmail(any(), eq(NEW_ORG_ADMIN_EMAIL), any());
        verify(emailService, times(1))
                .sendEmail(any(), eq(TRIBUNAL_EMAIL), any());
    }

    @Test
    void sendNotificationsShouldSendClaimantRepNoCEmails() {
        RetrieveOrgByIdResponse.SuperUser oldSuperUser = RetrieveOrgByIdResponse.SuperUser.builder()
                .email(OLD_ORG_ADMIN_EMAIL).build();
        RetrieveOrgByIdResponse retrieveOrgByIdResponse1 = RetrieveOrgByIdResponse.builder()
                .superUser(oldSuperUser).build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(OLD_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse1));

        RetrieveOrgByIdResponse.SuperUser newSuperUser = RetrieveOrgByIdResponse.SuperUser.builder()
                .email(NEW_ORG_ADMIN_EMAIL).build();
        RetrieveOrgByIdResponse retrieveOrgByIdResponse2 = RetrieveOrgByIdResponse.builder()
                .superUser(newSuperUser).build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(NEW_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse2));

        // Set up caseRoleId to trigger claimant NOC logic
        DynamicFixedListType caseRoleId = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        caseRoleId.setValue(dynamicValueType);
        caseDetailsBefore.getCaseData().getChangeOrganisationRequestField().setCaseRoleId(caseRoleId);

        // Mock respondent email
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent");
        respondentSumType.setRespondentEmail("respondent@unrepresented.com");
        when(nocRespondentHelper.getRespondent(any(), any())).thenReturn(respondentSumType);

        List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setId("123");
        respondentItem.setValue(respondentSumType);
        respondentCollection.add(respondentItem);

        caseDetailsBefore.getCaseData().setRespondentCollection(respondentCollection);
        caseDetailsNew.getCaseData().setRespondentCollection(respondentCollection);

        caseDetailsBefore.getCaseData().setTribunalCorrespondenceEmail(TRIBUNAL_EMAIL);

        // Mock emailService links
        when(emailService.getSyrCaseLink(anyString(), anyString())).thenReturn("syrLink");
        when(emailService.getExuiCaseLink(anyString())).thenReturn("exuiLink");
        when(emailService.getCitizenCaseLink(any())).thenReturn("citizenLink");
        when(caseAccessService.getCaseUserAssignmentsById(anyString())).thenReturn(
                new ArrayList<>());
        when(emailNotificationService.getRespondentsAndRepsEmailAddresses(any(), any()))
                .thenReturn(Map.of("respondent@unrepresented.com", "respondentId"));

        nocNotificationService.sendNotificationOfChangeEmails(
                caseDetailsBefore,
                caseDetailsNew,
                caseDetailsBefore.getCaseData().getChangeOrganisationRequestField());

        // Respondent email notification
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        // Claimant email notification
        verify(emailService, times(1)).sendEmail(any(), eq("claimant@unrepresented.com"), any());
        // Tribunal email notification
        verify(emailService, times(1)).sendEmail(any(), eq(TRIBUNAL_EMAIL), any());
    }
}
