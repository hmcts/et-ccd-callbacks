package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CLAIMANT_NOT_COMPLIED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"squid:S5961", "PMD.ExcessiveImports", "PMD.TooManyMethods"})
class TseAdminServiceTest {
    private TseAdminService tseAdminService;

    @MockBean
    private EmailService emailService;
    @MockBean
    private DocumentManagementService documentManagementService;

    private CaseData caseData;

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String CASE_NUMBER = "Some Case Number";
    private static final String CASE_ID = "4321";

    private static final String CLAIMANT_EMAIL = "Claimant@mail.com";
    private static final String CLAIMANT_FIRSTNAME = "Claim";
    private static final String CLAIMANT_LASTNAME = "Ant";

    private static final String RESPONDENT_EMAIL = "Respondent@mail.com";

    private static final String AUTH_TOKEN = "Bearer authToken";

    @BeforeEach
    void setUp() {
        tseAdminService = new TseAdminService(emailService, documentManagementService);
        ReflectionTestUtils.setField(tseAdminService, "emailToClaimantTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(tseAdminService, "emailToRespondentTemplateId", TEMPLATE_ID);
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void initialTseAdminTableMarkUp_withDoc_ReturnString() {

        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(getGenericTseApplicationTypeItemBuild())
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

        String fileDisplay4 = "<a href=\"/documents/%s\" target=\"_blank\">Admin (TXT, 1MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
            createUploadedDocumentType("admin.txt"), AUTH_TOKEN))
            .thenReturn(fileDisplay4);

        String expected = "| | |\r\n"
            + "|--|--|\r\n"
            + "|Respondent application | Amend response|\r\n"
            + "|Application date | 13 December 2022|\r\n"
            + "|Supporting material | " + fileDisplay1 + "|\r\n"
            + "\r\n"
            + "|Response 1 | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | Claimant|\r\n"
            + "|Response date | 23 December 2022|\r\n"
            + "|Details | Response Details|\r\n"
            + "|Supporting material | " + fileDisplay2 + "<br>" + fileDisplay3 + "<br>" + "|\r\n"
            + "\r\n"
            + "|Response 2 | |\r\n"
            + "|--|--|\r\n"
            + "|Response | Title of Response|\r\n"
            + "|Date | 24 December 2022|\r\n"
            + "|Sent by | Tribunal|\r\n"
            + "|Case management order or request? | Request|\r\n"
            + "|Response due | Yes - view document for details|\r\n"
            + "|Party or parties to respond | Both parties|\r\n"
            + "|Additional information | Optional Text entered by admin|\r\n"
            + "|Supporting material | " + fileDisplay4 + "|\r\n"
            + "|Request made by | Caseworker|\r\n"
            + "|Full name | Mr Jimmy|\r\n"
            + "|Sent to | Both parties|\r\n"
            + "\r\n";

        tseAdminService.initialTseAdminTableMarkUp(caseData, AUTH_TOKEN);
        assertThat(caseData.getTseAdminTableMarkUp())
            .isEqualTo(expected);
    }

    private GenericTseApplicationType getGenericTseApplicationTypeItemBuild() {
        TseRespondTypeItem claimantReply = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .from(CLAIMANT_TITLE)
                    .date("23 December 2022")
                    .response("Response Details")
                    .hasSupportingMaterial(YES)
                    .supportingMaterial(List.of(
                        createDocumentTypeItem("image.png"),
                        createDocumentTypeItem("Form.pdf")))
                    .copyToOtherParty(YES)
                    .build()
            ).build();

        TseRespondTypeItem adminReply = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .from(ADMIN)
                    .date("24 December 2022")
                    .enterResponseTitle("Title of Response")
                    .isCmoOrRequest("Request")
                    .isResponseRequired("Yes - view document for details")
                    .selectPartyRespond("Both parties")
                    .additionalInformation("Optional Text entered by admin")
                    .addDocument(createUploadedDocumentType("admin.txt"))
                    .requestMadeBy("Caseworker")
                    .madeByFullName("Mr Jimmy")
                    .selectPartyNotify("Both parties")
                    .build()
            )
            .build();

        return TseApplicationBuilder.builder()
            .withNumber("1")
            .withType(TSE_APP_AMEND_RESPONSE)
            .withApplicant(RESPONDENT_TITLE)
            .withDate("13 December 2022")
            .withDocumentUpload(createUploadedDocumentType("document.txt"))
            .withStatus(OPEN_STATE)
            .withRespondCollection(List.of(
                claimantReply,
                adminReply
            ))
            .build();
    }

    private static UploadedDocumentType createUploadedDocumentType(String fileName) {
        return UploadedDocumentBuilder.builder()
            .withFilename(fileName)
            .withUuid("1234")
            .build();
    }

    private DocumentTypeItem createDocumentTypeItem(String fileName) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1234");
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument(fileName, "1234").build());
        return documentTypeItem;
    }

    @Test
    void initialTseAdminTableMarkUp_NoDoc_ReturnString() {

        TseRespondTypeItem claimantReply = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .from(CLAIMANT_TITLE)
                    .date("23 December 2022")
                    .response("Response Details")
                    .hasSupportingMaterial(NO)
                    .copyToOtherParty(YES)
                    .build()
            ).build();

        TseRespondTypeItem adminReply = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .from(ADMIN)
                    .date("24 December 2022")
                    .isCmoOrRequest("Neither")
                    .selectPartyNotify("Both parties")
                    .build()
            )
            .build();

        GenericTseApplicationType genericTseApplication = TseApplicationBuilder.builder()
            .withNumber("1")
            .withType(TSE_APP_AMEND_RESPONSE)
            .withApplicant(RESPONDENT_TITLE)
            .withDate("13 December 2022")
            .withDetails("Details Text")
            .withStatus(OPEN_STATE)
            .withRespondCollection(List.of(
                claimantReply,
                adminReply
            ))
            .build();

        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(genericTseApplication)
                .build())
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        String expected = "| | |\r\n"
            + "|--|--|\r\n"
            + "|Respondent application | Amend response|\r\n"
            + "|Application date | 13 December 2022|\r\n"
            + "|Details of the application | Details Text|\r\n"
            + "\r\n"
            + "|Response 1 | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | Claimant|\r\n"
            + "|Response date | 23 December 2022|\r\n"
            + "|Details | Response Details|\r\n"
            + "|Supporting material | |\r\n" // TODO: Remove Empty Row
            + "\r\n"
            + "|Response 2 | |\r\n"
            + "|--|--|\r\n"
            + "|Response | |\r\n" // TODO: Remove Empty Row
            + "|Date | 24 December 2022|\r\n"
            + "|Sent by | Tribunal|\r\n"
            + "|Case management order or request? | Neither|\r\n"
            + "|Response due | |\r\n" // TODO: Remove Empty Row
            + "|Party or parties to respond | |\r\n" // TODO: Remove Empty Row
            + "|Additional information | |\r\n" // TODO: Remove Empty Row
            + "|Supporting material | |\r\n" // TODO: Remove Empty Row
            + "|Full name | |\r\n" // TODO: Remove Empty Row
            + "|Sent to | Both parties|\r\n"
            + "\r\n";

        tseAdminService.initialTseAdminTableMarkUp(caseData, AUTH_TOKEN);
        assertThat(caseData.getTseAdminTableMarkUp())
            .isEqualTo(expected);
    }

    @Test
    void saveTseAdminDataFromCaseData_Judgment_SaveString() {
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

        caseData.setTseAdminEnterNotificationTitle("Submit hearing agenda");
        caseData.setTseAdminDecision("Granted");
        caseData.setTseAdminTypeOfDecision("Judgment");
        caseData.setTseAdminAdditionalInformation("Additional information");
        caseData.setTseAdminResponseRequiredNoDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminDecisionMadeBy("Legal officer");
        caseData.setTseAdminDecisionMadeByFullName("Legal Officer Full Name");
        caseData.setTseAdminSelectPartyNotify(BOTH_PARTIES);

        tseAdminService.saveTseAdminDataFromCaseData(caseData);

        TseAdminRecordDecisionType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getAdminDecision().get(0).getValue();

        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterNotificationTitle())
            .isEqualTo("Submit hearing agenda");
        assertThat(actual.getDecision())
            .isEqualTo("Granted");
        assertThat(actual.getDecisionDetails())
            .isNull();
        assertThat(actual.getTypeOfDecision())
            .isEqualTo("Judgment");
        assertThat(actual.getIsResponseRequired())
            .isNull();
        assertThat(actual.getSelectPartyRespond())
            .isNull();
        assertThat(actual.getAdditionalInformation())
            .isEqualTo("Additional information");
        assertThat(actual.getResponseRequiredDoc())
            .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getDecisionMadeBy())
            .isEqualTo("Legal officer");
        assertThat(actual.getDecisionMadeByFullName())
            .isEqualTo("Legal Officer Full Name");
        assertThat(actual.getSelectPartyNotify())
            .isEqualTo(BOTH_PARTIES);
    }

    @Test
    void saveTseAdminDataFromCaseData_CmoYes_SaveString() {
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

        caseData.setTseAdminEnterNotificationTitle("View notice of hearing");
        caseData.setTseAdminDecision("Other");
        caseData.setTseAdminDecisionDetails("Decision details text");
        caseData.setTseAdminTypeOfDecision(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdminIsResponseRequired(YES);
        caseData.setTseAdminSelectPartyRespond(CLAIMANT_TITLE);
        caseData.setTseAdminAdditionalInformation("Additional information text");
        caseData.setTseAdminResponseRequiredYesDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify(CLAIMANT_ONLY);

        tseAdminService.saveTseAdminDataFromCaseData(caseData);

        TseAdminRecordDecisionType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getAdminDecision().get(0).getValue();

        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterNotificationTitle())
            .isEqualTo("View notice of hearing");
        assertThat(actual.getDecision())
            .isEqualTo("Other");
        assertThat(actual.getDecisionDetails())
            .isEqualTo("Decision details text");
        assertThat(actual.getTypeOfDecision())
            .isEqualTo(CASE_MANAGEMENT_ORDER);
        assertThat(actual.getIsResponseRequired())
            .isEqualTo(YES);
        assertThat(actual.getSelectPartyRespond())
            .isEqualTo(CLAIMANT_TITLE);
        assertThat(actual.getAdditionalInformation())
            .isEqualTo("Additional information text");
        assertThat(actual.getResponseRequiredDoc())
            .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getDecisionMadeBy())
            .isEqualTo("Judge");
        assertThat(actual.getDecisionMadeByFullName())
            .isEqualTo("Judge Full Name");
        assertThat(actual.getSelectPartyNotify())
            .isEqualTo(CLAIMANT_ONLY);
    }

    @Test
    void saveTseAdminDataFromCaseData_CmoNo_SaveString() {
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

        caseData.setTseAdminDecision("Refused");
        caseData.setTseAdminTypeOfDecision(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdminIsResponseRequired(NO);
        caseData.setTseAdminResponseRequiredNoDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify(RESPONDENT_ONLY);

        tseAdminService.saveTseAdminDataFromCaseData(caseData);

        TseAdminRecordDecisionType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getAdminDecision().get(0).getValue();

        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterNotificationTitle())
            .isNull();
        assertThat(actual.getDecision())
            .isEqualTo("Refused");
        assertThat(actual.getDecisionDetails())
            .isNull();
        assertThat(actual.getTypeOfDecision())
            .isEqualTo(CASE_MANAGEMENT_ORDER);
        assertThat(actual.getIsResponseRequired())
            .isEqualTo(NO);
        assertThat(actual.getSelectPartyRespond())
            .isNull();
        assertThat(actual.getAdditionalInformation())
            .isNull();
        assertThat(actual.getResponseRequiredDoc())
            .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getDecisionMadeBy())
            .isEqualTo("Judge");
        assertThat(actual.getDecisionMadeByFullName())
            .isEqualTo("Judge Full Name");
        assertThat(actual.getSelectPartyNotify())
            .isEqualTo(RESPONDENT_ONLY);
    }

    @ParameterizedTest
    @CsvSource({BOTH_PARTIES, CLAIMANT_ONLY, RESPONDENT_ONLY})
    void sendRecordADecisionEmails(String partyNotified) {
        caseData.setEthosCaseReference(CASE_NUMBER);
        createClaimant(caseData);
        createRespondent(caseData);
        caseData.setTseAdminSelectPartyNotify(partyNotified);

        Map<String, String> expectedPersonalisationClaimant =
            createPersonalisation(caseData, CLAIMANT_FIRSTNAME + " " + CLAIMANT_LASTNAME);
        Map<String, String> expectedPersonalisationRespondent =
            createPersonalisation(caseData, RESPONDENT_TITLE);

        tseAdminService.sendRecordADecisionEmails(CASE_ID, caseData);

        if (CLAIMANT_ONLY.equals(partyNotified)) {
            verify(emailService).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
            verify(emailService, never()).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        } else if (RESPONDENT_ONLY.equals(partyNotified)) {
            verify(emailService, never()).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
            verify(emailService).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        } else {
            verify(emailService).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
            verify(emailService).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        }
    }

    private void createClaimant(CaseData caseData) {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(CLAIMANT_EMAIL);

        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames(CLAIMANT_FIRSTNAME);
        claimantIndType.setClaimantLastName(CLAIMANT_LASTNAME);

        caseData.setClaimantType(claimantType);
        caseData.setClaimantIndType(claimantIndType);
    }

    private void createRespondent(CaseData caseData) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_TITLE);
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL);

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(respondentSumTypeItem)));
    }

    private Map<String, String> createPersonalisation(CaseData caseData,
                                                      String expectedName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("caseId", CASE_ID);
        personalisation.put("name", expectedName);
        return personalisation;
    }

    @Test
    void clearTseAdminDataFromCaseData() {
        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));
        caseData.setTseAdminTableMarkUp("| | |\r\n|--|--|\r\n|%s application | %s|\r\n|Application date | %s|\r\n\r\n");
        caseData.setTseAdminEnterNotificationTitle("View notice of hearing");
        caseData.setTseAdminDecision("Other");
        caseData.setTseAdminDecisionDetails("Decision details text");
        caseData.setTseAdminTypeOfDecision(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdminIsResponseRequired(YES);
        caseData.setTseAdminSelectPartyRespond(CLAIMANT_TITLE);
        caseData.setTseAdminAdditionalInformation("Additional information text");
        caseData.setTseAdminResponseRequiredYesDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminResponseRequiredNoDoc(null);
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify(CLAIMANT_ONLY);

        tseAdminService.clearTseAdminDataFromCaseData(caseData);

        assertThat(caseData.getTseAdminSelectApplication()).isNull();
        assertThat(caseData.getTseAdminTableMarkUp()).isNull();
        assertThat(caseData.getTseAdminEnterNotificationTitle()).isNull();
        assertThat(caseData.getTseAdminDecision()).isNull();
        assertThat(caseData.getTseAdminDecisionDetails()).isNull();
        assertThat(caseData.getTseAdminTypeOfDecision()).isNull();
        assertThat(caseData.getTseAdminIsResponseRequired()).isNull();
        assertThat(caseData.getTseAdminSelectPartyRespond()).isNull();
        assertThat(caseData.getTseAdminAdditionalInformation()).isNull();
        assertThat(caseData.getTseAdminResponseRequiredYesDoc()).isNull();
        assertThat(caseData.getTseAdminResponseRequiredNoDoc()).isNull();
        assertThat(caseData.getTseAdminDecisionMadeBy()).isNull();
        assertThat(caseData.getTseAdminDecisionMadeByFullName()).isNull();
        assertThat(caseData.getTseAdminSelectPartyNotify()).isNull();
    }

    @Test
    void updateStatusToClose() {
        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(TseApplicationBuilder.builder()
                    .withNumber("1")
                    .withType(TSE_APP_AMEND_RESPONSE)
                    .withStatus(OPEN_STATE)
                    .build())
                .build())
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));
        caseData.setTseAdminCloseApplicationTable("| | |\r\n|--|--|\r\n|%s application | %s|\r\n");
        caseData.setTseAdminCloseApplicationText("General notes");

        tseAdminService.aboutToSubmitCloseApplication(caseData);

        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue().getStatus())
            .isEqualTo(CLOSED_STATE);
        assertThat(caseData.getTseAdminSelectApplication())
            .isNull();
        assertThat(caseData.getTseAdminCloseApplicationTable())
            .isNull();
        assertThat(caseData.getTseAdminCloseApplicationText())
            .isNull();
    }

    @ParameterizedTest
    @MethodSource("generateCloseApplicationMarkdown")
    void generateCloseApplicationMarkdown(boolean appHasDoc, boolean appHasDetails,
                                          boolean hasDoc, boolean hasAdditionalInfo) {
        GenericTseApplicationType tseApplicationType =
            getTseAppType(appHasDoc, appHasDetails, hasDoc, hasAdditionalInfo);
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(tseApplicationType)
                        .build())
        );

        String expected = "| | |\r\n"
                + "|--|--|\r\n"
                + "|Applicant | Respondent|\r\n"
                + "|Type of application | Amend response|\r\n"
                + "|Application date | 13 December 2022|\r\n"
                + (appHasDetails ? "|What do you want to tell or ask the tribunal? | Details Text|\r\n" : "")
                + (appHasDoc
                    ? "|Supporting material | <a href=\"/documents/%s\" target=\"_blank\">document (TXT, 1MB)</a>|\r\n"
                    : "")
                + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | "
                + null + "|\r\n"
                + "\r\n"
                + "|Decision | |\r\n"
                + "|--|--|\r\n"
                + "|Notification | Response Details|\r\n"
                + "|Decision | decision|\r\n"
                + "|Decision details | decision details|\r\n"
                + "|Date | 23 December 2022|\r\n"
                + "|Sent by | Tribunal|\r\n"
                + "|Type of decision | type of decision|\r\n"
                + (hasAdditionalInfo ? "|Additional information | additional info|\r\n" : "")
                + (hasDoc ? "|Document | <a href=\"/documents/%s\" target=\"_blank\">document (TXT, 1MB)</a>|\r\n" : "")
                + "|Decision made by | decision made by|\r\n"
                + "|Name | made by full name|\r\n"
                + "|Sent to | party notify|\r\n"
                + "\r\n";

        String fileDisplay1 = "<a href=\"/documents/%s\" target=\"_blank\">document (TXT, 1MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
                any(), any()))
                .thenReturn(fileDisplay1);

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        assertThat(tseAdminService.generateCloseApplicationDetailsMarkdown(caseData, AUTH_TOKEN))
                .isEqualTo(expected);

    }

    private static Stream<Arguments> generateCloseApplicationMarkdown() {
        return Stream.of(
            Arguments.of(true, true, true, true),
            Arguments.of(true, false, true, false),
            Arguments.of(false, true, false, true)
        );
    }

    private static GenericTseApplicationType getTseAppType(boolean appHasDoc, boolean appHasDetails,
                                                           boolean hasDoc, boolean hasAdditionalInfo) {
        TseAdminRecordDecisionTypeItem recordDecisionTypeItem = TseAdminRecordDecisionTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(
                        TseAdminRecordDecisionType.builder()
                                .date("23 December 2022")
                                .enterNotificationTitle("Response Details")
                                .decision("decision")
                                .decisionDetails("decision details")
                                .typeOfDecision("type of decision")
                                .additionalInformation(hasAdditionalInfo ? "additional info" : null)
                                .decisionMadeBy("decision made by")
                                .decisionMadeByFullName("made by full name")
                                .selectPartyNotify("party notify")
                                .responseRequiredDoc(hasDoc ? createUploadedDocumentType("admin.txt") : null)
                                .build()
                ).build();

        GenericTseApplicationType tseApplicationBuilder = TseApplicationBuilder.builder()
                .withNumber("1")
                .withType(TSE_APP_AMEND_RESPONSE)
                .withApplicant(RESPONDENT_TITLE)
                .withDate("13 December 2022")
                .withDetails(appHasDetails ? "Details Text" : null)
                .withStatus(OPEN_STATE)
                .withDecisionCollection(List.of(
                        recordDecisionTypeItem
                ))
                .build();

        if (appHasDoc) {
            tseApplicationBuilder.setDocumentUpload(
                UploadedDocumentBuilder.builder()
                    .withFilename("test")
                    .withUuid("1234")
                    .build());
        }

        return tseApplicationBuilder;
    }
}
