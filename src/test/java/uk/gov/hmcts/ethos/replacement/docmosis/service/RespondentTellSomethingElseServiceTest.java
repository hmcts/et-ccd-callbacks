package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CLAIMANT_NOT_COMPLIED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONTACT_THE_TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_ORDER_OTHER_PARTY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RECONSIDER_JUDGEMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_VARY_OR_REVOKE_AN_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_MONTHS_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_RESPONDENT_APP_TYPE_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentTellSomethingElseService.CY_CLAIMANT_EMAIL_GROUP_A;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentTellSomethingElseService.CY_CLAIMANT_EMAIL_GROUP_B;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class RespondentTellSomethingElseServiceTest {
    private RespondentTellSomethingElseService respondentTellSomethingElseService;
    private TseService tseService;
    private EmailService emailService;

    @MockBean
    private UserIdamService userIdamService;

    @MockBean
    private TribunalOfficesService tribunalOfficesService;

    @MockBean
    private TornadoService tornadoService;

    @MockBean
    private DocumentManagementService documentManagementService;
    @Mock
    private FeatureToggleService featureToggleService;

    @Captor
    ArgumentCaptor<Map<String, Object>> personalisationCaptor;

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String I_DO_WANT_TO_COPY = "I do want to copy";
    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String LEGAL_REP_EMAIL = "mail@mail.com";
    private static final String CASE_ID = "669718251103419";

    private static final String GIVE_DETAIL_MISSING = "Use the text box or file upload to give details.";
    private static final String RULE_92_ANSWERED_NO_TEXT = "You have said that you do not want to copy this "
        + "correspondence to "
        + "the other party. \n \n"
        + "The tribunal will consider all correspondence and let you know what happens next.";
    private static final String RULE_92_ANSWERED_YES_GROUP_A = "The other party will be notified that any objections "
        + "to your %s application should be sent to the tribunal as soon as possible, and in any event "
        + "within 7 days.";
    private static final String RULE_92_ANSWERED_YES_GROUP_B = "The other party is not expected to respond to this "
        + "application.\n \nHowever, they have been notified that any objections to your %s application should be "
        + "sent to the tribunal as soon as possible, and in any event within 7 days.";

    private static final String EXPECTED_EMPTY_TABLE_MESSAGE = "There are no applications to view";
    private static final String EXPECTED_TABLE_MARKDOWN = "| No | Application type | Applicant | Application date | "
        + "Response due | Number of responses | Status "
        + "|\r\n|:---------|:---------|:---------|:---------|:---------|:---------|:---------|\r\n|1|testType"
        + "|Respondent|testDate|testDueDate|0|Open|\r\n\r\n";

    private static final String BRISTOL_OFFICE = "Bristol";

    @BeforeEach
    void setUp() {
        emailService = spy(new EmailUtils());
        respondentTellSomethingElseService =
                new RespondentTellSomethingElseService(emailService, userIdamService, tribunalOfficesService,
                        tornadoService, documentManagementService, featureToggleService);
        tseService = new TseService(documentManagementService);

        ReflectionTestUtils.setField(respondentTellSomethingElseService,
                "tseRespondentAcknowledgeTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(respondentTellSomethingElseService,
                "tseRespondentAcknowledgeTypeCTemplateId", "TypeCTemplateId");

        UserDetails userDetails = HelperTest.getUserDetails();
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_Blank_ReturnErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResTseSelectApplication(selectedApplication);
        List<String> errors = respondentTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(GIVE_DETAIL_MISSING));
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_HasDoc_NoErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResTseSelectApplication(selectedApplication);
        setDocForSelectedApplication(caseData);
        List<String> errors = respondentTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(0));
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_HasTextBox_NoErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResTseSelectApplication(selectedApplication);
        setTextBoxForSelectedApplication(caseData);
        List<String> errors = respondentTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(0));
    }

    private static Stream<Arguments> selectedApplicationList() {
        return Stream.of(
                Arguments.of(TSE_APP_AMEND_RESPONSE),
                Arguments.of(TSE_APP_CHANGE_PERSONAL_DETAILS),
                Arguments.of(TSE_APP_CLAIMANT_NOT_COMPLIED),
                Arguments.of(TSE_APP_CONSIDER_A_DECISION_AFRESH),
                Arguments.of(TSE_APP_CONTACT_THE_TRIBUNAL),
                Arguments.of(TSE_APP_ORDER_OTHER_PARTY),
                Arguments.of(TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE),
                Arguments.of(TSE_APP_POSTPONE_A_HEARING),
                Arguments.of(TSE_APP_RECONSIDER_JUDGEMENT),
                Arguments.of(TSE_APP_RESTRICT_PUBLICITY),
                Arguments.of(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM),
                Arguments.of(TSE_APP_VARY_OR_REVOKE_AN_ORDER));
    }

    private void setDocForSelectedApplication(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case TSE_APP_AMEND_RESPONSE:
                caseData.setResTseDocument1(createDocumentType("documentUrl"));
                break;
            case TSE_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseDocument2(createDocumentType("documentUrl"));
                break;
            case TSE_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseDocument3(createDocumentType("documentUrl"));
                break;
            case TSE_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseDocument4(createDocumentType("documentUrl"));
                break;
            case TSE_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseDocument5(createDocumentType("documentUrl"));
                break;
            case TSE_APP_ORDER_OTHER_PARTY:
                caseData.setResTseDocument6(createDocumentType("documentUrl"));
                break;
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseDocument7(createDocumentType("documentUrl"));
                break;
            case TSE_APP_POSTPONE_A_HEARING:
                caseData.setResTseDocument8(createDocumentType("documentUrl"));
                break;
            case TSE_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseDocument9(createDocumentType("documentUrl"));
                break;
            case TSE_APP_RESTRICT_PUBLICITY:
                caseData.setResTseDocument10(createDocumentType("documentUrl"));
                break;
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseDocument11(createDocumentType("documentUrl"));
                break;
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER:
                caseData.setResTseDocument12(createDocumentType("documentUrl"));
                break;
            default:
                break;
        }
    }

    private UploadedDocumentType createDocumentType(String documentUrl) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("binaryUrl/documents/");
        uploadedDocumentType.setDocumentFilename("testFileName");
        uploadedDocumentType.setDocumentUrl(documentUrl);
        return uploadedDocumentType;
    }

    private void setTextBoxForSelectedApplication(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case TSE_APP_AMEND_RESPONSE:
                caseData.setResTseTextBox1("Not Blank");
                break;
            case TSE_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseTextBox2("Not Blank");
                break;
            case TSE_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseTextBox3("Not Blank");
                break;
            case TSE_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseTextBox4("Not Blank");
                break;
            case TSE_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseTextBox5("Not Blank");
                break;
            case TSE_APP_ORDER_OTHER_PARTY:
                caseData.setResTseTextBox6("Not Blank");
                break;
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseTextBox7("Not Blank");
                break;
            case TSE_APP_POSTPONE_A_HEARING:
                caseData.setResTseTextBox8("Not Blank");
                break;
            case TSE_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseTextBox9("Not Blank");
                break;
            case TSE_APP_RESTRICT_PUBLICITY:
                caseData.setResTseTextBox10("Not Blank");
                break;
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseTextBox11("Not Blank");
                break;
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER:
                caseData.setResTseTextBox12("Not Blank");
                break;
            default:
                break;
        }
    }

    @ParameterizedTest
    @MethodSource("sendAcknowledgeEmailAndGeneratePdf")
    void sendAcknowledgeEmailAndGeneratePdf(String selectedApplication, String rule92Selection, String expectedAnswer,
                                        Boolean emailSent) {
        CaseData caseData = createCaseData(selectedApplication, rule92Selection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        Map<String, String> expectedPersonalisation = createPersonalisation(caseData, expectedAnswer,
            selectedApplication);

        respondentTellSomethingElseService.sendAcknowledgeEmail(caseDetails, AUTH_TOKEN);

        if (emailSent) {
            verify(emailService).sendEmail(TEMPLATE_ID, LEGAL_REP_EMAIL, expectedPersonalisation);
        } else {
            verify(emailService, never()).sendEmail(TEMPLATE_ID, LEGAL_REP_EMAIL, expectedPersonalisation);
        }
    }

    private static Stream<Arguments> sendAcknowledgeEmailAndGeneratePdf() {
        return Stream.of(
            Arguments.of(TSE_APP_AMEND_RESPONSE, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_CONTACT_THE_TRIBUNAL, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_POSTPONE_A_HEARING, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_VARY_OR_REVOKE_AN_ORDER, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_ORDER_OTHER_PARTY, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_CLAIMANT_NOT_COMPLIED, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_RESTRICT_PUBLICITY, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_CHANGE_PERSONAL_DETAILS, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_CONSIDER_A_DECISION_AFRESH, NO, RULE_92_ANSWERED_NO_TEXT, true),
            Arguments.of(TSE_APP_RECONSIDER_JUDGEMENT, NO, RULE_92_ANSWERED_NO_TEXT, true),

            Arguments.of(TSE_APP_AMEND_RESPONSE, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM, I_DO_WANT_TO_COPY,
                    RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_CONTACT_THE_TRIBUNAL, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_POSTPONE_A_HEARING, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_VARY_OR_REVOKE_AN_ORDER, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_ORDER_OTHER_PARTY, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_CLAIMANT_NOT_COMPLIED, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_RESTRICT_PUBLICITY, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_A, true),
            Arguments.of(TSE_APP_CHANGE_PERSONAL_DETAILS, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_B, true),
            Arguments.of(TSE_APP_CONSIDER_A_DECISION_AFRESH, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_B, true),
            Arguments.of(TSE_APP_RECONSIDER_JUDGEMENT, I_DO_WANT_TO_COPY, RULE_92_ANSWERED_YES_GROUP_B, true)
        );
    }

    @Test
    void sendAcknowledgeEmailTypeC() {
        CaseData caseData = createCaseData(TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE, NO);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        Map<String, String> expectedPersonalisation = Map.of(
            "caseNumber", caseData.getEthosCaseReference(),
            "claimant", caseData.getClaimant(),
            "respondents", getRespondentNames(caseData),
            "linkToExUI", "exuiUrl669718251103419"
        );

        respondentTellSomethingElseService.sendAcknowledgeEmail(caseDetails, AUTH_TOKEN);

        verify(emailService).sendEmail("TypeCTemplateId", LEGAL_REP_EMAIL, expectedPersonalisation);
    }

    @Test
    void claimantPersonalisation_buildsCorrectData_English_Language() throws NotificationClientException {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, I_DO_WANT_TO_COPY);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(ENGLISH_LANGUAGE);

        Map<String, Object> actual = respondentTellSomethingElseService.claimantPersonalisation(caseDetails, "test",
            new byte[]{});

        assertThat(actual.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(actual.get("applicationType"), is(TSE_APP_AMEND_RESPONSE));
        assertThat(actual.get("instructions"), is("test"));
        assertThat(actual.get("claimant"), is("claimant"));
        assertThat(actual.get("respondents"), is("Father Ted"));
        assertThat(actual.get("linkToDocument").toString(), is("{\"file\":\"\",\"confirm_email_before_download"
            + "\":true,\"retention_period\":\"52 weeks\",\"is_csv\":false}"));
    }

    @Test
    void claimantPersonalisation_buildsCorrectData_Welsh_Language() throws NotificationClientException {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, I_DO_WANT_TO_COPY);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        when(featureToggleService.isWelshEnabled()).thenReturn(true);

        Map<String, Object> actual = respondentTellSomethingElseService.claimantPersonalisation(caseDetails, "test",
                new byte[]{});

        assertThat(actual.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(actual.get("applicationType"), is(CY_RESPONDENT_APP_TYPE_MAP.get(TSE_APP_AMEND_RESPONSE)));
        assertThat(actual.get("instructions"), is("test"));
        assertThat(actual.get("claimant"), is("claimant"));
        assertThat(actual.get("respondents"), is("Father Ted"));
        assertThat(actual.get("linkToDocument").toString(), is("{\"file\":\"\",\"confirm_email_before_download"
                + "\":true,\"retention_period\":\"52 weeks\",\"is_csv\":false}"));
        assertTrue(((String) actual.get("linkToCitizenHub")).endsWith(WELSH_LANGUAGE_PARAM));
    }

    @Test
    void sendClaimantEmail_rule92No_doesNothing() {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, NO);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        respondentTellSomethingElseService.sendClaimantEmail(caseDetails);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendClaimantEmail_groupC_doesNothing() {
        CaseData caseData = createCaseData(TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE, NO);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        respondentTellSomethingElseService.sendClaimantEmail(caseDetails);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendClaimantEmail_groupA_sendsEmail() throws IOException {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, I_DO_WANT_TO_COPY);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(ENGLISH_LANGUAGE);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[] {});
        respondentTellSomethingElseService.sendClaimantEmail(caseDetails);
        verify(emailService).sendEmail(any(), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();
        String expectedInstructions = String.format("You should respond as soon as possible, and in any event by %s.",
            UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        assertThat(personalisation.get("instructions"), is(expectedInstructions));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        TSE_APP_AMEND_RESPONSE,
        TSE_APP_CLAIMANT_NOT_COMPLIED,
        TSE_APP_CONTACT_THE_TRIBUNAL,
        TSE_APP_ORDER_OTHER_PARTY,
        TSE_APP_POSTPONE_A_HEARING,
        TSE_APP_RESTRICT_PUBLICITY,
        TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM,
        TSE_APP_VARY_OR_REVOKE_AN_ORDER
    })
    void sendClaimantEmail_groupA_sendsEmail_Welsh(String applicationType) throws IOException {
        CaseData caseData = createCaseData(applicationType, I_DO_WANT_TO_COPY);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        when(featureToggleService.isWelshEnabled()).thenReturn(true);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        respondentTellSomethingElseService.sendClaimantEmail(caseDetails);
        verify(emailService).sendEmail(any(), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();
        String expectedDueDate = UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7);

        for (Map.Entry<String, String> monthEntry : CY_MONTHS_MAP.entrySet()) {
            if (expectedDueDate.contains(monthEntry.getKey())) {
                expectedDueDate = expectedDueDate.replace(monthEntry.getKey(), monthEntry.getValue());
                break;
            }
        }
        String expectedInstructions = String.format(CY_CLAIMANT_EMAIL_GROUP_A, expectedDueDate);

        assertThat(personalisation.get("instructions"), is(expectedInstructions));
    }

    @Test
    void sendClaimantEmail_groupB_sendsEmail() throws IOException {
        CaseData caseData = createCaseData(TSE_APP_CHANGE_PERSONAL_DETAILS, I_DO_WANT_TO_COPY);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(ENGLISH_LANGUAGE);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[] {});
        respondentTellSomethingElseService.sendClaimantEmail(caseDetails);
        verify(emailService).sendEmail(any(), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();
        String expected = String.format("You are not expected to respond to this application"
            + ".\r\n\r\nIf you do respond you should do so as soon as possible and in any event by %s.",
            UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        assertThat(personalisation.get("instructions"), is(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        TSE_APP_CHANGE_PERSONAL_DETAILS,
        TSE_APP_CONSIDER_A_DECISION_AFRESH,
        TSE_APP_RECONSIDER_JUDGEMENT
    })
    void sendClaimantEmail_groupB_sendsEmail_Welsh(String applicationType) throws IOException {
        CaseData caseData = createCaseData(applicationType, I_DO_WANT_TO_COPY);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        when(featureToggleService.isWelshEnabled()).thenReturn(true);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        respondentTellSomethingElseService.sendClaimantEmail(caseDetails);
        verify(emailService).sendEmail(any(), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();

        String expectedDueDate = UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7);

        for (Map.Entry<String, String> monthEntry : CY_MONTHS_MAP.entrySet()) {
            if (expectedDueDate.contains(monthEntry.getKey())) {
                expectedDueDate = expectedDueDate.replace(monthEntry.getKey(), monthEntry.getValue());
                break;
            }
        }
        String expectedInstructions = String.format(CY_CLAIMANT_EMAIL_GROUP_B, expectedDueDate);
        assertThat(personalisation.get("instructions"), is(expectedInstructions));
    }

    @ParameterizedTest
    @MethodSource("createRespondentApplication")
    void createApplication_withRespondentTseData_shouldPersistDataAndEmptyFields(String selectedApplication,
                                                                                 String textBoxData,
                                                                                 String documentUrl) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResTseSelectApplication(selectedApplication);
        setDocAndTextForSelectedApplication(caseData, textBoxData, documentUrl);
        caseData.setResTseCopyToOtherPartyYesOrNo("copyToOtherPartyYesOrNo");
        caseData.setResTseCopyToOtherPartyTextArea("copyToOtherPartyTextArea");

        tseService.createApplication(caseData, false);

        var genericTseApplicationType = caseData.getGenericTseApplicationCollection().get(0).getValue();
        assertThat(genericTseApplicationType.getDetails(), is(textBoxData));
        assertThat(genericTseApplicationType.getCopyToOtherPartyText(), is("copyToOtherPartyTextArea"));
        assertThat(genericTseApplicationType.getCopyToOtherPartyYesOrNo(), is("copyToOtherPartyYesOrNo"));
        assertThat(genericTseApplicationType.getDocumentUpload().getDocumentUrl(), is(documentUrl));
        assertThat(genericTseApplicationType.getApplicant(), is(RESPONDENT_TITLE));
        assertThat(genericTseApplicationType.getType(), is(selectedApplication));

        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        DocumentType actualDocumentType = documentCollection.get(0).getValue();

        assertThat(documentCollection.size(), is(1));
        assertEquals(selectedApplication, actualDocumentType.getShortDescription());
        assertEquals("Respondent correspondence", actualDocumentType.getTypeOfDocument());

        assertThat(caseData.getResTseSelectApplication(), is(nullValue()));
        assertThat(caseData.getResTseCopyToOtherPartyYesOrNo(), is(nullValue()));
        assertThat(caseData.getResTseCopyToOtherPartyTextArea(), is(nullValue()));
        assertThat(caseData.getResTseTextBox1(), is(nullValue()));
        assertThat(caseData.getResTseTextBox2(), is(nullValue()));
        assertThat(caseData.getResTseTextBox3(), is(nullValue()));
        assertThat(caseData.getResTseTextBox4(), is(nullValue()));
        assertThat(caseData.getResTseTextBox5(), is(nullValue()));
        assertThat(caseData.getResTseTextBox6(), is(nullValue()));
        assertThat(caseData.getResTseTextBox7(), is(nullValue()));
        assertThat(caseData.getResTseTextBox8(), is(nullValue()));
        assertThat(caseData.getResTseTextBox9(), is(nullValue()));
        assertThat(caseData.getResTseTextBox10(), is(nullValue()));
        assertThat(caseData.getResTseTextBox11(), is(nullValue()));
        assertThat(caseData.getResTseTextBox12(), is(nullValue()));
        assertThat(caseData.getResTseDocument1(), is(nullValue()));
        assertThat(caseData.getResTseDocument2(), is(nullValue()));
        assertThat(caseData.getResTseDocument3(), is(nullValue()));
        assertThat(caseData.getResTseDocument4(), is(nullValue()));
        assertThat(caseData.getResTseDocument5(), is(nullValue()));
        assertThat(caseData.getResTseDocument6(), is(nullValue()));
        assertThat(caseData.getResTseDocument7(), is(nullValue()));
        assertThat(caseData.getResTseDocument8(), is(nullValue()));
        assertThat(caseData.getResTseDocument9(), is(nullValue()));
        assertThat(caseData.getResTseDocument10(), is(nullValue()));
        assertThat(caseData.getResTseDocument11(), is(nullValue()));
        assertThat(caseData.getResTseDocument12(), is(nullValue()));
    }

    @Test
    void displayRespondentApplicationsTable_hasApplications() {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, NO);
        caseData.setGenericTseApplicationCollection(generateGenericTseApplicationList());

        assertThat(respondentTellSomethingElseService.generateTableMarkdown(caseData), is(EXPECTED_TABLE_MARKDOWN));
    }

    @Test
    void displayRespondentApplicationsTable_hasNoApplications() {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, NO);

        assertThat(respondentTellSomethingElseService.generateTableMarkdown(caseData),
                is(EXPECTED_EMPTY_TABLE_MESSAGE));
    }

    @Test
    void sendAdminEmail_DoesNothingWhenNoManagingOfficeIsSet() {
        CaseData caseData = createCaseData("", YES);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        respondentTellSomethingElseService.sendAdminEmail(caseDetails);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendAdminEmail_DoesNothingWhenNoManagingOfficeHasNoEmail() {
        CaseData caseData = createCaseData("", YES);
        CaseDetails caseDetails = new CaseDetails();
        caseData.setManagingOffice("Aberdeen");
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        respondentTellSomethingElseService.sendAdminEmail(caseDetails);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendAdminEmail_SendsEmail() {
        CaseData caseData = createCaseData("", YES);
        CaseDetails caseDetails = new CaseDetails();
        caseData.setManagingOffice("Bristol");
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(tribunalOfficesService.getTribunalOffice(any())).thenReturn(TribunalOffice.BRISTOL);
        respondentTellSomethingElseService.sendAdminEmail(caseDetails);

        Map<String, String> caseNumber = Map.of("caseNumber", "test",
                "emailFlag", "",
                "claimant", "claimant",
                "respondents", "Father Ted",
                "date", "Not set",
                "url", "exuiUrl669718251103419");

        verify(emailService, times(1)).sendEmail(any(), any(), eq(caseNumber));

    }

    @Test
    void getTribunalEmail() {
        CaseData caseData = createCaseData("", YES);
        caseData.setManagingOffice(BRISTOL_OFFICE);

        when(tribunalOfficesService.getTribunalOffice(BRISTOL_OFFICE)).thenReturn(TribunalOffice.BRISTOL);

        assertThat(respondentTellSomethingElseService.getTribunalEmail(caseData),
                is(TribunalOffice.BRISTOL.getOfficeEmail()));
    }

    private ListTypeItem<GenericTseApplicationType> generateGenericTseApplicationList() {
        GenericTseApplicationType respondentTseType = new GenericTseApplicationType();

        respondentTseType.setDate("testDate");
        respondentTseType.setNumber("number");
        respondentTseType.setApplicant(RESPONDENT_TITLE);
        respondentTseType.setDetails("testDetails");
        respondentTseType.setDocumentUpload(createDocumentType("test"));
        respondentTseType.setType("testType");
        respondentTseType.setCopyToOtherPartyYesOrNo("yes");
        respondentTseType.setCopyToOtherPartyText("text");
        respondentTseType.setDueDate("testDueDate");

        TypeItem<GenericTseApplicationType> tseApplicationTypeItem = new TypeItem<>();
        tseApplicationTypeItem.setId("id");
        tseApplicationTypeItem.setValue(respondentTseType);

        return ListTypeItem.from(tseApplicationTypeItem);
    }

    private void setDocAndTextForSelectedApplication(CaseData caseData,
                                                     String textBoxData,
                                                     String documentUrl) {
        switch (caseData.getResTseSelectApplication()) {
            case TSE_APP_AMEND_RESPONSE:
                caseData.setResTseTextBox1(textBoxData);
                caseData.setResTseDocument1(createDocumentType(documentUrl));
                break;
            case TSE_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseTextBox2(textBoxData);
                caseData.setResTseDocument2(createDocumentType(documentUrl));
                break;
            case TSE_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseTextBox3(textBoxData);
                caseData.setResTseDocument3(createDocumentType(documentUrl));
                break;
            case TSE_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseTextBox4(textBoxData);
                caseData.setResTseDocument4(createDocumentType(documentUrl));
                break;
            case TSE_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseTextBox5(textBoxData);
                caseData.setResTseDocument5(createDocumentType(documentUrl));
                break;
            case TSE_APP_ORDER_OTHER_PARTY:
                caseData.setResTseTextBox6(textBoxData);
                caseData.setResTseDocument6(createDocumentType(documentUrl));
                break;
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseTextBox7(textBoxData);
                caseData.setResTseDocument7(createDocumentType(documentUrl));
                break;
            case TSE_APP_POSTPONE_A_HEARING:
                caseData.setResTseTextBox8(textBoxData);
                caseData.setResTseDocument8(createDocumentType(documentUrl));
                break;
            case TSE_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseTextBox9(textBoxData);
                caseData.setResTseDocument9(createDocumentType(documentUrl));
                break;
            case TSE_APP_RESTRICT_PUBLICITY:
                caseData.setResTseTextBox10(textBoxData);
                caseData.setResTseDocument10(createDocumentType(documentUrl));
                break;
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseTextBox11(textBoxData);
                caseData.setResTseDocument11(createDocumentType(documentUrl));
                break;
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER:
                caseData.setResTseTextBox12(textBoxData);
                caseData.setResTseDocument12(createDocumentType(documentUrl));
                break;
            default:
                break;
        }
    }

    private static Stream<Arguments> createRespondentApplication() {
        return Stream.of(
            Arguments.of(TSE_APP_AMEND_RESPONSE, "textBox1", "document1"),
            Arguments.of(TSE_APP_CHANGE_PERSONAL_DETAILS, "textBox2", "document2"),
            Arguments.of(TSE_APP_CLAIMANT_NOT_COMPLIED, "textBox3", "document3"),
            Arguments.of(TSE_APP_CONSIDER_A_DECISION_AFRESH, "textBox4", "document4"),
            Arguments.of(TSE_APP_CONTACT_THE_TRIBUNAL, "textBox5", "document5"),
            Arguments.of(TSE_APP_ORDER_OTHER_PARTY, "textBox6", "document6"),
            Arguments.of(TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE, "textBox7", "document7"),
            Arguments.of(TSE_APP_POSTPONE_A_HEARING, "textBox8", "document8"),
            Arguments.of(TSE_APP_RECONSIDER_JUDGEMENT, "textBox9", "document9"),
            Arguments.of(TSE_APP_RESTRICT_PUBLICITY, "textBox10", "document10"),
            Arguments.of(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM, "textBox11", "document11"),
            Arguments.of(TSE_APP_VARY_OR_REVOKE_AN_ORDER, "textBox12", "document12")
        );
    }

    private CaseData createCaseData(String selectedApplication, String selectedRule92Answer) {
        CaseData caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("test")
            .withClaimant("claimant")
            .withClaimantType("person@email.com")
            .build();
        caseData.setResTseSelectApplication(selectedApplication);
        caseData.setResTseCopyToOtherPartyYesOrNo(selectedRule92Answer);
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));

        return caseData;
    }

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Father Ted");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }

    private Map<String, String> createPersonalisation(CaseData caseData,
                                                      String expectedAnswer,
                                                      String selectedApplication) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("claimant", caseData.getClaimant());
        personalisation.put("respondents", getRespondentNames(caseData));
        personalisation.put("shortText", selectedApplication);
        personalisation.put("linkToExUI", "exuiUrl669718251103419");
        if (expectedAnswer != null) {
            personalisation.put("customisedText", String.format(expectedAnswer, selectedApplication));
        }
        return personalisation;
    }

    @Test
    void generatesAndAddsTsePdfToDocumentCollection() {
        CaseData caseData = new CaseData();
        caseData.setResTseSelectApplication("Amend response");
        respondentTellSomethingElseService.generateAndAddTsePdf(caseData, "token", "typeId");

        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        DocumentType actual = documentCollection.get(0).getValue();

        DocumentType expected = DocumentType.builder().typeOfDocument("Respondent correspondence")
            .shortDescription("Amend response").build();

        Assertions.assertThat(documentCollection).hasSize(1);
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
