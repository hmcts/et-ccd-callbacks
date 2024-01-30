package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponse;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotification;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.I_DO_NOT_WANT_TO_COPY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class PseRespondToTribunalServiceTest {
    private PseRespondToTribunalService pseRespondToTribService;
    private EmailService emailService;
    private CaseData caseData;
    @Mock
    private FeatureToggleService featureToggleService;

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String WELSH_TEMPLATE_ID = "welshTemplateId";
    private static final String RESPONSE = "Some Response";
    private static final String TEST_CASE_ID = "1677174791076683";
    private static final String RULE92_NO_DETAILS = "Rule 92 Reasons";
    private static final String SUBMITTED_BODY = """
        ### What happens next\r
        \r
        %sThe tribunal will consider all correspondence and let you know what happens next.""";
    private static final String RULE92_ANSWERED_YES =
            "You have responded to the tribunal and copied your response to the other party.\r\n\r\n";
    private static final String LINK_TO_CITIZEN_HUB = "linkToCitizenHub";
    private static final String LINK_TO_EXUI = "linkToExUI";
    private static final String CITIZEN_HUB_URL = "citizenUrl";
    private static final String EXUI_URL = "exuiUrl";

    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private HearingSelectionService hearingSelectionService;
    @MockBean
    private TribunalOfficesService tribunalOfficesService;

    @BeforeEach
    void setUp() {
        emailService = spy(new EmailUtils());
        pseRespondToTribService = new PseRespondToTribunalService(emailService, userIdamService,
                hearingSelectionService,
                tribunalOfficesService, featureToggleService);
        caseData = CaseDataBuilder.builder().build();
        ReflectionTestUtils.setField(pseRespondToTribService, "acknowledgeEmailYesTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(pseRespondToTribService, "acknowledgeEmailNoTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(pseRespondToTribService, "notificationToClaimantTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(pseRespondToTribService, "cyNotificationToClaimantTemplateId", WELSH_TEMPLATE_ID);
        ReflectionTestUtils.setField(pseRespondToTribService, "notificationToAdminTemplateId", TEMPLATE_ID);
        ReflectionTestUtils.setField(pseRespondToTribService, "notificationToAdminTemplateId", TEMPLATE_ID);
    }

    @Test
    void populateSelectDropdown_checkSendNotificationNotify_returnList() {
        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .build())
                .build(),
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("2")
                    .sendNotificationTitle("Submit hearing agenda")
                    .sendNotificationNotify(CLAIMANT_ONLY)
                    .build())
                .build(),
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("3")
                    .sendNotificationTitle("Send Notification Title")
                    .sendNotificationNotify(RESPONDENT_ONLY)
                    .build())
                .build()
        ));

        DynamicFixedListType expected = DynamicFixedListType.from(List.of(
                DynamicValueType.create("1", "1 View notice of hearing"),
                DynamicValueType.create("3", "3 Send Notification Title")
            ));

        assertThat(pseRespondToTribService.populateSelectDropdown(caseData),
            is(expected));
    }

    @Test
    void populateSelectDropdown_checkRespondCollection_returnList() {
        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .build())
                .build(),
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("2")
                    .sendNotificationTitle("Submit hearing agenda")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .respondCollection(ListTypeItem.from(TypeItem.<PseResponse>builder()
                        .id(UUID.randomUUID().toString())
                        .value(PseResponse.builder()
                            .from(CLAIMANT_TITLE)
                            .build())
                        .build()))
                    .build())
                .build(),
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("3")
                    .sendNotificationTitle("Send Notification Title")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .respondCollection(ListTypeItem.from(TypeItem.<PseResponse>builder()
                        .id(UUID.randomUUID().toString())
                        .value(PseResponse.builder()
                            .from(RESPONDENT_TITLE)
                            .build())
                        .build()))
                    .build())
                .build()
        ));

        DynamicFixedListType expected = DynamicFixedListType.from(List.of(
            DynamicValueType.create("1", "1 View notice of hearing"),
            DynamicValueType.create("2", "2 Submit hearing agenda")
        ));

        assertThat(pseRespondToTribService.populateSelectDropdown(caseData),
            is(expected));
    }

    @Test
    void initialOrdReqDetailsTableMarkUp_noSupportingMaterial() {

        TypeItem<PseResponse> pseResponseTypeItem = TypeItem.<PseResponse>builder()
            .id(UUID.randomUUID().toString())
            .value(PseResponse.builder()
                .from(CLAIMANT_TITLE)
                .date("10 Aug 2022")
                .response("Response text entered")
                .copyToOtherParty(YES)
                .build())
            .build();

        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .date("5 Aug 2022")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationLetter(YES)
                    .sendNotificationUploadDocument(List.of(
                        createDocumentTypeItem("Letter 4.8 - Hearing notice - hearing agenda.pdf",
                            "5fac5af5-b8ac-458c-a329-31cce78da5c2",
                            "Notice of Hearing and Submit Hearing Agenda document")))
                    .sendNotificationSubject(List.of("Hearing", "Case management orders / requests"))
                    .sendNotificationSelectHearing(DynamicFixedListType.of(
                        DynamicValueType.create("3", "3: Hearing - Leeds - 14 Aug 2022")))
                    .sendNotificationCaseManagement("Case management order")
                    .sendNotificationResponseTribunal("Yes - view document for details")
                    .sendNotificationSelectParties(BOTH_PARTIES)
                    .sendNotificationWhoCaseOrder("Legal Officer")
                    .sendNotificationFullName("Mr Lee Gal Officer")
                    .sendNotificationAdditionalInfo("Additional Info")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .respondCollection(ListTypeItem.from(pseResponseTypeItem))
                    .build())
                .build()
        ));

        caseData.setPseRespondentSelectOrderOrRequest(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        String expected = """
            |View Application||\r
            |--|--|\r
            |Notification|View notice of hearing|\r
            |Hearing|3: Hearing - Leeds - 14 Aug 2022|\r
            |Date sent|5 Aug 2022|\r
            |Sent by|Tribunal|\r
            |Case management order or request?|Case management order|\r
            |Is a response required?|Yes - view document for details|\r
            |Party or parties to respond|Both parties|\r
            |Additional information|Additional Info|\r
            |Document|<a href="/documents/5fac5af5-b8ac-458c-a329-31cce78da5c2/binary" target="_blank">Letter 4.8 - Hearing notice - hearing agenda.pdf</a>|\r
            |Description|Notice of Hearing and Submit Hearing Agenda document|\r
            |Request made by|Legal Officer|\r
            |Name|Mr Lee Gal Officer|\r
            |Sent to|Both parties|\r
            |Response 1 | |\r
            |--|--|\r
            |Response from | Claimant|\r
            |Response date | 10 Aug 2022|\r
            |What's your response to the tribunal? | Response text entered|\r
            |Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | Yes|\r
            \r
            """;

        assertThat(pseRespondToTribService.initialOrdReqDetailsTableMarkUp(caseData),
            is(expected));
    }

    @Test
    void initialOrdReqDetailsTableMarkUp_withHearing() {

        TypeItem<PseResponse> pseResponseTypeItem = TypeItem.<PseResponse>builder()
            .id(UUID.randomUUID().toString())
            .value(PseResponse.builder()
                .from(CLAIMANT_TITLE)
                .date("10 Aug 2022")
                .response("Response text entered")
                .hasSupportingMaterial(YES)
                .supportingMaterial(List.of(createDocumentTypeItem("My claimant hearing agenda.pdf",
                    "ca35bccd-f507-4243-9133-f6081fb0fe5e")))
                .copyToOtherParty(YES)
                .build())
            .build();

        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .date("5 Aug 2022")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationLetter(YES)
                    .sendNotificationUploadDocument(List.of(
                        createDocumentTypeItem("Letter 4.8 - Hearing notice - hearing agenda.pdf",
                            "5fac5af5-b8ac-458c-a329-31cce78da5c2",
                            "Notice of Hearing and Submit Hearing Agenda document")))
                    .sendNotificationSubject(List.of("Hearing", "Case management orders / requests"))
                    .sendNotificationSelectHearing(DynamicFixedListType.of(
                        DynamicValueType.create("3", "3: Hearing - Leeds - 14 Aug 2022")))
                    .sendNotificationCaseManagement("Case management order")
                    .sendNotificationResponseTribunal("Yes - view document for details")
                    .sendNotificationSelectParties(BOTH_PARTIES)
                    .sendNotificationWhoCaseOrder("Legal Officer")
                    .sendNotificationFullName("Mr Lee Gal Officer")
                    .sendNotificationAdditionalInfo("Additional Info")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .respondCollection(ListTypeItem.from(pseResponseTypeItem))
                    .build())
                .build()
        ));

        caseData.setPseRespondentSelectOrderOrRequest(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        String expected = """
            |View Application||\r
            |--|--|\r
            |Notification|View notice of hearing|\r
            |Hearing|3: Hearing - Leeds - 14 Aug 2022|\r
            |Date sent|5 Aug 2022|\r
            |Sent by|Tribunal|\r
            |Case management order or request?|Case management order|\r
            |Is a response required?|Yes - view document for details|\r
            |Party or parties to respond|Both parties|\r
            |Additional information|Additional Info|\r
            |Document|<a href="/documents/5fac5af5-b8ac-458c-a329-31cce78da5c2/binary" target="_blank">Letter 4.8 - Hearing notice - hearing agenda.pdf</a>|\r
            |Description|Notice of Hearing and Submit Hearing Agenda document|\r
            |Request made by|Legal Officer|\r
            |Name|Mr Lee Gal Officer|\r
            |Sent to|Both parties|\r
            |Response 1 | |\r
            |--|--|\r
            |Response from | Claimant|\r
            |Response date | 10 Aug 2022|\r
            |What's your response to the tribunal? | Response text entered|\r
            |Supporting material | <a href="/documents/ca35bccd-f507-4243-9133-f6081fb0fe5e/binary" target="_blank">My claimant hearing agenda.pdf</a>\r
            |\r
            |Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | Yes|\r
            \r
            """;

        assertThat(pseRespondToTribService.initialOrdReqDetailsTableMarkUp(caseData),
            is(expected));
    }

    private DocumentTypeItem createDocumentTypeItem(String fileName, String uuid) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(DocumentTypeBuilder.builder()
            .withUploadedDocument(fileName, uuid)
            .build());
        return documentTypeItem;
    }

    private DocumentTypeItem createDocumentTypeItem(String fileName, String uuid, String shortDescription) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(DocumentTypeBuilder.builder()
            .withUploadedDocument(fileName, uuid)
                .withShortDescription(shortDescription)
            .build());
        return documentTypeItem;
    }

    @Test
    void initialOrdReqDetailsTableMarkUp_NoHearing() {
        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .date("5 Aug 2022")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationLetter(NO)
                    .sendNotificationSubject(List.of("Case management orders / requests"))
                    .sendNotificationCaseManagement("Request")
                    .sendNotificationResponseTribunal("No")
                    .sendNotificationRequestMadeBy("Judge")
                    .sendNotificationFullName("Mr Lee Gal Officer")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .build())
                .build()
        ));

        caseData.setPseRespondentSelectOrderOrRequest(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        String expected = """
            |View Application||\r
            |--|--|\r
            |Notification|View notice of hearing|\r
            |Hearing||\r
            |Date sent|5 Aug 2022|\r
            |Sent by|Tribunal|\r
            |Case management order or request?|Request|\r
            |Is a response required?|No|\r
            |Request made by|Judge|\r
            |Name|Mr Lee Gal Officer|\r
            |Sent to|Both parties|\r
            """;

        assertThat(pseRespondToTribService.initialOrdReqDetailsTableMarkUp(caseData), is(expected));
    }

    @ParameterizedTest
    @MethodSource("inputList")
    void validateInput_CountErrors(String responseText, String supportingMaterial, int expectedErrorCount) {
        caseData.setPseRespondentOrdReqResponseText(responseText);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(supportingMaterial);

        List<String> errors = pseRespondToTribService.validateInput(caseData);

        assertEquals(expectedErrorCount, errors.size());
    }

    private static Stream<Arguments> inputList() {
        return Stream.of(
            Arguments.of(null, null, 1),
            Arguments.of(null, NO, 1),
            Arguments.of(null, YES, 0),
            Arguments.of(RESPONSE, null, 0),
            Arguments.of(RESPONSE, NO, 0),
            Arguments.of(RESPONSE, YES, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("createRespondentResponses")
    void addRespondentResponseToJON(String response, String hasSupportingMaterial,
                                                int supportingDocsSize, String copyOtherParty, String copyDetails) {

        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                        .number("1")
                        .date("5 Aug 2022")
                        .sendNotificationTitle("View notice of hearing, submit hearing agenda")
                        .sendNotificationResponsesCount("0")
                    .build())
                .build()
        ));

        caseData.setPseRespondentSelectOrderOrRequest(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing, submit hearing agenda")));

        caseData.setPseRespondentOrdReqResponseText(response);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(hasSupportingMaterial);
        caseData.setPseRespondentOrdReqCopyToOtherParty(copyOtherParty);
        caseData.setPseRespondentOrdReqCopyNoGiveDetails(copyDetails);

        if (supportingDocsSize > 0) {
            ListTypeItem<DocumentType> supportingMaterials = new ListTypeItem<>();
            for (int i = 0; i < supportingDocsSize; i++) {
                supportingMaterials.add(createDocumentType(Integer.toString(i)));
            }
            caseData.setPseRespondentOrdReqUploadDocument(supportingMaterials);
        }

        SendNotification notificationType = caseData.getSendNotificationCollection().get(0).getValue();

        assertEquals("0", notificationType.getSendNotificationResponsesCount());

        pseRespondToTribService.addRespondentResponseToJON(caseData);

        PseResponse savedResponse = notificationType.getRespondCollection().get(0).getValue();

        assertEquals(RESPONDENT_TITLE, savedResponse.getFrom());
        assertEquals(response, savedResponse.getResponse());
        assertEquals(hasSupportingMaterial, savedResponse.getHasSupportingMaterial());
        assertEquals(copyOtherParty, savedResponse.getCopyToOtherParty());
        assertEquals(copyDetails, savedResponse.getCopyNoGiveDetails());
        assertEquals("1", notificationType.getSendNotificationResponsesCount());

        if (supportingDocsSize > 0) {
            assertEquals(savedResponse.getSupportingMaterial().size(), supportingDocsSize);
        } else {
            assertNull(savedResponse.getSupportingMaterial());
        }
    }

    private static Stream<Arguments> createRespondentResponses() {
        return Stream.of(
            Arguments.of(RESPONSE, YES, 2, YES, null),
            Arguments.of(RESPONSE, YES, 1, I_DO_NOT_WANT_TO_COPY, RULE92_NO_DETAILS),
            Arguments.of(RESPONSE, NO, 0, YES, null),
            Arguments.of(RESPONSE, NO, 0, I_DO_NOT_WANT_TO_COPY, RULE92_NO_DETAILS)
        );
    }

    private DocumentTypeItem createDocumentType(String id) {
        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(new UploadedDocumentType());
        documentType.getUploadedDocument().setDocumentBinaryUrl("binaryUrl/documents/");
        documentType.getUploadedDocument().setDocumentFilename("testFileName");

        DocumentTypeItem document = new DocumentTypeItem();
        document.setId(id);
        document.setValue(documentType);

        return document;
    }

    @Test
    void sendAcknowledgeEmail_rule92Yes() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("6000001/2023")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1677174791076683");
        caseDetails.getCaseData().setPseRespondentOrdReqCopyToOtherParty(YES);

        when(userIdamService.getUserDetails(any())).thenReturn(HelperTest.getUserDetails());

        Map<String, String> expectedMap = Map.of(
            "caseNumber", "6000001/2023",
                LINK_TO_EXUI, EXUI_URL + "1677174791076683"
        );

        pseRespondToTribService.sendAcknowledgeEmail(caseDetails, AUTH_TOKEN);
        verify(emailService).sendEmail(TEMPLATE_ID, "mail@mail.com", expectedMap);
    }

    @Test
    void sendAcknowledgeEmail_rule92No() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("6000001/2023")
            .withClaimant("Claimant Name")
            .withRespondent("Respondent One", YES, "01-Jan-2023", false)
            .withRespondent("Respondent Two", YES, "02-Jan-2023", false)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1677174791076683");

        caseDetails.getCaseData().setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationSelectHearing(DynamicFixedListType.of(
                        DynamicValueType.create("1", "1: Hearing - Leeds - 25 Dec 2023")))
                    .build())
                .build()
        ));

        caseDetails.getCaseData().setPseRespondentOrdReqCopyToOtherParty(NO);

        caseDetails.getCaseData().setPseRespondentSelectOrderOrRequest(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        when(userIdamService.getUserDetails(any())).thenReturn(HelperTest.getUserDetails());

        DateListedType selectedListing = new DateListedType();
        selectedListing.setListedDate("2023-12-25T12:00:00.000");
        when(hearingSelectionService.getSelectedListingWithList(isA(CaseData.class),
            isA(DynamicFixedListType.class))).thenReturn(selectedListing);

        Map<String, String> expectedMap = Map.of(
            "caseNumber", "6000001/2023",
            "claimant", "Claimant Name",
            "respondents", "Respondent One, Respondent Two",
            "hearingDate", "25 December 2023 12:00",
            LINK_TO_EXUI, EXUI_URL + "1677174791076683"
        );

        pseRespondToTribService.sendAcknowledgeEmail(caseDetails, AUTH_TOKEN);
        verify(emailService).sendEmail(TEMPLATE_ID, "mail@mail.com", expectedMap);
    }

    @Test
    void sendClaimantEmail_rule92Yes_SendEmail() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("6000001/2023")
            .withClaimant("Claimant Name")
            .withClaimantType("claimant@email.com")
            .withRespondent("Respondent One", YES, "01-Jan-2023", false)
            .withRespondent("Respondent Two", YES, "02-Jan-2023", false)
                .withClaimantHearingPreference(ENGLISH_LANGUAGE)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1677174791076683");
        caseDetails.getCaseData().setPseRespondentOrdReqCopyToOtherParty(YES);

        Map<String, String> expectedMap = Map.of(
            "caseNumber", "6000001/2023",
            "claimant", "Claimant Name",
            "respondents", "Respondent One, Respondent Two",
            LINK_TO_CITIZEN_HUB, CITIZEN_HUB_URL + "1677174791076683"
        );

        pseRespondToTribService.sendClaimantEmail(caseDetails);
        verify(emailService).sendEmail(TEMPLATE_ID, "claimant@email.com", expectedMap);
    }

    @Test
    void sendClaimantEmail_rule92Yes_SendEmail_Welsh() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("6000001/2023")
                .withClaimant("Claimant Name")
                .withClaimantType("claimant@email.com")
                .withRespondent("Respondent One", YES, "01-Jan-2023", false)
                .withRespondent("Respondent Two", YES, "02-Jan-2023", false)
                .withClaimantHearingPreference(WELSH_LANGUAGE)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(TEST_CASE_ID);
        caseDetails.getCaseData().setPseRespondentOrdReqCopyToOtherParty(YES);
        when(featureToggleService.isWelshEnabled()).thenReturn(true);

        Map<String, String> expectedMap = Map.of(
                "caseNumber", "6000001/2023",
                "claimant", "Claimant Name",
                "respondents", "Respondent One, Respondent Two",
                LINK_TO_CITIZEN_HUB, CITIZEN_HUB_URL + TEST_CASE_ID + WELSH_LANGUAGE_PARAM
        );

        pseRespondToTribService.sendClaimantEmail(caseDetails);
        verify(emailService).sendEmail(WELSH_TEMPLATE_ID, "claimant@email.com", expectedMap);
    }

    @Test
    void sendClaimantEmail_rule92No_NotSend() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withClaimantHearingPreference(ENGLISH_LANGUAGE)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.getCaseData().setPseRespondentOrdReqCopyToOtherParty(NO);

        pseRespondToTribService.sendClaimantEmail(caseDetails);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendTribunalEmail_withHearing() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("6000001/2023")
            .withClaimant("Claimant Name")
            .withRespondent("Respondent One", YES, "01-Jan-2023", false)
            .withRespondent("Respondent Two", YES, "02-Jan-2023", false)
            .withManagingOffice("Manchester")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1677174791076683");

        caseDetails.getCaseData().setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationSelectHearing(DynamicFixedListType.of(
                        DynamicValueType.create("1", "1: Hearing - Leeds - 25 Dec 2023")))
                    .build())
                .build()
        ));

        caseDetails.getCaseData().setPseRespondentSelectOrderOrRequest(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        when(tribunalOfficesService.getTribunalOffice(any()))
            .thenReturn(TribunalOffice.valueOfOfficeName("Manchester"));

        DateListedType selectedListing = new DateListedType();
        selectedListing.setListedDate("2023-12-25T12:00:00.000");
        when(hearingSelectionService.getSelectedListingWithList(isA(CaseData.class),
            isA(DynamicFixedListType.class))).thenReturn(selectedListing);

        Map<String, String> expectedMap = Map.of(
            "caseNumber", "6000001/2023",
            "application", "View notice of hearing",
            "claimant", "Claimant Name",
            "respondents", "Respondent One, Respondent Two",
            "hearingDate", "25 December 2023 12:00",
                LINK_TO_EXUI, EXUI_URL + "1677174791076683"
        );

        pseRespondToTribService.sendTribunalEmail(caseDetails);
        verify(emailService).sendEmail(TEMPLATE_ID, "manchesteret@justice.gov.uk", expectedMap);
    }

    @Test
    void sendTribunalEmail_withoutHearing() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("6000001/2023")
            .withClaimant("Claimant Name")
            .withRespondent("Respondent One", YES, "01-Jan-2023", false)
            .withManagingOffice("Manchester")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1677174791076683");

        caseDetails.getCaseData().setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .number("1")
                    .sendNotificationTitle("View notice of hearing")
                    .build())
                .build()
        ));

        caseDetails.getCaseData().setPseRespondentSelectOrderOrRequest(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        when(tribunalOfficesService.getTribunalOffice(any()))
            .thenReturn(TribunalOffice.valueOfOfficeName("Manchester"));

        Map<String, String> expectedMap = Map.of(
            "caseNumber", "6000001/2023",
            "application", "View notice of hearing",
            "claimant", "Claimant Name",
            "respondents", "Respondent One",
            "hearingDate", "",
                LINK_TO_EXUI, EXUI_URL + "1677174791076683"
        );

        pseRespondToTribService.sendTribunalEmail(caseDetails);
        verify(emailService).sendEmail(TEMPLATE_ID, "manchesteret@justice.gov.uk", expectedMap);
    }

    @Test
    void clearRespondentResponse() {
        DynamicFixedListType newType = new DynamicFixedListType("Hello World");
        caseData.setPseRespondentSelectOrderOrRequest(newType);
        caseData.setPseRespondentOrdReqTableMarkUp("|Hearing, case management order or request | |\r\n|--|--|\r\n");
        caseData.setPseRespondentOrdReqResponseText(RESPONSE);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(YES);
        caseData.setPseRespondentOrdReqUploadDocument(new ListTypeItem<>());
        caseData.setPseRespondentOrdReqCopyToOtherParty(I_DO_NOT_WANT_TO_COPY);
        caseData.setPseRespondentOrdReqCopyNoGiveDetails(RULE92_NO_DETAILS);

        pseRespondToTribService.clearRespondentResponse(caseData);

        assertNull(caseData.getPseRespondentOrdReqTableMarkUp());
        assertNull(caseData.getPseRespondentOrdReqResponseText());
        assertNull(caseData.getPseRespondentOrdReqHasSupportingMaterial());
        assertNull(caseData.getPseRespondentOrdReqUploadDocument());
        assertNull(caseData.getPseRespondentOrdReqCopyToOtherParty());
        assertNull(caseData.getPseRespondentOrdReqCopyNoGiveDetails());
    }

    @Test
    void submittedBody_NoCopy() {
        DynamicFixedListType newType = new DynamicFixedListType("1");
        caseData.setPseRespondentSelectOrderOrRequest(newType);

        caseData.setSendNotificationCollection(List.of(
                SendNotificationTypeItem.builder().id(UUID.randomUUID().toString()).value(
                        SendNotification.builder().number("1").respondCollection(
                                        ListTypeItem.from(TypeItem.<PseResponse>builder().value(
                                                PseResponse.builder().copyToOtherParty(NO).build()
                                        ).build()))
                                .build()).build()));

        String actual = pseRespondToTribService.getSubmittedBody(caseData);

        assertEquals(actual, String.format(SUBMITTED_BODY, ""));
    }

    @Test
    void submittedBody_YesCopy() {
        DynamicFixedListType newType = new DynamicFixedListType("1");
        caseData.setPseRespondentSelectOrderOrRequest(newType);

        caseData.setSendNotificationCollection(List.of(
                SendNotificationTypeItem.builder().id(UUID.randomUUID().toString()).value(
                        SendNotification.builder().number("1").respondCollection(
                                        ListTypeItem.from(TypeItem.<PseResponse>builder().value(
                                                PseResponse.builder().copyToOtherParty(YES).build()
                                        ).build()))
                                .build()).build()));

        String actual = pseRespondToTribService.getSubmittedBody(caseData);

        assertEquals(actual, String.format(SUBMITTED_BODY, RULE92_ANSWERED_YES));
    }
}
