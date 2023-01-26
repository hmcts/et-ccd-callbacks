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
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
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

    private static final String BOTH = "Both parties";
    private static final String CLAIMANT_ONLY = "Claimant only";
    private static final String RESPONDENT_ONLY = "Respondent only";
    private static final String CLAIMANT_EMAIL = "Claimant@mail.com";
    private static final String CLAIMANT_FIRSTNAME = "Claim";
    private static final String CLAIMANT_LASTNAME = "Ant";

    private static final String RESPONDENT_EMAIL = "Respondent@mail.com";
    private static final String RESPONDENT_NAME = "Respondent";

    private static final String AUTH_TOKEN = "Bearer authToken";

    @BeforeEach
    void setUp() {
        tseAdminService = new TseAdminService(emailService, documentManagementService);
        ReflectionTestUtils.setField(tseAdminService, "emailToClaimantTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(tseAdminService, "emailToRespondentTemplateId", TEMPLATE_ID);
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void initialTseAdminTableMarkUp_ReturnString() {
        TseRespondTypeItem tseRespondTypeItem = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .from("Claimant")
                    .date("23 December 2022")
                    .response("Response Details")
                    .hasSupportingMaterial(YES)
                    .supportingMaterial(List.of(createDocumentTypeItem("image.png"),
                        createDocumentTypeItem("Form.pdf")))
                    .copyToOtherParty(YES)
                    .build()
            ).build();

        GenericTseApplicationType genericTseApplicationType = TseApplicationBuilder.builder()
            .withNumber("1")
            .withType("Amend response")
            .withApplicant("Respondent")
            .withDate("13 December 2022")
            .withDocumentUpload(createUploadedDocumentType("document.txt"))
            .withDetails("Details Text")
            .withStatus("Open")
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
            + "|Respondent application | Amend response|\r\n"
            + "|Application date | 13 December 2022|\r\n"
            + "|Give details | Details Text|\r\n"
            + "|Supporting material | " + fileDisplay1 + "|\r\n"
            + "\r\n"
            + "|Response 1 | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | Claimant|\r\n"
            + "|Response date | 23 December 2022|\r\n"
            + "|Details | Response Details|\r\n"
            + "|Supporting material | " + fileDisplay2 + "<br>" + fileDisplay3 + "<br>" + "|\r\n"
            + "\r\n";

        tseAdminService.initialTseAdminTableMarkUp(caseData, AUTH_TOKEN);
        assertThat(caseData.getTseAdminTableMarkUp())
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
    void saveTseAdminDataFromCaseData_Judgment_SaveString() {
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

        caseData.setTseAdminEnterNotificationTitle("Submit hearing agenda");
        caseData.setTseAdminDecision("Granted");
        caseData.setTseAdminTypeOfDecision("Judgment");
        caseData.setTseAdminAdditionalInformation("Additional information");
        caseData.setTseAdminResponseRequiredNoDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminDecisionMadeBy("Legal officer");
        caseData.setTseAdminDecisionMadeByFullName("Legal Officer Full Name");
        caseData.setTseAdminSelectPartyNotify("Both parties");

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
            .isEqualTo("Both parties");
    }

    @Test
    void saveTseAdminDataFromCaseData_CmoYes_SaveString() {
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

        caseData.setTseAdminEnterNotificationTitle("View notice of hearing");
        caseData.setTseAdminDecision("Other");
        caseData.setTseAdminDecisionDetails("Decision details text");
        caseData.setTseAdminTypeOfDecision("Case management order");
        caseData.setTseAdminIsResponseRequired(YES);
        caseData.setTseAdminSelectPartyRespond("Claimant");
        caseData.setTseAdminAdditionalInformation("Additional information text");
        caseData.setTseAdminResponseRequiredYesDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify("Claimant only");

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
            .isEqualTo("Case management order");
        assertThat(actual.getIsResponseRequired())
            .isEqualTo(YES);
        assertThat(actual.getSelectPartyRespond())
            .isEqualTo("Claimant");
        assertThat(actual.getAdditionalInformation())
            .isEqualTo("Additional information text");
        assertThat(actual.getResponseRequiredDoc())
            .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getDecisionMadeBy())
            .isEqualTo("Judge");
        assertThat(actual.getDecisionMadeByFullName())
            .isEqualTo("Judge Full Name");
        assertThat(actual.getSelectPartyNotify())
            .isEqualTo("Claimant only");
    }

    @Test
    void saveTseAdminDataFromCaseData_CmoNo_SaveString() {
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

        caseData.setTseAdminDecision("Refused");
        caseData.setTseAdminTypeOfDecision("Case management order");
        caseData.setTseAdminIsResponseRequired(NO);
        caseData.setTseAdminResponseRequiredNoDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify("Respondent only");

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
            .isEqualTo("Case management order");
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
            .isEqualTo("Respondent only");
    }

    @ParameterizedTest
    @CsvSource({BOTH, CLAIMANT_ONLY, RESPONDENT_ONLY})
    void sendRecordADecisionEmails(String partyNotified) {
        caseData.setEthosCaseReference(CASE_NUMBER);
        createClaimant(caseData);
        createRespondent(caseData);
        caseData.setTseAdminSelectPartyNotify(partyNotified);

        Map<String, String> expectedPersonalisationClaimant =
            createPersonalisation(caseData, CLAIMANT_FIRSTNAME + " " + CLAIMANT_LASTNAME);
        Map<String, String> expectedPersonalisationRespondent =
            createPersonalisation(caseData, RESPONDENT_NAME);

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
        respondentSumType.setRespondentName(RESPONDENT_NAME);
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
        caseData.setTseAdminTypeOfDecision("Case management order");
        caseData.setTseAdminIsResponseRequired(YES);
        caseData.setTseAdminSelectPartyRespond("Claimant");
        caseData.setTseAdminAdditionalInformation("Additional information text");
        caseData.setTseAdminResponseRequiredYesDoc(createUploadedDocumentType("document.txt"));
        caseData.setTseAdminResponseRequiredNoDoc(null);
        caseData.setTseAdminDecisionMadeBy("Judge");
        caseData.setTseAdminDecisionMadeByFullName("Judge Full Name");
        caseData.setTseAdminSelectPartyNotify("Claimant only");

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
