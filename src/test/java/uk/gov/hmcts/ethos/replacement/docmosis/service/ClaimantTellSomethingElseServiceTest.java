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
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.WITHDRAWAL_OF_ALL_OR_PART_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.WITHDRAWAL_SETTLED;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_APP_TYPE_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_MONTHS_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_TYPE_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_AMEND_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CONSIDER_DECISION_AFRESH;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CONTACT_THE_TRIBUNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_ORDER_OTHER_PARTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_RECONSIDER_JUDGMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.GIVE_DETAIL_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class ClaimantTellSomethingElseServiceTest {
    private ClaimantTellSomethingElseService claimantTellSomethingElseService;
    private EmailService emailService;

    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private TornadoService tornadoService;
    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private TribunalOfficesService tribunalOfficesService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Captor
    ArgumentCaptor<Map<String, Object>> personalisationCaptor;
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String I_DO_WANT_TO_COPY = "I do want to copy";
    private static final String TEMPLATE_ID_NO = "NoTemplateId";
    private static final String TEMPLATE_ID_A = "TypeATemplateId";
    private static final String TEMPLATE_ID_A_CY = "TypeACYTemplateId";
    private static final String TEMPLATE_ID_B = "TypeBTemplateId";
    private static final String TEMPLATE_ID_B_CY = "TypeBCYTemplateId";
    private static final String TEMPLATE_ID_C = "TypeCTemplateId";
    private static final String LEGAL_REP_EMAIL = "mail@mail.com";
    private static final String CASE_ID = "669718251103419";

    private static final Map<String, BiConsumer<CaseData, String>> APPLICATION_SETTER_MAP = new ConcurrentHashMap<>();
    private static final Map<String, BiConsumer<CaseData, UploadedDocumentType>>
            DOCUMENT_SETTER_MAP = new ConcurrentHashMap<>();

    static {
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_AMEND_CLAIM, CaseData::setClaimantTseTextBox1);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, CaseData::setClaimantTseTextBox2);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, CaseData::setClaimantTseTextBox3);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, CaseData::setClaimantTseTextBox4);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND, CaseData::setClaimantTseTextBox5);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_ORDER_OTHER_PARTY, CaseData::setClaimantTseTextBox6);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_POSTPONE_A_HEARING, CaseData::setClaimantTseTextBox7);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_RECONSIDER_JUDGMENT, CaseData::setClaimantTseTextBox8);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, CaseData::setClaimantTseTextBox9);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_RESTRICT_PUBLICITY, CaseData::setClaimantTseTextBox10);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, CaseData::setClaimantTseTextBox11);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, CaseData::setClaimantTseTextBox12);
        APPLICATION_SETTER_MAP.put(CLAIMANT_TSE_WITHDRAW_CLAIM, CaseData::setClaimantTseTextBox13);
    }

    static {
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_AMEND_CLAIM, CaseData::setClaimantTseDocument1);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, CaseData::setClaimantTseDocument2);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, CaseData::setClaimantTseDocument3);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, CaseData::setClaimantTseDocument4);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND, CaseData::setClaimantTseDocument5);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_ORDER_OTHER_PARTY, CaseData::setClaimantTseDocument6);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_POSTPONE_A_HEARING, CaseData::setClaimantTseDocument7);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_RECONSIDER_JUDGMENT, CaseData::setClaimantTseDocument8);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, CaseData::setClaimantTseDocument9);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_RESTRICT_PUBLICITY, CaseData::setClaimantTseDocument10);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, CaseData::setClaimantTseDocument11);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, CaseData::setClaimantTseDocument12);
        DOCUMENT_SETTER_MAP.put(CLAIMANT_TSE_WITHDRAW_CLAIM, CaseData::setClaimantTseDocument13);
    }

    private static final String EXPECTED_TABLE_MARKDOWN = "| No | Application type | Applicant | Application date | "
            + "Response due | Number of responses | Status "
            + "|\r\n|:---------|:---------|:---------|:---------|:---------|:---------|:---------|\r\n|1|testType"
            + "|Claimant Representative|testDate|testDueDate|0|Open|\r\n\r\n";

    private static final String EXPECTED_EMPTY_TABLE_MESSAGE = "There are no applications to view";

    @BeforeEach
    void setUp() {
        emailService = spy(new EmailUtils());
        claimantTellSomethingElseService =
                new ClaimantTellSomethingElseService(documentManagementService, tornadoService,
                        userIdamService, emailService, featureToggleService, tribunalOfficesService);

        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "tseClaimantRepAcknowledgeNoTemplateId", TEMPLATE_ID_NO);
        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "tseClaimantRepAcknowledgeTypeATemplateId", TEMPLATE_ID_A);
        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "tseClaimantRepAcknowledgeTypeBTemplateId", TEMPLATE_ID_B);
        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "tseClaimantRepAcknowledgeTypeCTemplateId", TEMPLATE_ID_C);

        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "tseClaimantRepToRespAcknowledgeTypeATemplateId", TEMPLATE_ID_A);
        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "tseClaimantRepToRespAcknowledgeTypeBTemplateId", TEMPLATE_ID_B);
        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "cyTseClaimantToRespondentTypeATemplateId", TEMPLATE_ID_A_CY);
        ReflectionTestUtils.setField(claimantTellSomethingElseService,
                "cyTseClaimantToRespondentTypeBTemplateId", TEMPLATE_ID_B_CY);

        UserDetails userDetails = HelperTest.getUserDetails();
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_Blank_ReturnErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(selectedApplication);
        List<String> errors = claimantTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(GIVE_DETAIL_MISSING));
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_HasDoc_NoErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(selectedApplication);
        setDocForSelectedApplication(caseData);
        List<String> errors = claimantTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(0));
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_HasTextBox_NoErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(selectedApplication);
        setTextBoxForSelectedApplication(caseData);
        List<String> errors = claimantTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(0));
    }

    @Test
    void generateAndAddApplicationPdf() throws IOException {
        CaseData caseData = new CaseData();
        caseData.setClaimantTseSelectApplication("Withdraw all or part of claim");
        DocumentInfo documentInfo = new DocumentInfo("document.pdf", "Withdraw Claim",
                "binaryUrl/documents/", "<>Some doc</>");
        when(tornadoService.generateEventDocument(any(), any(), any(), any())).thenReturn(documentInfo);

        claimantTellSomethingElseService.generateAndAddApplicationPdf(caseData, "token", "typeId");

        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        DocumentType actual = documentCollection.get(0).getValue();

        DocumentType expected = DocumentType.builder()
                .shortDescription("Withdraw all or part of claim")
                .dateOfCorrespondence(LocalDate.now().toString())
                .topLevelDocuments(WITHDRAWAL_SETTLED)
                .documentType(WITHDRAWAL_OF_ALL_OR_PART_CLAIM)
                .withdrawalSettledDocuments(WITHDRAWAL_OF_ALL_OR_PART_CLAIM)
                .build();

        Assertions.assertThat(documentCollection).hasSize(1);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateAndAddApplicationPdf_CollectionNotEmpty() throws IOException {
        CaseData caseData = new CaseData();
        caseData.setClaimantTseSelectApplication("Withdraw all or part of claim");
        DocumentInfo documentInfo = new DocumentInfo("document.pdf", "Withdraw Claim",
                "binaryUrl/documents/", "<>Some doc</>");
        when(tornadoService.generateEventDocument(any(), any(), any(), any())).thenReturn(documentInfo);

        claimantTellSomethingElseService.generateAndAddApplicationPdf(caseData, "token", "typeId");
        claimantTellSomethingElseService.generateAndAddApplicationPdf(caseData, "token", "typeId");

        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        DocumentType actual = documentCollection.get(1).getValue();

        DocumentType expected = DocumentType.builder()
                .shortDescription("Withdraw all or part of claim")
                .dateOfCorrespondence(LocalDate.now().toString())
                .topLevelDocuments(WITHDRAWAL_SETTLED)
                .documentType(WITHDRAWAL_OF_ALL_OR_PART_CLAIM)
                .withdrawalSettledDocuments(WITHDRAWAL_OF_ALL_OR_PART_CLAIM)
                .build();

        Assertions.assertThat(documentCollection).hasSize(2);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateAndAddApplicationPdf_Error() {
        CaseData caseData = new CaseData();
        caseData.setClaimantTseSelectApplication("Withdraw all or part of claim");
        try {
            claimantTellSomethingElseService.generateAndAddApplicationPdf(caseData, "token", "typeId");
        } catch (Exception e) {
            assertThat(e.getMessage(), is(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference())));
        }
    }

    @Test
    void populateClaimantTse_Success() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setClaimantTseRule92("Yes");
        caseData.setClaimantTseRule92AnsNoGiveDetails("Some text");
        setTextBoxForSelectedApplication(caseData);
        setDocForSelectedApplication(caseData);
        claimantTellSomethingElseService.populateClaimantTse(caseData);

        ClaimantTse claimantTse = caseData.getClaimantTse();
        assertThat(claimantTse.getContactApplicationType(), is(caseData.getClaimantTseSelectApplication()));
        assertThat(claimantTse.getCopyToOtherPartyYesOrNo(), is(caseData.getClaimantTseRule92()));
        assertThat(claimantTse.getCopyToOtherPartyText(), is(caseData.getClaimantTseRule92AnsNoGiveDetails()));

        TSEApplicationTypeData selectedAppData =
                ClaimantTellSomethingElseHelper.getSelectedApplicationType(caseData);
        assertThat(claimantTse.getContactApplicationText(), is(selectedAppData.getSelectedTextBox()));
        assertThat(claimantTse.getContactApplicationFile(), is(selectedAppData.getUploadedTseDocument()));
    }

    @Test
    void buildApplicationCompleteResponse_Rule92Yes_RespOffline() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseRespNotAvailable("Yes");
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setDocMarkUp("Document");

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_REP_TITLE)
                .withCopyToOtherPartyYesOrNo(YES)
                .withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem latestTSEApplication = new GenericTseApplicationTypeItem();
        latestTSEApplication.setId(UUID.randomUUID().toString());
        latestTSEApplication.setValue(build);

        caseData.setGenericTseApplicationCollection(List.of(latestTSEApplication));

        String response = claimantTellSomethingElseService.buildApplicationCompleteResponse(caseData);
        assertThat(response, is(String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE,
                caseData.getDocMarkUp())));
    }

    @Test
    void buildApplicationCompleteResponse_Rule92Yes_RespOnline() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseRespNotAvailable(NO);
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setDocMarkUp("Document");

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_REP_TITLE)
                .withCopyToOtherPartyYesOrNo(YES)
                .withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem latestTSEApplication = new GenericTseApplicationTypeItem();
        latestTSEApplication.setId(UUID.randomUUID().toString());
        latestTSEApplication.setValue(build);

        caseData.setGenericTseApplicationCollection(List.of(latestTSEApplication));

        String response = claimantTellSomethingElseService.buildApplicationCompleteResponse(caseData);
        assertThat(response, is(String.format(TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE,
                UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7), caseData.getDocMarkUp())));
    }

    @Test
    void buildApplicationCompleteResponse_Rule92No() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseRespNotAvailable("anything");
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setDocMarkUp("Document");

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_REP_TITLE)
                .withCopyToOtherPartyYesOrNo(NO)
                .withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem latestTSEApplication = new GenericTseApplicationTypeItem();
        latestTSEApplication.setId(UUID.randomUUID().toString());
        latestTSEApplication.setValue(build);

        caseData.setGenericTseApplicationCollection(List.of(latestTSEApplication));

        String response = claimantTellSomethingElseService.buildApplicationCompleteResponse(caseData);
        assertThat(response, is(String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_NO, caseData.getDocMarkUp())));
    }

    private static Stream<Arguments> selectedApplicationList() {
        return Stream.of(
        Arguments.of(CLAIMANT_TSE_AMEND_CLAIM),
        Arguments.of(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS),
        Arguments.of(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH),
        Arguments.of(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL),
        Arguments.of(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND),
        Arguments.of(CLAIMANT_TSE_ORDER_OTHER_PARTY),
        Arguments.of(CLAIMANT_TSE_POSTPONE_A_HEARING),
        Arguments.of(CLAIMANT_TSE_RECONSIDER_JUDGMENT),
        Arguments.of(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED),
        Arguments.of(CLAIMANT_TSE_RESTRICT_PUBLICITY),
        Arguments.of(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART),
        Arguments.of(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER),
        Arguments.of(CLAIMANT_TSE_WITHDRAW_CLAIM)
        );
    }

    private void setTextBoxForSelectedApplication(CaseData caseData) {
        String applicationType = caseData.getClaimantTseSelectApplication();
        BiConsumer<CaseData, String> setter = APPLICATION_SETTER_MAP.get(applicationType);
        if (setter != null) {
            setter.accept(caseData, "Some text");
        } else {
            throw new IllegalArgumentException("Unexpected application type");
        }
    }

    private void setDocForSelectedApplication(CaseData caseData) {
        String applicationType = caseData.getClaimantTseSelectApplication();
        BiConsumer<CaseData, UploadedDocumentType> setter = DOCUMENT_SETTER_MAP.get(applicationType);
        if (setter != null) {
            setter.accept(caseData, createDocumentType());
        } else {
            throw new IllegalArgumentException("Unexpected application type");
        }
    }

    private UploadedDocumentType createDocumentType() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("binaryUrl/documents/");
        uploadedDocumentType.setDocumentFilename("testFileName");
        uploadedDocumentType.setDocumentUrl("Some doc");
        return uploadedDocumentType;
    }

    @ParameterizedTest
    @MethodSource("sendAcknowledgeEmailAndGeneratePdf")
    void sendAcknowledgeEmailAndGeneratePdf(String selectedApplication, String rule92Selection,
                                            String expectedTemplateId) {
        CaseData caseData = createCaseData(selectedApplication, rule92Selection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        Map<String, String> expectedPersonalisation = createEmailContent(caseData, selectedApplication);

        claimantTellSomethingElseService.sendAcknowledgementEmail(caseDetails, AUTH_TOKEN);

        verify(emailService).sendEmail(expectedTemplateId, LEGAL_REP_EMAIL, expectedPersonalisation);
    }

    private static Stream<Arguments> sendAcknowledgeEmailAndGeneratePdf() {
        return Stream.of(
            Arguments.of(CLAIMANT_TSE_AMEND_CLAIM, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_POSTPONE_A_HEARING, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_ORDER_OTHER_PARTY, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_RESTRICT_PUBLICITY, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, NO, TEMPLATE_ID_NO),
            Arguments.of(CLAIMANT_TSE_RECONSIDER_JUDGMENT, NO, TEMPLATE_ID_NO),

            Arguments.of(CLAIMANT_TSE_AMEND_CLAIM, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
            Arguments.of(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
            Arguments.of(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
            Arguments.of(CLAIMANT_TSE_POSTPONE_A_HEARING, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
            Arguments.of(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
            Arguments.of(CLAIMANT_TSE_ORDER_OTHER_PARTY, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
            Arguments.of(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
            Arguments.of(CLAIMANT_TSE_RESTRICT_PUBLICITY, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),

            Arguments.of(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, I_DO_WANT_TO_COPY, TEMPLATE_ID_B),
            Arguments.of(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, I_DO_WANT_TO_COPY, TEMPLATE_ID_B),
            Arguments.of(CLAIMANT_TSE_RECONSIDER_JUDGMENT, I_DO_WANT_TO_COPY, TEMPLATE_ID_B)
        );
    }

    @Test
    void sendAcknowledgeEmailAndGeneratePdf_TypeC() {
        CaseData caseData = createCaseData(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND, NO);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        Map<String, String> expectedPersonalisation = createEmailContentTypeC(caseData);

        claimantTellSomethingElseService.sendAcknowledgementEmail(caseDetails, AUTH_TOKEN);

        verify(emailService).sendEmail(TEMPLATE_ID_C, LEGAL_REP_EMAIL, expectedPersonalisation);
    }

    @Test
    void displayRespondentApplicationsTable_hasApplications() {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, NO);
        caseData.setGenericTseApplicationCollection(generateGenericTseApplicationList());

        assertThat(claimantTellSomethingElseService.generateClaimantApplicationTableMarkdown(caseData),
                is(EXPECTED_TABLE_MARKDOWN));
    }

    @Test
    void displayRespondentApplicationsTable_hasNoApplications() {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, NO);

        assertThat(claimantTellSomethingElseService.generateClaimantApplicationTableMarkdown(caseData),
                is(EXPECTED_EMPTY_TABLE_MESSAGE));
    }

    private List<GenericTseApplicationTypeItem> generateGenericTseApplicationList() {
        GenericTseApplicationType tseApplicationType = new GenericTseApplicationType();

        tseApplicationType.setDate("testDate");
        tseApplicationType.setNumber("number");
        tseApplicationType.setApplicant(CLAIMANT_REP_TITLE);
        tseApplicationType.setDetails("testDetails");
        tseApplicationType.setDocumentUpload(createDocumentType());
        tseApplicationType.setType("testType");
        tseApplicationType.setCopyToOtherPartyYesOrNo("yes");
        tseApplicationType.setCopyToOtherPartyText("text");
        tseApplicationType.setDueDate("testDueDate");

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId("id");
        tseApplicationTypeItem.setValue(tseApplicationType);

        List<GenericTseApplicationTypeItem> tseApplicationCollection = new ArrayList<>();
        tseApplicationCollection.add(tseApplicationTypeItem);

        return tseApplicationCollection;
    }

    private CaseData createCaseData(String selectedApplication, String selectedRule92Answer) {
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("test")
                .withClaimant("claimant")
                .withClaimantType("person@email.com")
                .build();
        caseData.setClaimantTseSelectApplication(selectedApplication);
        caseData.setClaimantTseRule92(selectedRule92Answer);
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

    private Map<String, String> createEmailContent(CaseData caseData,
                                                   String selectedApplication) {
        Map<String, String> content = new ConcurrentHashMap<>();
        content.put("caseNumber", caseData.getEthosCaseReference());
        content.put("claimant", caseData.getClaimant());
        content.put("respondentNames", getRespondentNames(caseData));
        content.put("hearingDate", "Not set");
        content.put("shortText", selectedApplication);
        content.put("exuiCaseDetailsLink", "exuiUrl669718251103419");
        return content;
    }

    private Map<String, String> createEmailContentTypeC(CaseData caseData) {
        Map<String, String> content = new ConcurrentHashMap<>();
        content.put("caseNumber", caseData.getEthosCaseReference());
        content.put("claimant", caseData.getClaimant());
        content.put("respondentNames", getRespondentNames(caseData));
        content.put("exuiCaseDetailsLink", "exuiUrl669718251103419");
        return content;
    }

    @ParameterizedTest
    @ValueSource(strings = {
        CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART,
        CLAIMANT_TSE_AMEND_CLAIM,
        CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED,
        CLAIMANT_TSE_POSTPONE_A_HEARING,
        CLAIMANT_TSE_CONTACT_THE_TRIBUNAL,
        CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER,
        CLAIMANT_TSE_ORDER_OTHER_PARTY,
        CLAIMANT_TSE_RESTRICT_PUBLICITY,
    })
    void sendRespondentEmail_groupA_sendsEmail(String applicationType) throws IOException {
        CaseData caseData = createCaseDataWithHearing(applicationType);
        caseData.getRespondentCollection().get(0).setId("123-456-789");
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[] {});
        claimantTellSomethingElseService.sendRespondentsEmail(caseDetails);
        verify(emailService).sendEmail(eq(TEMPLATE_ID_A), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();

        assertThat(personalisation.get("claimant"), is("claimant"));
        assertThat(personalisation.get("respondentNames"), is("Respondent"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("16 May 2069"));
        assertThat(personalisation.get("shortText"), is(applicationType));
        assertThat(personalisation.get("datePlus7"), is(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7)));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null," +
                "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));
    }

    @Test
    void sendRespondentEmail_TypeC() throws IOException {
        CaseData caseData = createCaseDataWithHearing(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[] {});
        claimantTellSomethingElseService.sendRespondentsEmail(caseDetails);
        assertThat(caseData.getClaimantTseSelectApplication(), is(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART,
        CLAIMANT_TSE_AMEND_CLAIM,
        CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED,
        CLAIMANT_TSE_POSTPONE_A_HEARING,
        CLAIMANT_TSE_CONTACT_THE_TRIBUNAL,
        CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER,
        CLAIMANT_TSE_ORDER_OTHER_PARTY,
        CLAIMANT_TSE_RESTRICT_PUBLICITY,
    })
    void sendRespondentEmail_groupA_sendsEmail_Welsh(String applicationType) throws IOException {
        CaseData caseData = createCaseDataWithHearing(applicationType);
        caseData.getRespondentCollection().get(0).setId("123-456-789");
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(featureToggleService.isWelshEnabled()).thenReturn(true);
        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        claimantTellSomethingElseService.sendRespondentsEmail(caseDetails);
        verify(emailService).sendEmail(eq(TEMPLATE_ID_A_CY), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();

        String expectedDueDate = UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7);
        for (Map.Entry<String, String> monthEntry : CY_MONTHS_MAP.entrySet()) {
            if (expectedDueDate.contains(monthEntry.getKey())) {
                expectedDueDate = expectedDueDate.replace(monthEntry.getKey(), monthEntry.getValue());
                break;
            }
        }

        assertThat(personalisation.get("claimant"), is("claimant"));
        assertThat(personalisation.get("respondentNames"), is("Respondent"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("16 Mai 2069"));
        assertThat(personalisation.get("shortText"), is(CY_APP_TYPE_MAP.get(
                APPLICATION_TYPE_MAP.get(applicationType))));
        assertThat(personalisation.get("datePlus7"), is(expectedDueDate));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null," +
                "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));

        assertTrue(((String) personalisation.get("linkToCitizenHub")).endsWith(WELSH_LANGUAGE_PARAM));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        CLAIMANT_TSE_WITHDRAW_CLAIM,
        CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS,
        CLAIMANT_TSE_CONSIDER_DECISION_AFRESH,
        CLAIMANT_TSE_RECONSIDER_JUDGMENT
    })
    void sendRespondentEmail_groupB_sendsEmail(String applicationType) throws IOException {
        CaseData caseData = createCaseDataWithHearing(applicationType);
        caseData.getRespondentCollection().get(0).setId("123-456-789");
        caseData.setHearingCollection(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[] {});
        claimantTellSomethingElseService.sendRespondentsEmail(caseDetails);
        verify(emailService).sendEmail(eq(TEMPLATE_ID_B), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();

        assertThat(personalisation.get("claimant"), is("claimant"));
        assertThat(personalisation.get("respondentNames"), is("Respondent"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("Not set"));
        assertThat(personalisation.get("shortText"), is(applicationType));
        assertThat(personalisation.get("datePlus7"), is(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7)));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null," +
                "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));


    }

    @ParameterizedTest
    @ValueSource(strings = {
        CLAIMANT_TSE_WITHDRAW_CLAIM,
        CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS,
        CLAIMANT_TSE_CONSIDER_DECISION_AFRESH,
        CLAIMANT_TSE_RECONSIDER_JUDGMENT
    })
    void sendRespondentEmail_groupB_sendsEmail_Welsh(String applicationType) throws IOException {
        CaseData caseData = createCaseDataWithHearing(applicationType);
        caseData.getRespondentCollection().get(0).setId("123-456-789");
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        caseData.setHearingCollection(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(featureToggleService.isWelshEnabled()).thenReturn(true);
        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        claimantTellSomethingElseService.sendRespondentsEmail(caseDetails);
        verify(emailService).sendEmail(eq(TEMPLATE_ID_B_CY), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();

        String expectedDueDate = UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7);
        for (Map.Entry<String, String> monthEntry : CY_MONTHS_MAP.entrySet()) {
            if (expectedDueDate.contains(monthEntry.getKey())) {
                expectedDueDate = expectedDueDate.replace(monthEntry.getKey(), monthEntry.getValue());
                break;
            }
        }

        assertThat(personalisation.get("claimant"), is("claimant"));
        assertThat(personalisation.get("respondentNames"), is("Respondent"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("Heb ei anfon"));
        assertThat(personalisation.get("shortText"), is(CY_APP_TYPE_MAP.get(
                APPLICATION_TYPE_MAP.get(applicationType))));
        assertThat(personalisation.get("datePlus7"), is(expectedDueDate));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null," +
                "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));
        assertTrue(((String) personalisation.get("linkToCitizenHub")).endsWith(WELSH_LANGUAGE_PARAM));
    }

    private CaseData createCaseDataWithHearing(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("test")
                .withClaimant("claimant")
                .withClaimantType("person1@email.com")
                .withClaimantHearingPreference(NotificationServiceConstants.ENGLISH_LANGUAGE)
                .withRespondentWithAddress("Respondent",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null, "respondent@unrepresented.com")
                .withHearing("1", "Hearing", "Judge", "Bodmin",
                        List.of("In person"), "60", "Days", "Sit Alone")
                .withHearingSession(0, "2069-05-16T01:00:00.000", "Listed", false)
                .build();

        RepresentedTypeR representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative("Respondent")
                        .respRepName("Respondent")
                        .representativeEmailAddress("test.rep@test.com")
                        .myHmctsYesNo("Yes")
                        .build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId("1111-2222-3333-1111");
        representedTypeRItem.setValue(representedType);
        caseData.setRepCollection(new ArrayList<>());
        caseData.getRepCollection().add(representedTypeRItem);
        caseData.setClaimantTseSelectApplication(selectedApplication);
        caseData.setClaimantTseRule92(YES);
        return caseData;
    }

    @Test
    void sendAdminEmail_DoesNothingWhenNoManagingOfficeIsSet() {
        CaseData caseData = createCaseData("", YES);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        claimantTellSomethingElseService.sendAdminEmail(caseDetails);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendAdminEmail_DoesNothingWhenNoManagingOfficeHasNoEmail() {
        CaseData caseData = createCaseData("", YES);
        CaseDetails caseDetails = new CaseDetails();
        caseData.setManagingOffice("Aberdeen");
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        claimantTellSomethingElseService.sendAdminEmail(caseDetails);
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
        claimantTellSomethingElseService.sendAdminEmail(caseDetails);

        Map<String, String> caseNumber = Map.of("caseNumber", "test",
                "emailFlag", "",
                "claimant", "claimant",
                "respondents", "Father Ted",
                "date", "Not set",
                "url", "exuiUrl669718251103419");

        verify(emailService, times(1)).sendEmail(any(), any(), eq(caseNumber));
    }
}
