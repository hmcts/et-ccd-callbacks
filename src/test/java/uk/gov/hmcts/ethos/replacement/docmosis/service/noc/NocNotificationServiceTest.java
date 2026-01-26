package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
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
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.LawOfDemeter"})
class NocNotificationServiceTest {
    private static final String NEW_ORG_ADMIN_EMAIL = "orgadmin1@test.com";
    private static final String OLD_ORG_ADMIN_EMAIL = "orgadmin2@test.com";
    private static final String NEW_ORG_ID = "new_organisation_id";
    private static final String OLD_ORG_ID = "old_organisation_id";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String REPRESENTATIVE_ID = "Representative ID";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL = "claimant_representative@hmcts.org";
    private static final String RESPONDENT_ID = "Respondent ID";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String CASE_ID = "1234567890123456";
    private static final String ETHOS_CASE_REFERENCE = "6000001/2026";
    private static final String EXUI_CASE_LINK = "http://localhost:3000/cases/case-details/" + CASE_ID;
    private static final String CLAIMANT_TEMPLATE_ID = "3d0c5784-0055-4863-9c03-7d37d9b2ad8d";
    private static final String FIELD_NAME_CLAIMANT_TEMPLATE_ID = "claimantTemplateId";
    private static final String FIELD_NAME_PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID =
            "previousRespondentSolicitorTemplateId";
    private static final String PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID = "fe52b39f-852c-43ca-a42a-b9a27c43b130";
    private static final String FIELD_NAME_TRIBUNAL_TEMPLATE_ID = "tribunalTemplateId";
    private static final String TRIBUNAL_TEMPLATE_ID = "1d5efcbd-1971-4ebe-bfe8-72ba36b5abac";
    private static final String FIELD_NAME_RESPONDENT_TEMPLATE_ID = "respondentTemplateId";
    private static final String RESPONDENT_TEMPLATE_ID = "a3539d79-65c0-491c-b578-b58cf321f83e";
    private static final String ADMIN_USER_TOKEN = "adminUserToken";
    private static final String AUTH_TOKEN = "authToken";
    private static final String ORGANISATION_ADMIN_EMAIL = "organisation_admin@hmcts.org";
    private static final String TRIBUNAL_CORRESPONDENCE_EMAIL = "tribunal_correspondence@hmcts.org";
    private static final String RESPONDENT_EMAIL = "respondent@hmcts.org";

    private static final String EXCEPTION_RESPONDENT_EMAIL_SEND =
            "Dummy exception occurred while sending email to respondent";
    private static final String EXPECTED_ERROR_FAILED_TO_SEND_EMAIL_RESPONDENT =
            "Failed to send email to respondent respondent@hmcts.org, error: Dummy exception occurred while sending "
                    + "email to respondent";

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

    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingUtils.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
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
    void theSendRespondentRepresentationRemovalNotifications() {
        // when revoked representatives are empty should not send any e-mail
        CaseDetails oldCaseDetails = new CaseDetails();
        CaseDetails newCaseDetails = new CaseDetails();
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails, null);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(anyString(), anyString(), anyMap());
        // when revoked representatives has only one invalid representative should not send any e-mail
        List<RepresentedTypeRItem> revokedRepresentatives = List.of(RepresentedTypeRItem.builder().build());
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(anyString(), anyString(), anyMap());
        // when representative's respondent not found should not send any email.
        revokedRepresentatives.getFirst().setId(REPRESENTATIVE_ID);
        revokedRepresentatives.getFirst().setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID).build());
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(anyString(), anyString(), anyMap());
        // when claimant is not system user and not represented by my hmcts organisation should not send email to
        // claimant.
        oldCaseDetails.setCaseData(new  CaseData());
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        oldCaseDetails.getCaseData().setRespondentCollection(List.of(respondent));
        oldCaseDetails.getCaseData().setClaimantRepresentedQuestion(NO);
        oldCaseDetails.getCaseData().setRepresentativeClaimantType(RepresentedTypeC.builder()
                .representativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL).build());
        oldCaseDetails.getCaseData().setClaimant(CLAIMANT_NAME);
        oldCaseDetails.getCaseData().setEthosCaseReference(ETHOS_CASE_REFERENCE);
        oldCaseDetails.getCaseData().getRepresentativeClaimantType().setMyHmctsOrganisation(Organisation.builder()
                .build());
        newCaseDetails.setCaseId(CASE_ID);
        oldCaseDetails.setCaseId(CASE_ID);
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(anyString(), anyString(), anyMap());
        // when claimant not represented by my hmcts organisation but system user should send claimant email
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_CLAIMANT_TEMPLATE_ID, CLAIMANT_TEMPLATE_ID);
        when(emailService.getExuiCaseLink(CASE_ID)).thenReturn(EXUI_CASE_LINK);
        doNothing().when(emailService).sendEmail(eq(CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_REPRESENTATIVE_EMAIL), anyMap());
        oldCaseDetails.getCaseData().setEt1OnlineSubmission(YES);
        oldCaseDetails.getCaseData().setClaimantRepresentedQuestion(NO);
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(CLAIMANT_TEMPLATE_ID),
                eq(CLAIMANT_REPRESENTATIVE_EMAIL), anyMap());
        // when claimant not system user but represented by my hmcts company should send claimant email
        oldCaseDetails.getCaseData().setEt1OnlineSubmission(null);
        oldCaseDetails.getCaseData().setClaimantRepresentedQuestion(YES);
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_TWO)).sendEmail(eq(CLAIMANT_TEMPLATE_ID),
                eq(CLAIMANT_REPRESENTATIVE_EMAIL), anyMap());
        // when revoked representative does not have organisation id should not send any email to organisation admin
        revokedRepresentatives.getFirst().getValue().setRespondentOrganisation(Organisation.builder().build());
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        // when revoked representative has organisation id should send email to organisation admin
        oldCaseDetails.getCaseData().setEt1OnlineSubmission(null);
        oldCaseDetails.getCaseData().setClaimantRepresentedQuestion(NO);
        revokedRepresentatives.getFirst().getValue().getRespondentOrganisation().setOrganisationID(OLD_ORG_ID);
        doNothing().when(emailService).sendEmail(eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID,
                PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        when(organisationClient.getOrganisationById(ADMIN_USER_TOKEN, AUTH_TOKEN, OLD_ORG_ID)).thenReturn(
                new ResponseEntity<>(RetrieveOrgByIdResponse.builder()
                        .superUser(RetrieveOrgByIdResponse.SuperUser.builder().email(ORGANISATION_ADMIN_EMAIL).build())
                        .build(), HttpStatus.OK)
        );
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(PREVIOUS_RESPONDENT_SOLICITOR_TEMPLATE_ID),
                eq(ORGANISATION_ADMIN_EMAIL), anyMap());
        // when old case data has tribunal correspondence email should send email to tribunal
        revokedRepresentatives.getFirst().getValue().getRespondentOrganisation().setOrganisationID(StringUtils.EMPTY);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_TRIBUNAL_TEMPLATE_ID, TRIBUNAL_TEMPLATE_ID);
        oldCaseDetails.getCaseData().setTribunalCorrespondenceEmail(TRIBUNAL_CORRESPONDENCE_EMAIL);
        doNothing().when(emailService).sendEmail(eq(TRIBUNAL_TEMPLATE_ID), eq(TRIBUNAL_CORRESPONDENCE_EMAIL), anyMap());
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(TRIBUNAL_TEMPLATE_ID),
                eq(TRIBUNAL_CORRESPONDENCE_EMAIL), anyMap());
        // when respondent doesn't have email address should not send any email to respondent
        oldCaseDetails.getCaseData().setTribunalCorrespondenceEmail(StringUtils.EMPTY);
        ReflectionTestUtils.setField(nocNotificationService, FIELD_NAME_RESPONDENT_TEMPLATE_ID, RESPONDENT_TEMPLATE_ID);
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ZERO)).sendEmail(eq(RESPONDENT_TEMPLATE_ID),
                eq(RESPONDENT_EMAIL), anyMap());
        // when respondent has email address but not able to send e-mail should log exception
        respondent.getValue().setRespondentEmail(RESPONDENT_EMAIL);
        doThrow(new RuntimeException(EXCEPTION_RESPONDENT_EMAIL_SEND)).when(emailService).sendEmail(
                eq(RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_ONE)).sendEmail(eq(RESPONDENT_TEMPLATE_ID),
                eq(RESPONDENT_EMAIL), anyMap());
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_ERROR_FAILED_TO_SEND_EMAIL_RESPONDENT);
        // if there is no exception thrown, should successfully send email
        doNothing().when(emailService).sendEmail(eq(RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
        nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                revokedRepresentatives);
        verify(emailService, times(NumberUtils.INTEGER_TWO)).sendEmail(eq(RESPONDENT_TEMPLATE_ID),
                eq(RESPONDENT_EMAIL), anyMap());
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

        caseDetailsBefore.getCaseData().setTribunalCorrespondenceEmail("tribunal@email.com");

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
        verify(emailService, times(1)).sendEmail(any(), eq("tribunal@email.com"), any());
    }
}
