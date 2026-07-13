package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.COMPANY_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;

public class HelperTest {

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails4;
    private CaseDetails caseDetailsScot1;
    private CaseDetails caseDetailsScot2;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
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
        return new TokenResponse("abcefg", "28799", "pqrst", "hijklmno",
                "openid profile roles", "Bearer");
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws URISyntaxException, IOException {
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
    void isClaimantNonSystemUserTest() {
        CaseData caseData = new CaseData();
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(null);
        assertTrue(isClaimantNonSystemUser(caseData));

        caseData.setEt1OnlineSubmission("Yes");
        caseData.setHubLinksStatuses(null);
        assertFalse(isClaimantNonSystemUser(caseData));

        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        assertFalse(isClaimantNonSystemUser(caseData));

        // System user but migrated from ECM so not a system user
        caseData.setEt1OnlineSubmission(YES);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        caseData.setMigratedFromEcm(YES);
        assertTrue(isClaimantNonSystemUser(caseData));
    }

    @Test
    void isRespondentNonSystemUser() {
        CaseData caseData = new CaseData();

        caseData.setRepCollection(new ArrayList<>());
        boolean actual = Helper.isRespondentSystemUser(caseData);
        assertFalse(actual);

        RespondentSumType typeRespondent = new RespondentSumType();
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId("123");
        respondentSumTypeItem.setValue(typeRespondent);
        caseData.setRespondentCollection(Collections.singletonList(respondentSumTypeItem));

        RepresentedTypeR typeR = new RepresentedTypeR();
        typeR.setRespondentId("123");
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

        typeR.setMyHmctsYesNo(NO);
        typeRespondent.setIdamId("456");
        boolean actual5 = Helper.isRespondentSystemUser(caseData);
        assertTrue(actual5);

        caseData.setRepCollection(null);
        boolean actual6 = Helper.isRespondentSystemUser(caseData);
        assertTrue(actual6);
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

    @ParameterizedTest
    @MethodSource("isClaimantRepresentedByMyHmctsOrganisationParameter")
    void isClaimantRepresentedByMyHmctsOrganisation(String caseSource, String claimantRepresentedQuestion,
                                                    RepresentedTypeC representedTypeC, boolean expected) {
        CaseData caseData = new CaseData();
        caseData.setCaseSource(caseSource);
        caseData.setClaimantRepresentedQuestion(claimantRepresentedQuestion);
        caseData.setRepresentativeClaimantType(representedTypeC);
        assertEquals(expected, Helper.isClaimantRepresentedByMyHmctsOrganisation(caseData));
    }

    private static Stream<Arguments> isClaimantRepresentedByMyHmctsOrganisationParameter() {
        Organisation organisation = Organisation.builder()
                .organisationID("dummyId")
                .build();
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setMyHmctsOrganisation(organisation);
        return Stream.of(
            Arguments.of("ET1", NO, null, false),
            Arguments.of("ET1", NO, representedTypeC, false),
            Arguments.of("ET1", YES, null, false),
            Arguments.of("ET1", YES, representedTypeC, true),
            Arguments.of("MyHMCTS", NO, null, false),
            Arguments.of("MyHMCTS", NO, representedTypeC, false),
            Arguments.of("MyHMCTS", YES, null, false),
            Arguments.of("MyHMCTS", YES, representedTypeC, true)
        );
    }

    @Test
    void trims_spaces_from_claimant_company_name() {
        CaseData caseData = new CaseData();
        caseData.setClaimantTypeOfClaimant(COMPANY_TYPE_CLAIMANT);
        caseData.setClaimantCompany("  Company Name  ");

        Helper.removeSpacesFromPartyNames(caseData);

        assertEquals("Company Name", caseData.getClaimantCompany());
    }

    @Test
    void trims_spaces_from_claimant_individual_names() {
        CaseData caseData = new CaseData();
        caseData.setClaimantTypeOfClaimant(INDIVIDUAL_TYPE_CLAIMANT);
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames("  First  ");
        claimantIndType.setClaimantLastName("  Last  ");
        caseData.setClaimantIndType(claimantIndType);

        Helper.removeSpacesFromPartyNames(caseData);

        assertEquals("First", caseData.getClaimantIndType().getClaimantFirstNames());
        assertEquals("Last", caseData.getClaimantIndType().getClaimantLastName());
    }

    @Test
    void trims_spaces_from_representative_claimant_name() {
        CaseData caseData = new CaseData();
        RepresentedTypeC representativeClaimantType = new RepresentedTypeC();
        representativeClaimantType.setNameOfRepresentative("  Representative Name  ");
        caseData.setRepresentativeClaimantType(representativeClaimantType);

        Helper.removeSpacesFromPartyNames(caseData);

        assertEquals("Representative Name", caseData.getRepresentativeClaimantType().getNameOfRepresentative());
    }

    @Test
     void handles_null_claimant_individual_names() {
        CaseData caseData = new CaseData();
        caseData.setClaimantTypeOfClaimant(INDIVIDUAL_TYPE_CLAIMANT);
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames(null);
        claimantIndType.setClaimantLastName(null);
        caseData.setClaimantIndType(claimantIndType);

        assertDoesNotThrow(() -> Helper.removeSpacesFromPartyNames(caseData));
        assertNull(caseData.getClaimantIndType().getClaimantFirstNames());
        assertNull(caseData.getClaimantIndType().getClaimantLastName());
    }

    @Test
    void trims_spaces_from_respondent_names() {
        RespondentSumType respondent1Value = new RespondentSumType();
        respondent1Value.setRespondentName("Respondent 1  ");
        RespondentSumTypeItem respondent1 = new RespondentSumTypeItem();
        respondent1.setValue(respondent1Value);

        RespondentSumType respondent2Value = new RespondentSumType();
        respondent2Value.setRespondentName("  Respondent 2");
        RespondentSumTypeItem respondent2 = new RespondentSumTypeItem();
        respondent2.setValue(respondent2Value);

        CaseData caseData = new CaseData();
        caseData.setClaimantTypeOfClaimant(COMPANY_TYPE_CLAIMANT);
        caseData.setClaimantCompany("  Company Name  ");
        caseData.setRespondentCollection(List.of(respondent1, respondent2));

        Helper.removeSpacesFromPartyNames(caseData);

        assertEquals("Company Name", caseData.getClaimantCompany());
        assertEquals("Respondent 1", caseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals("Respondent 2", caseData.getRespondentCollection().get(1).getValue().getRespondentName());
    }

    @Test
    void trims_spaces_from_representative_names() {
        RepresentedTypeR representedTypeR = new RepresentedTypeR();
        representedTypeR.setNameOfRepresentative("  Representative Name  ");
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setValue(representedTypeR);

        CaseData caseData = new CaseData();
        caseData.setRepCollection(List.of(representedTypeRItem));

        Helper.removeSpacesFromPartyNames(caseData);

        assertEquals("Representative Name", caseData.getRepCollection().getFirst().getValue()
                .getNameOfRepresentative());
    }
}
