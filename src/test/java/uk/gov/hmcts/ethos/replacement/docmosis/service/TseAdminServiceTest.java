package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TestEmailService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
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
class TseAdminServiceTest {
    private TseAdminService tseAdminService;
    private EmailService emailService;

    @MockBean
    private TseService tseService;

    private CaseData caseData;

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String CASE_NUMBER = "Some Case Number";
    private static final String CASE_ID = "4321";

    private static final String CLAIMANT_EMAIL = "Claimant@mail.com";
    private static final String CLAIMANT_FIRSTNAME = "Claim";
    private static final String CLAIMANT_LASTNAME = "Ant";

    private static final String RESPONDENT_EMAIL = "Respondent@mail.com";

    private static final String AUTH_TOKEN = "Bearer authToken";

    private static final String XUI_URL = "exuiUrl";

    private static final String CITIZEN_URL = "citizenUrl";

    @BeforeEach
    void setUp() {
        emailService = spy(new TestEmailService());
        tseAdminService = new TseAdminService(emailService, tseService);
        ReflectionTestUtils.setField(tseAdminService, "tseAdminRecordClaimantTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(tseAdminService, "tseAdminRecordRespondentTemplateId", TEMPLATE_ID);

        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void initialTseAdminTableMarkUp_ReturnString() {
        GenericTseApplicationType application = getGenericTseApplicationTypeItemBuild();

        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(application)
                .build())
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        List<String[]> applicationDetailsRows = new ArrayList<>();
        applicationDetailsRows.add(new String[] {"details", ""});
        when(tseService.getApplicationDetailsRows(application, AUTH_TOKEN, true))
            .thenReturn(applicationDetailsRows);

        List<String[]> formattedApplicationResponses = new ArrayList<>();
        formattedApplicationResponses.add(new String[] {"responses", ""});
        when(tseService.formatApplicationResponses(any(), any(), anyBoolean()))
            .thenReturn(formattedApplicationResponses);

        String expected = """
            |Application||\r
            |--|--|\r
            |details||\r
            |responses||\r
            """;

        tseAdminService.initialTseAdminTableMarkUp(caseData, AUTH_TOKEN);

        assertThat(caseData.getTseAdminTableMarkUp()).isEqualTo(expected);
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
                    .addDocument(createDocumentList("admin.txt"))
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

    private List<GenericTypeItem<DocumentType>> createDocumentList(String fileName) {
        return List.of(GenericTypeItem.from(DocumentType.from(createUploadedDocumentType(fileName))));
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
        List<GenericTypeItem<DocumentType>> documentList = createDocumentList("document.txt");
        caseData.setTseAdminResponseRequiredNoDoc(documentList);
        caseData.setTseAdminDecisionMadeBy("Legal officer");
        caseData.setTseAdminDecisionMadeByFullName("Legal Officer Full Name");
        caseData.setTseAdminSelectPartyNotify(BOTH_PARTIES);

        tseAdminService.saveTseAdminDataFromCaseData(caseData);

        GenericTseApplicationType app = caseData.getGenericTseApplicationCollection().get(0).getValue();
        assertThat(app.getApplicationState())
            .isEqualTo("notViewedYet");

        TseAdminRecordDecisionType actual = app.getAdminDecision().get(0).getValue();

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
            .isEqualTo(documentList);
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
        List<GenericTypeItem<DocumentType>> documentList = createDocumentList("document.txt");
        caseData.setTseAdminResponseRequiredYesDoc(documentList);
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify(CLAIMANT_ONLY);

        tseAdminService.saveTseAdminDataFromCaseData(caseData);

        GenericTseApplicationType app = caseData.getGenericTseApplicationCollection().get(0).getValue();
        assertThat(app.getApplicationState())
            .isEqualTo("notViewedYet");

        TseAdminRecordDecisionType actual = app.getAdminDecision().get(0).getValue();

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
            .isEqualTo(documentList);
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
        List<GenericTypeItem<DocumentType>> documentList = createDocumentList("document.txt");
        caseData.setTseAdminResponseRequiredNoDoc(documentList);
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify(RESPONDENT_ONLY);

        tseAdminService.saveTseAdminDataFromCaseData(caseData);

        GenericTseApplicationType app = caseData.getGenericTseApplicationCollection().get(0).getValue();
        assertThat(app.getApplicationState())
            .isEqualTo("notViewedYet");

        TseAdminRecordDecisionType actual = app.getAdminDecision().get(0).getValue();

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
            .isEqualTo(documentList);
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
        personalisation.put("name", expectedName);
        personalisation.put("linkToExUI", XUI_URL + CASE_ID);
        personalisation.put("linkToCitizenHub", CITIZEN_URL + CASE_ID);
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
        caseData.setTseAdminResponseRequiredYesDoc(createDocumentList("document.txt"));
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

}
