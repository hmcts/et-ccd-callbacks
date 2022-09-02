package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;

@SuppressWarnings({"PMD.SingularField", "PMD.TooManyMethods"})
class ReferralHelperTest {

    private UserService userService;
    private CaseData caseData;

    private static final String JUDGE_ROLE_ENG = "caseworker-employment-etjudge-englandwales";
    private static final String JUDGE_ROLE_SCOT = "caseworker-employment-etjudge-scotland";
    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String INVALID_EMAIL_ERROR_MESSAGE = "The email address entered is invalid.";

    private final String expectedSingleHearingDetails = "<hr><h3>Hearing details </h3><pre>Date &nbsp;&#09&#09&#09&#"
        + "09&#09&nbsp; 25 December 2021<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#09&#0"
        + "9&#09&#09 N/A</pre><hr>";

    private final String expectedMultipleHearingDetails = "<hr><h3>Hearing details 1</h3><pre>Date &nbsp;&#09&#09&#0"
        + "9&#09&#09&nbsp; 25 December 2021<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#0"
        + "9&#09&#09&#09 N/A</pre><hr><h3>Hearing details 2</h3><pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; 26 December"
        + " 2021<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 N/A</pre><hr>";

    private final String expectedHearingReferralDetailsSingleReply = "<hr><h3>Hearing details </h3><pre>Date &nbs"
        + "p;&#09&#09&#09&#09&#09&nbsp; 11 November 2030<br><br>Hearing &#09&#09&#09&#09&nbsp; null<br><br>Type &nbsp"
        + ";&nbsp;&#09&#09&#09&#09&#09 N/A</pre><hr><h3>Referral</h3><pre>Referred by &nbsp;&#09&#09&#09&#09&#09&#09&"
        + "#09&#09&#09&nbsp; null<br><br>Referred to &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; null<br><"
        + "br>Email address &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; null<br><br>Urgent &nbsp;&#09&#09&#09&#09&#0"
        + "9&#09&#09&#09&#09&#09&#09&nbsp; null<br><br>Referral date &#09&#09&#09&#09&#09&#09&#09&#09&#09 null<br><br"
        + ">Next hearing date &#09&#09&#09&#09&#09&#09&#09 11 Nov 2030<br><br>Referral subject &#09&#09&#09&#09&#09&#"
        + "09&#09&#09 null<br><br>Details of the referral &#09&#09&#09&#09&#09&#09 null<br><br>Documents &nbsp;&#09&"
        + "#09&#09&#09&#09&#09&#09&#09&#09 <a href=\"/documents/\" download>testFileName</a>&nbsp;<br><br>Documents &n"
        + "bsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 <a href=\"/documents/\" download>testFileName</a>&nbsp;</pre><hr>"
        + "<h3>Reply </h3><pre>Reply by &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 replyBy<br><br>Reply to &"
        + "nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 directionTo<br><br>Email address &nbsp;&#09&#09&#09&#0"
        + "9&#09&#09&#09&#09 replyToEmail<br><br>Urgent &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 isUrgent<b"
        + "r><br>Referral date &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 replyDate<br><br>Hearing date &nbsp;&nbsp"
        + ";&#09&#09&#09&#09&#09&#09&#09&#09 11 Nov 2030<br><br>Referral subject &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&"
        + "#09 null<br><br>Directions &nbsp;&nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 details<br><br>Documents "
        + "&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 <a href=\"/documents/\" download>testFileName</a>&nbsp;<br><br>"
        + "General notes &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 replyNotes</pre><hr>";

    private final String expectedHearingReferralDetailsMultipleReplies = "<hr><h3>Hearing details </h3><pre>Date &"
        + "nbsp;&#09&#09&#09&#09&#09&nbsp; 11 November 2030<br><br>Hearing &#09&#09&#09&#09&nbsp; null<br><br>Type &n"
        + "bsp;&nbsp;&#09&#09&#09&#09&#09 N/A</pre><hr><h3>Referral</h3><pre>Referred by &nbsp;&#09&#09&#09&#09&#09&#"
        + "09&#09&#09&#09&nbsp; null<br><br>Referred to &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; null<b"
        + "r><br>Email address &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; null<br><br>Urgent &nbsp;&#09&#09&#09&#09"
        + "&#09&#09&#09&#09&#09&#09&#09&nbsp; null<br><br>Referral date &#09&#09&#09&#09&#09&#09&#09&#09&#09 null<br>"
        + "<br>Next hearing date &#09&#09&#09&#09&#09&#09&#09 11 Nov 2030<br><br>Referral subject &#09&#09&#09&#09&#0"
        + "9&#09&#09&#09 null<br><br>Details of the referral &#09&#09&#09&#09&#09&#09 null<br><br>Documents &nbsp;&#0"
        + "9&#09&#09&#09&#09&#09&#09&#09&#09 <a href=\"/documents/\" download>testFileName</a>&nbsp;<br><br>Documents"
        + " &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 <a href=\"/documents/\" download>testFileName</a>&nbsp;</pre>"
        + "<hr><h3>Reply 1</h3><pre>Reply by &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 replyBy<br><br>Repl"
        + "y to &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 directionTo<br><br>Email address &nbsp;&#09&#09&"
        + "#09&#09&#09&#09&#09&#09 replyToEmail<br><br>Urgent &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 isUr"
        + "gent<br><br>Referral date &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 replyDate<br><br>Hearing date &nbsp;"
        + "&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 11 Nov 2030<br><br>Referral subject &nbsp;&nbsp;&#09&#09&#09&#09&#0"
        + "9&#09&#09 null<br><br>Directions &nbsp;&nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 details<br><br>Doc"
        + "uments &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 <a href=\"/documents/\" download>testFileName</a>&nbsp;<"
        + "br><br>General notes &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 replyNotes</pre><hr><h3>Reply 2</h3><pre>Repl"
        + "y by &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 replyBy<br><br>Reply to &nbsp;&nbsp;&#09&#09&#09"
        + "&#09&#09&#09&#09&#09&#09&#09 directionTo<br><br>Email address &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 reply"
        + "ToEmail<br><br>Urgent &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 isUrgent<br><br>Referral date &nb"
        + "sp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 replyDate<br><br>Hearing date &nbsp;&nbsp;&#09&#09&#09&#09&#09&#0"
        + "9&#09&#09 11 Nov 2030<br><br>Referral subject &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09 null<br><br>Directi"
        + "ons &nbsp;&nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 details<br><br>Documents &nbsp;&#09&#09&#09&#0"
        + "9&#09&#09&#09&#09&#09 <a href=\"/documents/\" download>testFileName</a>&nbsp;<br><br>General notes &nbsp;&"
        + "#09&#09&#09&#09&#09&#09&#09&#09 replyNotes</pre><hr>";

    private final String expectedCreatedReferralReply = "ReferralReplyType(directionTo=directionTo, replyToEmailAddre"
        + "ss=replyTo, isUrgentReply=isUrgent, directionDetails=directionDetails, replyDocument=null, replyGeneralNot"
        + "es=generalNotes, replyBy=Judge Alex, replyDate=" + Helper.getCurrentDate() + ")";

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Judge");
        userDetails.setLastName("Judy");
        userDetails.setEmail("judge.judy@aol.com");
        userDetails.setRoles(new ArrayList<>(Arrays.asList(JUDGE_ROLE_ENG, JUDGE_ROLE_SCOT)));
        when(userService.getUserDetails("")).thenReturn(userDetails);
        caseData = CaseDataBuilder.builder().build();
        caseData.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        addHearingToCaseData(caseData);
    }

    @Test
    void populateSingleHearingDetails() {
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000",
                HEARING_STATUS_POSTPONED, false)
            .build();

        assertThat(ReferralHelper.populateHearingDetails(caseData))
            .isEqualTo(expectedSingleHearingDetails);
    }

    @Test
    void populateMultipleHearingDetails() {
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000",
                HEARING_STATUS_POSTPONED, false)
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(1, "1", "2021-12-26T00:00:00.000",
                HEARING_STATUS_HEARD, false)
            .build();

        assertThat(ReferralHelper.populateHearingDetails(caseData))
            .isEqualTo(expectedMultipleHearingDetails);
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

        ReferralHelper.createReferral(caseData, "Judge Judy");

        String expected = "ReferralType(referralNumber=1, referralHearingDate=11 Nov 2030, referCaseTo=Judge Judy, re"
            + "ferentE"
            + "mail=judge.judy@aol.com, isUrgent=Yes, referralSubject=Subject line here, referralSubjectSpecify=Cust"
            + "om subject line, referralDetails=This is an explanation, referralDocument=[DocumentTypeItem(id=1, value"
            + "=DocumentType(typeOfDocument=null, uploadedDocument=UploadedDocumentType(documentBinaryUrl=binaryUrl/d"
            + "ocuments/, documentFilename=testFileName, documentUrl=null), ownerDocument=null, creationDate=null, sho"
            + "rtDescription=null)), DocumentTypeItem(id=2, value=DocumentType(typeOfDocument=null, uploadedDocument=U"
            + "ploadedDocumentType(documentBinaryUrl=binaryUrl/documents/, documentFilename=testFileName, documentUrl="
            + "null), ownerDocument=null, creationDate=null, shortDescription=null))], referralInstruction=Custom inst"
            + "ructions for judge, referredBy=Judge Judy, referralDate="
            + Helper.getCurrentDate()
            + ", referralStatus=Awaiting instructions, referralRep"
            + "lyCollection=null)";

        String actual = caseData.getReferralCollection().get(0).getValue().toString();
        assertEquals(expected, actual);
    }

    @Test
    void addNewReferralToReferralCollection() {
        ReferralHelper.createReferral(caseData, "");
        ReferralHelper.createReferral(caseData, "");

        assertEquals(2, caseData.getReferralCollection().size());
    }

    @Test
    void saveTheUserDetailsOfTheReferrerWithTheReferral() {
        ReferralHelper.createReferral(caseData, "Judge Judy");

        String referredBy = caseData.getReferralCollection().get(0).getValue().getReferredBy();

        assertEquals("Judge Judy", referredBy);
    }

    @Test
    void clearReferralInformationFromCaseDataAfterSaving() {
        caseData.setReferCaseTo("Judge Judy");
        caseData.setIsUrgent("Yes");
        caseData.setReferralSubject("Subject line here");
        caseData.setReferralSubjectSpecify("Custom subject line");
        caseData.setReferralDetails("This is an explanation");
        caseData.setReferralInstruction("Custom instructions for judge");

        ReferralHelper.createReferral(caseData, "");

        assertNull(caseData.getReferCaseTo());
        assertNull(caseData.getIsUrgent());
        assertNull(caseData.getReferralSubject());
        assertNull(caseData.getReferralSubjectSpecify());
        assertNull(caseData.getReferralDetails());
        assertNull(caseData.getReferralDocument());
        assertNull(caseData.getReferralInstruction());
    }

    @Test
    void whenCalledWithNoReferrals_ReturnEmptyDropdown() {
        ReferralHelper.populateSelectReferralDropdown(caseData);

        assertNull(caseData.getSelectReferral());
    }

    @Test
    void whenCalledWithOneReferral_ReturnOneDropdownItem() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData));
        assertEquals(1, caseData.getSelectReferral().getListItems().size());
    }

    @Test
    void whenCalledWithMultipleReferrals_ReturnMultipleDropdownItems() {
        ReferralTypeItem referralTypeItem = createReferralTypeItem();
        caseData.setReferralCollection(List.of(referralTypeItem, referralTypeItem, referralTypeItem));
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData));
        assertEquals(3, caseData.getSelectReferral().getListItems().size());
    }

    @Test
    void isJudge() {
        assertEquals(TRUE, ReferralHelper.isJudge(Arrays.asList(JUDGE_ROLE_ENG)));
        assertEquals(TRUE, ReferralHelper.isJudge(Arrays.asList(JUDGE_ROLE_SCOT)));
        assertEquals(FALSE, ReferralHelper.isJudge(Arrays.asList()));
    }

    @Test
    void populateHearingReferralDetails_SingleReply() {
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        ReferralType referral = new ReferralType();
        referral.setReferralReplyCollection(List.of(createReferralReplyTypeItem("1")));
        referral.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        referralTypeItem.setValue(referral);
        caseData.setReferralCollection(List.of(referralTypeItem));

        assertEquals(expectedHearingReferralDetailsSingleReply,
            ReferralHelper.populateHearingReferralDetails(caseData));
    }

    @Test
    void populateHearingReferralDetails_MultipleReplies() {
        caseData.setSelectReferral(new DynamicFixedListType("1"));
        ReferralType referral = new ReferralType();
        referral.setReferralReplyCollection(List.of(createReferralReplyTypeItem("1"),
            createReferralReplyTypeItem("2")));
        referral.setReferralDocument(List.of(createDocumentType("1"), createDocumentType("2")));
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("1");
        referralTypeItem.setValue(referral);
        caseData.setReferralCollection(List.of(referralTypeItem));

        assertEquals(expectedHearingReferralDetailsMultipleReplies,
            ReferralHelper.populateHearingReferralDetails(caseData));
    }

    @Test
    void clearReferralReplyDataFromCaseData() {
        setReferralReplyData();
        ReferralHelper.clearReferralReplyDataFromCaseData(caseData);

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
        DynamicFixedListType selectReferralList = ReferralHelper.populateSelectReferralDropdown(caseData);
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);
        setReferralReplyData();

        ReferralHelper.createReferralReply(caseData, "Judge Alex");

        assertEquals(expectedCreatedReferralReply,
            caseData.getReferralCollection().get(0).getValue()
                .getReferralReplyCollection().get(0).getValue().toString());
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
        DynamicFixedListType selectReferralList = ReferralHelper.populateSelectReferralDropdown(caseData);
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);

        ReferralHelper.setReferralStatusToClosed(caseData);
        assertEquals(ReferralStatus.CLOSED, caseData.getReferralCollection().get(0).getValue().getReferralStatus());
    }

    @Test
    void validateEmail() {
        assertThat(ReferralHelper.validateEmail("valid.email@example.com").contains(null));
        assertThat(ReferralHelper.validateEmail("invalid.email.example").contains(INVALID_EMAIL_ERROR_MESSAGE));
    }

    @Test
    void buildPersonalisation() {
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setEthosCaseReference("caseRef");
        caseData.setClaimant("claimant");
        caseData.setIsUrgent("Yes");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));

        assertEquals(getExpectedPersonalisation(), ReferralHelper.buildPersonalisation(caseData, false, true));
    }

    private Map<String, String> getExpectedPersonalisation() {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", "caseRef");
        personalisation.put("emailFlag", "URGENT");
        personalisation.put("claimant", "claimant");
        personalisation.put("respondents", "Andrew Smith");
        personalisation.put("date", "11 Nov 2030");
        personalisation.put("body", "You have a new message about this employment tribunal case.");
        return personalisation;
    }

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Andrew Smith");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
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

    private ReferralReplyTypeItem createReferralReplyTypeItem(String id) {
        ReferralReplyType referralReplyType = new ReferralReplyType();
        referralReplyType.setReplyDate("replyDate");
        referralReplyType.setReplyBy("replyBy");
        referralReplyType.setReplyGeneralNotes("replyNotes");
        referralReplyType.setIsUrgentReply("isUrgent");
        referralReplyType.setDirectionDetails("details");
        referralReplyType.setReplyToEmailAddress("replyToEmail");
        referralReplyType.setDirectionTo("directionTo");
        referralReplyType.setReplyDocument(List.of(createDocumentType("1")));
        ReferralReplyTypeItem referralReplyTypeItem = new ReferralReplyTypeItem();
        referralReplyTypeItem.setId(id);
        referralReplyTypeItem.setValue(referralReplyType);
        return referralReplyTypeItem;
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

}