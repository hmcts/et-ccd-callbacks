package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("squid:S5961")
class RespondentTellSomethingElseServiceTest {
    private RespondentTellSomethingElseService respondentTellSomethingElseService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserService userService;

    @MockBean
    private TornadoService tornadoService;

    @MockBean
    private DocumentManagementService documentManagementService;

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String YES = "I do want to copy";
    private static final String NO = "I do not want to copy";
    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String LEGAL_REP_EMAIL = "mail@mail.com";
    private static final String CASE_ID = "669718251103419";
    private static final String APPLICANT_CLAIMANT = "Claimant";

    private static final String SELECTED_APP_AMEND_RESPONSE = "Amend response";
    private static final String SELECTED_APP_CHANGE_PERSONAL_DETAILS = "Change personal details";
    private static final String SELECTED_APP_CLAIMANT_NOT_COMPLIED = "Claimant not complied";
    private static final String SELECTED_APP_CONSIDER_A_DECISION_AFRESH = "Consider a decision afresh";
    private static final String SELECTED_APP_CONTACT_THE_TRIBUNAL = "Contact the tribunal";
    private static final String SELECTED_APP_ORDER_OTHER_PARTY = "Order other party";
    private static final String SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE =
            "Order a witness to attend to give evidence";
    private static final String SELECTED_APP_POSTPONE_A_HEARING = "Postpone a hearing";
    private static final String SELECTED_APP_RECONSIDER_JUDGEMENT = "Reconsider judgement";
    private static final String SELECTED_APP_RESTRICT_PUBLICITY = "Restrict publicity";
    private static final String SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM = "Strike out all or part of a claim";
    private static final String SELECTED_APP_VARY_OR_REVOKE_AN_ORDER = "Vary or revoke an order";

    private static final String GIVE_DETAIL_MISSING = "Use the text box or file upload to give details.";
    private static final String rule92AnsweredNoText = "You have said that you do not want to copy this "
        + "correspondence to "
        + "the other party. \n \n"
        + "The tribunal will consider all correspondence and let you know what happens next.";
    private static final String rule92AnsweredYesGroupA = "The other party will be notified that any objections "
        + "to your %s application should be sent to the tribunal as soon as possible, and in any event "
        + "within 7 days.";
    private static final String rule92AnsweredYesGroupB = "The other party is not expected to respond to this "
        + "application.\n \nHowever, they have been notified that any objections to your %s application should be "
        + "sent to the tribunal as soon as possible, and in any event within 7 days.";

    @BeforeEach
    void setUp() {
        respondentTellSomethingElseService =
                new RespondentTellSomethingElseService(emailService, userService, tornadoService, documentManagementService);
        ReflectionTestUtils.setField(respondentTellSomethingElseService, "emailTemplateId", TEMPLATE_ID);

        UserDetails userDetails = HelperTest.getUserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
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
                Arguments.of(SELECTED_APP_AMEND_RESPONSE),
                Arguments.of(SELECTED_APP_CHANGE_PERSONAL_DETAILS),
                Arguments.of(SELECTED_APP_CLAIMANT_NOT_COMPLIED),
                Arguments.of(SELECTED_APP_CONSIDER_A_DECISION_AFRESH),
                Arguments.of(SELECTED_APP_CONTACT_THE_TRIBUNAL),
                Arguments.of(SELECTED_APP_ORDER_OTHER_PARTY),
                Arguments.of(SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE),
                Arguments.of(SELECTED_APP_POSTPONE_A_HEARING),
                Arguments.of(SELECTED_APP_RECONSIDER_JUDGEMENT),
                Arguments.of(SELECTED_APP_RESTRICT_PUBLICITY),
                Arguments.of(SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM),
                Arguments.of(SELECTED_APP_VARY_OR_REVOKE_AN_ORDER));
    }

    private void setDocForSelectedApplication(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                caseData.setResTseDocument1(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseDocument2(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseDocument3(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseDocument4(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseDocument5(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                caseData.setResTseDocument6(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseDocument7(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_POSTPONE_A_HEARING:
                caseData.setResTseDocument8(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseDocument9(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                caseData.setResTseDocument10(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseDocument11(createDocumentType("documentUrl"));
                break;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
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
            case SELECTED_APP_AMEND_RESPONSE:
                caseData.setResTseTextBox1("Not Blank");
                break;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseTextBox2("Not Blank");
                break;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseTextBox3("Not Blank");
                break;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseTextBox4("Not Blank");
                break;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseTextBox5("Not Blank");
                break;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                caseData.setResTseTextBox6("Not Blank");
                break;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseTextBox7("Not Blank");
                break;
            case SELECTED_APP_POSTPONE_A_HEARING:
                caseData.setResTseTextBox8("Not Blank");
                break;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseTextBox9("Not Blank");
                break;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                caseData.setResTseTextBox10("Not Blank");
                break;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseTextBox11("Not Blank");
                break;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                caseData.setResTseTextBox12("Not Blank");
                break;
            default:
                break;
        }
    }

    @ParameterizedTest
    @MethodSource("sendRespondentApplicationEmail")
    void sendRespondentApplicationEmail(String selectedApplication, String rule92Selection, String expectedAnswer,
                                        Boolean emailSent) {
        CaseData caseData = createCaseData(selectedApplication, rule92Selection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        Map<String, String> expectedPersonalisation = createPersonalisation(caseData, expectedAnswer,
            selectedApplication);

        respondentTellSomethingElseService.sendRespondentApplicationEmail(caseDetails, AUTH_TOKEN);

        if (emailSent) {
            verify(emailService).sendEmail(TEMPLATE_ID, LEGAL_REP_EMAIL, expectedPersonalisation);
        } else {
            verify(emailService, never()).sendEmail(TEMPLATE_ID, LEGAL_REP_EMAIL, expectedPersonalisation);
        }
    }

    private static Stream<Arguments> sendRespondentApplicationEmail() {
        return Stream.of(
            Arguments.of("Amend response", NO, rule92AnsweredNoText, true),
            Arguments.of("Strike out all or part of a claim", NO, rule92AnsweredNoText, true),
            Arguments.of("Contact the tribunal", NO, rule92AnsweredNoText, true),
            Arguments.of("Postpone a hearing", NO, rule92AnsweredNoText, true),
            Arguments.of("Vary or revoke an order", NO, rule92AnsweredNoText, true),
            Arguments.of("Order other party", NO, rule92AnsweredNoText, true),
            Arguments.of("Claimant not complied", NO, rule92AnsweredNoText, true),
            Arguments.of("Restrict publicity", NO, rule92AnsweredNoText, true),
            Arguments.of("Change personal details", NO, rule92AnsweredNoText, true),
            Arguments.of("Consider a decision afresh", NO, rule92AnsweredNoText, true),
            Arguments.of("Reconsider judgement", NO, rule92AnsweredNoText, true),

            Arguments.of("Amend response", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Strike out all or part of a claim", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Contact the tribunal", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Postpone a hearing", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Vary or revoke an order", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Order other party", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Claimant not complied", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Restrict publicity", YES, rule92AnsweredYesGroupA, true),
            Arguments.of("Change personal details", YES, rule92AnsweredYesGroupB, true),
            Arguments.of("Consider a decision afresh", YES, rule92AnsweredYesGroupB, true),
            Arguments.of("Reconsider judgement", YES, rule92AnsweredYesGroupB, true),

            Arguments.of("Order a witness to attend to give evidence", null, null, false)
        );
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

        respondentTellSomethingElseService.createRespondentApplication(caseData);

        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue().getDetails(), is(textBoxData));
        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue()
            .getCopyToOtherPartyText(), is("copyToOtherPartyTextArea"));

        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue()
            .getCopyToOtherPartyYesOrNo(), is("copyToOtherPartyYesOrNo"));

        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue()
            .getDocumentUpload().getDocumentUrl(), is(documentUrl));

        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue()
            .getApplicant(), is(APPLICANT_CLAIMANT));

        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue()
            .getType(), is(selectedApplication));

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

    private void setDocAndTextForSelectedApplication(CaseData caseData,
                                                     String textBoxData,
                                                     String documentUrl) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                caseData.setResTseTextBox1(textBoxData);
                caseData.setResTseDocument1(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseTextBox2(textBoxData);
                caseData.setResTseDocument2(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseTextBox3(textBoxData);
                caseData.setResTseDocument3(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseTextBox4(textBoxData);
                caseData.setResTseDocument4(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseTextBox5(textBoxData);
                caseData.setResTseDocument5(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                caseData.setResTseTextBox6(textBoxData);
                caseData.setResTseDocument6(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseTextBox7(textBoxData);
                caseData.setResTseDocument7(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_POSTPONE_A_HEARING:
                caseData.setResTseTextBox8(textBoxData);
                caseData.setResTseDocument8(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseTextBox9(textBoxData);
                caseData.setResTseDocument9(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                caseData.setResTseTextBox10(textBoxData);
                caseData.setResTseDocument10(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseTextBox11(textBoxData);
                caseData.setResTseDocument11(createDocumentType(documentUrl));
                break;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                caseData.setResTseTextBox12(textBoxData);
                caseData.setResTseDocument12(createDocumentType(documentUrl));
                break;
            default:
                break;
        }
    }

    private static Stream<Arguments> createRespondentApplication() {
        return Stream.of(
            Arguments.of(SELECTED_APP_AMEND_RESPONSE, "textBox1", "document1"),
            Arguments.of(SELECTED_APP_CHANGE_PERSONAL_DETAILS, "textBox2", "document2"),
            Arguments.of(SELECTED_APP_CLAIMANT_NOT_COMPLIED, "textBox3", "document3"),
            Arguments.of(SELECTED_APP_CONSIDER_A_DECISION_AFRESH, "textBox4", "document4"),
            Arguments.of(SELECTED_APP_CONTACT_THE_TRIBUNAL, "textBox5", "document5"),
            Arguments.of(SELECTED_APP_ORDER_OTHER_PARTY, "textBox6", "document6"),
            Arguments.of(SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE, "textBox7", "document7"),
            Arguments.of(SELECTED_APP_POSTPONE_A_HEARING, "textBox8", "document8"),
            Arguments.of(SELECTED_APP_RECONSIDER_JUDGEMENT, "textBox9", "document9"),
            Arguments.of(SELECTED_APP_RESTRICT_PUBLICITY, "textBox10", "document10"),
            Arguments.of(SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM, "textBox11", "document11"),
            Arguments.of(SELECTED_APP_VARY_OR_REVOKE_AN_ORDER, "textBox12", "document12")
        );
    }

    private UploadedDocumentType createDocumentTypeWithUrl(String url) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("binaryUrl/documents/");
        uploadedDocumentType.setDocumentFilename("testFileName");
        uploadedDocumentType.setDocumentUrl(url);
        return uploadedDocumentType;
    }

    private CaseData createCaseData(String selectedApplication, String selectedRule92Answer) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResTseSelectApplication(selectedApplication);
        caseData.setResTseCopyToOtherPartyYesOrNo(selectedRule92Answer);
        caseData.setEthosCaseReference("test");
        caseData.setClaimant("claimant");
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
        personalisation.put("caseId", CASE_ID);
        if (expectedAnswer != null) {
            personalisation.put("customisedText", String.format(expectedAnswer, selectedApplication));
        }
        return personalisation;
    }
}
