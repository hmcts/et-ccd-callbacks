package uk.gov.hmcts.ethos.replacement.docmosis.service;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeItemUtil.createSupportingMaterial;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationUtil.getGenericTseApplicationTypeItem;

@ExtendWith(SpringExtension.class)
class TseRespondentReplyServiceTest {
    @MockBean
    private TornadoService tornadoService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private UserService userService;
    @MockBean
    private NotificationProperties notificationProperties;
    @MockBean
    private TseService tseService;
    @MockBean
    private RespondentTellSomethingElseService respondentTellSomethingElseService;

    private TseRespondentReplyService tseRespondentReplyService;
    private UserDetails userDetails;
    private CaseData caseData;
    private MockedStatic<TseHelper> mockStatic;
    private GenericTseApplicationType genericTseApplicationType;

    @BeforeEach
    void setUp() throws Exception {
        tseRespondentReplyService = new TseRespondentReplyService(tornadoService, emailService, userService,
            notificationProperties, respondentTellSomethingElseService, tseService);

        userDetails = HelperTest.getUserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        doNothing().when(emailService).sendEmail(any(), any(), any());

        mockStatic = mockStatic(TseHelper.class, Mockito.CALLS_REAL_METHODS);
        mockStatic.when(() -> TseHelper.getPersonalisationForResponse(any(), any(), any()))
            .thenReturn(Collections.emptyMap());
        mockStatic.when(() -> TseHelper.getPersonalisationForAcknowledgement(any(), any()))
            .thenReturn(Collections.emptyMap());

        caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("9876")
            .withClaimantType("person@email.com")
            .withRespondent("respondent", YES, "01-Jan-2003", false)
            .build();

        genericTseApplicationType = GenericTseApplicationType.builder().applicant(CLAIMANT_TITLE)
            .date("13 December 2022").dueDate("20 December 2022").type("Withdraw my claim")
            .copyToOtherPartyYesOrNo(YES).details("Text").applicationState("notStartedYet")
            .number("1").responsesCount("0").status(OPEN_STATE).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = GenericTseApplicationTypeItem.builder()
            .id(UUID.randomUUID().toString()).value(genericTseApplicationType).build();

        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        caseData.setClaimant("Claimant LastName");
    }

    @AfterEach
    void afterEach() {
        mockStatic.close();
    }

    @Nested
    class UpdateApplicationStatus {
        @Test
        void noStatusChangeWhenAllAdminRequestsForInfoAreAnswered() {
            genericTseApplicationType.setRespondentResponseRequired(NO);

            tseRespondentReplyService.updateApplicationState(caseData);

            assertThat(genericTseApplicationType.getApplicationState()).isEqualTo("notStartedYet");

        }

        @Test
        void changeStatusToUpdatedWhenHasDueRequestForInfo() {
            genericTseApplicationType.setRespondentResponseRequired(YES);

            tseRespondentReplyService.updateApplicationState(caseData);

            assertThat(genericTseApplicationType.getApplicationState()).isEqualTo("updated");
        }
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

            tseRespondentReplyService.saveReplyToApplication(caseData, false);

            TseRespondType replyType = caseData.getGenericTseApplicationCollection().get(0)
                .getValue().getRespondCollection().get(0).getValue();

            String dateNow = UtilHelper.formatCurrentDate(LocalDate.now());

            assertThat(replyType.getDate()).isEqualTo(dateNow);
            assertThat(replyType.getResponse()).isEqualTo("ResponseText");
            assertThat(replyType.getCopyNoGiveDetails()).isEqualTo("It's a secret");
            assertThat(replyType.getHasSupportingMaterial()).isEqualTo(YES);
            assertThat(replyType.getCopyToOtherParty()).isEqualTo(NO);
            assertThat(replyType.getFrom()).isEqualTo(RESPONDENT_TITLE);
            assertThat(replyType.getSupportingMaterial().get(0).getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo("image.png");
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

            tseRespondentReplyService.saveReplyToApplication(caseData, true);

            String respondentResponseRequired = caseData.getGenericTseApplicationCollection().get(0)
                .getValue().getRespondentResponseRequired();

            assertThat(respondentResponseRequired).isEqualTo(NO);
        }
    }

    @Test
    void sendAcknowledgementAndClaimantEmail_rule92Yes() {
        caseData.setTseResponseCopyToOtherParty(YES);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);

        tseRespondentReplyService.sendAcknowledgementAndClaimantEmail(caseDetails, "userToken");

        verify(emailService, times(2)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail(any(), eq(caseData.getClaimantType().getClaimantEmailAddress()), any());
        verify(emailService).sendEmail(any(), eq(userDetails.getEmail()), any());
    }

    @Test
    void sendAcknowledgementAndClaimantEmail_rule92No() {
        caseData.setTseResponseCopyToOtherParty(NO);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);

        tseRespondentReplyService.sendAcknowledgementAndClaimantEmail(caseDetails, "userToken");

        verify(emailService, times(1)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail(any(), eq(userDetails.getEmail()), any());
    }

    @Test
    void resetReplyToApplicationPage_resetsData() {
        caseData.setTseResponseCopyToOtherParty(YES);
        caseData.setTseResponseCopyNoGiveDetails(YES);
        caseData.setTseResponseText(YES);
        caseData.setTseResponseIntro(YES);
        caseData.setTseResponseTable(YES);
        caseData.setTseResponseHasSupportingMaterial(YES);
        caseData.setTseResponseSupportingMaterial(createSupportingMaterial());
        caseData.setTseRespondingToTribunal(YES);

        tseRespondentReplyService.resetReplyToApplicationPage(caseData);

        assertNull(caseData.getTseResponseText());
        assertNull(caseData.getTseResponseIntro());
        assertNull(caseData.getTseResponseTable());
        assertNull(caseData.getTseResponseHasSupportingMaterial());
        assertNull(caseData.getTseResponseSupportingMaterial());
        assertNull(caseData.getTseResponseCopyToOtherParty());
        assertNull(caseData.getTseResponseCopyNoGiveDetails());
        assertNull(caseData.getTseRespondingToTribunal());
    }

    @Test
    void initialResReplyToTribunalTableMarkUp() {
        when(tseService.formatApplicationDetails(any(), any(), anyBoolean())).thenReturn("applicationDetails");
        when(tseService.formatApplicationResponses(any(), any(), anyBoolean())).thenReturn("responses");

        tseRespondentReplyService.initialResReplyToTribunalTableMarkUp(caseData, "token");
        String expectedResponseTables = "applicationDetails" + "\r\n" + "responses";
        assertThat(caseData.getTseResponseTable(), is(expectedResponseTables));
        assertThat(caseData.getTseRespondingToTribunal(), is(expectedResponseTables));
    }

    @ParameterizedTest
    @MethodSource
    void validateInput(String responseText, String supportingMaterial, int expectedErrorCount) {
        caseData.setTseResponseText(responseText);
        caseData.setTseResponseHasSupportingMaterial(supportingMaterial);

        List<String> errors = tseRespondentReplyService.validateInput(caseData);

        assertThat(errors.size(), is(expectedErrorCount));
    }

    private static Stream<Arguments> validateInput() {
        return Stream.of(
                Arguments.of(null, YES, 0),
                Arguments.of("testResponseText", YES, 0),
                Arguments.of(null, NO, 1)
        );
    }

    @ParameterizedTest
    @MethodSource
    void isRespondingToTribunal(boolean respondentResponseRequired, boolean isRespondingToTribunal) {
        caseData.setTseRespondSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("1", "test")));

        mockStatic.when(() -> TseHelper.getRespondentSelectedApplicationType(any()))
                .thenReturn(getApplicationType(respondentResponseRequired));

        assertThat(tseRespondentReplyService.isRespondingToTribunal(caseData), is(isRespondingToTribunal));
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
}
