package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CASE_MANAGEMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeItemUtil.createSupportingMaterial;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationUtil.getGenericTseApplicationTypeItem;

@ExtendWith(SpringExtension.class)
class TseClaimantRepReplyServiceTest {
    @MockBean
    private TornadoService tornadoService;
    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private TseService tseService;
    @MockBean
    private ClaimantTellSomethingElseService claimantTellSomethingElseService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private TseClaimantRepReplyService tseClaimantRepReplyService;
    @MockBean
    private FeatureToggleService featureToggleService;

    private static final String TRIBUNAL_EMAIL = "tribunalOffice@test.com";
    private static final String REPLY_TO_TRIB_ACK_TEMPLATE_YES = "replyToTribAckTemplateYes";
    private static final String REPLY_TO_TRIB_ACK_TEMPLATE_NO = "replyToTribAckTemplateNo";
    private static final String REPLY_TO_APP_ACK_TEMPLATE_YES = "replyToAppAckTemplateYes";
    private static final String REPLY_TO_APP_ACK_TEMPLATE_NO = "replyToAppAckTemplateNo";
    private static final String CASE_NUMBER = "9876";
    private static final String TEST_XUI_URL = "exuiUrl";
    private static final String TEST_CUI_URL = "citizenUrl";
    private static final String WITHDRAW_MY_CLAIM = "Withdraw all/part of claim";

    private EmailService emailService;
    private UserDetails userDetails;
    private CaseData caseData;
    private MockedStatic<TseHelper> mockStatic;
    private GenericTseApplicationType genericTseApplicationType;

    @BeforeEach
    void setUp() throws Exception {
        emailService = spy(new EmailUtils());
        tseClaimantRepReplyService = new TseClaimantRepReplyService(tseService, documentManagementService,
                tornadoService, featureToggleService, emailService, claimantTellSomethingElseService, userIdamService);

        userDetails = HelperTest.getUserDetails();
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        doNothing().when(emailService).sendEmail(any(), any(), any());

        mockStatic = mockStatic(TseHelper.class, Mockito.CALLS_REAL_METHODS);
        mockStatic.when(() -> TseHelper.getPersonalisationForResponse(any(), any(), any(), anyBoolean()))
                .thenReturn(Collections.emptyMap());
        mockStatic.when(() -> TseHelper.getPersonalisationForAcknowledgement(any(), any(), anyBoolean()))
                .thenReturn(Collections.emptyMap());

        caseData = CaseDataBuilder.builder()
                .withEthosCaseReference(CASE_NUMBER)
                .withClaimantType("person@email.com")
                .withRespondent("respondent", YES, "01-Jan-2003", false)
                .build();

        genericTseApplicationType = GenericTseApplicationType.builder().applicant(RESPONDENT_TITLE)
                .date("13 December 2022").dueDate("20 December 2022").type(TSE_APP_CHANGE_PERSONAL_DETAILS)
                .copyToOtherPartyYesOrNo(YES).details("Text").applicationState("notStartedYet")
                .number("1").responsesCount("0").status(OPEN_STATE).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString()).value(genericTseApplicationType).build();

        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        caseData.setClaimantRepRespondSelectApplication(TseHelper.populateClaimantRepSelectApplication(caseData));
        caseData.getClaimantRepRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        caseData.setClaimant("Claimant LastName");
    }

    @AfterEach
    void afterEach() {
        mockStatic.close();
    }

    @ParameterizedTest
    @MethodSource("changeApplicationState")
    void changeApplicationState(String applicant, String respondentResponseRequired, String result) {
        genericTseApplicationType.setApplicant(applicant);
        genericTseApplicationType.setClaimantResponseRequired(respondentResponseRequired);

        tseClaimantRepReplyService.updateApplicationState(caseData);

        assertThat(genericTseApplicationType.getApplicationState()).isEqualTo(result);
    }

    private static Stream<Arguments> changeApplicationState() {
        return Stream.of(
                Arguments.of(CLAIMANT_REP_TITLE, NO, NOT_STARTED_YET),
                Arguments.of(CLAIMANT_REP_TITLE, YES, NOT_STARTED_YET),
                Arguments.of(RESPONDENT_TITLE, NO, UPDATED),
                Arguments.of(RESPONDENT_TITLE, YES, UPDATED)

        );
    }

    @Nested
    class SaveReplyToApplication {
        @Test
        void savesReplyCorrectly() {
            caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
            caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

            caseData.setTseResponseText("ResponseText");
            caseData.setTseResponseSupportingMaterial(createSupportingMaterial());

            caseData.setTseResponseHasSupportingMaterial(YES);
            caseData.setTseResponseCopyToOtherParty(NO);
            caseData.setTseResponseCopyNoGiveDetails("It's a secret");

            tseClaimantRepReplyService.saveReplyToApplication(caseData, false);

            TseRespondType replyType = caseData.getGenericTseApplicationCollection().get(0)
                    .getValue().getRespondCollection().get(0).getValue();

            String dateNow = UtilHelper.formatCurrentDate(LocalDate.now());

            assertThat(replyType.getDate()).isEqualTo(dateNow);
            assertThat(replyType.getResponse()).isEqualTo("ResponseText");
            assertThat(replyType.getCopyNoGiveDetails()).isEqualTo("It's a secret");
            assertThat(replyType.getHasSupportingMaterial()).isEqualTo(YES);
            assertThat(replyType.getCopyToOtherParty()).isEqualTo(NO);
            assertThat(replyType.getFrom()).isEqualTo(CLAIMANT_REP_TITLE);
            assertThat(replyType.getSupportingMaterial().get(0).getValue().getUploadedDocument().getDocumentFilename())
                    .isEqualTo("image.png");

            // WA properties
            String dateTimeParsedForTesting = UtilHelper.formatCurrentDate(
                    LocalDateTime.parse(replyType.getDateTime()).toLocalDate()
            );
            assertThat(dateTimeParsedForTesting).isEqualTo(dateNow);
            assertThat(replyType.getApplicationType()).isEqualTo(TSE_APP_CHANGE_PERSONAL_DETAILS);
        }

        @Test
        void saveReplyToApplication_withTribunalResponse_setRespondentResponseRequired() {
            GenericTseApplicationTypeItem genericTseApplicationTypeItem = getGenericTseApplicationTypeItem(NO);
            caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

            caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
            caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

            caseData.setTseResponseText("ResponseText");
            caseData.setTseResponseSupportingMaterial(createSupportingMaterial());

            caseData.setTseResponseHasSupportingMaterial(YES);
            caseData.setTseResponseCopyToOtherParty(NO);
            caseData.setTseResponseCopyNoGiveDetails("It's a secret");

            tseClaimantRepReplyService.saveReplyToApplication(caseData, true);

            String respondentResponseRequired = caseData.getGenericTseApplicationCollection().get(0)
                    .getValue().getRespondentResponseRequired();

            assertThat(respondentResponseRequired).isEqualTo(NO);
        }
    }

    @ParameterizedTest
    @MethodSource("sendRespondingToApplicationEmails")
    void sendRespondingToApplicationEmails(String rule92, VerificationMode isEmailSentToRespondent,
                                           String ackEmailTemplate) {
        mockStatic.when(() -> TseHelper.getPersonalisationForResponse(any(), any(), any(), anyBoolean()))
                .thenReturn(new ConcurrentHashMap<>());

        caseData.setTseResponseCopyToOtherParty(rule92);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(ENGLISH_LANGUAGE);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);

        ReflectionTestUtils.setField(tseClaimantRepReplyService,
                "acknowledgementRule92YesEmailTemplateId", REPLY_TO_APP_ACK_TEMPLATE_YES);
        ReflectionTestUtils.setField(tseClaimantRepReplyService,
                "acknowledgementRule92NoEmailTemplateId", REPLY_TO_APP_ACK_TEMPLATE_NO);

        Organisation respondentOrg = Organisation.builder()
                .organisationID("org_id")
                .organisationName("New Organisation").build();
        RepresentedTypeR representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative("Respondent")
                        .respRepName("Respondent")
                        .representativeEmailAddress("person@email.com")
                        .myHmctsYesNo("Yes")
                        .respondentOrganisation(respondentOrg)
                        .build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId("1111-2222-3333-1111");
        representedTypeRItem.setValue(representedType);
        caseData.setRepCollection(new ArrayList<>());
        caseData.getRepCollection().add(representedTypeRItem);

        caseData.setRespondentCollection(new ArrayList<>());
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId("1111-2222-3333-1111");
        RespondentSumType respondentSumType = RespondentSumType.builder()
                .respondentEmail("respondent@gmail.com")
                .respondentName("Respondent")
                .build();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.getRespondentCollection().add(respondentSumTypeItem);
        tseClaimantRepReplyService.sendRespondingToApplicationEmails(caseDetails, "userToken");

        verify(emailService).sendEmail(any(), eq(userDetails.getEmail()), any());
        verify(emailService, isEmailSentToRespondent)
                .sendEmail(any(), eq(respondentSumType.getRespondentEmail()), any());
        verify(claimantTellSomethingElseService).sendAdminEmail(any());

        verify(emailService)
                .sendEmail(eq(ackEmailTemplate), eq(userDetails.getEmail()), any());
    }

    private static Stream<Arguments> sendRespondingToApplicationEmails() {
        return Stream.of(
                Arguments.of(YES, atLeastOnce(), REPLY_TO_APP_ACK_TEMPLATE_YES),
                Arguments.of(NO, never(), REPLY_TO_APP_ACK_TEMPLATE_NO)
        );
    }

    @ParameterizedTest
    @MethodSource("sendRespondingToApplicationEmails")
    void sendRespondingToApplicationEmailsNoRep(String rule92, VerificationMode isEmailSentToRespondent,
                                           String ackEmailTemplate) {
        mockStatic.when(() -> TseHelper.getPersonalisationForResponse(any(), any(), any(), anyBoolean()))
                .thenReturn(new ConcurrentHashMap<>());
        caseData.setTseResponseCopyToOtherParty(rule92);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(ENGLISH_LANGUAGE);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);

        ReflectionTestUtils.setField(tseClaimantRepReplyService,
                "acknowledgementRule92YesEmailTemplateId", REPLY_TO_APP_ACK_TEMPLATE_YES);
        ReflectionTestUtils.setField(tseClaimantRepReplyService,
                "acknowledgementRule92NoEmailTemplateId", REPLY_TO_APP_ACK_TEMPLATE_NO);

        caseData.setRespondentCollection(new ArrayList<>());
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId("1111-2222-3333-1111");
        RespondentSumType respondentSumType = RespondentSumType.builder()
                .respondentEmail("respondent@gmail.com")
                .respondentName("Respondent")
                .build();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.getRespondentCollection().add(respondentSumTypeItem);
        tseClaimantRepReplyService.sendRespondingToApplicationEmails(caseDetails, "userToken");

        verify(emailService).sendEmail(any(), eq(userDetails.getEmail()), any());
        verify(emailService, isEmailSentToRespondent)
                .sendEmail(any(), eq(respondentSumType.getRespondentEmail()), any());
        verify(claimantTellSomethingElseService).sendAdminEmail(any());

        verify(emailService)
                .sendEmail(eq(ackEmailTemplate), eq(userDetails.getEmail()), any());
    }

    @Test
    void addTseRespondentReplyPdfToDocCollection() throws IOException {
        when(tornadoService.generateEventDocument(any(), anyString(), anyString(), anyString()))
                .thenReturn(new DocumentInfo());
        when(documentManagementService.addDocumentToDocumentField(any()))
                .thenReturn(DocumentFixtures.getUploadedDocumentType());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);

        tseClaimantRepReplyService.addTseClaimantRepReplyPdfToDocCollection(caseData, "testUserToken",
                caseDetails.getCaseTypeId());

        MatcherAssert.assertThat(caseData.getDocumentCollection().size(), is(1));
        MatcherAssert.assertThat(caseData.getDocumentCollection().get(0).getValue().getTopLevelDocuments(),
                is(CASE_MANAGEMENT));
    }

    @ParameterizedTest
    @MethodSource("sendRespondingToTribunalEmails")
    void sendRespondingToTribunalEmails(String rule92, VerificationMode isEmailSentToClaimant,
                                        String ackEmailTemplate) {
        caseData.setTseResponseCopyToOtherParty(rule92);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);
        RepresentedTypeR typeR = new RepresentedTypeR();
        typeR.setMyHmctsYesNo(YES);
        typeR.setRepresentativeEmailAddress("rep@email.com");
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setValue(typeR);
        caseData.setRepCollection(Collections.singletonList(representedTypeRItem));

        when(claimantTellSomethingElseService.getTribunalEmail(any())).thenReturn(TRIBUNAL_EMAIL);

        ReflectionTestUtils.setField(tseClaimantRepReplyService,
                "replyToTribunalAckEmailToLRRule92YesTemplateId", REPLY_TO_TRIB_ACK_TEMPLATE_YES);
        ReflectionTestUtils.setField(tseClaimantRepReplyService,
                "replyToTribunalAckEmailToLRRule92NoTemplateId", REPLY_TO_TRIB_ACK_TEMPLATE_NO);

        Map<String, String> tribunalPersonalisation = Map.of(
                NotificationServiceConstants.CASE_NUMBER, CASE_NUMBER,
                APPLICATION_TYPE, TSE_APP_CHANGE_PERSONAL_DETAILS,
                LINK_TO_EXUI, TEST_XUI_URL + "caseId");

        tseClaimantRepReplyService.sendRespondingToTribunalEmails(caseDetails, "token");
        verify(emailService).sendEmail(any(), eq(TRIBUNAL_EMAIL), eq(tribunalPersonalisation));
    }

    private static Stream<Arguments> sendRespondingToTribunalEmails() {
        return Stream.of(
                Arguments.of(YES, atLeastOnce(), REPLY_TO_TRIB_ACK_TEMPLATE_YES),
                Arguments.of(NO, never(), REPLY_TO_TRIB_ACK_TEMPLATE_NO)
        );
    }

    @Test
    void resetReplyToApplicationPage_resetsData() {
        caseData.setClaimantRepResponseCopyToOtherParty(YES);
        caseData.setClaimantRepResponseCopyNoGiveDetails(YES);
        caseData.setClaimantRepResponseText(YES);
        caseData.setClaimantRepResponseIntro(YES);
        caseData.setClaimantRepResponseTable(YES);
        caseData.setClaimantRepResponseHasSupportingMaterial(YES);
        caseData.setClaimantRepResSupportingMaterial(createSupportingMaterial());
        caseData.setClaimantRepRespondingToTribunal(YES);
        caseData.setClaimantRepRespondingToTribunalText(YES);

        tseClaimantRepReplyService.resetReplyToApplicationPage(caseData);

        assertNull(caseData.getClaimantRepResponseText());
        assertNull(caseData.getClaimantRepResponseIntro());
        assertNull(caseData.getClaimantRepResponseTable());
        assertNull(caseData.getClaimantRepResponseHasSupportingMaterial());
        assertNull(caseData.getClaimantRepResSupportingMaterial());
        assertNull(caseData.getClaimantRepResponseCopyToOtherParty());
        assertNull(caseData.getClaimantRepResponseCopyNoGiveDetails());
        assertNull(caseData.getClaimantRepRespondingToTribunal());
        assertNull(caseData.getClaimantRepRespondingToTribunalText());
    }

    @Test
    void initialResReplyToTribunalTableMarkUp() {
        List<String[]> strings = new ArrayList<>();
        strings.add(new String[] {"applicationDetails", ""});
        when(tseService.getApplicationDetailsRows(any(), any(), anyBoolean())).thenReturn(strings);

        List<String[]> formattedApplicationResponses = new ArrayList<>();
        formattedApplicationResponses.add(new String[] {"responses", ""});
        when(tseService.formatApplicationResponses(any(), any(), anyBoolean()))
                .thenReturn(formattedApplicationResponses);

        tseClaimantRepReplyService.initialResReplyToTribunalTableMarkUp(caseData, "token");

        MatcherAssert.assertThat(caseData.getClaimantRepResponseTable(),
                is("|Application||\r\n|--|--|\r\n|applicationDetails||\r\n|responses||\r\n"));
        MatcherAssert.assertThat(caseData.getClaimantRepRespondingToTribunal(), is(YES));
    }

    @ParameterizedTest
    @MethodSource("validateInput")
    void validateInput(String responseText, String respondingToTribunalText,
                       String supportingMaterial, int expectedErrorCount) {
        caseData.setClaimantRepResponseText(responseText);
        caseData.setClaimantRepRespondingToTribunalText(respondingToTribunalText);
        caseData.setClaimantRepResponseHasSupportingMaterial(supportingMaterial);

        List<String> errors = tseClaimantRepReplyService.validateInput(caseData);

        MatcherAssert.assertThat(errors.size(), is(expectedErrorCount));
    }

    private static Stream<Arguments> validateInput() {
        return Stream.of(
                Arguments.of(null, null, YES, 0),
                Arguments.of("testResponseText", null, NO, 0),
                Arguments.of("testResponseText", null, YES, 0),
                Arguments.of(null, "respondingToTribunalText", NO, 0),
                Arguments.of(null, "respondingToTribunalText", YES, 0),
                Arguments.of(null, null, NO, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("isRespondingToTribunal")
    void isRespondingToTribunal(boolean respondentResponseRequired, boolean isRespondingToTribunal) {
        caseData.setClaimantRepRespondSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("1", "test")));

        mockStatic.when(() -> TseHelper.getClaimantRepSelectedApplicationType(any()))
                .thenReturn(getApplicationType(respondentResponseRequired));

        MatcherAssert.assertThat(tseClaimantRepReplyService.isRespondingToTribunal(caseData),
                is(isRespondingToTribunal));
    }

    private static Stream<Arguments> isRespondingToTribunal() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(false, false)
        );
    }

    private GenericTseApplicationType getApplicationType(boolean respondentResponseRequired) {
        GenericTseApplicationType applicationType = GenericTseApplicationType.builder()
                .respondentResponseRequired(respondentResponseRequired ? YES : NO).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(applicationType);

        return applicationType;
    }

    @Test
    void claimantReplyToTse() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
        GenericTseApplicationTypeItem genericTseApplicationTypeItem = getGenericTseApplicationTypeItem(NO);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        String userToken = "userToken";
        tseClaimantRepReplyService.claimantReplyToTse(userToken, caseDetails, caseData);
    }
}
