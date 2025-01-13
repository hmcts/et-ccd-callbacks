package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.PARTY_NOT_RESPONDED_COMPILED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.PARTY_NOT_RESPONDED_COMPLIED;

class ReferralHelperTest {
    private CaseData caseData;
    private static final String JUDGE_ROLE_ENG = "caseworker-employment-etjudge-englandwales";
    private static final String JUDGE_ROLE_SCOT = "caseworker-employment-etjudge-scotland";
    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String INVALID_EMAIL_ERROR_MESSAGE = "The email address entered is invalid.";
    private static final DateTimeFormatter REFERRAL_DATE_PATTERN = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().build();
        caseData.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        addHearingToCaseData(caseData);
    }

    @Test
    void populateSingleHearingDetails() {
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "Costs Hearing", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(0, LocalDateTime.now().plusYears(1).format(OLD_DATE_TIME_PATTERN),
                HEARING_STATUS_LISTED, false)
            .build();

        String markdown = ReferralHelper.populateHearingDetails(caseData);
        assertTrue(markdown.contains("|Hearing 1||"));
        assertTrue(markdown.contains("|Hearing venue|Venue|"));
    }

    @Test
    void populateMultipleHearingDetails() {
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "Costs Hearing", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(0, LocalDateTime.now().plusYears(1).format(OLD_DATE_TIME_PATTERN),
                HEARING_STATUS_POSTPONED, false)
            .withHearing("2", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(1, LocalDateTime.now().plusYears(1).format(OLD_DATE_TIME_PATTERN),
                HEARING_STATUS_LISTED, false)
            .build();

        String markdown = ReferralHelper.populateHearingDetails(caseData);
        assertFalse(markdown.contains("|Hearing 1||")); // Should be false as first hearing is postponed
        assertTrue(markdown.contains("|Hearing 2||"));
        assertTrue(markdown.contains("|Hearing venue|Venue|"));
    }

    @Test
    void saveReferralToTheReferralCollection() {
        caseData.setReferCaseTo("Judge Judy");
        caseData.setIsUrgent("Yes");
        caseData.setReferentEmail("judge.judy@aol.com");
        caseData.setReferralSubject("Subject line here");
        caseData.setReferralSubjectSpecify("Custom subject line");
        caseData.setReferralDetails("This is an explanation");
        caseData.setReferralInstruction("Custom instructions for judge");

        ReferralHelper.createReferral(caseData, "Judge Judy", null);

        String expected = "ReferralType(referralNumber=1, referralHearingDate=11 Nov 2030, referCaseTo=Judge Judy, "
                + "referentEmail=judge.judy@aol.com, isUrgent=Yes, referralSubject=Subject line here, "
                + "referralSubjectSpecify=Custom subject line, referralDetails=This is an explanation, "
                + "referralDocument=[GenericTypeItem(id=1, value=DocumentType(typeOfDocument=null, "
                + "uploadedDocument=UploadedDocumentType(documentBinaryUrl=binaryUrl/documents/, "
                + "documentFilename=testFileName, documentUrl=null, categoryId=null, uploadTimestamp=null), "
                + "ownerDocument=null, creationDate=null, "
                + "shortDescription=null, topLevelDocuments=null, startingClaimDocuments=null, "
                + "responseClaimDocuments=null, initialConsiderationDocuments=null, caseManagementDocuments=null, "
                + "withdrawalSettledDocuments=null, hearingsDocuments=null, judgmentAndReasonsDocuments=null, "
                + "reconsiderationDocuments=null, miscDocuments=null, documentType=null, dateOfCorrespondence=null"
                + ", docNumber=null, tornadoEmbeddedPdfUrl=null, excludeFromDcf=null, documentIndex=null)), "
                + "GenericTypeItem(id=2, "
                + "value=DocumentType(typeOfDocument=null, uploadedDocument=UploadedDocumentType("
                + "documentBinaryUrl=binaryUrl/documents/, documentFilename=testFileName, documentUrl=null, "
                + "categoryId=null, uploadTimestamp=null), "
                + "ownerDocument=null, creationDate=null, shortDescription=null, topLevelDocuments=null, "
                + "startingClaimDocuments=null, responseClaimDocuments=null, initialConsiderationDocuments=null, "
                + "caseManagementDocuments=null, withdrawalSettledDocuments=null, hearingsDocuments=null, j"
                + "udgmentAndReasonsDocuments=null, reconsiderationDocuments=null, miscDocuments=null, "
                + "documentType=null, dateOfCorrespondence=null, docNumber=null, tornadoEmbeddedPdfUrl=null, "
                + "excludeFromDcf=null, documentIndex=null))], "
                + "referralInstruction=Custom instructions for judge, "
                + "referredBy=Judge Judy, "
                + "referralDate=" + Helper.getCurrentDate() + ", referralStatus=Awaiting instructions, "
                + "closeReferralGeneralNotes=null, "
                + "referralReplyCollection=null, updateReferralCollection=null, referralSummaryPdf=null)";

        String actual = caseData.getReferralCollection().get(0).getValue().toString();
        assertEquals(expected, actual);
    }

    @Test
    void addNewReferralToReferralCollection() {
        ReferralHelper.createReferral(caseData, "", null);
        ReferralHelper.createReferral(caseData, "", null);

        assertEquals(2, caseData.getReferralCollection().size());
    }

    @Test
    void saveTheUserDetailsOfTheReferrerWithTheReferral() {
        ReferralHelper.createReferral(caseData, "Judge Judy", null);

        String referredBy = caseData.getReferralCollection().get(0).getValue().getReferredBy();

        assertEquals("Judge Judy", referredBy);
    }

    @Test
    void whenCalledWithNoReferrals_ReturnEmptyDropdown() {
        ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection());

        assertNull(caseData.getSelectReferral());
    }

    @Test
    void whenCalledWithOneReferral_ReturnOneDropdownItem() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection()));
        assertEquals(1, caseData.getSelectReferral().getListItems().size());
    }

    @Test
    void whenCalledWithMultipleReferrals_ReturnMultipleDropdownItems() {
        ReferralTypeItem referralTypeItem = createReferralTypeItem();
        caseData.setReferralCollection(List.of(referralTypeItem, referralTypeItem, referralTypeItem));
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection()));
        assertEquals(3, caseData.getSelectReferral().getListItems().size());
    }

    @Test
    void isJudge() {
        assertEquals(TRUE, ReferralHelper.isJudge(List.of(JUDGE_ROLE_ENG)));
        assertEquals(TRUE, ReferralHelper.isJudge(List.of(JUDGE_ROLE_SCOT)));
        assertEquals(FALSE, ReferralHelper.isJudge(List.of()));
    }

    @Test
    void populateHearingReferralDetails_SingleReply() {
        String replyDateTime = Helper.getCurrentDateTime();
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        ReferralType referral = new ReferralType();
        referral.setReferralReplyCollection(List.of(createReferralReplyTypeItem("1", replyDateTime)));
        referral.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        referralTypeItem.setValue(referral);
        caseData.setReferralCollection(List.of(referralTypeItem));
        caseData.setConciliationTrack(CONCILIATION_TRACK_NO_CONCILIATION);

        String markdown = ReferralHelper.populateHearingReferralDetails(caseData);
        assertTrue(markdown.contains("|Reply 1||"));
        assertTrue(markdown.contains("|<pre>Reply by|replyBy|"));
    }

    @Test
    void populateHearingReferralDetails_SingleReply_MultipleCaseType() {
        String replyDateTime = Helper.getCurrentDateTime();

        ReferralType referral = new ReferralType();
        referral.setReferralReplyCollection(List.of(createReferralReplyTypeItem("1", replyDateTime)));
        referral.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        referralTypeItem.setValue(referral);

        MultipleData multipleCase = new MultipleData();
        multipleCase.setReferralCollection(List.of(referralTypeItem));
        multipleCase.setSelectReferral(new DynamicFixedListType("1"));
        caseData.setConciliationTrack(CONCILIATION_TRACK_NO_CONCILIATION);

        String markdown = ReferralHelper.populateHearingReferralDetails(multipleCase, caseData);
        assertTrue(markdown.contains("|Reply 1||"));
        assertTrue(markdown.contains("|<pre>Reply by|replyBy|"));
    }

    @Test
    void populateHearingReferralDetails_MultipleReplies() {
        String replyDateTime = Helper.getCurrentDateTime();
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        ReferralType referral = new ReferralType();
        referral.setReferralReplyCollection(List.of(
                createReferralReplyTypeItem("1", replyDateTime),
                createReferralReplyTypeItem("2", replyDateTime)));
        referral.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        referralTypeItem.setValue(referral);
        caseData.setReferralCollection(List.of(referralTypeItem));
        caseData.setConciliationTrack(CONCILIATION_TRACK_FAST_TRACK);

        String markdown = ReferralHelper.populateHearingReferralDetails(caseData);
        assertTrue(markdown.contains("|Reply 1||"));
        assertTrue(markdown.contains("|Reply 2||"));
    }

    @Test
    void populateUpdateDetails() {
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        ReferralType referral = new ReferralType();
        referral.setUpdateReferralCollection(ListTypeItem.from(createUpdateReferralType()));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        referralTypeItem.setValue(referral);
        caseData.setReferralCollection(List.of(referralTypeItem));
        String markdown = ReferralHelper.populateHearingReferralDetails(caseData);
        assertTrue(markdown.contains("|Update 1||"));
        assertTrue(markdown.contains("|Updated by|FullName|"));
    }

    @Test
    void populateHearingReferralDetails_MultipleReplies_MultipleCaseType() {
        String replyDateTime = Helper.getCurrentDateTime();

        ReferralType referral = new ReferralType();
        referral.setReferralReplyCollection(List.of(
                createReferralReplyTypeItem("1", replyDateTime),
                createReferralReplyTypeItem("2", replyDateTime)));
        referral.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        referralTypeItem.setValue(referral);

        MultipleData multipleCase = new MultipleData();
        multipleCase.setReferralCollection(List.of(referralTypeItem));
        multipleCase.setSelectReferral(new DynamicFixedListType("1"));

        caseData.setConciliationTrack(CONCILIATION_TRACK_FAST_TRACK);

        String markdown = ReferralHelper.populateHearingReferralDetails(multipleCase, caseData);
        assertTrue(markdown.contains("|Reply 1||"));
        assertTrue(markdown.contains("|Reply 2||"));
    }

    @Test
    void populateUpdateReferralDetails() {
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        ReferralType referral = caseData.getReferralCollection().get(0).getValue();
        referral.setReferCaseTo("Judge");
        referral.setReferralSubject("Subject");
        referral.setReferralDetails("Details");
        referral.setIsUrgent("Yes");
        referral.setReferralInstruction("Instruction");
        referral.setReferralSubjectSpecify("Subject Specify");
        ReferralHelper.populateUpdateReferralDetails(caseData);
        assertEquals("Judge", caseData.getUpdateReferCaseTo());
        assertEquals("Subject", caseData.getUpdateReferralSubject());
        assertEquals("Details", caseData.getUpdateReferralDetails());
        assertEquals("Yes", caseData.getUpdateIsUrgent());
        assertEquals("Instruction", caseData.getUpdateReferralInstruction());
        assertEquals("Subject Specify", caseData.getUpdateReferralSubjectSpecify());
    }

    @ParameterizedTest
    @MethodSource("partyNotCompliedCorrectly")
    void handlePartyNotCompliedCorrectly(String subject, String expected) {
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        ReferralType referral = caseData.getReferralCollection().get(0).getValue();
        referral.setReferralSubject(subject);
        ReferralHelper.populateUpdateReferralDetails(caseData);
        assertEquals(expected, caseData.getUpdateReferralSubject());
    }

    public static Stream<Arguments> partyNotCompliedCorrectly() {
        return Stream.of(
                Arguments.of(PARTY_NOT_RESPONDED_COMPLIED, PARTY_NOT_RESPONDED_COMPILED),
                Arguments.of(PARTY_NOT_RESPONDED_COMPILED, PARTY_NOT_RESPONDED_COMPILED),
                Arguments.of("Other", "Other"),
                Arguments.of("ET1", "ET1")
        );
    }

    @Test
    void updateReferral() {
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setUpdateReferCaseTo("Judge");
        caseData.setUpdateReferralSubject("Subject");
        caseData.setUpdateReferralDetails("Details");
        caseData.setUpdateIsUrgent("Yes");
        caseData.setUpdateReferralInstruction("Instruction");
        caseData.setUpdateReferralSubjectSpecify("Subject Specify");
        ReferralHelper.updateReferral(caseData, "FullName", "None");
        ReferralType referral = caseData.getReferralCollection().get(0).getValue();
        UpdateReferralType updateReferralType = referral.getUpdateReferralCollection().get(0).getValue();
        assertEquals("Judge", updateReferralType.getUpdateReferCaseTo());
        assertEquals("Subject", updateReferralType.getUpdateReferralSubject());
        assertEquals("Details", updateReferralType.getUpdateReferralDetails());
        assertEquals("Yes", updateReferralType.getUpdateIsUrgent());
        assertEquals("Instruction", updateReferralType.getUpdateReferralInstruction());
        assertEquals("Subject Specify", updateReferralType.getUpdateReferralSubjectSpecify());
    }

    @Test
    void clearReferralReplyDataFromCaseData() {
        setSelectReferralData();
        setReferralReplyData();

        ReferralHelper.clearReferralReplyDataFromCaseData(caseData);
        assertNull(caseData.getSelectReferral());
        assertNull(caseData.getHearingAndReferralDetails());
        assertNull(caseData.getDirectionTo());
        assertNull(caseData.getReplyToEmailAddress());
        assertNull(caseData.getIsUrgentReply());
        assertNull(caseData.getDirectionDetails());
        assertNull(caseData.getReplyGeneralNotes());
        assertNull(caseData.getReplyTo());
        assertNull(caseData.getReplyDetails());
    }

    @Test
    void createReferralReply() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        DynamicFixedListType selectReferralList =
            ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);
        setReferralReplyData();

        ReferralHelper.createReferralReply(caseData, "Judge Alex", true);

        ReferralReplyType testReply = caseData.getReferralCollection()
                .get(0).getValue().getReferralReplyCollection()
                .get(0).getValue();

        assertEquals("directionTo", testReply.getDirectionTo());
        assertEquals("replyTo", testReply.getReplyToEmailAddress());
        assertEquals("isUrgent", testReply.getIsUrgentReply());
        assertEquals("directionDetails", testReply.getDirectionDetails());
        assertEquals("generalNotes", testReply.getReplyGeneralNotes());
        assertEquals("Judge Alex", testReply.getReplyBy());
        assertEquals("Other", testReply.getReferralSubject());

        LocalDate replyDate = LocalDate.parse(testReply.getReplyDate(), REFERRAL_DATE_PATTERN);
        assertEquals(LocalDate.now(), replyDate);

        LocalDateTime replyDateTime = LocalDateTime.parse(testReply.getReplyDateTime(), OLD_DATE_TIME_PATTERN);
        assertTrue(LocalDateTime.now().isAfter(replyDateTime));
    }

    @Test
    void clearCloseReferralDataFromCaseData() {
        caseData.setSelectReferral(new DynamicFixedListType());
        caseData.setCloseReferralHearingDetails("hearingDetails");
        caseData.setConfirmCloseReferral(new ArrayList<>());
        caseData.setCloseReferralGeneralNotes("generalNotes");

        ReferralHelper.clearCloseReferralDataFromCaseData(caseData);

        assertNull(caseData.getSelectReferral());
        assertNull(caseData.getCloseReferralHearingDetails());
        assertNull(caseData.getConfirmCloseReferral());
        assertNull(caseData.getCloseReferralGeneralNotes());
    }

    @Test
    void setReferralStatusToClosed() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        DynamicFixedListType selectReferralList =
                ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);

        ReferralHelper.setReferralStatusToClosed(caseData);
        assertEquals(ReferralStatus.CLOSED, caseData.getReferralCollection().get(0).getValue().getReferralStatus());
    }

    @Test
    void validateEmail() {
        assertTrue(ReferralHelper.validateEmail("valid.email@example.com").isEmpty());
        assertThat(ReferralHelper.validateEmail("invalid.email.example")).contains(INVALID_EMAIL_ERROR_MESSAGE);
        assertThat(ReferralHelper.validateEmail("invalid.email@")).contains(INVALID_EMAIL_ERROR_MESSAGE);
        assertThat(ReferralHelper.validateEmail("@example")).contains(INVALID_EMAIL_ERROR_MESSAGE);
        assertThat(ReferralHelper.validateEmail("invalid@example")).contains(INVALID_EMAIL_ERROR_MESSAGE);
        assertThat(ReferralHelper.validateEmail("invalid @example")).contains(INVALID_EMAIL_ERROR_MESSAGE);
        assertThat(ReferralHelper.validateEmail("invalid@example com")).contains(INVALID_EMAIL_ERROR_MESSAGE);
    }

    @Test
    void buildPersonalisation() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setEthosCaseReference("caseRef");
        caseData.setClaimant("claimant");
        caseData.setIsUrgent("Yes");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));
        caseData.setReferralSubject("ET1");

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("123");
        caseDetails.setCaseData(caseData);

        Map<String, String> actual = ReferralHelper.buildPersonalisation(
                caseDetails.getCaseData(), "1", true, "First Last", "linkToExui"
        );

        assertEquals(getExpectedPersonalisation(), actual);
    }

    @Test
    void buildPersonalisation_multiple() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setClaimant("claimant");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("123");
        caseDetails.setCaseData(caseData);

        MultipleData multipleData = new MultipleData();
        multipleData.setMultipleReference("caseRef");
        multipleData.setReferralCollection(caseData.getReferralCollection());
        multipleData.setIsUrgent("Yes");
        multipleData.setReferralSubject("ET1");

        Map<String, String> actual = ReferralHelper.buildPersonalisation(
                multipleData, caseDetails.getCaseData(), "1", true, "First Last", "linkToExui"
        );

        assertEquals(getExpectedPersonalisation(), actual);
    }

    @Test
    void buildPersonalisation_multipleReply() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setClaimant("claimant");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("123");
        caseDetails.setCaseData(caseData);

        MultipleData multipleData = new MultipleData();
        multipleData.setMultipleReference("caseRef");
        multipleData.setReferralCollection(caseData.getReferralCollection());
        multipleData.setIsUrgentReply("Yes");
        multipleData.setReferralSubject("ET1");
        multipleData.setSelectReferral(new DynamicFixedListType("1"));
        multipleData.getReferralCollection().get(0).getValue().setReferralSubject("ET1");

        Map<String, String> actual = ReferralHelper.buildPersonalisation(
                multipleData, caseDetails.getCaseData(), "1", false, "First Last", "linkToExui"
        );

        var expected = getExpectedPersonalisation();
        expected.put("body", "You have a reply to a referral on this case.");
        expected.put("replyReferral", "Reply by");
        assertEquals(expected, actual);
    }

    @Test
    void documentRequestNewReferral() throws JsonProcessingException {
        setReferralReplyData();
        caseData.setReferentEmail("info@test.com");

        String expectedDocumentSummaryNew = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-EGW-ENG-00067."
            + "docx\",\"outputName\":\"Referral Summary.pdf\",\"data\":{\"referralStatus\":\"Awaiting instructions\","
            + "\"caseNumber\":null,\"referralDate\":\"" + Helper.getCurrentDate()
            + "\",\"referredBy\":null,\"referCaseTo\":null,"
            + "\"referentEmail\":\"info@test.com\",\"isUrgent\":null,\"nextHearingDate\":\"11 Nov 2030\","
            + "\"referralSubject\":null,\"referralDetails\":null,"
            + "\"referralDocument\":[{\"id\":\"1\",\"value\":{\"typeOfDocument\":null,"
            + "\"uploadedDocument\":{\"document_binary_url\":\"binaryUrl/documents/\","
            + "\"document_filename\":\"testFileName\",\"document_url\":null,\"category_id\":null,"
            + "\"upload_timestamp\":null},\"ownerDocument\":null,"
            + "\"creationDate\":null,\"shortDescription\":null,\"topLevelDocuments\":null,\""
            + "startingClaimDocuments\":null,\"responseClaimDocuments\":null,\""
            + "initialConsiderationDocuments\":null,\"caseManagementDocuments\":null,\""
            + "withdrawalSettledDocuments\":null,\"hearingsDocuments\":null,\"judgmentAndReasonsDocuments\":null,\""
            + "reconsiderationDocuments\":null,\"miscDocuments\":null,\"documentType\":null,\""
            + "dateOfCorrespondence\":null,\"docNumber\":null,\"tornadoEmbeddedPdfUrl\":null,\"excludeFromDcf\":null,"
            + "\"documentIndex\":null}},{\"id\":\"2\",\"value\":{\"typeOfDocument\":null,"
            + "\"uploadedDocument\":{\"document_binary_url\":\"binaryUrl/documents/\","
            + "\"document_filename\":\"testFileName\",\"document_url\":null,\"category_id\":null,\"upload_timestamp\""
            + ":null},\"ownerDocument\":null,"
            + "\"creationDate\":null,\"shortDescription\":null,\"topLevelDocuments\":null,\""
            + "startingClaimDocuments\":null,\"responseClaimDocuments\":null,\"initialConsiderationDocuments\":null"
            + ",\"caseManagementDocuments\":null,\"withdrawalSettledDocuments\":null,\"hearingsDocuments\":null,\""
            + "judgmentAndReasonsDocuments\":null,\"reconsiderationDocuments\":null,\"miscDocuments\":null,\""
            + "documentType\":null,\"dateOfCorrespondence\":null,\"docNumber\":null,\"tornadoEmbeddedPdfUrl\":null,"
            + "\"excludeFromDcf\":null,\"documentIndex\":null}}],"
            + "\"referralInstruction\":null,\"referralReplyCollection\":null,\"updateReferralCollection\":null}}";

        String result = ReferralHelper.getDocumentRequest(caseData, "key");
        assertEquals(expectedDocumentSummaryNew, result);
    }

    @Test
    void documentRequestNewReferralOnMultiple() throws JsonProcessingException {
        setReferralReplyData();
        caseData.setReferentEmail("info@test.com");
        caseData.setMultipleReference("123456");

        String expectedDocumentSummaryNew = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-EGW-ENG-00067."
            + "docx\",\"outputName\":\"Referral Summary.pdf\",\"data\":{\"referralStatus\":\"Awaiting instructions\","
            + "\"caseNumber\":\"123456\",\"referralDate\":\"" + Helper.getCurrentDate()
            + "\",\"referredBy\":null,\"referCaseTo\":null,"
            + "\"referentEmail\":\"info@test.com\",\"isUrgent\":null,\"nextHearingDate\":\"11 Nov 2030\","
            + "\"referralSubject\":null,\"referralDetails\":null,"
            + "\"referralDocument\":[{\"id\":\"1\",\"value\":{\"typeOfDocument\":null,"
            + "\"uploadedDocument\":{\"document_binary_url\":\"binaryUrl/documents/\","
            + "\"document_filename\":\"testFileName\",\"document_url\":null,\"category_id\":null,"
            + "\"upload_timestamp\":null},\"ownerDocument\":null,"
            + "\"creationDate\":null,\"shortDescription\":null,\"topLevelDocuments\":null,\""
            + "startingClaimDocuments\":null,\"responseClaimDocuments\":null,\""
            + "initialConsiderationDocuments\":null,\"caseManagementDocuments\":null,\""
            + "withdrawalSettledDocuments\":null,\"hearingsDocuments\":null,\"judgmentAndReasonsDocuments\":null,\""
            + "reconsiderationDocuments\":null,\"miscDocuments\":null,\"documentType\":null,\""
            + "dateOfCorrespondence\":null,\"docNumber\":null,\"tornadoEmbeddedPdfUrl\":null,\"excludeFromDcf\":null,"
            + "\"documentIndex\":null}},{\"id\":\"2\",\"value\":{\"typeOfDocument\":null,"
            + "\"uploadedDocument\":{\"document_binary_url\":\"binaryUrl/documents/\","
            + "\"document_filename\":\"testFileName\",\"document_url\":null,\"category_id\":null,\"upload_timestamp\""
            + ":null},\"ownerDocument\":null,"
            + "\"creationDate\":null,\"shortDescription\":null,\"topLevelDocuments\":null,\""
            + "startingClaimDocuments\":null,\"responseClaimDocuments\":null,\"initialConsiderationDocuments\":null"
            + ",\"caseManagementDocuments\":null,\"withdrawalSettledDocuments\":null,\"hearingsDocuments\":null,\""
            + "judgmentAndReasonsDocuments\":null,\"reconsiderationDocuments\":null,\"miscDocuments\":null,\""
            + "documentType\":null,\"dateOfCorrespondence\":null,\"docNumber\":null,\"tornadoEmbeddedPdfUrl\":null,"
            + "\"excludeFromDcf\":null,\"documentIndex\":null}}],"
            + "\"referralInstruction\":null,\"referralReplyCollection\":null,\"updateReferralCollection\":null}}";

        String result = ReferralHelper.getDocumentRequest(caseData, "key");
        assertEquals(expectedDocumentSummaryNew, result);
    }

    @Test
    void documentRequestExistingReferral() throws JsonProcessingException {
        ReferralType referralType = createReferralTypeItem().getValue();
        String replyDateTime = Helper.getCurrentDateTime();
        referralType.setReferralReplyCollection(List.of(createReferralReplyTypeItem("1", replyDateTime)));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setValue(referralType);
        caseData.setReferralCollection(List.of(referralTypeItem));

        DynamicFixedListType selectReferralList =
            ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);
        String replyDate = referralType.getReferralDate();

        String expectedDocumentSummaryExisting = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-EGW-ENG-00067."
            + "docx\",\"outputName\":\"Referral Summary.pdf\",\"data\":{\"referralStatus\":\"Awaiting instructions\","
            + "\"caseNumber\":null,\"referralDate\":" + replyDate
            + ",\"referredBy\":null,\"referCaseTo\":null,"
            + "\"referentEmail\":null,\"isUrgent\":null,\"nextHearingDate\":\"11 Nov 2030\","
            + "\"referralSubject\":\"Other\",\"referralDetails\":null,"
            + "\"referralDocument\":null,\"referralInstruction\":null,\"referralReplyCollection\":[{\"id\":\"1\","
            + "\"value\":{\"directionTo\":\"directionTo\","
            + "\"replyToEmailAddress\":\"replyToEmail\",\"isUrgentReply\":\"isUrgent\","
            + "\"directionDetails\":\"details\",\"replyDocument\":[{\"id\":\"1\",\"value\":{\"typeOfDocument\":null,"
            + "\"uploadedDocument\":{\"document_binary_url\":\"binaryUrl/documents/\","
            + "\"document_filename\":\"testFileName\",\"document_url\":null,\"category_id\":null,\"upload_timestamp\""
            + ":null},\"ownerDocument\":null,"
            + "\"creationDate\":null,\"shortDescription\":null,\"topLevelDocuments\":null,\""
            + "startingClaimDocuments\":null,\"responseClaimDocuments\":null,\"initialConsiderationDocuments\":null,"
            + "\"caseManagementDocuments\":null,\"withdrawalSettledDocuments\":null,\"hearingsDocuments\":null,\""
            + "judgmentAndReasonsDocuments\":null,\"reconsiderationDocuments\":null,\"miscDocuments\":null,\""
            + "documentType\":null,\"dateOfCorrespondence\":null,\"docNumber\":null,\"tornadoEmbeddedPdfUrl\":null,"
            + "\"excludeFromDcf\":null,\"documentIndex\":null}}],"
            + "\"replyGeneralNotes\":\"replyNotes\",\"replyBy\":"
            + "\"replyBy\",\"replyDate\":\"" + Helper.getCurrentDate() + "\",\"replyDateTime\":\"" + replyDateTime
            + "\",\"referralSubject\":\"Other\",\"referralNumber\":\"1\"}}],\"updateReferralCollection\":null}}";

        String result = ReferralHelper.getDocumentRequest(caseData, "key");
        assertEquals(expectedDocumentSummaryExisting, result);
    }

    @Test
    void addErrorDocumentUpload() {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        DocumentType documentType = new DocumentType();
        documentType.setShortDescription("shortDescription");
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(documentType);
        caseData = new CaseData();
        caseData.setReferralDocument(List.of(documentTypeItem));
        List<String> errors = new ArrayList<>();
        ReferralHelper.addDocumentUploadErrors(caseData.getReferralDocument(), errors);
        assertEquals("Short description is added but document is not uploaded.", errors.get(0));
    }

    private Map<String, String> getExpectedPersonalisation() {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", "caseRef");
        personalisation.put("emailFlag", "URGENT");
        personalisation.put("claimant", "claimant");
        personalisation.put("respondents", "Andrew Smith");
        personalisation.put("date", "11 Nov 2030");
        personalisation.put("body", "You have a new referral on this case.");
        personalisation.put("refNumber", "1");
        personalisation.put("subject", "ET1");
        personalisation.put("username", "First Last");
        personalisation.put("replyReferral", "Referred by");
        personalisation.put("linkToExUI", "linkToExui");
        return personalisation;
    }

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Andrew Smith");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }

    private void setSelectReferralData() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        DynamicFixedListType selectReferralList =
                ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);
    }

    private void setReferralReplyData() {
        caseData.setHearingAndReferralDetails("hearingDetails");
        caseData.setDirectionTo("directionTo");
        caseData.setReplyToEmailAddress("replyTo");
        caseData.setIsUrgentReply("isUrgent");
        caseData.setDirectionDetails("directionDetails");
        caseData.setReplyGeneralNotes("generalNotes");
        caseData.setReplyTo("replyTo");
        caseData.setReplyDetails("replyDetails");
    }

    private ReferralTypeItem createReferralTypeItem() {
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        referralType.setReferralSubject("Other");
        referralTypeItem.setValue(referralType);
        referralType.setReferralStatus(ReferralStatus.AWAITING_INSTRUCTIONS);
        return referralTypeItem;
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

    private ReferralReplyTypeItem createReferralReplyTypeItem(String id, String replyDateTime) {
        ReferralReplyType referralReplyType = new ReferralReplyType();
        referralReplyType.setReplyDate(Helper.getCurrentDate());
        referralReplyType.setReplyBy("replyBy");
        referralReplyType.setReplyGeneralNotes("replyNotes");
        referralReplyType.setIsUrgentReply("isUrgent");
        referralReplyType.setDirectionDetails("details");
        referralReplyType.setReplyToEmailAddress("replyToEmail");
        referralReplyType.setDirectionTo("directionTo");
        referralReplyType.setReplyDocument(List.of(createDocumentType("1")));
        referralReplyType.setReplyDateTime(replyDateTime);
        referralReplyType.setReferralSubject("Other");
        referralReplyType.setReferralNumber("1");

        ReferralReplyTypeItem referralReplyTypeItem = new ReferralReplyTypeItem();
        referralReplyTypeItem.setId(id);
        referralReplyTypeItem.setValue(referralReplyType);

        return referralReplyTypeItem;
    }

    private UpdateReferralType createUpdateReferralType() {
        UpdateReferralType updateReferralType = new UpdateReferralType();
        updateReferralType.setUpdateReferCaseTo("Judge");
        updateReferralType.setUpdateReferralSubject("Subject");
        updateReferralType.setUpdateReferralDetails("Details");
        updateReferralType.setUpdateIsUrgent("Yes");
        updateReferralType.setUpdateReferralInstruction("Instruction");
        updateReferralType.setUpdateReferralSubjectSpecify("Subject Specify");
        updateReferralType.setUpdateReferredBy("FullName");
        updateReferralType.setUpdateReferralDate(Helper.getCurrentDate());
        return updateReferralType;
    }

    private static void addHearingToCaseData(CaseData caseData) {
        HearingType hearingType = new HearingType();
        hearingType.setHearingNumber("1");
        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingVenueDay(DynamicFixedListType.of(DynamicValueType.create("Manchester", "Manchester")));
        dateListedType.setListedDate("2030-11-11T00:00:00.000");
        dateListedType.setHearingStatus("Listed");
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        caseData.setHearingCollection(List.of(hearingTypeItem));
    }

    @Test
    void addReferralDocumentToDocumentCollection() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        UploadedDocumentType doc = UploadedDocumentType.builder().documentFilename("fileName").documentUrl("url")
                .documentBinaryUrl("binaryUrl").build();
        caseData.getReferralCollection().get(0).getValue().setReferralSummaryPdf(doc);
        DynamicFixedListType selectReferralList =
            ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);
        ReferralHelper.addReferralDocumentToDocumentCollection(caseData);
        assertEquals(1, caseData.getDocumentCollection().size());

    }

    @ParameterizedTest
    @MethodSource("provideReferralSubject")
    void updateReferralSubject(String initialSubject, String expectedSubject) {
        assertEquals(expectedSubject, ReferralHelper.setReferralSubject(initialSubject));
    }

    private static Stream<Arguments> provideReferralSubject() {
        return Stream.of(
                Arguments.of(PARTY_NOT_RESPONDED_COMPILED, PARTY_NOT_RESPONDED_COMPLIED),
                Arguments.of("Rule 50 application", "Rule 49 Application"),
                Arguments.of("Rule 21", "Rule 22"),
                Arguments.of("Other", "Other"),
                Arguments.of("ET1", "ET1")
        );
    }
}
