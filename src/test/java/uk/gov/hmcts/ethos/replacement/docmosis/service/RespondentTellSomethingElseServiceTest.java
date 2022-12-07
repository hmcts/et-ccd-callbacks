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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;

@ExtendWith(SpringExtension.class)
class RespondentTellSomethingElseServiceTest {
    private RespondentTellSomethingElseService respondentTellSomethingElseService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserService userService;

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String YES = "I do want to copy";
    private static final String NO = "I do not want to copy";
    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String LEGAL_REP_EMAIL = "mail@mail.com";
    private static final String CASE_ID = "669718251103419";

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
        respondentTellSomethingElseService = new RespondentTellSomethingElseService(emailService, userService);
        ReflectionTestUtils.setField(respondentTellSomethingElseService, "emailTemplateId", TEMPLATE_ID);

        UserDetails userDetails = HelperTest.getUserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
    }

    @ParameterizedTest
    @MethodSource
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
