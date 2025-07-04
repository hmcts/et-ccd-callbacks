package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseAdminHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEITHER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REQUEST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CLAIMANT_NOT_COMPLIED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class TseAdmReplyServiceTest {
    private TseAdmReplyService tseAdmReplyService;
    private EmailService emailService;

    @MockitoBean
    public TseAdminHelper tseAdminHelper;
    @MockitoBean
    private DocumentManagementService documentManagementService;
    @MockitoBean
    private TornadoService tornadoService;
    @MockitoBean
    private TseService tseService;
    @MockitoBean
    private FeatureToggleService featureToggleService;

    private CaseData caseData;

    private static final String FILE_NAME = "document.txt";
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ERROR_MSG_ADD_DOC_MISSING = "Select or fill the required Add document field";

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String CASE_NUMBER = "Some Case Number";
    private static final String CASE_ID = "someCaseId";

    private static final String CLAIMANT_EMAIL = "Claimant@mail.com";
    private static final String RESPONDENT_EMAIL_1 = "Respondent@mail.com";
    private static final String RESPONDENT_EMAIL_2 = "Respondent2@mail.com";
    private static final String RESPONSE_REQUIRED =
        "The tribunal requires some information from you about an application.";
    private static final String RESPONSE_NOT_REQUIRED =
        "You have a new message from HMCTS about a claim made to an employment tribunal.";
    private static final String RESPONDENT_1 = "Respondent 1";
    private static final String RESPONDENT_2 = "Respondent 2";
    private static final String REP_EMAIL = "rep@test.com";

    @BeforeEach
    void setUp() {
        emailService = spy(new EmailUtils());
        tseAdmReplyService = new TseAdmReplyService(documentManagementService, emailService,
                tornadoService, tseService, featureToggleService);
        ReflectionTestUtils.setField(tseAdmReplyService, "tseAdminReplyClaimantTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(tseAdmReplyService, "tseAdminReplyRespondentTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(tseAdmReplyService, "tseAdminReplyRespondentTemplateId", TEMPLATE_ID);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(tseService.formatViewApplication(any(), any(), eq(false))).thenReturn("Application Details\r\n");

        Resource resource = new ByteArrayResource(new byte[] { 10, 20, 15});
        UploadedDocument uploadedDocument = UploadedDocument.builder().content(resource)
                .contentType("application/pdf").name("test uploaded doc").build();
        when(documentManagementService.downloadFile(any(), any())).thenReturn(uploadedDocument);
        caseData = CaseDataBuilder.builder().build();
    }

    private List<GenericTypeItem<DocumentType>> createDocumentList() {
        return List.of(GenericTypeItem.from(DocumentType.from(createUploadedDocumentType())));
    }

    private UploadedDocumentType createUploadedDocumentType() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("http://dm-store:8080/documents/1234/binary");
        uploadedDocumentType.setDocumentFilename(FILE_NAME);
        uploadedDocumentType.setDocumentUrl("http://dm-store:8080/documents/1234");
        return uploadedDocumentType;
    }

    @Test
    void validateInput_Yes_NoDoc_ReturnErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoIsResponseRequired(YES);
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(ERROR_MSG_ADD_DOC_MISSING);
    }

    @Test
    void validateInput_CmoYes_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoIsResponseRequired(YES);
        caseData.setTseAdmReplyAddDocument(createDocumentList());
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_RequestYes_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(REQUEST);
        caseData.setTseAdmReplyRequestIsResponseRequired(YES);
        caseData.setTseAdmReplyAddDocument(createDocumentList());
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_No_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoIsResponseRequired(NO);
        caseData.setTseAdmReplyAddDocument(createDocumentList());
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_No_NoDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoIsResponseRequired(NO);
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_Neither_HaveDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(NEITHER);
        caseData.setTseAdmReplyAddDocument(createDocumentList());
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_Neither_NoDoc_NoErrorMsg() {
        caseData.setTseAdmReplyIsCmoOrRequest(NEITHER);
        List<String> errors = tseAdmReplyService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Nested
    class UpdateApplicationStatusRequest {
        @BeforeEach
        void setUp() {
            caseData.setGenericTseApplicationCollection(
                    List.of(GenericTseApplicationTypeItem.builder()
                            .id(UUID.randomUUID().toString())
                            .value(TseApplicationBuilder.builder().withNumber("1").build())
                            .build()
                    )
            );
            caseData.setTseAdminSelectApplication(DynamicFixedListType.of(DynamicValueType.create("1", "")));
            caseData.setTseAdmReplyIsCmoOrRequest("Request");
            caseData.setTseAdmReplyRequestIsResponseRequired("Yes");
        }

        @ParameterizedTest
        @MethodSource("partyAndStatusArguments")
        void requestInformationFromParty(String party, String expectedState) {
            caseData.setTseAdmReplyRequestSelectPartyRespond(party);

            tseAdmReplyService.updateApplicationState(caseData);

            GenericTseApplicationType actual = caseData.getGenericTseApplicationCollection().get(0).getValue();
            assertThat(actual.getApplicationState()).isEqualTo(expectedState);
        }

        static Stream<Arguments> partyAndStatusArguments() {
            return Stream.of(
                    Arguments.of(CLAIMANT_TITLE, NOT_STARTED_YET),
                    Arguments.of(RESPONDENT_TITLE, UPDATED),
                    Arguments.of("Both parties", NOT_STARTED_YET)
            );
        }

        @Nested
        class UpdateApplicationStatusCMO {
            @BeforeEach
            void setUp() {
                caseData.setGenericTseApplicationCollection(
                        List.of(GenericTseApplicationTypeItem.builder()
                                .id(UUID.randomUUID().toString())
                                .value(TseApplicationBuilder.builder().withNumber("1").build())
                                .build()
                        )
                );
                caseData.setTseAdminSelectApplication(DynamicFixedListType.of(DynamicValueType.create("1", "")));
                caseData.setTseAdmReplyIsCmoOrRequest("Case management order");
                caseData.setTseAdmReplyCmoIsResponseRequired("Yes");
            }

            @ParameterizedTest
            @MethodSource("partyAndStatusArguments")
            void requestInformationFromParty(String party, String expectedState) {
                caseData.setTseAdmReplyCmoSelectPartyRespond(party);

                tseAdmReplyService.updateApplicationState(caseData);

                GenericTseApplicationType actual = caseData.getGenericTseApplicationCollection().get(0).getValue();
                assertThat(actual.getApplicationState()).isEqualTo(expectedState);
            }

            static Stream<Arguments> partyAndStatusArguments() {
                return Stream.of(
                        Arguments.of("Claimant", "notStartedYet"),
                        Arguments.of("Respondent", "updated"),
                        Arguments.of("Both parties", "notStartedYet")
                );
            }
        }
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
        List<GenericTypeItem<DocumentType>> documentList = createDocumentList();
        caseData.setTseAdmReplyAddDocument(documentList);
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyCmoEnterFullName("Full Name");
        caseData.setTseAdmReplyCmoIsResponseRequired(YES);
        caseData.setTseAdmReplyCmoSelectPartyRespond(BOTH_PARTIES);
        caseData.setTseAdmReplySelectPartyNotify(CLAIMANT_ONLY);

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseRespondType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getRespondCollection().get(0).getValue();

        String dateNow = UtilHelper.formatCurrentDate(LocalDate.now());

        String actualDateTimeParsedForTesting = UtilHelper.formatCurrentDate(
            LocalDateTime.parse(actual.getDateTime()).toLocalDate()
        );

        assertThat(actual.getDate()).isEqualTo(dateNow);
        assertThat(actualDateTimeParsedForTesting).isEqualTo(dateNow);

        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
            .isEqualTo("Submit hearing agenda");
        assertThat(actual.getAdditionalInformation())
            .isEqualTo("Additional Information Details");
        assertThat(actual.getAddDocument())
            .isEqualTo(documentList);
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

    @ParameterizedTest
    @MethodSource("saveTseAdmReplyDataFromCaseData")
    void saveTseAdmReplyDataFromCaseData(String requestSelectPartyRespond,
                                                                  String cmoSelectPartyRespond,
                                                                  String respondentResponseRequired,
                                                                  String claimantResponseRequired) {
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

        caseData.setTseAdmReplyRequestSelectPartyRespond(requestSelectPartyRespond);
        caseData.setTseAdmReplyCmoSelectPartyRespond(cmoSelectPartyRespond);

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        GenericTseApplicationType application = caseData.getGenericTseApplicationCollection().get(0).getValue();
        assertThat(application.getRespondentResponseRequired()).isEqualTo(respondentResponseRequired);
        assertThat(application.getClaimantResponseRequired()).isEqualTo(claimantResponseRequired);

    }

    private static Stream<Arguments> saveTseAdmReplyDataFromCaseData() {
        return Stream.of(
                Arguments.of(RESPONDENT_TITLE, null, YES, null),
                Arguments.of(CLAIMANT_TITLE, null, null, YES),
                Arguments.of(BOTH_PARTIES, null, YES, YES),
                Arguments.of(null, RESPONDENT_TITLE, YES, null),
                Arguments.of(null, CLAIMANT_TITLE, null, YES),
                Arguments.of(null, BOTH_PARTIES, YES, YES)
        );
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
        List<GenericTypeItem<DocumentType>> admReplyDocument = createDocumentList();
        caseData.setTseAdmReplyAddDocument(admReplyDocument);
        caseData.setTseAdmReplyIsCmoOrRequest(REQUEST);
        caseData.setTseAdmReplyRequestMadeBy("Judge");
        caseData.setTseAdmReplyRequestEnterFullName("Full Name");
        caseData.setTseAdmReplyRequestIsResponseRequired(NO);
        caseData.setTseAdmReplySelectPartyNotify(RESPONDENT_ONLY);

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseRespondType actual =
            caseData.getGenericTseApplicationCollection().get(0).getValue()
                .getRespondCollection().get(0).getValue();

        String dateNow = UtilHelper.formatCurrentDate(LocalDate.now());

        assertThat(actual.getDate()).isEqualTo(dateNow);
        assertThat(actual.getEnterResponseTitle())
            .isNull();
        assertThat(actual.getAdditionalInformation())
            .isNull();
        assertThat(actual.getAddDocument())
            .isEqualTo(admReplyDocument);
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

        // WA properties
        String actualDateTimeParsedForTesting = UtilHelper.formatCurrentDate(
            LocalDateTime.parse(actual.getDateTime()).toLocalDate()
        );
        assertThat(actualDateTimeParsedForTesting).isEqualTo(dateNow);
        assertThat(actual.getApplicationType()).isEqualTo("Claimant not complied");
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
                                         String expectedClaimantCustomText) {
        caseData.setEthosCaseReference(CASE_NUMBER);
        createClaimant(caseData);
        caseData.setTseAdmReplySelectPartyNotify(admReplySelectPartyNotify);
        caseData.setTseAdmReplyRequestIsResponseRequired(admReplyIsResponseRequired);
        caseData.setTseAdmReplyRequestSelectPartyRespond(admReplySelectPartyRespond);
        setDocCollection(caseData);
        Map<String, Object> resultMap = tseAdmReplyService.sendNotifyEmailsToClaimant(CASE_ID, caseData, AUTH_TOKEN);

        if (emailSentToClaimant) {
            verify(emailService, times(1)).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, resultMap);
        } else {
            verify(emailService, never()).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL,
                    resultMap);
        }
    }

    private void setDocCollection(CaseData caseData) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentUrl("test url");
        uploadedDocumentType.setDocumentFilename("test file name");
        uploadedDocumentType.setDocumentBinaryUrl("test binary url");
        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        caseData.setDocumentCollection(List.of(documentTypeItem));
    }

    private static Stream<Arguments> sendEmails() {
        return Stream.of(
            Arguments.of(BOTH_PARTIES, "Yes", BOTH_PARTIES,
                true, RESPONSE_REQUIRED),
            Arguments.of(BOTH_PARTIES, "Yes", CLAIMANT_TITLE,
                true, RESPONSE_REQUIRED),
            Arguments.of(BOTH_PARTIES, "Yes", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED),

            Arguments.of(BOTH_PARTIES, "No", BOTH_PARTIES,
                true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_PARTIES, "No", CLAIMANT_TITLE,
                true, RESPONSE_NOT_REQUIRED),
            Arguments.of(BOTH_PARTIES, "No", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED),

            Arguments.of(CLAIMANT_ONLY, "Yes", BOTH_PARTIES,
                true, RESPONSE_REQUIRED),
            Arguments.of(CLAIMANT_ONLY, "Yes", CLAIMANT_TITLE,
                true, RESPONSE_REQUIRED),
            Arguments.of(CLAIMANT_ONLY, "Yes", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED),

            Arguments.of(CLAIMANT_ONLY, "No", BOTH_PARTIES,
                true, RESPONSE_NOT_REQUIRED),
            Arguments.of(CLAIMANT_ONLY, "No", CLAIMANT_TITLE,
                true, RESPONSE_NOT_REQUIRED),
            Arguments.of(CLAIMANT_ONLY, "No", RESPONDENT_TITLE,
                true, RESPONSE_NOT_REQUIRED),

            Arguments.of(RESPONDENT_ONLY, "Yes", BOTH_PARTIES,
                false, "never sent"),
            Arguments.of(RESPONDENT_ONLY, "Yes", CLAIMANT_TITLE,
                false, "never sent"),
            Arguments.of(RESPONDENT_ONLY, "Yes", RESPONDENT_TITLE,
                false, "never sent"),

            Arguments.of(RESPONDENT_ONLY, "No", BOTH_PARTIES,
                false, "never sent"),
            Arguments.of(RESPONDENT_ONLY, "No", CLAIMANT_TITLE,
                false, "never sent"),
            Arguments.of(RESPONDENT_ONLY, "No", RESPONDENT_TITLE,
                false, "never sent")
        );
    }

    @Test
    void clearTseAdminDataFromCaseData() {
        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));
        caseData.setTseAdmReplyTableMarkUp("| | |\r\n|--|--|\r\n|%s application | %s|\r\n\r\n");
        caseData.setTseAdmReplyEnterResponseTitle("View notice of hearing");
        caseData.setTseAdmReplyAdditionalInformation("Additional information text");
        caseData.setTseAdmReplyAddDocument(createDocumentList());
        caseData.setTseAdmReplyIsCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyRequestMadeBy("Legal Officer");
        caseData.setTseAdmReplyCmoEnterFullName("Enter Full Name");
        caseData.setTseAdmReplyCmoIsResponseRequired(YES);
        caseData.setTseAdmReplyRequestEnterFullName("Enter Full Name");
        caseData.setTseAdmReplyRequestIsResponseRequired(YES);
        caseData.setTseAdmReplyRequestSelectPartyRespond(BOTH_PARTIES);
        caseData.setTseAdmReplyCmoSelectPartyRespond(BOTH_PARTIES);
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
        assertThat(caseData.getTseAdmReplyCmoEnterFullName()).isNull();
        assertThat(caseData.getTseAdmReplyCmoIsResponseRequired()).isNull();
        assertThat(caseData.getTseAdmReplyRequestEnterFullName()).isNull();
        assertThat(caseData.getTseAdmReplyRequestIsResponseRequired()).isNull();
        assertThat(caseData.getTseAdmReplyRequestSelectPartyRespond()).isNull();
        assertThat(caseData.getTseAdmReplyCmoSelectPartyRespond()).isNull();
        assertThat(caseData.getTseAdmReplySelectPartyNotify()).isNull();
    }

    private void createClaimant(CaseData caseData) {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(CLAIMANT_EMAIL);
        caseData.setClaimantType(claimantType);
    }

    @Test
    void sendNotifyEmailsToRespondents_selectPartyNotifyClaimantOnly_noEmailSent() {
        caseData.setTseAdmReplySelectPartyNotify(CLAIMANT_ONLY);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        tseAdmReplyService.sendNotifyEmailsToRespondents(caseDetails, "testToken");

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("sendEmailsToRespondents")
    void sendNotifyEmailsToRespondents_sendEmailToRespondents(String admReplyIsCmoOrRequest,
                                                              String admReplyCmoIsResponseRequired,
                                                              String admReplyCmoSelectPartyRespond,
                                                              String expectedCustomText) {
        // Given that there are two respondents in the case, one has a Representative assigned and one does not.
        setRespondents();
        setRepresentative();

        caseData.setEthosCaseReference(CASE_NUMBER);
        caseData.setTseAdmReplyIsCmoOrRequest(admReplyIsCmoOrRequest);
        caseData.setTseAdmReplyCmoIsResponseRequired(admReplyCmoIsResponseRequired);
        caseData.setTseAdmReplyCmoSelectPartyRespond(admReplyCmoSelectPartyRespond);
        setDocCollection(caseData);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        Map<String, Object> resultMap = tseAdmReplyService.sendNotifyEmailsToRespondents(caseDetails, AUTH_TOKEN);

        // Email is sent to respondent. if representative is present, email is sent to both
        // representative and respondent.
        verify(emailService).sendEmail(TEMPLATE_ID, "rep@test.com", resultMap);
        verify(emailService, times(1)).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL_1, resultMap);
    }

    @Test
    void addTseAdmReplyPdfToDocCollection_addsPdfFile() {
        caseData.setEthosCaseReference(CASE_NUMBER);
        caseData.setTseAdmReplyIsCmoOrRequest("Case management order");
        caseData.setTseAdmReplyCmoIsResponseRequired("Yes");
        caseData.setTseAdmReplyCmoSelectPartyRespond("Both");
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        tseAdmReplyService.addTseAdmReplyPdfToDocCollection(caseDetails, "test token");

        assertThat(caseData.getDocumentCollection()).isNotNull();
    }

    private static Stream<Arguments> sendEmailsToRespondents() {
        return Stream.of(
                Arguments.of(CASE_MANAGEMENT_ORDER, YES, RESPONDENT_TITLE, RESPONSE_REQUIRED),
                Arguments.of(CASE_MANAGEMENT_ORDER, YES, BOTH_PARTIES, RESPONSE_REQUIRED),
                Arguments.of(CASE_MANAGEMENT_ORDER, YES, CLAIMANT_TITLE, RESPONSE_NOT_REQUIRED),
                Arguments.of(CASE_MANAGEMENT_ORDER, NO, RESPONDENT_TITLE, RESPONSE_NOT_REQUIRED)

        );
    }

    private void setRepresentative() {
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setValue(RepresentedTypeR.builder()
                .respRepName(RESPONDENT_1)
                .representativeEmailAddress(REP_EMAIL)
                .myHmctsYesNo(YES)
                .respondentOrganisation(Organisation.builder()
                        .organisationName("MyHMCTS")
                        .organisationID("12345")
                        .build())
                .build());

        caseData.setRepCollection(List.of(representedTypeRItem));
    }

    private void setRespondents() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL_1);
        respondentSumType.setRespondentName(RESPONDENT_1);

        RespondentSumType respondentSumType2 = new RespondentSumType();
        respondentSumType2.setRespondentEmail(RESPONDENT_EMAIL_2);
        respondentSumType2.setRespondentName(RESPONDENT_2);

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId("1");
        respondentSumTypeItem.setValue(respondentSumType);

        RespondentSumTypeItem respondentSumTypeItem2 = new RespondentSumTypeItem();
        respondentSumTypeItem2.setId("2");
        respondentSumTypeItem2.setValue(respondentSumType2);

        caseData.setRespondentCollection(List.of(respondentSumTypeItem, respondentSumTypeItem2));
    }
}
