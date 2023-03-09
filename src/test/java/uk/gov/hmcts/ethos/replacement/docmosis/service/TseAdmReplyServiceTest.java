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
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEITHER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REQUEST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CLAIMANT_NOT_COMPLIED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class TseAdmReplyServiceTest {

    private TseAdmReplyService tseAdmReplyService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private DocumentManagementService documentManagementService;

    private CaseData caseData;

    private static final String AUTH_TOKEN = "Bearer authToken";
    private static final String ERROR_MSG_ADD_DOC_MISSING = "Select or fill the required Add document field";

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String CASE_NUMBER = "Some Case Number";
    private static final String CASE_ID = "someCaseId";

    private static final String CLAIMANT_EMAIL = "Claimant@mail.com";
    private static final String RESPONDENT_EMAIL = "Respondent@mail.com";
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
    void initialTseAdminTableMarkUp_Rule92Yes_ReturnString() {
        TseRespondType tseRespondType = TseRespondType.builder()
            .from(CLAIMANT_TITLE)
            .date("23 December 2022")
            .response("Response Details")
            .hasSupportingMaterial(YES)
            .supportingMaterial(List.of(
                createDocumentTypeItem("image.png"),
                createDocumentTypeItem("Form.pdf")))
            .copyToOtherParty(YES)
            .build();

        TseRespondTypeItem tseRespondTypeItem = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(tseRespondType)
            .build();

        GenericTseApplicationType genericTseApplicationType = TseApplicationBuilder.builder()
            .withNumber("1")
            .withType(TSE_APP_AMEND_RESPONSE)
            .withApplicant(RESPONDENT_TITLE)
            .withDate("13 December 2022")
            .withDocumentUpload(createUploadedDocumentType("document.txt"))
            .withDetails("Details Text")
            .withCopyToOtherPartyYesOrNo(YES)
            .withStatus(OPEN_STATE)
            .withRespondCollection(List.of(tseRespondTypeItem))
            .build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = GenericTseApplicationTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(genericTseApplicationType)
            .build();

        caseData.setGenericTseApplicationCollection(
            List.of(genericTseApplicationTypeItem)
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
            + "|Details of the application | Details Text|\r\n"
            + "|Supporting material | " + fileDisplay1 + "|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
            + "| Yes|\r\n"
            + "\r\n"
            + "|Response 1 | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | Claimant|\r\n"
            + "|Response date | 23 December 2022|\r\n"
            + "|What’s your response to the respondent’s application? | Response Details|\r\n"
            + "|Supporting material | " + fileDisplay2 + "<br>" + fileDisplay3 + "<br>" + "|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
            + "| Yes|\r\n"
            + "\r\n";

        String actual = tseAdmReplyService.initialTseAdmReplyTableMarkUp(caseData, AUTH_TOKEN);

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    void initialTseAdminTableMarkUp_Rule92No_ReturnString() {
        TseRespondTypeItem tseRespondTypeItem = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(TseRespondType.builder()
                .from(CLAIMANT_TITLE)
                .date("23 December 2022")
                .response("Response Details")
                .copyToOtherParty(NO)
                .copyNoGiveDetails("No Details")
                .build())
            .build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = GenericTseApplicationTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(TseApplicationBuilder.builder()
                .withNumber("1")
                .withType(TSE_APP_AMEND_RESPONSE)
                .withApplicant(RESPONDENT_TITLE)
                .withDate("13 December 2022")
                .withDetails("Details Text")
                .withCopyToOtherPartyYesOrNo(NO)
                .withCopyToOtherPartyText("Rule92 Text")
                .withStatus(OPEN_STATE)
                .withRespondCollection(List.of(tseRespondTypeItem))
                .build())
            .build();

        caseData.setGenericTseApplicationCollection(
            List.of(genericTseApplicationTypeItem)
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        String expected = "| | |\r\n"
            + "|--|--|\r\n"
            + "|Applicant | Respondent|\r\n"
            + "|Type of application | Amend response|\r\n"
            + "|Application date | 13 December 2022|\r\n"
            + "|Details of the application | Details Text|\r\n"
            + "|Supporting material | |\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | No|\r\n"
            + "|Details of why you do not want to inform the other party | Rule92 Text|\r\n"
            + "\r\n"
            + "|Response 1 | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | Claimant|\r\n"
            + "|Response date | 23 December 2022|\r\n"
            + "|What’s your response to the respondent’s application? | Response Details|\r\n"
            + "|Supporting material | |\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | No|\r\n"
            + "|Details of why you do not want to inform the other party | No Details|\r\n"
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
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyIsResponseRequired(YES);
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(ERROR_MSG_ADD_DOC_MISSING);
    }

    @Test
    void validateInput_CmoYes_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_RequestYes_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(REQUEST);
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_No_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyIsResponseRequired(NO);
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_No_NoDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyIsResponseRequired(NO);
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_Neither_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(NEITHER);
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_Neither_NoDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(NEITHER);
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
                    .withType(TSE_APP_CHANGE_PERSONAL_DETAILS)
                    .build())
                .build())
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("2", "2 - Change personal details")));
        caseData.setTseAdmReplyEnterResponseTitle("Submit hearing agenda");
        caseData.setTseAdmReplyAdditionalInformation("Additional Information Details");
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyEnterFullName("Full Name");
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplySelectPartyRespond(BOTH_PARTIES);
        caseData.setTseAdmReplySelectPartyNotify(CLAIMANT_ONLY);

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseRespondType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getRespondCollection().get(0).getValue();

        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
            .isEqualTo("Submit hearing agenda");
        assertThat(actual.getAdditionalInformation())
            .isEqualTo("Additional Information Details");
        assertThat(actual.getAddDocument())
            .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getIsCmoOrRequest())
            .isEqualTo(CASE_MANAGEMENT_ORDER);
        assertThat(actual.getCmoMadeBy())
            .isEqualTo("Legal Officer");
        assertThat(actual.getRequestMadeBy())
            .isNull();
        assertThat(actual.getMadeByFullName())
            .isEqualTo("Full Name");
        assertThat(actual.getIsResponseRequired())
            .isEqualTo(YES);
        assertThat(actual.getSelectPartyRespond())
            .isEqualTo(BOTH_PARTIES);
        assertThat(actual.getSelectPartyNotify())
            .isEqualTo(CLAIMANT_ONLY);
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_RequestNo_SaveString() {
        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(TseApplicationBuilder.builder()
                    .withNumber("3")
                    .withType(TSE_APP_CLAIMANT_NOT_COMPLIED)
                    .build())
                .build())
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("3", "3 - Claimant not complied")));
        caseData.setTseAdmReplyAddDocument(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest(REQUEST);
        caseData.setTseAdmReplyRequestMadeBy("Judge");
        caseData.setTseAdmReplyEnterFullName("Full Name");
        caseData.setTseAdmReplyIsResponseRequired(NO);
        caseData.setTseAdmReplySelectPartyNotify(RESPONDENT_ONLY);

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseRespondType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getRespondCollection().get(0).getValue();

        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
            .isNull();
        assertThat(actual.getAdditionalInformation())
            .isNull();
        assertThat(actual.getAddDocument())
            .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getIsCmoOrRequest())
            .isEqualTo(REQUEST);
        assertThat(actual.getCmoMadeBy())
            .isNull();
        assertThat(actual.getRequestMadeBy())
            .isEqualTo("Judge");
        assertThat(actual.getMadeByFullName())
            .isEqualTo("Full Name");
        assertThat(actual.getIsResponseRequired())
            .isEqualTo(NO);
        assertThat(actual.getSelectPartyRespond())
            .isNull();
        assertThat(actual.getSelectPartyNotify())
            .isEqualTo(RESPONDENT_ONLY);
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_Neither_SaveString() {
        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(TseApplicationBuilder.builder()
                    .withNumber("4")
                    .withType(TSE_APP_CONSIDER_A_DECISION_AFRESH)
                    .build())
                .build())
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("4", "4 - Consider a decision afresh")));
        caseData.setTseAdmReplyIsCmoOrRequest(NEITHER);
        caseData.setTseAdmReplySelectPartyNotify(BOTH_PARTIES);

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseRespondType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getRespondCollection().get(0).getValue();

        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
            .isNull();
        assertThat(actual.getAdditionalInformation())
            .isNull();
        assertThat(actual.getAddDocument())
            .isNull();
        assertThat(actual.getIsCmoOrRequest())
            .isEqualTo(NEITHER);
        assertThat(actual.getCmoMadeBy())
            .isNull();
        assertThat(actual.getRequestMadeBy())
            .isNull();
        assertThat(actual.getMadeByFullName())
            .isNull();
        assertThat(actual.getIsResponseRequired())
            .isNull();
        assertThat(actual.getSelectPartyRespond())
            .isNull();
        assertThat(actual.getSelectPartyNotify())
            .isEqualTo(BOTH_PARTIES);
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
            Arguments.of(BOTH_PARTIES, "Yes", BOTH_PARTIES,
                true, RESPONSE_REQUIRED, true, RESPONSE_REQUIRED),
            Arguments.of(BOTH_PARTIES, "Yes", CLAIMANT_TITLE,
                true, RESPONSE_REQUIRED, true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_PARTIES, "Yes", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_REQUIRED),

            Arguments.of(BOTH_PARTIES, "No", BOTH_PARTIES,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_PARTIES, "No", CLAIMANT_TITLE,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_PARTIES, "No", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED, true, RESPONSE_NOT_REQUIRED),

            Arguments.of(CLAIMANT_ONLY, "Yes", BOTH_PARTIES,
                true, RESPONSE_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_ONLY, "Yes", CLAIMANT_TITLE,
                true, RESPONSE_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_ONLY, "Yes", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),

            Arguments.of(CLAIMANT_ONLY, "No", BOTH_PARTIES,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_ONLY, "No", CLAIMANT_TITLE,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),
            Arguments.of(CLAIMANT_ONLY, "No", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED, false, "never sent"),

            Arguments.of(RESPONDENT_ONLY, "Yes", BOTH_PARTIES,
                false, "never sent", true, RESPONSE_REQUIRED),
            Arguments.of(RESPONDENT_ONLY, "Yes", CLAIMANT_TITLE,
                false, "never sent", true, RESPONSE_NOT_REQUIRED),
            Arguments.of(RESPONDENT_ONLY, "Yes", RESPONDENT_TITLE,
                false, "never sent", true, RESPONSE_REQUIRED),

            Arguments.of(RESPONDENT_ONLY, "No", BOTH_PARTIES,
                false, "never sent", true, RESPONSE_NOT_REQUIRED),
            Arguments.of(RESPONDENT_ONLY, "No", CLAIMANT_TITLE,
                false, "never sent", true, RESPONSE_NOT_REQUIRED),
            Arguments.of(RESPONDENT_ONLY, "No", RESPONDENT_TITLE,
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
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyRequestMadeBy("Legal Officer");
        caseData.setTseAdmReplyEnterFullName("Enter Full Name");
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplySelectPartyRespond(BOTH_PARTIES);
        caseData.setTseAdmReplySelectPartyNotify(CLAIMANT_ONLY);

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
