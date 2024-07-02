package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenResponse;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class HelperTest {

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails4;
    private CaseDetails caseDetailsScot1;
    private CaseDetails caseDetailsScot2;

    @BeforeEach
    public void setUp() throws Exception {
        caseDetails1 = generateCaseDetails("caseDetailsTest1.json");
        caseDetails4 = generateCaseDetails("caseDetailsTest4.json");
        caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        caseDetailsScot2 = generateCaseDetails("caseDetailsScotTest2.json");
    }

    public static UserDetails getUserDetails() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUid("id");
        userDetails.setEmail("mail@mail.com");
        userDetails.setFirstName("Mike");
        userDetails.setLastName("Jordan");
        userDetails.setRoles(Collections.singletonList("role"));
        return userDetails;
    }

    public static TokenResponse getUserToken() {
        return new TokenResponse("abcefg", "28799", "pqrst", "hijklmno", "openid profile roles", "Bearer");
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void nullCheck() {
        assertEquals("Value ' example ' ", Helper.nullCheck("Value \" example \" "));
        assertEquals("", Helper.nullCheck(null));
        assertEquals("Value example", Helper.nullCheck("Value example"));
        assertEquals("Value ' example '", Helper.nullCheck("Value ' example '"));
    }

    @Test
    void getDocumentName() {
        String expected = "EM-TRB-EGW-ENG-00029_4.2";
        assertEquals(expected, Helper.getDocumentName(caseDetails4.getCaseData().getCorrespondenceType(),
                caseDetails4.getCaseData().getCorrespondenceScotType()));
    }

    @Test
    void getActiveRespondentsAllFound() {
        int activeRespondentsFound = 3;

        List<RespondentSumTypeItem> activeRespondents = Helper.getActiveRespondents(caseDetails1.getCaseData());

        assertEquals(activeRespondentsFound, activeRespondents.size());
    }

    @Test
    void getActiveRespondentsSomeFound() {
        int activeRespondentsFound = 2;

        List<RespondentSumTypeItem> activeRespondents = Helper.getActiveRespondents(caseDetailsScot1.getCaseData());

        assertEquals(activeRespondentsFound, activeRespondents.size());
    }

    @Test
    void getActiveRespondentsNoneFound() {
        int activeRespondentsFound = 0;

        List<RespondentSumTypeItem> activeRespondents = Helper.getActiveRespondents(caseDetailsScot2.getCaseData());

        assertEquals(activeRespondentsFound, activeRespondents.size());
    }

    @Test
    void getCurrentDate() {
        String currentDate = Helper.getCurrentDate();
        Pattern pattern = Pattern.compile("\\d{2} [A-Za-z]{3,4} \\d{4}");
        Matcher matcher = pattern.matcher(currentDate);
        assertTrue(matcher.matches());
    }

    @Test
    void getDocumentMatcher() {
        Matcher matcher = Helper.getDocumentMatcher("testUrl");
        assertEquals("^.+?/documents/", matcher.pattern().toString());
    }

    @Test
    void createLinkForUploadedDocument() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("http://dm-store:8080/documents/1234/binary");
        uploadedDocumentType.setDocumentFilename("testFileName");
        uploadedDocumentType.setDocumentUrl("http://dm-store:8080/documents/1234");
        String expected = "<a href=\"/documents/1234/binary\" target=\"_blank\">testFileName</a>";
        String actual = Helper.createLinkForUploadedDocument(uploadedDocumentType);
        assertEquals(expected, actual);
    }

    @Test
    void isClaimantNonSystemUser() {
        CaseData caseData = new CaseData();
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(null);
        boolean actual = Helper.isClaimantNonSystemUser(caseData);
        assertTrue(actual);

        caseData.setEt1OnlineSubmission("Yes");
        caseData.setHubLinksStatuses(null);
        boolean actual2 = Helper.isClaimantNonSystemUser(caseData);
        assertFalse(actual2);

        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        boolean actual3 = Helper.isClaimantNonSystemUser(caseData);
        assertFalse(actual3);
    }

    @Test
    void isRespondentNonSystemUser() {
        CaseData caseData = new CaseData();

        caseData.setRepCollection(new ArrayList<>());
        boolean actual = Helper.isRespondentSystemUser(caseData);
        assertFalse(actual);

        RepresentedTypeR typeR = new RepresentedTypeR();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setValue(typeR);
        caseData.setRepCollection(Collections.singletonList(representedTypeRItem));
        boolean actual2 = Helper.isRespondentSystemUser(caseData);
        assertFalse(actual2);

        typeR.setMyHmctsYesNo(YES);
        boolean actual3 = Helper.isRespondentSystemUser(caseData);
        assertTrue(actual3);

        typeR.setMyHmctsYesNo(NO);
        boolean actual4 = Helper.isRespondentSystemUser(caseData);
        assertFalse(actual4);
    }

    @Test
    void getLastNullCollection() {
        assertNull(Helper.getLast(null));
    }

    @Test
    void getLastEmptyCollection() {
        assertNull(Helper.getLast(new ArrayList<>()));
    }

    @Test
    void getLastNonEmptyCollection() {
        List<String> list = new ArrayList<>();
        list.add("test");
        list.add("test2");
        assertEquals("test2", Helper.getLast(list));
    }
}
