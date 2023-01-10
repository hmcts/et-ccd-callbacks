package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondentReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondentReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("squid:S5961")
class TseAdmReplyServiceTest {

    private TseAdmReplyService tseAdmReplyService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private DocumentManagementService documentManagementService;

    private CaseData caseData;

    private static final String AUTH_TOKEN = "Bearer authToken";
    private static final String ERROR_MSG_ADD_DOC_MISSING = "Select or fill the required Add document field";
    private static final String IS_CMO_OR_REQUEST_CMO = "Case management order";
    private static final String IS_CMO_OR_REQUEST_REQUEST = "Request";

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String CASE_NUMBER = "Some Case Number";
    private static final String CASE_ID = "someCaseId";

    private static final String BOTH_NOTIFY = "Both parties";
    private static final String CLAIMANT_NOTIFY_ONLY = "Claimant only";
    private static final String RESPONDENT_NOTIFY_ONLY = "Respondent only";

    private static final String CLAIMANT_EMAIL = "Claimant@mail.com";
    private static final String RESPONDENT_EMAIL = "Respondent@mail.com";
    private static final String BOTH_RESPOND = "Both parties";
    private static final String CLAIMANT_RESPOND_ONLY = "Claimant";
    private static final String RESPONDENT_RESPOND_ONLY = "Respondent";
    private static final String RESPONSE_REQUIRED =
        "The tribunal requires some information from you about an application.";
    private static final String RESPONSE_NOT_REQUIRED =
        "You have a new message from HMCTS about a claim made to an employment tribunal.";

    @BeforeEach
    void setUp() {
        tseAdmReplyService = new TseAdmReplyService(emailService, documentManagementService);
        ReflectionTestUtils.setField(tseAdmReplyService, "emailToClaimantTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(tseAdmReplyService, "emailToRespondentTemplateId", TEMPLATE_ID);

        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void initialTseAdminTableMarkUp_ReturnString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("1")
                                .withType("Amend response")
                                .withApplicant("Respondent")
                                .withDate("13 December 2022")
                                .withDocumentUpload(createUploadedDocumentType("document.txt"))
                                .withDetails("Details Text")
                                .withCopyToOtherPartyYesOrNo("I confirm I want to copy")
                                .withStatus("Open")
                                .withRespondentReply(List.of(TseRespondentReplyTypeItem.builder()
                                        .id(UUID.randomUUID().toString())
                                        .value(
                                                TseRespondentReplyType.builder()
                                                        .from("Claimant")
                                                        .date("23 December 2022")
                                                        .response("Response Details")
                                                        .hasSupportingMaterial(YES)
                                                        .supportingMaterial(List.of(
                                                                createDocumentTypeItem("image.png"),
                                                                createDocumentTypeItem("Form.pdf")))
                                                        .copyToOtherParty("I do not want to copy")
                                                        .build()
                                        ).build()))
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        String fileDisplay1 = "<a href=\"/documents/%s\" target=\"_blank\">document (TXT, 1MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
                createUploadedDocumentType("document.txt"), AUTH_TOKEN))
                .thenReturn(fileDisplay1);

        String fileDisplay2 = "<a href=\"/documents/%s\" target=\"_blank\">image (PNG, 2MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
                createUploadedDocumentType("image.png"), AUTH_TOKEN))
                .thenReturn(fileDisplay2);

        String fileDisplay3 = "<a href=\"/documents/%s\" target=\"_blank\">Form (PDF, 3MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
                createUploadedDocumentType("Form.pdf"), AUTH_TOKEN))
                .thenReturn(fileDisplay3);

        String expected = "| | |\r\n"
                + "|--|--|\r\n"
                + "|Applicant | Respondent|\r\n"
                + "|Type of application | Amend response|\r\n"
                + "|Application date | 13 December 2022|\r\n"
                + "|Give details | Details Text|\r\n"
                + "|Supporting material | " + fileDisplay1 + "|\r\n"
                + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
                + "| Yes|\r\n"
                + "\r\n"
                + "|Response 1 | |\r\n"
                + "|--|--|\r\n"
                + "|Response from | Claimant|\r\n"
                + "|Response date | 23 December 2022|\r\n"
                + "|What’s your response to the claimant’s application? | Response Details|\r\n"
                + "|Supporting material | " + fileDisplay2 + "<br>" + fileDisplay3 + "<br>" + "|\r\n"
                + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
                + "| No|\r\n"
                + "\r\n";

        String actual = tseAdmReplyService.initialTseAdmReplyTableMarkUp(caseData, AUTH_TOKEN);

        assertThat(actual)
                .isEqualTo(expected);
    }

    private UploadedDocumentType createUploadedDocumentType(String fileName) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("http://dm-store:8080/documents/1234/binary");
        uploadedDocumentType.setDocumentFilename(fileName);
        uploadedDocumentType.setDocumentUrl("http://dm-store:8080/documents/1234");
        return uploadedDocumentType;
    }

    private DocumentTypeItem createDocumentTypeItem(String fileName) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1234");
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument(fileName, "1234").build());
        return documentTypeItem;
    }

    @Test
    void validateInput_Yes_NoDoc_ReturnErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(IS_CMO_OR_REQUEST_CMO);
        caseData.setTseAdmReplyIsResponseRequired(YES);
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(ERROR_MSG_ADD_DOC_MISSING);
    }

    @Test
    void validateInput_CmoYes_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(IS_CMO_OR_REQUEST_CMO);
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_RequestYes_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(IS_CMO_OR_REQUEST_REQUEST);
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_No_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(IS_CMO_OR_REQUEST_CMO);
        caseData.setTseAdmReplyIsResponseRequired(NO);
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_No_NoDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(IS_CMO_OR_REQUEST_CMO);
        caseData.setTseAdmReplyIsResponseRequired(NO);
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_Neither_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest("Neither");
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_Neither_NoDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest("Neither");
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_CmoYes_SaveString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("2")
                                .withType("Change personal details")
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("2", "2 - Change personal details")));
        caseData.setTseAdmReplyEnterResponseTitle("Submit hearing agenda");
        caseData.setTseAdmReplyAdditionalInformation("Additional Information Details");
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest("Case management order");
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyEnterFullName("Full Name");
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplySelectPartyRespond("Both parties");
        caseData.setTseAdmReplySelectPartyNotify("Claimant only");

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseAdminReplyType actual =
                caseData.getGenericTseApplicationCollection().get(0).getValue()
                        .getAdminReply().get(0).getValue();

        assertThat(actual.getDate())
                .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
                .isEqualTo("Submit hearing agenda");
        assertThat(actual.getAdditionalInformation())
                .isEqualTo("Additional Information Details");
        assertThat(actual.getAddDocument())
                .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getIsCmoOrRequest())
                .isEqualTo("Case management order");
        assertThat(actual.getCmoMadeBy())
                .isEqualTo("Legal Officer");
        assertThat(actual.getRequestMadeBy())
                .isNull();
        assertThat(actual.getEnterFullName())
                .isEqualTo("Full Name");
        assertThat(actual.getIsResponseRequired())
                .isEqualTo(YES);
        assertThat(actual.getSelectPartyRespond())
                .isEqualTo("Both parties");
        assertThat(actual.getSelectPartyNotify())
                .isEqualTo("Claimant only");
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_RequestNo_SaveString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("3")
                                .withType("Claimant not complied")
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("3", "3 - Claimant not complied")));
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest("Request");
        caseData.setTseAdmReplyRequestMadeBy("Judge");
        caseData.setTseAdmReplyEnterFullName("Full Name");
        caseData.setTseAdmReplyIsResponseRequired(NO);
        caseData.setTseAdmReplySelectPartyNotify("Respondent only");

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseAdminReplyType actual =
                caseData.getGenericTseApplicationCollection().get(0).getValue()
                        .getAdminReply().get(0).getValue();

        assertThat(actual.getDate())
                .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
                .isNull();
        assertThat(actual.getAdditionalInformation())
                .isNull();
        assertThat(actual.getAddDocument())
                .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getIsCmoOrRequest())
                .isEqualTo("Request");
        assertThat(actual.getCmoMadeBy())
                .isNull();
        assertThat(actual.getRequestMadeBy())
                .isEqualTo("Judge");
        assertThat(actual.getEnterFullName())
                .isEqualTo("Full Name");
        assertThat(actual.getIsResponseRequired())
                .isEqualTo(NO);
        assertThat(actual.getSelectPartyRespond())
                .isNull();
        assertThat(actual.getSelectPartyNotify())
                .isEqualTo("Respondent only");
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_Neither_SaveString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("4")
                                .withType("Consider a decision afresh")
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("4", "4 - Consider a decision afresh")));
        caseData.setTseAdmReplyIsCmoOrRequest("Neither");
        caseData.setTseAdmReplySelectPartyNotify("Both parties");

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseAdminReplyType actual =
                caseData.getGenericTseApplicationCollection().get(0).getValue()
                        .getAdminReply().get(0).getValue();

        assertThat(actual.getDate())
                .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
                .isNull();
        assertThat(actual.getAdditionalInformation())
                .isNull();
        assertThat(actual.getAddDocument())
                .isNull();
        assertThat(actual.getIsCmoOrRequest())
                .isEqualTo("Neither");
        assertThat(actual.getCmoMadeBy())
                .isNull();
        assertThat(actual.getRequestMadeBy())
                .isNull();
        assertThat(actual.getEnterFullName())
                .isNull();
        assertThat(actual.getIsResponseRequired())
                .isNull();
        assertThat(actual.getSelectPartyRespond())
                .isNull();
        assertThat(actual.getSelectPartyNotify())
                .isEqualTo("Both parties");
    }

    @ParameterizedTest
    @MethodSource("sendEmails")
    void sendAdmReplyEmailsNotifications(String admReplySelectPartyNotify,
                                         String admReplyIsResponseRequired,
                                         String admReplySelectPartyRespond,
                                         Boolean emailSentToClaimant,
                                         String expectedClaimantCustomText,
                                         Boolean emailSentToRespondent,
                                         String expectedRespondentCustomText) {
        caseData.setEthosCaseReference(CASE_NUMBER);
        createClaimant(caseData);
        createRespondent(caseData);

        caseData.setTseAdmReplySelectPartyNotify(admReplySelectPartyNotify);
        caseData.setTseAdmReplyIsResponseRequired(admReplyIsResponseRequired);
        caseData.setTseAdmReplySelectPartyRespond(admReplySelectPartyRespond);

        Map<String, String> expectedPersonalisationClaimant =
            createPersonalisation(caseData, expectedClaimantCustomText);
        Map<String, String> expectedPersonalisationRespondent =
            createPersonalisation(caseData, expectedRespondentCustomText);

        tseAdmReplyService.sendAdmReplyEmails(CASE_ID, caseData);

        if (emailSentToClaimant) {
            verify(emailService).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
        } else {
            verify(emailService, never()).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
        }

        if (emailSentToRespondent) {
            verify(emailService).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        } else {
            verify(emailService, never()).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        }
    }

    private static Stream<Arguments> sendEmails() {
        return Stream.of(
            Arguments.of(BOTH_NOTIFY, "Yes", BOTH_RESPOND,
                true, RESPONSE_REQUIRED, true, RESPONSE_REQUIRED),
            Arguments.of(BOTH_NOTIFY, "Yes", CLAIMANT_RESPOND_ONLY,
                true, RESPONSE_REQUIRED, true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_NOTIFY, "Yes", RESPONDENT_RESPOND_ONLY,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_REQUIRED),

            Arguments.of(BOTH_NOTIFY, "No", BOTH_RESPOND,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_NOTIFY, "No", CLAIMANT_RESPOND_ONLY,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_NOTIFY, "No", RESPONDENT_RESPOND_ONLY,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_NOT_REQUIRED),

            Arguments.of(CLAIMANT_NOTIFY_ONLY, "Yes", BOTH_RESPOND,
                true, RESPONSE_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_NOTIFY_ONLY, "Yes", CLAIMANT_RESPOND_ONLY,
                true, RESPONSE_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_NOTIFY_ONLY, "Yes", RESPONDENT_RESPOND_ONLY,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),

            Arguments.of(CLAIMANT_NOTIFY_ONLY, "No", BOTH_RESPOND,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_NOTIFY_ONLY, "No", CLAIMANT_RESPOND_ONLY,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_NOTIFY_ONLY, "No", RESPONDENT_RESPOND_ONLY,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),

            Arguments.of(RESPONDENT_NOTIFY_ONLY, "Yes", BOTH_RESPOND,
                false, "never sent", true, RESPONSE_REQUIRED),
            Arguments.of(RESPONDENT_NOTIFY_ONLY, "Yes", CLAIMANT_RESPOND_ONLY,
                false, "never sent", true, RESPONSE_NOT_REQUIRED),
            Arguments.of(RESPONDENT_NOTIFY_ONLY, "Yes", RESPONDENT_RESPOND_ONLY,
                false, "never sent", true, RESPONSE_REQUIRED),

            Arguments.of(RESPONDENT_NOTIFY_ONLY, "No", BOTH_RESPOND,
                false, "never sent", true, RESPONSE_NOT_REQUIRED),
            Arguments.of(RESPONDENT_NOTIFY_ONLY, "No", CLAIMANT_RESPOND_ONLY,
                false, "never sent", true, RESPONSE_NOT_REQUIRED),
            Arguments.of(RESPONDENT_NOTIFY_ONLY, "No", RESPONDENT_RESPOND_ONLY,
                false, "never sent", true, RESPONSE_NOT_REQUIRED)
        );
    }

    private Map<String, String> createPersonalisation(CaseData caseData,
                                                      String expectedCustomText) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("caseId", CASE_ID);
        if (expectedCustomText != null) {
            personalisation.put("customisedText", expectedCustomText);
        }
        return personalisation;
    }

    @Test
    void clearTseAdminDataFromCaseData() {
        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));
        caseData.setTseAdmReplyTableMarkUp("| | |\r\n|--|--|\r\n|%s application | %s|\r\n\r\n");
        caseData.setTseAdmReplyEnterResponseTitle("View notice of hearing");
        caseData.setTseAdmReplyAdditionalInformation("Additional information text");
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest("Case management order");
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyRequestMadeBy("Legal Officer");
        caseData.setTseAdmReplyEnterFullName("Enter Full Name");
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplySelectPartyRespond("Both parties");
        caseData.setTseAdmReplySelectPartyNotify("Claimant only");

        tseAdmReplyService.clearTseAdmReplyDataFromCaseData(caseData);

        assertThat(caseData.getTseAdminSelectApplication()).isNull();
        assertThat(caseData.getTseAdmReplyTableMarkUp()).isNull();
        assertThat(caseData.getTseAdmReplyEnterResponseTitle()).isNull();
        assertThat(caseData.getTseAdmReplyAdditionalInformation()).isNull();
        assertThat(caseData.getTseAdmReplyAddDocument()).isNull();
        assertThat(caseData.getTseAdmReplyIsCmoOrRequest()).isNull();
        assertThat(caseData.getTseAdmReplyCmoMadeBy()).isNull();
        assertThat(caseData.getTseAdmReplyRequestMadeBy()).isNull();
        assertThat(caseData.getTseAdmReplyEnterFullName()).isNull();
        assertThat(caseData.getTseAdmReplyIsResponseRequired()).isNull();
        assertThat(caseData.getTseAdmReplySelectPartyRespond()).isNull();
        assertThat(caseData.getTseAdmReplySelectPartyNotify()).isNull();
    }

    private void createClaimant(CaseData caseData) {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(CLAIMANT_EMAIL);
        caseData.setClaimantType(claimantType);
    }

    private void createRespondent(CaseData caseData) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL);

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(respondentSumTypeItem)));
    }
}
