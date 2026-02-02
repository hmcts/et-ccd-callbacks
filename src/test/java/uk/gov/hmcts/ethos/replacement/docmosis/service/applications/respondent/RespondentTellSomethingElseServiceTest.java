package uk.gov.hmcts.ethos.replacement.docmosis.service.applications.respondent;

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
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
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
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CASE_MANAGEMENT;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_MONTHS_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_RESPONDENT_APP_TYPE_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICANT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.RESPONDENT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;

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
    @MockBean
    EmailNotificationService emailNotificationService;
    @MockBean
    CaseAccessService caseAccessService;

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

    private static final String GIVE_DETAIL_MISSING = "Use the text box or file upload to give details.";
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
                        tornadoService, documentManagementService, featureToggleService, caseAccessService,
                        emailNotificationService);
        tseService = new TseService(documentManagementService);

        ReflectionTestUtils.setField(respondentTellSomethingElseService,
                "tseRespondentAcknowledgeNoTemplateId", TEMPLATE_ID_NO);
        ReflectionTestUtils.setField(respondentTellSomethingElseService,
            "tseRespondentAcknowledgeTypeATemplateId", TEMPLATE_ID_A);
        ReflectionTestUtils.setField(respondentTellSomethingElseService,
            "tseRespondentAcknowledgeTypeBTemplateId", TEMPLATE_ID_B);
        ReflectionTestUtils.setField(respondentTellSomethingElseService,
                "tseRespondentAcknowledgeTypeCTemplateId", TEMPLATE_ID_C);

        ReflectionTestUtils.setField(respondentTellSomethingElseService,
            "tseRespondentToClaimantTypeATemplateId", TEMPLATE_ID_A);
        ReflectionTestUtils.setField(respondentTellSomethingElseService,
            "tseRespondentToClaimantTypeBTemplateId", TEMPLATE_ID_B);
        ReflectionTestUtils.setField(respondentTellSomethingElseService,
            "cyTseRespondentToClaimantTypeATemplateId", TEMPLATE_ID_A_CY);
        ReflectionTestUtils.setField(respondentTellSomethingElseService,
            "cyTseRespondentToClaimantTypeBTemplateId", TEMPLATE_ID_B_CY);

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
        assertThat(errors.getFirst(), is(GIVE_DETAIL_MISSING));
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
            case TSE_APP_AMEND_RESPONSE -> caseData.setResTseDocument1(createDocumentType("documentUrl"));
            case TSE_APP_CHANGE_PERSONAL_DETAILS -> caseData.setResTseDocument2(createDocumentType("documentUrl"));
            case TSE_APP_CLAIMANT_NOT_COMPLIED -> caseData.setResTseDocument3(createDocumentType("documentUrl"));
            case TSE_APP_CONSIDER_A_DECISION_AFRESH -> caseData.setResTseDocument4(createDocumentType("documentUrl"));
            case TSE_APP_CONTACT_THE_TRIBUNAL -> caseData.setResTseDocument5(createDocumentType("documentUrl"));
            case TSE_APP_ORDER_OTHER_PARTY -> caseData.setResTseDocument6(createDocumentType("documentUrl"));
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE ->
                caseData.setResTseDocument7(createDocumentType("documentUrl"));
            case TSE_APP_POSTPONE_A_HEARING -> caseData.setResTseDocument8(createDocumentType("documentUrl"));
            case TSE_APP_RECONSIDER_JUDGEMENT -> caseData.setResTseDocument9(createDocumentType("documentUrl"));
            case TSE_APP_RESTRICT_PUBLICITY -> caseData.setResTseDocument10(createDocumentType("documentUrl"));
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM ->
                caseData.setResTseDocument11(createDocumentType("documentUrl"));
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER -> caseData.setResTseDocument12(createDocumentType("documentUrl"));
            default -> {
                // No action needed for other applications
            }
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
            case TSE_APP_AMEND_RESPONSE -> caseData.setResTseTextBox1("Not Blank");
            case TSE_APP_CHANGE_PERSONAL_DETAILS -> caseData.setResTseTextBox2("Not Blank");
            case TSE_APP_CLAIMANT_NOT_COMPLIED -> caseData.setResTseTextBox3("Not Blank");
            case TSE_APP_CONSIDER_A_DECISION_AFRESH -> caseData.setResTseTextBox4("Not Blank");
            case TSE_APP_CONTACT_THE_TRIBUNAL -> caseData.setResTseTextBox5("Not Blank");
            case TSE_APP_ORDER_OTHER_PARTY -> caseData.setResTseTextBox6("Not Blank");
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE -> caseData.setResTseTextBox7("Not Blank");
            case TSE_APP_POSTPONE_A_HEARING -> caseData.setResTseTextBox8("Not Blank");
            case TSE_APP_RECONSIDER_JUDGEMENT -> caseData.setResTseTextBox9("Not Blank");
            case TSE_APP_RESTRICT_PUBLICITY -> caseData.setResTseTextBox10("Not Blank");
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM -> caseData.setResTseTextBox11("Not Blank");
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER -> caseData.setResTseTextBox12("Not Blank");
            default -> {
                // No action needed for other applications
            }
        }
    }

    @ParameterizedTest
    @MethodSource("sendAcknowledgeEmailAndGeneratePdf")
    void sendAcknowledgeEmailAndGeneratePdf(String selectedApplication, String rule92Selection,
                                            String expectedTemplateId) {
        CaseData caseData = createCaseData(selectedApplication, rule92Selection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        Map<String, String> expectedPersonalisation = createPersonalisation(caseData, selectedApplication);
        expectedPersonalisation.put(APPLICANT_NAME, "You");
        CaseUserAssignment mockAssignment1 = CaseUserAssignment
                .builder()
                .userId("respSolicitorUserId1")
                .caseRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
                .build();

        CaseUserAssignment mockAssignment2 = CaseUserAssignment
                .builder()
                .userId("respSolicitorUserId2")
                .caseRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
                .build();

        when(caseAccessService.filterCaseAssignmentsByOrgId(anyList(), any()))
                .thenReturn(Set.of(mockAssignment1, mockAssignment2));
        when(emailNotificationService.getRespondentSolicitorEmails(anyList()))
                .thenReturn(Set.of(LEGAL_REP_EMAIL));
        respondentTellSomethingElseService.sendAcknowledgeEmail(caseDetails, AUTH_TOKEN, anyList());

        verify(emailService).sendEmail(expectedTemplateId, LEGAL_REP_EMAIL, expectedPersonalisation);
    }

    private static Stream<Arguments> sendAcknowledgeEmailAndGeneratePdf() {
        return Stream.of(
                Arguments.of(TSE_APP_AMEND_RESPONSE, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_CONTACT_THE_TRIBUNAL, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_POSTPONE_A_HEARING, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_VARY_OR_REVOKE_AN_ORDER, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_ORDER_OTHER_PARTY, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_CLAIMANT_NOT_COMPLIED, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_RESTRICT_PUBLICITY, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_CHANGE_PERSONAL_DETAILS, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_CONSIDER_A_DECISION_AFRESH, NO, TEMPLATE_ID_NO),
                Arguments.of(TSE_APP_RECONSIDER_JUDGEMENT, NO, TEMPLATE_ID_NO),

                Arguments.of(TSE_APP_AMEND_RESPONSE, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
                Arguments.of(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
                Arguments.of(TSE_APP_CONTACT_THE_TRIBUNAL, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
                Arguments.of(TSE_APP_POSTPONE_A_HEARING, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
                Arguments.of(TSE_APP_VARY_OR_REVOKE_AN_ORDER, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
                Arguments.of(TSE_APP_ORDER_OTHER_PARTY, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
                Arguments.of(TSE_APP_CLAIMANT_NOT_COMPLIED, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),
                Arguments.of(TSE_APP_RESTRICT_PUBLICITY, I_DO_WANT_TO_COPY, TEMPLATE_ID_A),

                Arguments.of(TSE_APP_CHANGE_PERSONAL_DETAILS, I_DO_WANT_TO_COPY, TEMPLATE_ID_B),
                Arguments.of(TSE_APP_CONSIDER_A_DECISION_AFRESH, I_DO_WANT_TO_COPY, TEMPLATE_ID_B),
                Arguments.of(TSE_APP_RECONSIDER_JUDGEMENT, I_DO_WANT_TO_COPY, TEMPLATE_ID_B)
        );
    }

    @ParameterizedTest
    @MethodSource("sendAcknowledgeEmailAndGeneratePdf")
    void sendAcknowledgeEmailAndGeneratePdfIncludingToSharedList(String selectedApplication, String rule92Selection,
                                            String expectedTemplateId) {
        CaseData caseData = createCaseData(selectedApplication, rule92Selection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        CaseUserAssignment mockAssignment1 = CaseUserAssignment
                .builder()
                .userId("respSolicitorUserId1")
                .caseRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
                .build();

        CaseUserAssignment mockAssignment2 = CaseUserAssignment
                .builder()
                .userId("respSolicitorUserId2")
                .caseRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
                .build();

        when(caseAccessService.filterCaseAssignmentsByOrgId(anyList(), any()))
                .thenReturn(Set.of(mockAssignment1, mockAssignment2));
        when(emailNotificationService.getRespondentSolicitorEmails(anyList()))
                .thenReturn(Set.of(LEGAL_REP_EMAIL, "respSolicitor@test.com"));
        respondentTellSomethingElseService.sendAcknowledgeEmail(caseDetails, AUTH_TOKEN, anyList());

        Map<String, String> expectedPersonalisation = createPersonalisation(caseData, selectedApplication);
        expectedPersonalisation.put(APPLICANT_NAME, "You");

        verify(emailService).sendEmail(expectedTemplateId, "respSolicitor@test.com", expectedPersonalisation);
        verify(emailService).sendEmail(expectedTemplateId, LEGAL_REP_EMAIL, expectedPersonalisation);
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
            "respondentNames", getRespondentNames(caseData),
            "exuiCaseDetailsLink", "exuiUrl669718251103419"
        );

        respondentTellSomethingElseService.sendAcknowledgeEmail(caseDetails, AUTH_TOKEN, anyList());

        verify(emailService).sendEmail(TEMPLATE_ID_C, LEGAL_REP_EMAIL, expectedPersonalisation);
    }

    @Test
    void sendClaimantEmail_rule92No_doesNothing() {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, NO);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        respondentTellSomethingElseService.sendClaimantEmail(caseDetails, new ArrayList<>());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendClaimantEmail_groupC_doesNothing() {
        CaseData caseData = createCaseData(TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE, NO);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        respondentTellSomethingElseService.sendClaimantEmail(caseDetails, new ArrayList<>());
        verify(emailService, never()).sendEmail(any(), any(), any());
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
    void sendClaimantEmail_groupA_sendsEmail(String applicationType) throws IOException {
        CaseData caseData = createCaseDataWithHearing(applicationType);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[] {});
        CaseUserAssignment mockAssignment = CaseUserAssignment
                .builder()
                .userId("claimantSolicitorUserId")
                .caseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .build();

        when(emailNotificationService.getCaseClaimantSolicitorEmails(anyList()))
                .thenReturn(List.of(LEGAL_REP_EMAIL));

        respondentTellSomethingElseService.sendClaimantEmail(caseDetails, List.of(mockAssignment));
        verify(emailService, times(2)).sendEmail(eq(TEMPLATE_ID_A), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();

        assertThat(personalisation.get("claimant"), is("claimant"));
        assertThat(personalisation.get("respondentNames"), is("Father Ted"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("16 May 2069"));
        assertThat(personalisation.get("shortText"), is(applicationType));
        assertThat(personalisation.get("datePlus7"), is(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7)));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null,"
                + "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));
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
        CaseData caseData = createCaseDataWithHearing(applicationType);
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(featureToggleService.isWelshEnabled()).thenReturn(true);
        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        respondentTellSomethingElseService.sendClaimantEmail(caseDetails, new ArrayList<>());
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
        assertThat(personalisation.get("respondentNames"), is("Father Ted"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("16 Mai 2069"));
        assertThat(personalisation.get("shortText"), is(CY_RESPONDENT_APP_TYPE_MAP.get(applicationType)));
        assertThat(personalisation.get("datePlus7"), is(expectedDueDate));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null,"
                + "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));
        assertTrue(((String) personalisation.get("linkToCitizenHub")).endsWith(WELSH_LANGUAGE_PARAM));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        TSE_APP_CHANGE_PERSONAL_DETAILS,
        TSE_APP_CONSIDER_A_DECISION_AFRESH,
        TSE_APP_RECONSIDER_JUDGEMENT
    })
    void sendClaimantEmail_groupB_sendsEmail(String applicationType) throws IOException {
        CaseData caseData = createCaseDataWithHearing(applicationType);
        caseData.setHearingCollection(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[] {});
        respondentTellSomethingElseService.sendClaimantEmail(caseDetails, new ArrayList<>());
        verify(emailService).sendEmail(eq(TEMPLATE_ID_B), any(), personalisationCaptor.capture());
        Map<String, Object> personalisation = personalisationCaptor.getValue();

        assertThat(personalisation.get("claimant"), is("claimant"));
        assertThat(personalisation.get("respondentNames"), is("Father Ted"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("Not set"));
        assertThat(personalisation.get("shortText"), is(applicationType));
        assertThat(personalisation.get("datePlus7"), is(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7)));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null,"
                + "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        TSE_APP_CHANGE_PERSONAL_DETAILS,
        TSE_APP_CONSIDER_A_DECISION_AFRESH,
        TSE_APP_RECONSIDER_JUDGEMENT
    })
    void sendClaimantEmail_groupB_sendsEmail_Welsh(String applicationType) throws IOException {
        CaseData caseData = createCaseDataWithHearing(applicationType);
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        caseData.setHearingCollection(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);

        when(featureToggleService.isWelshEnabled()).thenReturn(true);
        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        respondentTellSomethingElseService.sendClaimantEmail(caseDetails, new ArrayList<>());
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
        assertThat(personalisation.get("respondentNames"), is("Father Ted"));
        assertThat(personalisation.get("caseNumber"), is(caseData.getEthosCaseReference()));
        assertThat(personalisation.get("hearingDate"), is("Heb ei anfon"));
        assertThat(personalisation.get("shortText"), is(CY_RESPONDENT_APP_TYPE_MAP.get(applicationType)));
        assertThat(personalisation.get("datePlus7"), is(expectedDueDate));
        assertThat(personalisation.get("linkToDocument").toString(), is("{\"file\":\"\",\"filename\":null,"
                + "\"confirm_email_before_download\":true,\"retention_period\":\"52 weeks\"}"));
        assertTrue(((String) personalisation.get("linkToCitizenHub")).endsWith(WELSH_LANGUAGE_PARAM));
    }

    private CaseData createCaseDataWithHearing(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("test")
                .withClaimant("claimant")
                .withClaimantType("person@email.com")
                .withClaimantHearingPreference(ENGLISH_LANGUAGE)
                .withRespondent("Father Ted", NO, null, false)
                .withHearing("1", "Hearing", "Judge", "Bodmin", List.of("In person"), "60", "Days", "Sit Alone")
                .withHearingSession(0, "2069-05-16T01:00:00.000", "Listed", false)
                .withClaimantRepresentedQuestion(YES)
                .withRepresentativeClaimantType("claimantSolicitorUserId", LEGAL_REP_EMAIL)
            .build();
        caseData.setResTseSelectApplication(selectedApplication);
        caseData.setResTseCopyToOtherPartyYesOrNo(YES);
        caseData.setEt1OnlineSubmission(YES);
        return caseData;
    }

    @ParameterizedTest
    @MethodSource("createRespondentApplication")
    void createApplication_withRespondentTseData_shouldPersistDataAndEmptyFields(String selectedApplication,
                                                                                 String textBoxData,
                                                                                 String documentUrl) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResTseSelectApplication(selectedApplication);
        RespondentTse respondentTse = new RespondentTse();
        respondentTse.setRespondentIdamId("12312312");
        caseData.setRespondentTse(respondentTse);
        setDocAndTextForSelectedApplication(caseData, textBoxData, documentUrl);
        caseData.setResTseCopyToOtherPartyYesOrNo("copyToOtherPartyYesOrNo");
        caseData.setResTseCopyToOtherPartyTextArea("copyToOtherPartyTextArea");

        tseService.createApplication(caseData, RESPONDENT_REP_TITLE);

        var genericTseApplicationType = caseData.getGenericTseApplicationCollection().getFirst().getValue();
        assertThat(genericTseApplicationType.getDetails(), is(textBoxData));
        assertThat(genericTseApplicationType.getCopyToOtherPartyText(), is("copyToOtherPartyTextArea"));
        assertThat(genericTseApplicationType.getCopyToOtherPartyYesOrNo(), is("copyToOtherPartyYesOrNo"));
        assertThat(genericTseApplicationType.getDocumentUpload().getDocumentUrl(), is(documentUrl));
        assertThat(genericTseApplicationType.getApplicant(), is(RESPONDENT_REP_TITLE));
        assertThat(genericTseApplicationType.getType(), is(selectedApplication));

        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        DocumentType actualDocumentType = documentCollection.getFirst().getValue();

        assertThat(documentCollection.size(), is(1));

        assertEquals(selectedApplication, actualDocumentType.getShortDescription());

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

    private List<GenericTseApplicationTypeItem> generateGenericTseApplicationList() {
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

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId("id");
        tseApplicationTypeItem.setValue(respondentTseType);

        List<GenericTseApplicationTypeItem> tseApplicationCollection = new ArrayList<>();
        tseApplicationCollection.add(tseApplicationTypeItem);

        return tseApplicationCollection;
    }

    private void setDocAndTextForSelectedApplication(CaseData caseData,
                                                     String textBoxData,
                                                     String documentUrl) {
        switch (caseData.getResTseSelectApplication()) {
            case TSE_APP_AMEND_RESPONSE -> {
                caseData.setResTseTextBox1(textBoxData);
                caseData.setResTseDocument1(createDocumentType(documentUrl));
            }
            case TSE_APP_CHANGE_PERSONAL_DETAILS -> {
                caseData.setResTseTextBox2(textBoxData);
                caseData.setResTseDocument2(createDocumentType(documentUrl));
            }
            case TSE_APP_CLAIMANT_NOT_COMPLIED -> {
                caseData.setResTseTextBox3(textBoxData);
                caseData.setResTseDocument3(createDocumentType(documentUrl));
            }
            case TSE_APP_CONSIDER_A_DECISION_AFRESH -> {
                caseData.setResTseTextBox4(textBoxData);
                caseData.setResTseDocument4(createDocumentType(documentUrl));
            }
            case TSE_APP_CONTACT_THE_TRIBUNAL -> {
                caseData.setResTseTextBox5(textBoxData);
                caseData.setResTseDocument5(createDocumentType(documentUrl));
            }
            case TSE_APP_ORDER_OTHER_PARTY -> {
                caseData.setResTseTextBox6(textBoxData);
                caseData.setResTseDocument6(createDocumentType(documentUrl));
            }
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE -> {
                caseData.setResTseTextBox7(textBoxData);
                caseData.setResTseDocument7(createDocumentType(documentUrl));
            }
            case TSE_APP_POSTPONE_A_HEARING -> {
                caseData.setResTseTextBox8(textBoxData);
                caseData.setResTseDocument8(createDocumentType(documentUrl));
            }
            case TSE_APP_RECONSIDER_JUDGEMENT -> {
                caseData.setResTseTextBox9(textBoxData);
                caseData.setResTseDocument9(createDocumentType(documentUrl));
            }
            case TSE_APP_RESTRICT_PUBLICITY -> {
                caseData.setResTseTextBox10(textBoxData);
                caseData.setResTseDocument10(createDocumentType(documentUrl));
            }
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM -> {
                caseData.setResTseTextBox11(textBoxData);
                caseData.setResTseDocument11(createDocumentType(documentUrl));
            }
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER -> {
                caseData.setResTseTextBox12(textBoxData);
                caseData.setResTseDocument12(createDocumentType(documentUrl));
            }
            default -> {
                // No action needed for other applications
            }
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
        RespondentTse tse = new RespondentTse();
        tse.setRespondentIdamId("respondentIdamId");
        caseData.setRespondentTse(tse);

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
                                                      String selectedApplication) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("claimant", caseData.getClaimant());
        personalisation.put("respondentNames", getRespondentNames(caseData));
        personalisation.put("hearingDate", "Not set");
        personalisation.put("shortText", selectedApplication);
        personalisation.put("exuiCaseDetailsLink", "exuiUrl669718251103419");
        return personalisation;
    }

    @Test
    void generatesAndAddsTsePdfToDocumentCollection() {
        CaseData caseData = new CaseData();
        caseData.setResTseSelectApplication("Amend response");
        respondentTellSomethingElseService.generateAndAddTsePdf(caseData, "token", "typeId");

        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        DocumentType actual = documentCollection.getFirst().getValue();

        DocumentType expected = DocumentType.builder()
                .shortDescription("Amend response")
                .dateOfCorrespondence(LocalDate.now().toString())
                .topLevelDocuments(CASE_MANAGEMENT)
                .caseManagementDocuments(APP_TO_AMEND_RESPONSE)
                .documentType(APP_TO_AMEND_RESPONSE)
                .docNumber("1")
                .build();

        Assertions.assertThat(documentCollection).hasSize(1);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void clearApplicationData() {
        CaseData caseData = createCaseData(TSE_APP_AMEND_RESPONSE, NO);
        tseService.clearApplicationData(caseData);
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
        assertThat(caseData.getResTseDocument11(), is(nullValue()));
        assertThat(caseData.getResTseDocument12(), is(nullValue()));
    }

    @Test
    void sendEmails_shouldSendAllRelevantEmails_withoutSpy() {
        // Arrange
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("123456");
        CaseData caseDataWithRespondent = new CaseData();
        caseDataWithRespondent.setResTseSelectApplication("Amend response");
        caseDataWithRespondent.setEthosCaseReference("ET-1234-5678-9012-3456");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent Ltd");
        respondentSumTypeItem.setValue(respondentSumType);
        caseDataWithRespondent.setRespondentCollection(List.of(respondentSumTypeItem));
        caseDataWithRespondent.setClaimant("John Doe");

        HearingType hearingType = new HearingType();
        DateListedTypeItem hearingItem = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate("2069-05-16T10:00:00.000");
        hearingItem.setValue(dateListedType);
        hearingType.setHearingDateCollection(new ArrayList<>(List.of(hearingItem)));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        caseDataWithRespondent.setHearingCollection(List.of(hearingTypeItem));

        caseDetails.setCaseData(caseDataWithRespondent);
        caseDetails.setCaseId("123456");

        List<CaseUserAssignment> assignments = List.of(
                CaseUserAssignment.builder().userId("user1").caseRole("role1").build()
        );

        when(caseAccessService.getCaseUserAssignmentsById("123456")).thenReturn(assignments);

        RespondentTellSomethingElseService service = new RespondentTellSomethingElseService(
                emailService, userIdamService, tribunalOfficesService, tornadoService,
                documentManagementService, featureToggleService, caseAccessService, emailNotificationService
        );

        service.sendEmails(caseDetails, "token");

        verify(emailService, atLeastOnce()).sendEmail(any(), any(), any());
    }

    @Test
    void sendEmails_shouldLogWarningAndSendAdminEmailWhenNoAssignments_withoutSpy() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("123456");
        CaseData caseDataWithRespondent = new CaseData();
        caseDataWithRespondent.setResTseSelectApplication("Amend response");
        caseDataWithRespondent.setEthosCaseReference("ET-1234-5678-9012-3456");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent Ltd");
        respondentSumTypeItem.setValue(respondentSumType);
        caseDataWithRespondent.setRespondentCollection(List.of(respondentSumTypeItem));
        caseDataWithRespondent.setClaimant("John Doe");

        HearingType hearingType = new HearingType();
        DateListedTypeItem hearingItem = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate("2069-05-16T10:00:00.000");
        hearingItem.setValue(dateListedType);
        hearingType.setHearingDateCollection(new ArrayList<>(List.of(hearingItem)));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        caseDataWithRespondent.setHearingCollection(List.of(hearingTypeItem));

        caseDetails.setCaseData(caseDataWithRespondent);
        caseDetails.setCaseId("123456");

        when(caseAccessService.getCaseUserAssignmentsById("123456")).thenReturn(List.of());
        TribunalOffice tribunalLeedsOffice = TribunalOffice.valueOfOfficeName("Leeds");
        when(tribunalOfficesService.getTribunalOffice(any()))
                .thenReturn(tribunalLeedsOffice);

        RespondentTellSomethingElseService service = new RespondentTellSomethingElseService(
                emailService, userIdamService, tribunalOfficesService, tornadoService,
                documentManagementService, featureToggleService, caseAccessService, emailNotificationService
        );

        service.sendEmails(caseDetails, "token");

        verify(emailService, atLeastOnce()).sendEmail(any(), any(), any());
    }
}
