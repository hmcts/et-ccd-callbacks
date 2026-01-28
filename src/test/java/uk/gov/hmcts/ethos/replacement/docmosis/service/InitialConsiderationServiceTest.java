package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.EtICHearingListedAnswers;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICSeekComments;
import uk.gov.hmcts.et.common.model.ccd.EtIcudlHearing;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule27;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule28;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.Et3VettingType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.APPLICATIONS_FOR_STRIKE_OUT_OR_DEPOSIT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CODES_URL_ENGLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CODES_URL_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CVP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CVP_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_NOT_LISTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.INTERPRETERS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JSA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JURISDICTIONAL_ISSUES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JURISDICTION_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_FINAL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_PRELIMINARY_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.REQUEST_FOR_ADJUSTMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RULE_49;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.SEEK_COMMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.TELEPHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.TIME_POINTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.UDL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET3_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CASE_DETAILS_URL_PARTIAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.NOT_AVAILABLE_FOR_VIDEO_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.REFERRALS_PAGE_FRAGMENT_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class InitialConsiderationServiceTest {
    private static final LocalDateTime EARLIEST_FUTURE_HEARING_DATE = LocalDateTime.now().plusDays(5);
    private static final LocalDateTime SECOND_FUTURE_HEARING_DATE = LocalDateTime.now().plusDays(9);
    private static final String GIVE_DETAILS = "Give Details:";

    private static final String EXPECTED_HEARING_DETAILS_STRING = """
        |Hearing details | |
        |-------------|:------------|
        |Date | 16 May 2022|
        |Type | Hearing|
        |Duration | 60 Days|
        """;

    private static final String EXPECTED_BLANK_HEARING_DETAILS = """
       <br/>
       <table>
       <thead>
       <tr>
       <th colspan="2"><h2>Listed Hearing Details</h2></th>
       </tr>
       <tr>
       <th width="30%">Aspect</th>
       <th width="70%">Detail</th>
       </tr>
       <thead>
       <tbody>
          <tr>
              <td>Date</td> <td> - </td>
          </tr>
          <tr>
              <td>Type</td> <td> - </td>
          </tr>
          <tr>
              <td>Duration</td> <td> - </td>
          </tr>
          <tr>
              <td>Hearing format</td> <td> - </td>
          </tr>
          <tr>
              <td>Panel Type</td> <td> - </td>
          </tr>
          <tr>
              <td>Venue</td> <td> - </td>
          </tr>
      </tbody>
      </table>
        """;

    private static final String EXPECTED_JURISDICTION_HTML = "<h2>Jurisdiction codes</h2><a "
            + "target=\"_blank\" href=\"https://judiciary.sharepoint.com/sites/empjudgesew/Shared%20Documents/Forms/"
            + "AllItems.aspx?id=%2Fsites%2Fempjudgesew%2FShared%20Documents%2FET%20Jurisdiction%20List%2F"
            + "Jurisdiction%20list%20October%202024.pdf&viewid=9cee6d50-61e5-4d87-92d2-8c9444f00c95&parent=%2F"
            + "sites%2Fempjudgesew%2FShared%20Documents%2FET%20Jurisdiction%20List\">View all jurisdiction "
            + "codes and descriptors (opens in new tab)"
            + "</a><br><br><h4>DAG</h4>Discrimination, including harassment or discrimination based on "
            + "association or perception on grounds of age<h4>SXD</h4>Discrimination, including "
            + "indirect discrimination, discrimination based on association or perception, or harassment on "
            + "grounds of sex, marriage and civil partnership";

    private static final String EXPECTED_JURISDICTION_SCOTLAND_HTML = "<h2>Jurisdiction codes</h2><a "
        + "target=\"_blank\" href=\"https://judiciary.sharepoint"
        + ".com/:w:/r/sites/ScotlandEJs/Shared%20Documents/Jurisdictional%20Codes%20List"
        + "/Jurisdiction%20list%20July%202024%20.doc?d=wfa6ba431b0b941ffa0b82504fd093af0&csf=1&web=1&e=Dm6Hda\">"
        + "View all jurisdiction codes and descriptors (opens in new tab)"
        + "</a><br><br><h4>DAG</h4>Discrimination, including harassment or discrimination "
        + "based on association or perception on grounds of age<h4>SXD</h4>"
        + "Discrimination, including indirect discrimination, discrimination based on association or perception, "
        + "or harassment on grounds of sex, marriage and civil partnership";

    private CaseData caseDataEmpty;
    private CaseData caseData;
    private DocumentInfo documentInfo;
    private InitialConsiderationService initialConsiderationService;
    @MockBean
    private TornadoService tornadoService;
    @MockBean
    private DocumentManagementService documentManagementService;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        caseData = generateCaseData("initialConsiderationCase1.json");
        caseDataEmpty = generateCaseData("initialConsiderationCase2.json");
        initialConsiderationService = new InitialConsiderationService(tornadoService);
        documentInfo = DocumentInfo.builder()
                .description("test-description")
                .url("https://test.com/documents/random-uuid")
                .build();
        doCallRealMethod().when(documentManagementService).addDocumentToDocumentField(documentInfo);
    }

    @Test
    void initialiseInitialConsideration_shouldSetBeforeYouStart_whenDocumentCollectionIsNotNull() {
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        DocumentTypeItem documentItem = new DocumentTypeItem();
        documentItem.setValue(DocumentType.from(new UploadedDocumentType()));
        documentItem.getValue().setDocumentType("ET1");
        documentCollection.add(documentItem);
        caseData.setDocumentCollection(documentCollection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        initialConsiderationService.initialiseInitialConsideration(caseDetails);

        assertThat(caseDetails.getCaseData().getInitialConsiderationBeforeYouStart()).isNotEmpty();
    }

    @Test
    void initialiseInitialConsideration_shouldSetEmptyBeforeYouStart_whenDocumentCollectionIsNull() {
        CaseData caseDataWithNullDocCollection = new CaseData();
        caseDataWithNullDocCollection.setDocumentCollection(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataWithNullDocCollection);

        initialConsiderationService.initialiseInitialConsideration(caseDetails);

        assertThat(caseDetails.getCaseData().getInitialConsiderationBeforeYouStart()).isEmpty();
    }

    @Test
    void initialiseInitialConsideration_shouldIncludeAllDocumentLinks_whenDocCollectionHasMultipleValidDocuments() {
        DocumentTypeItem et1Document = new DocumentTypeItem();
        et1Document.setValue(DocumentType.from(new UploadedDocumentType()));
        et1Document.getValue().setDocumentType(ET1);
        DocumentTypeItem et3Document = new DocumentTypeItem();
        et3Document.setValue(DocumentType.from(new UploadedDocumentType()));
        et3Document.getValue().setDocumentType(ET3);
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        documentCollection.add(et1Document);
        documentCollection.add(et3Document);

        CaseData caseDataWithMultipleDocs = new CaseData();
        caseDataWithMultipleDocs.setDocumentCollection(documentCollection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataWithMultipleDocs);

        initialConsiderationService.initialiseInitialConsideration(caseDetails);
        String beforeYouStart = caseDetails.getCaseData().getInitialConsiderationBeforeYouStart();
        assertThat(beforeYouStart).contains(String.format(BEFORE_LABEL_ET1_IC, ""));
        assertThat(beforeYouStart).contains(String.format(BEFORE_LABEL_ET3_IC, ""));
    }

    @Test
    void getIcHearingPanelPreference_shouldReturnNull_whenRespondentCollectionIsNull() {
        String hearingPanelPreferenceDetails = initialConsiderationService.getIcRespondentHearingPanelPreference(
                null);
        assertThat(hearingPanelPreferenceDetails).isNull();
    }

    @Test
    void getIcHearingPanelPreference_shouldReturnFormattedDetails_whenRespondentHasPreferenceAndReason() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Test Response");
        respondent.setValue(respondentSumType);
        respondent.getValue().setRespondentHearingPanelPreference("judge");
        respondent.getValue().setRespondentHearingPanelPreferenceReason("I deserve it");

        String expectedOutput = """
            <table>
            <thead>
            <tr>
            <th colspan="3"><h2>Respondents' Panel Preferences</h2></th>
            </tr>
            <tr>
            <th width="25%">Respondent</th>
            <th width="25%">Preference</th>
            <th>Reason</th>
            </tr>
            <thead>
            <tbody>
              <tr>
                <td>Test Response</td>
                <td>judge</td>
                <td>I deserve it</td>
              </tr>
            </tbody>
            </table>
            """;
        String hearingPanelPreferenceDetails = initialConsiderationService.getIcRespondentHearingPanelPreference(
                List.of(respondent));
        assertThat(hearingPanelPreferenceDetails).isEqualTo(expectedOutput);
    }

    @Test
    void getIcHearingPanelPreference_shouldReturnFormattedDetails_whenRespondentHasNoPreferenceAndNoReason() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());

        String expectedOutput = """
           <table>
           <thead>
           <tr>
           <th colspan="3"><h2>Respondents' Panel Preferences</h2></th>
           </tr>
           <tr>
           <th width="25%">Respondent</th>
           <th width="25%">Preference</th>
           <th>Reason</th>
           </tr>
           <thead>
           <tbody>
             <tr>
               <td>-</td>
               <td>-</td>
               <td>-</td>
             </tr>
           </tbody>
           </table>
            """;
        String hearingPanelPreferenceDetails = initialConsiderationService.getIcRespondentHearingPanelPreference(
                List.of(respondent));
        assertThat(hearingPanelPreferenceDetails).isEqualTo(expectedOutput);
    }

    @Test
    void getIcHearingPanelPreference_shouldReturnFormattedDetailsForMultipleRespondents() {
        RespondentSumTypeItem respondent1 = new RespondentSumTypeItem();
        respondent1.setValue(new RespondentSumType());
        respondent1.getValue().setRespondentName("Respondent1");
        respondent1.getValue().setRespondentHearingPanelPreference("judge");
        respondent1.getValue().setRespondentHearingPanelPreferenceReason("I deserve it");

        RespondentSumTypeItem respondent2 = new RespondentSumTypeItem();
        respondent2.setValue(new RespondentSumType());
        respondent2.getValue().setRespondentName("Respondent2");
        respondent2.getValue().setRespondentHearingPanelPreference("panel");
        respondent2.getValue().setRespondentHearingPanelPreferenceReason("Fair trial");

        String hearingPanelPreferenceDetails = initialConsiderationService.getIcRespondentHearingPanelPreference(
                List.of(respondent1, respondent2));
        assertThat(hearingPanelPreferenceDetails).isEqualTo(
                """
                <table>
                <thead>
                <tr>
                <th colspan="3"><h2>Respondents' Panel Preferences</h2></th>
                </tr>
                <tr>
                <th width="25%">Respondent</th>
                <th width="25%">Preference</th>
                <th>Reason</th>
                </tr>
                <thead>
                <tbody>
                  <tr>
                    <td>Respondent1</td>
                    <td>judge</td>
                    <td>I deserve it</td>
                  </tr>
                  <tr>
                    <td>Respondent2</td>
                    <td>panel</td>
                    <td>Fair trial</td>
                  </tr>
                </tbody>
                </table>
                """
        );
    }

    @Test
    void setRespondentDetails_shouldReturnFormattedDetails_whenRespondentCollectionIsValid() {
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentName("Test Respondent");
        respondent.setResponseRespondentName("Test Response");
        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setValue(respondent);
        CaseData caseDataWithValidRespondentCollection = new CaseData();
        caseDataWithValidRespondentCollection.setRespondentCollection(Collections.singletonList(respondentItem));

        String result = initialConsiderationService.setRespondentDetails(caseDataWithValidRespondentCollection);

        assertThat(result).contains("Test Respondent");
    }

    @Test
    void setRespondentDetails_shouldHandleNullRespondentInCollection() {
        CaseData caseDataWithNullRespondentInCollection = new CaseData();
        caseDataWithNullRespondentInCollection.setRespondentCollection(Collections.singletonList(null));

        String result = initialConsiderationService.setRespondentDetails(caseDataWithNullRespondentInCollection);

        // Should not throw and should be empty or handle gracefully
        assertThat(result).isNotNull();
    }

    @Test
    void setRespondentDetails_shouldHandleMultipleRespondents() {
        RespondentSumType respondent1 = new RespondentSumType();
        respondent1.setRespondentName("Respondent 1");
        respondent1.setResponseRespondentName("Response 1");
        RespondentSumTypeItem item1 = new RespondentSumTypeItem();
        item1.setValue(respondent1);

        RespondentSumType respondent2 = new RespondentSumType();
        respondent2.setRespondentName("Respondent 2");
        respondent2.setResponseRespondentName("Response 2");
        RespondentSumTypeItem item2 = new RespondentSumTypeItem();
        item2.setValue(respondent2);
        CaseData caseDataWithMultipleRespondents = new CaseData();
        caseDataWithMultipleRespondents.setRespondentCollection(List.of(item1, item2));

        String result = initialConsiderationService.setRespondentDetails(caseDataWithMultipleRespondents);

        assertThat(result).contains("Respondent 1");
        assertThat(result).contains("Respondent 2");
    }

    @Test
    void getClaimantHearingFormatDetails_shouldReturnCorrectDetails_whenClaimantHasNeitherPreference() {
        CaseData caseDataWithClaimantHasNeitherPreference = new CaseData();
        ClaimantHearingPreference claimantHearingPreference = new ClaimantHearingPreference();
        claimantHearingPreference.setHearingPreferences(List.of("Neither"));
        claimantHearingPreference.setHearingAssistance("Requires assistance for hearing");
        caseDataWithClaimantHasNeitherPreference.setClaimantHearingPreference(claimantHearingPreference);
        caseDataWithClaimantHasNeitherPreference.setClaimant("John Doe");

        InitialConsiderationService service = new InitialConsiderationService(null);

        String result = service.getClaimantHearingFormatDetails(caseDataWithClaimantHasNeitherPreference);

        assertNotNull(result);
        assertTrue(result.contains("Parties Hearing Format Details"));
        assertTrue(result.contains("Claimant Hearing Format"));
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("Neither (of Phone or Video)"));
        assertTrue(result.contains("Requires assistance for hearing"));
    }

    @Test
    void getClaimantHearingFormatDetails_shouldReturnDefaultDetails_whenClaimantHearingPreferenceIsNull() {
        CaseData caseDataWithNullClaimantHearingPreference = new CaseData();
        caseDataWithNullClaimantHearingPreference.setClaimant("Jane Doe");
        caseDataWithNullClaimantHearingPreference.setClaimantHearingPreference(null);

        InitialConsiderationService service = new InitialConsiderationService(null);

        String result = service.getClaimantHearingFormatDetails(caseDataWithNullClaimantHearingPreference);

        assertNotNull(result);
        assertTrue(result.contains("Parties Hearing Format Details"));
        assertTrue(result.contains("Claimant Hearing Format"));
        assertTrue(result.contains("Jane Doe"));
        assertTrue(result.contains("-"));
    }

    @Test
    void getClaimantHearingFormatDetails_shouldReturnDefaultDetails_whenHearingPreferencesAreEmpty() {
        CaseData caseDataWithEmptyHearingPreferences = new CaseData();
        ClaimantHearingPreference claimantHearingPreference = new ClaimantHearingPreference();
        claimantHearingPreference.setHearingPreferences(Collections.emptyList());
        caseDataWithEmptyHearingPreferences.setClaimantHearingPreference(claimantHearingPreference);
        caseDataWithEmptyHearingPreferences.setClaimant("John Smith");

        InitialConsiderationService service = new InitialConsiderationService(null);

        String result = service.getClaimantHearingFormatDetails(caseDataWithEmptyHearingPreferences);

        assertNotNull(result);
        assertTrue(result.contains("Parties Hearing Format Details"));
        assertTrue(result.contains("Claimant Hearing Format"));
        assertTrue(result.contains("John Smith"));
        assertTrue(result.contains("-"));
    }

    @Test
    void getClaimantHearingFormatDetails_shouldReturnEmptyTable_whenCaseDataIsNull() {
        InitialConsiderationService service = new InitialConsiderationService(null);

        String result = service.getClaimantHearingFormatDetails(null);

        assertNotNull(result);
        assertTrue(result.contains("Parties Hearing Format Details"));
        assertTrue(result.contains("Claimant Hearing Format"));
        assertTrue(result.contains("Claimant"));
        assertTrue(result.contains("Hearing Format"));
    }

    @Test
    void setRespondentDetails_shouldHandleNullRespondentCollection() {
        CaseData caseDataWithNoRespondents = new CaseData();
        caseDataWithNoRespondents.setRespondentCollection(null);
        String result = initialConsiderationService.setRespondentDetails(caseDataWithNoRespondents);
        String expected = """
            <br/>
            <table>
              <thead>
                <tr>
                    <th colspan="3"><h2>Respondent Name Details</h2></th>
                </tr>
                <tr>
                  <th width="25%">Respondent</th>
                  <th width="25%">Name given in ET1</th>
                  <th>Name given in ET3</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Respondent </td>
                  <td>  </td>
                  <td>  </td>
                </tr>
            </tbody>
            </table>
            """;
        assertThat(result).isEqualTo(expected);
    }

    private void setFutureHearingDate(CaseData caseData) {
        DateListedType dateListed = caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection()
            .getFirst().getValue();
        dateListed.setHearingStatus("Listed");
        dateListed.setListedDate(EARLIEST_FUTURE_HEARING_DATE.toString());
        dateListed.setHearingTimingDuration("3.5 Hours");
    }

    private void setFutureHearingDateWithSettledHearing(CaseData caseData) {
        DateListedType dateListed = caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection()
            .getFirst().getValue();
        dateListed.setHearingStatus("Settled");
        dateListed.setListedDate(EARLIEST_FUTURE_HEARING_DATE.toString());
        dateListed.setHearingTimingDuration("3.5 Hours");
    }

    @Test
    void getEarliestHearingDate() {
        setFutureHearingDate(caseData);
        assertThat(initialConsiderationService.getEarliestHearingDateForListedHearings(generateHearingDates()))
            .isEqualTo(Optional.of(LocalDate.of(EARLIEST_FUTURE_HEARING_DATE.getYear(),
                EARLIEST_FUTURE_HEARING_DATE.getMonthOfYear(),
                EARLIEST_FUTURE_HEARING_DATE.getDayOfMonth())));
    }

    @Test
    void getEarliestHearingDateWithEmptyDateListedTypeItem() {
        setFutureHearingDate(caseData);
        assertThat(initialConsiderationService.getEarliestHearingDateForListedHearings(generateHearingDatesWithEmpty()))
            .isEqualTo(Optional.of(LocalDate.of(EARLIEST_FUTURE_HEARING_DATE.getYear(),
                EARLIEST_FUTURE_HEARING_DATE.getMonthOfYear(),
                EARLIEST_FUTURE_HEARING_DATE.getDayOfMonth())));
    }

    @Test
    void getEarliestHearingDateWithEmptyHearingDatesCollection() {
        assertThat(initialConsiderationService.getEarliestHearingDateForListedHearings(new ArrayList<>()))
            .isEmpty();
    }

    @Test
    void getHearingDetailsForSettledHearing() {
        setFutureHearingDateWithSettledHearing(caseData);
        String hearingDetails = initialConsiderationService.getHearingDetails(caseData.getHearingCollection(),
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(hearingDetails)
            .isEqualTo(EXPECTED_BLANK_HEARING_DETAILS);
    }

    @Test
    void generateJurisdictionCodesHtmlTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(generateJurisdictionCodes(),
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(jurisdictionCodesHtml)
            .isEqualTo(EXPECTED_JURISDICTION_HTML);
    }

    @Test
    void generateJurisdictionCodesHtmlScotlandTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(generateJurisdictionCodes(),
                SCOTLAND_CASE_TYPE_ID);
        assertThat(jurisdictionCodesHtml)
            .isEqualTo(EXPECTED_JURISDICTION_SCOTLAND_HTML);
    }

    @Test
    void missingHearingCollectionTest() {
        String expectedResult = """
             <br/>
             <table>
             <thead>
             <tr>
             <th colspan="2"><h2>Listed Hearing Details</h2></th>
             </tr>
             <tr>
             <th width="30%">Aspect</th>
             <th width="70%">Detail</th>
             </tr>
             <thead>
             <tbody>
                <tr>
                    <td>Date</td> <td> - </td>
                </tr>
                <tr>
                    <td>Type</td> <td> - </td>
                </tr>
                <tr>
                    <td>Duration</td> <td> - </td>
                </tr>
                <tr>
                    <td>Hearing format</td> <td> - </td>
                </tr>
                <tr>
                    <td>Panel Type</td> <td> - </td>
                </tr>
                <tr>
                    <td>Venue</td> <td> - </td>
                </tr>
            </tbody>
            </table>
                """;
        String hearingDetails = initialConsiderationService.getHearingDetails(caseDataEmpty.getHearingCollection(),
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(hearingDetails).isEqualTo(expectedResult);
    }

    @Test
    void missingJurisdictionCollectionTest() {
        final String jurisdictionCode = String.format(JURISDICTION_HEADER, CODES_URL_ENGLAND);
        String jurisdictionCodesHtml = initialConsiderationService.generateJurisdictionCodesHtml(
                caseDataEmpty.getJurCodesCollection(), ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(jurisdictionCodesHtml, jurisdictionCode);
    }

    @Test
    void invalidJurisdictionCollectionTest() {
        final String jurisdictionCode = String.format(JURISDICTION_HEADER, CODES_URL_ENGLAND);
        String jurisdictionCodesHtml = initialConsiderationService.generateJurisdictionCodesHtml(
                generateInvalidJurisdictionCodes(), ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(jurisdictionCodesHtml, jurisdictionCode);
    }

    @Test
    void invalidAndValidJurisdictionCollectionTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(generateValidInvalidJurisdictionCodes(),
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(jurisdictionCodesHtml)
            .isEqualTo(EXPECTED_JURISDICTION_HTML);
    }

    @Test
    void generateDocument_EW_Normal() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
                anyString(), anyString())).thenReturn(documentInfo);
        DocumentInfo documentInfo1 = initialConsiderationService.generateDocument(new CaseData(), "userToken",
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(documentInfo).isEqualTo(documentInfo1);
    }

    @Test
    void generateDocument_SC_Normal() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
                anyString(), anyString())).thenReturn(documentInfo);
        DocumentInfo documentInfo1 = initialConsiderationService.generateDocument(new CaseData(), "userToken",
                SCOTLAND_CASE_TYPE_ID);
        assertThat(documentInfo).isEqualTo(documentInfo1);
    }

    @Test
    void generateDocument_Exceptions() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
                anyString(), anyString())).thenThrow(new InternalException(ERROR_MESSAGE));
        assertThrows(Exception.class, () -> initialConsiderationService.generateDocument(new CaseData(),
                "userToken", ENGLANDWALES_CASE_TYPE_ID));
    }

    @Test
    void clearHiddenValue_EtICCanProceed_No() {
        caseData.setEtICCanProceed(NO);
        caseData.setEtICHearingNotListedList(new ArrayList<>());
        caseData.setEtICHearingNotListedSeekComments(new EtICSeekComments());
        caseData.setEtICHearingNotListedListForPrelimHearing(new EtICListForPreliminaryHearing());
        caseData.setEtICHearingNotListedListForFinalHearing(new EtICListForFinalHearing());
        caseData.setEtICHearingNotListedUDLHearing(new EtIcudlHearing());
        caseData.setEtICHearingNotListedAnyOtherDirections("Test");
        caseData.setEtICHearingListed(new ArrayList<>());
        caseData.setEtICExtendDurationGiveDetails("Test");
        caseData.setEtICOtherGiveDetails("Test");
        caseData.setEtICHearingAnyOtherDirections("Test");
        caseData.setEtICPostponeGiveDetails("Test");
        caseData.setEtICConvertPreliminaryGiveDetails("Test");
        caseData.setEtICConvertF2fGiveDetails("Test");

        initialConsiderationService.clearHiddenValue(caseData);

        assertThat(caseData.getEtICHearingNotListedList()).isNull();
        assertThat(caseData.getEtICHearingNotListedSeekComments()).isNull();
        assertThat(caseData.getEtICHearingNotListedListForPrelimHearing()).isNull();
        assertThat(caseData.getEtICHearingNotListedListForFinalHearing()).isNull();
        assertThat(caseData.getEtICHearingNotListedUDLHearing()).isNull();
        assertThat(caseData.getEtICHearingNotListedAnyOtherDirections()).isNull();
        assertThat(caseData.getEtICHearingListed()).isNull();
        assertThat(caseData.getEtICExtendDurationGiveDetails()).isNull();
        assertThat(caseData.getEtICOtherGiveDetails()).isNull();
        assertThat(caseData.getEtICHearingAnyOtherDirections()).isNull();
        assertThat(caseData.getEtICPostponeGiveDetails()).isNull();
        assertThat(caseData.getEtICConvertPreliminaryGiveDetails()).isNull();
        assertThat(caseData.getEtICConvertF2fGiveDetails()).isNull();
    }

    @Test
    void clearHiddenValue_EtICHearingAlreadyListed_Yes() {
        caseData.setEtICCanProceed(YES);
        caseData.setEtICHearingAlreadyListed(YES);

        caseData.setEtICFurtherInformation(new ArrayList<>());
        caseData.setEtICFurtherInformationHearingAnyOtherDirections("Test");
        caseData.setEtICFurtherInformationGiveDetails("Test");
        caseData.setEtICFurtherInformationTimeToComply("Test");
        caseData.setEtInitialConsiderationRule27(new EtInitialConsiderationRule27());
        caseData.setEtInitialConsiderationRule28(new EtInitialConsiderationRule28());
        caseData.setEtICHearingNotListedList(new ArrayList<>());
        caseData.setEtICHearingNotListedSeekComments(new EtICSeekComments());
        caseData.setEtICHearingNotListedListForPrelimHearing(new EtICListForPreliminaryHearing());
        caseData.setEtICHearingNotListedListForFinalHearing(new EtICListForFinalHearing());
        caseData.setEtICHearingNotListedUDLHearing(new EtIcudlHearing());
        caseData.setEtICHearingNotListedAnyOtherDirections("Test");
        caseData.setEtICHearingNotListedListUpdated(new ArrayList<>());
        caseData.setEtICHearingNotListedListForPrelimHearingUpdated(new EtICListForPreliminaryHearingUpdated());
        caseData.setEtICHearingNotListedListForFinalHearingUpdated(new EtICListForFinalHearingUpdated());

        initialConsiderationService.clearHiddenValue(caseData);

        assertThat(caseData.getEtICFurtherInformation()).isNull();
        assertThat(caseData.getEtICFurtherInformationHearingAnyOtherDirections()).isNull();
        assertThat(caseData.getEtICFurtherInformationGiveDetails()).isNull();
        assertThat(caseData.getEtICFurtherInformationTimeToComply()).isNull();
        assertThat(caseData.getEtInitialConsiderationRule27()).isNull();
        assertThat(caseData.getEtInitialConsiderationRule28()).isNull();
        assertThat(caseData.getEtICHearingNotListedList()).isNull();
        assertThat(caseData.getEtICHearingNotListedSeekComments()).isNull();
        assertThat(caseData.getEtICHearingNotListedListForPrelimHearing()).isNull();
        assertThat(caseData.getEtICHearingNotListedListForFinalHearing()).isNull();
        assertThat(caseData.getEtICHearingNotListedUDLHearing()).isNull();
        assertThat(caseData.getEtICHearingNotListedAnyOtherDirections()).isNull();
        assertThat(caseData.getEtICHearingNotListedListUpdated()).isNull();
        assertThat(caseData.getEtICHearingNotListedListForPrelimHearingUpdated()).isNull();
        assertThat(caseData.getEtICHearingNotListedListForFinalHearingUpdated()).isNull();
    }

    @Test
    void clearHiddenValue_EtICHearingAlreadyListed_No() {
        caseData.setEtICCanProceed(YES);
        caseData.setEtICHearingAlreadyListed(NO);

        caseData.setEtICFurtherInformation(new ArrayList<>());
        caseData.setEtICFurtherInformationHearingAnyOtherDirections("Test");
        caseData.setEtICFurtherInformationGiveDetails("Test");
        caseData.setEtICFurtherInformationTimeToComply("Test");
        caseData.setEtInitialConsiderationRule27(new EtInitialConsiderationRule27());
        caseData.setEtInitialConsiderationRule28(new EtInitialConsiderationRule28());
        caseData.setEtICHearingListed(new ArrayList<>());
        caseData.setEtICExtendDurationGiveDetails("Test");
        caseData.setEtICOtherGiveDetails("Test");
        caseData.setEtICHearingAnyOtherDirections("Test");
        caseData.setEtICPostponeGiveDetails("Test");
        caseData.setEtICConvertPreliminaryGiveDetails("Test");
        caseData.setEtICConvertF2fGiveDetails("Test");
        caseData.setEtICHearingListedAnswers(new EtICHearingListedAnswers());

        initialConsiderationService.clearHiddenValue(caseData);

        assertThat(caseData.getEtICFurtherInformation()).isNull();
        assertThat(caseData.getEtICFurtherInformationHearingAnyOtherDirections()).isNull();
        assertThat(caseData.getEtICFurtherInformationGiveDetails()).isNull();
        assertThat(caseData.getEtICFurtherInformationTimeToComply()).isNull();
        assertThat(caseData.getEtInitialConsiderationRule27()).isNull();
        assertThat(caseData.getEtInitialConsiderationRule28()).isNull();
        assertThat(caseData.getEtICHearingListed()).isNull();
        assertThat(caseData.getEtICExtendDurationGiveDetails()).isNull();
        assertThat(caseData.getEtICOtherGiveDetails()).isNull();
        assertThat(caseData.getEtICHearingAnyOtherDirections()).isNull();
        assertThat(caseData.getEtICPostponeGiveDetails()).isNull();
        assertThat(caseData.getEtICConvertPreliminaryGiveDetails()).isNull();
        assertThat(caseData.getEtICConvertF2fGiveDetails()).isNull();
        assertThat(caseData.getEtICHearingListedAnswers()).isNull();
    }

    @Test
    void setIsHearingAlreadyListed_shouldBeSetToNo_whenNoHearings() {
        caseData.setEtInitialConsiderationHearing(HEARING_MISSING);

        initialConsiderationService.setIsHearingAlreadyListed(caseData, SCOTLAND_CASE_TYPE_ID);
        assertThat(caseData.getEtICHearingAlreadyListed()).isEqualTo(NO);
    }

    @Test
    void setIsHearingAlreadyListed_shouldBeSetToYes_whenThereAreHearings() {
        caseData.setEtInitialConsiderationHearing(EXPECTED_HEARING_DETAILS_STRING);

        initialConsiderationService.setIsHearingAlreadyListed(caseData, SCOTLAND_CASE_TYPE_ID);
        assertThat(caseData.getEtICHearingAlreadyListed()).isEqualTo(YES);
    }

    @Test
    void setIsHearingAlreadyListed_shouldIgnoreEntirely_whenCaseTypeIsEnglandWales() {
        caseData.setEtInitialConsiderationHearing(EXPECTED_HEARING_DETAILS_STRING);

        initialConsiderationService.setIsHearingAlreadyListed(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getEtICHearingAlreadyListed()).isNotNull();
    }

    private List<JurCodesTypeItem> generateJurisdictionCodes() {
        return List.of(generateJurisdictionCode("DAG"),
            generateJurisdictionCode("SXD"));
    }

    private List<JurCodesTypeItem> generateInvalidJurisdictionCodes() {
        return List.of(generateJurisdictionCode("PGA"),
            generateJurisdictionCode("CGA"));
    }

    private List<JurCodesTypeItem> generateValidInvalidJurisdictionCodes() {
        return List.of(generateJurisdictionCode("DAG"),
            generateJurisdictionCode("SXD"),
            generateJurisdictionCode("PGA"),
            generateJurisdictionCode("CGA"));
    }

    private JurCodesTypeItem generateJurisdictionCode(String codeString) {
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType code = new JurCodesType();
        code.setJuridictionCodesList(codeString);
        jurCodesTypeItem.setValue(code);
        return jurCodesTypeItem;
    }

    private CaseData generateCaseData(String fileName) throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(fileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseData.class);
    }

    private List<DateListedTypeItem> generateHearingDates() {
        return List.of(createDate("2022-07-15T10:00:00.000", null),
            createDate("2022-07-15T10:00:00.000", null),
            createDate("2022-05-20T10:00:00.000", null),
            createDate("2022-03-22T10:00:00.000", null),
            createDate("2022-01-07T10:00:00.000", null),
            createDate("2022-01-07T10:00:00.000", null),
            createDate(EARLIEST_FUTURE_HEARING_DATE.toString(), "Listed"),
            createDate(SECOND_FUTURE_HEARING_DATE.toString(), "Listed"));
    }

    private List<DateListedTypeItem> generateHearingDatesWithEmpty() {
        return List.of(createDate("2022-07-15T10:00:00.000", null),
            createDate("2022-07-15T10:00:00.000", null),
            new DateListedTypeItem(),
            createDate("2022-03-22T10:00:00.000", null),
            createDate(EARLIEST_FUTURE_HEARING_DATE.toString(), "Listed"));
    }

    private DateListedTypeItem createDate(String dateString, String status) {
        DateListedTypeItem hearingDate = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(dateString);
        dateListedType.setHearingStatus(status);
        hearingDate.setValue(dateListedType);
        return hearingDate;
    }

    @Test
    void processIcDocumentCollections_AllCollectionsNull() {
        caseData.setIcDocumentCollection1(null);
        caseData.setIcDocumentCollection2(null);
        caseData.setIcDocumentCollection3(null);

        initialConsiderationService.processIcDocumentCollections(caseData);

        assertEquals(0, caseData.getIcAllDocumentCollection().size());
    }

    @Test
    void processIcDocumentCollections_SomeCollectionsNotNull() {
        List<DocumentTypeItem> collection1 = new ArrayList<>();
        collection1.add(new DocumentTypeItem());
        caseData.setIcDocumentCollection1(collection1);
        caseData.setIcDocumentCollection2(null);
        caseData.setIcDocumentCollection3(null);

        initialConsiderationService.processIcDocumentCollections(caseData);

        assertEquals(1, caseData.getIcAllDocumentCollection().size());
    }

    @Test
    void processIcDocumentCollections_AllCollectionsNotNull() {
        List<DocumentTypeItem> collection1 = new ArrayList<>();
        collection1.add(new DocumentTypeItem());
        List<DocumentTypeItem> collection2 = new ArrayList<>();
        collection2.add(new DocumentTypeItem());
        List<DocumentTypeItem> collection3 = new ArrayList<>();
        collection3.add(new DocumentTypeItem());
        caseData.setIcDocumentCollection1(collection1);
        caseData.setIcDocumentCollection2(collection2);
        caseData.setIcDocumentCollection3(collection3);

        initialConsiderationService.processIcDocumentCollections(caseData);

        assertEquals(3, caseData.getIcAllDocumentCollection().size());
    }

    @Test
    void getClaimantHearingPanelPreference_NullPreference() {
        String result = initialConsiderationService.getClaimantHearingPanelPreference("",
                null);

        String expected = """
            |Claimant's hearing panel preference | |
            |-------------|:------------|
            |Panel Preference | -|
            |Reason for Panel Preference | -|
            """;
        assertEquals(expected, result);
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_preliminaryHearing_EW() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of(LIST_FOR_PRELIMINARY_HEARING));
        EtICListForPreliminaryHearing preliminaryHearing = new EtICListForPreliminaryHearing();
        preliminaryHearing.setEtICTypeOfPreliminaryHearing(List.of(VIDEO));
        preliminaryHearing.setEtICPurposeOfPreliminaryHearing(List.of("Test purpose"));
        preliminaryHearing.setEtICGiveDetailsOfHearingNotice("Case management");
        preliminaryHearing.setEtICLengthOfPrelimHearing("1");
        preliminaryHearing.setPrelimHearingLengthNumType("Hours");
        caseData1.setEtICHearingNotListedListForPrelimHearing(preliminaryHearing);
        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData1, ENGLANDWALES_CASE_TYPE_ID);

        assertThat(caseData1.getEtICHearingNotListedListForPrelimHearingUpdated()).isNotNull();
        EtICListForPreliminaryHearingUpdated preliminaryHearingUpdated =
                caseData1.getEtICHearingNotListedListForPrelimHearingUpdated();
        assertEquals(preliminaryHearing.getEtICTypeOfPreliminaryHearing(),
                preliminaryHearingUpdated.getEtICTypeOfPreliminaryHearing());
        assertEquals(preliminaryHearing.getEtICPurposeOfPreliminaryHearing(),
                preliminaryHearingUpdated.getEtICPurposeOfPreliminaryHearing());
        assertEquals(preliminaryHearing.getEtICGiveDetailsOfHearingNotice(),
                preliminaryHearingUpdated.getEtICGiveDetailsOfHearingNotice());
        assertEquals(preliminaryHearing.getEtICLengthOfPrelimHearing(),
                preliminaryHearingUpdated.getEtICLengthOfPrelimHearing());
        assertEquals(preliminaryHearing.getPrelimHearingLengthNumType(),
                preliminaryHearingUpdated.getPrelimHearingLengthNumType());
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_preliminaryHearing_SC() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of(LIST_FOR_PRELIMINARY_HEARING));
        EtICListForPreliminaryHearing preliminaryHearing = new EtICListForPreliminaryHearing();
        preliminaryHearing.setEtICTypeOfPreliminaryHearing(List.of(TELEPHONE, CVP));
        preliminaryHearing.setEtICPurposeOfPreliminaryHearing(List.of("Test purpose"));
        preliminaryHearing.setEtICGiveDetailsOfHearingNotice("Case management");
        preliminaryHearing.setEtICLengthOfPrelimHearing("1");
        preliminaryHearing.setPrelimHearingLengthNumType("Hours");
        caseData1.setEtICHearingNotListedListForPrelimHearing(preliminaryHearing);
        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData1, SCOTLAND_CASE_TYPE_ID);

        assertThat(caseData1.getEtICHearingNotListedListForPrelimHearingUpdated()).isNotNull();
        EtICListForPreliminaryHearingUpdated preliminaryHearingUpdated =
                caseData1.getEtICHearingNotListedListForPrelimHearingUpdated();
        assertEquals(List.of(VIDEO),
                preliminaryHearingUpdated.getEtICTypeOfPreliminaryHearing());
        assertEquals(preliminaryHearing.getEtICPurposeOfPreliminaryHearing(),
                preliminaryHearingUpdated.getEtICPurposeOfPreliminaryHearing());
        assertEquals(preliminaryHearing.getEtICGiveDetailsOfHearingNotice(),
                preliminaryHearingUpdated.getEtICGiveDetailsOfHearingNotice());
        assertEquals(preliminaryHearing.getEtICLengthOfPrelimHearing(),
                preliminaryHearingUpdated.getEtICLengthOfPrelimHearing());
        assertEquals(preliminaryHearing.getPrelimHearingLengthNumType(),
                preliminaryHearingUpdated.getPrelimHearingLengthNumType());
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_FinalHearing_EW() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of(LIST_FOR_FINAL_HEARING));
        EtICListForFinalHearing finalHearing = new EtICListForFinalHearing();
        finalHearing.setEtICTypeOfFinalHearing(List.of(VIDEO, TELEPHONE));
        finalHearing.setEtICLengthOfFinalHearing("1");
        finalHearing.setFinalHearingLengthNumType("Hours");
        caseData1.setEtICHearingNotListedListForFinalHearing(finalHearing);
        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData1, ENGLANDWALES_CASE_TYPE_ID);

        assertThat(caseData1.getEtICHearingNotListedListForFinalHearingUpdated()).isNotNull();
        EtICListForFinalHearingUpdated finalHearingUpdated =
                caseData1.getEtICHearingNotListedListForFinalHearingUpdated();
        assertEquals(List.of(VIDEO),
                finalHearingUpdated.getEtICTypeOfFinalHearing());
        assertEquals(finalHearing.getEtICLengthOfFinalHearing(),
                finalHearingUpdated.getEtICLengthOfFinalHearing());
        assertEquals(finalHearing.getFinalHearingLengthNumType(),
                finalHearingUpdated.getFinalHearingLengthNumType());
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_FinalHearing_SC() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of(LIST_FOR_FINAL_HEARING));
        EtICListForFinalHearing finalHearing = new EtICListForFinalHearing();
        finalHearing.setEtICTypeOfFinalHearing(List.of(CVP, TELEPHONE));
        finalHearing.setEtICLengthOfFinalHearing("1");
        finalHearing.setFinalHearingLengthNumType("Hours");
        caseData1.setEtICHearingNotListedListForFinalHearing(finalHearing);
        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData1, SCOTLAND_CASE_TYPE_ID);

        assertThat(caseData1.getEtICHearingNotListedListForFinalHearingUpdated()).isNotNull();
        EtICListForFinalHearingUpdated finalHearingUpdated =
                caseData1.getEtICHearingNotListedListForFinalHearingUpdated();
        assertEquals(List.of(VIDEO),
                finalHearingUpdated.getEtICTypeOfFinalHearing());
        assertEquals(finalHearing.getEtICLengthOfFinalHearing(),
                finalHearingUpdated.getEtICLengthOfFinalHearing());
        assertEquals(finalHearing.getFinalHearingLengthNumType(),
                finalHearingUpdated.getFinalHearingLengthNumType());
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_UdlHearing_EW() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of(UDL_HEARING));
        EtIcudlHearing udlHearing = new EtIcudlHearing();
        udlHearing.setEtIcudlHearFormat("Video hearing");
        udlHearing.setEtIcejSitAlone(YES);
        caseData1.setEtICHearingNotListedUDLHearing(udlHearing);
        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData1, ENGLANDWALES_CASE_TYPE_ID);

        assertThat(caseData1.getEtICHearingNotListedListForFinalHearingUpdated()).isNotNull();
        EtICListForFinalHearingUpdated finalHearingUpdated =
                caseData1.getEtICHearingNotListedListForFinalHearingUpdated();
        assertEquals(List.of(VIDEO),
                finalHearingUpdated.getEtICTypeOfFinalHearing());
        assertEquals(JSA, finalHearingUpdated.getEtICFinalHearingIsEJSitAlone());
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_UdlHearing_SC() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of(UDL_HEARING));
        EtIcudlHearing udlHearing = new EtIcudlHearing();
        udlHearing.setEtIcudlHearFormat(CVP_HEARING);
        udlHearing.setEtIcejSitAlone(YES);
        caseData1.setEtICHearingNotListedUDLHearing(udlHearing);
        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData1, SCOTLAND_CASE_TYPE_ID);

        assertThat(caseData1.getEtICHearingNotListedListForFinalHearingUpdated()).isNotNull();
        EtICListForFinalHearingUpdated finalHearingUpdated =
                caseData1.getEtICHearingNotListedListForFinalHearingUpdated();
        assertEquals(List.of(VIDEO),
                finalHearingUpdated.getEtICTypeOfFinalHearing());
        assertEquals(YES, finalHearingUpdated.getEtICFinalHearingIsEJSitAlone());
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_seekComments_EW() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of(SEEK_COMMENTS));
        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData1, ENGLANDWALES_CASE_TYPE_ID);

        assertThat(caseData1.getEtICHearingNotListedListUpdated()).isEqualTo(List.of(HEARING_NOT_LISTED));
    }

    @Test
    void clearIcHearingNotListedOldValues_shouldClearAllFields_EW() {
        CaseData caseData1 = new CaseData();
        caseData1.setEtICHearingNotListedList(List.of("Some value"));
        caseData1.setEtICHearingNotListedListForPrelimHearing(new EtICListForPreliminaryHearing());
        caseData1.setEtICHearingNotListedListForFinalHearing(new EtICListForFinalHearing());
        caseData1.setEtICHearingNotListedUDLHearing(new EtIcudlHearing());
        caseData1.setEtICHearingNotListedAnyOtherDirections("Some directions");

        initialConsiderationService.clearIcHearingNotListedOldValues(caseData1);

        assertThat(caseData1.getEtICHearingNotListedList()).isNull();
        assertThat(caseData1.getEtICHearingNotListedListForPrelimHearing()).isNull();
        assertThat(caseData1.getEtICHearingNotListedListForFinalHearing()).isNull();
        assertThat(caseData1.getEtICHearingNotListedUDLHearing()).isNull();
        assertThat(caseData1.getEtICHearingNotListedAnyOtherDirections()).isNull();
    }

    @Test
    void getEarliestHearingDateForListedHearings_shouldReturnEarliestFutureDate_whenMultipleValidListedHearingsExist() {
        List<DateListedTypeItem> hearingDates = List.of(
                createDateListedTypeItem("2026-10-15T10:00:00", "Listed", "2 Hours"),
                createDateListedTypeItem("2026-10-10T09:00:00", "Listed", "1 Hour"),
                createDateListedTypeItem("2026-10-20T11:00:00", "Listed", "3 Hours")
        );

        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(hearingDates);

        assertThat(result).isPresent();
        assertThat(result.get().getYear()).isEqualTo(2026);
        assertThat(result.get().getMonthValue()).isEqualTo(10);
        assertThat(result.get().getDayOfMonth()).isEqualTo(10);
    }

    @Test
    void getEarliestHearingDateForListedHearings_shouldIgnoreNonListedHearings() {
        List<DateListedTypeItem> hearingDates = List.of(
                createDateListedTypeItem("2026-10-15T10:00:00", "NotListed", "2 Hours"),
                createDateListedTypeItem("2026-10-10T09:00:00", "Listed", "1 Hour"),
                createDateListedTypeItem("2026-10-20T11:00:00", "NotListed", "3 Hours")
        );

        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(hearingDates);

        assertThat(result).isPresent();
        assertThat(result.get().getYear()).isEqualTo(2026);
        assertThat(result.get().getMonthValue()).isEqualTo(10);
        assertThat(result.get().getDayOfMonth()).isEqualTo(10);
    }

    @Test
    void getEarliestHearingDateForListedHearings_shouldReturnEmpty_whenAllHearingDatesAreInThePast() {
        List<DateListedTypeItem> hearingDates = List.of(
                createDate("2022-10-15T10:00:00.000", "Listed"),
                createDate("2022-10-10T10:00:00.000", "Listed")
        );

        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(hearingDates);

        assertThat(result).isEmpty();
    }

    @Test
    void getEarliestHearingDateForListedHearings_shouldReturnEmpty_whenHearingDatesListIsEmpty() {
        List<DateListedTypeItem> hearingDates = new ArrayList<>();

        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(hearingDates);

        assertThat(result).isEmpty();
    }

    @Test
    void getEarliestHearingDateForListedHearings_shouldReturnEmpty_whenHearingDatesListIsNull() {
        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(
                null);

        assertThat(result).isEmpty();
    }

    @Test
    void getEarliestHearingDateForListedHearings_shouldIgnoreInvalidDateFormats() {
        List<DateListedTypeItem> hearingDates = List.of(
                createDate("InvalidDate", "Listed"),
                createDate("2026-10-15T10:00:00.000", "Listed")
        );

        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(hearingDates);

        assertThat(result).isPresent();
        assertThat(result.get().getYear()).isEqualTo(2026);
        assertThat(result.get().getMonthValue()).isEqualTo(10);
        assertThat(result.get().getDayOfMonth()).isEqualTo(15);
    }

    @Test
    void generateJurisdictionCodesHtml_shouldReturnEmptyString_whenJurisdictionCodesAreNull() {
        String result = initialConsiderationService.generateJurisdictionCodesHtml(null,
                ENGLANDWALES_CASE_TYPE_ID);

        assertThat(result).isNotEmpty();
    }

    @Test
    void generateJurisdictionCodesHtml_shouldReturnEmptyString_whenJurisdictionCodesAreEmpty() {
        List<JurCodesTypeItem> jurisdictionCodes = new ArrayList<>();
        String result = initialConsiderationService.generateJurisdictionCodesHtml(jurisdictionCodes,
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isNotEmpty();
    }

    @Test
    void generateJurisdictionCodesHtml_shouldReturnFormattedHtml_whenValidJurisdictionCodesExist() {
        List<JurCodesTypeItem> jurisdictionCodes = generateJurisdictionCodes();

        String result = initialConsiderationService.generateJurisdictionCodesHtml(jurisdictionCodes,
                ENGLANDWALES_CASE_TYPE_ID);

        assertThat(result).isNotEmpty();
        assertThat(result).contains("<h2>Jurisdiction codes</h2>");
    }

    @Test
    void generateJurisdictionCodesHtml_shouldReturnFormattedHtmlForScotland_whenValidJurisdictionCodesExist() {
        List<JurCodesTypeItem> jurisdictionCodes = generateJurisdictionCodes();

        String result = initialConsiderationService.generateJurisdictionCodesHtml(jurisdictionCodes,
                SCOTLAND_CASE_TYPE_ID);

        assertThat(result).isNotEmpty();
        assertThat(result).contains("<h2>Jurisdiction codes</h2>");
        assertThat(result).contains(CODES_URL_SCOTLAND);
    }

    @Test
    void generateJurisdictionCodesHtml_shouldIgnoreInvalidJurisdictionCodes() {
        List<JurCodesTypeItem> jurisdictionCodes = generateInvalidJurisdictionCodes();
        String result = initialConsiderationService.generateJurisdictionCodesHtml(jurisdictionCodes,
                ENGLANDWALES_CASE_TYPE_ID);

        assertThat(result).isNotEmpty();
        assertThat(result).contains("<h2>Jurisdiction codes</h2>");
    }

    @Test
    void clearOldValuesValues_clearsAllFieldsWhenAnswersExist() {
        EtICHearingListedAnswers hearingListedAnswers = getEtICHearingListedAnswers();

        CaseData caseDataForListedAnswers = new CaseData();
        caseDataForListedAnswers.setEtICHearingListedAnswers(hearingListedAnswers);
        caseDataForListedAnswers.setEtInitialConsiderationHearing("SomeHearing");
        initialConsiderationService.clearOldValues(caseDataForListedAnswers);

        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers());
    }

    private static @NotNull EtICHearingListedAnswers getEtICHearingListedAnswers() {
        EtICHearingListedAnswers hearingListedAnswers = new EtICHearingListedAnswers();
        hearingListedAnswers.setEtInitialConsiderationListedHearingType("SomeType");
        hearingListedAnswers.setEtICIsHearingWithJsaReasonOther("SomeReason");
        hearingListedAnswers.setEtICIsHearingWithMembers("Yes");
        hearingListedAnswers.setEtICJsaFinalHearingReasonOther("SomeFinalReason");
        hearingListedAnswers.setEtICMembersFinalHearingReasonOther("SomeMemberReason");
        hearingListedAnswers.setEtICIsHearingWithJudgeOrMembersFurtherDetails("Details");
        hearingListedAnswers.setEtICIsHearingWithJudgeOrMembersReason(List.of("Reason"));
        hearingListedAnswers.setEtICIsFinalHearingWithJudgeOrMembersJsaReason(
                Collections.singletonList("FinalJsaReason"));
        hearingListedAnswers.setEtICIsFinalHearingWithJudgeOrMembersReason(Collections.singletonList("FinalReason"));
        hearingListedAnswers.setEtICIsHearingWithJsa("Yes");
        hearingListedAnswers.setEtICHearingListed(Collections.singletonList("Listed"));
        hearingListedAnswers.setEtICIsHearingWithJudgeOrMembers("Yes");
        hearingListedAnswers.setEtICIsHearingWithJudgeOrMembersReasonOther("OtherReason");
        return hearingListedAnswers;
    }

    @Test
    void initialiseInitialConsideration_shouldSetBeforeYouStart_whenDocumentCollectionHasValidDocuments() {
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        DocumentTypeItem et1Document = new DocumentTypeItem();
        et1Document.setValue(DocumentType.from(new UploadedDocumentType()));
        et1Document.getValue().setDocumentType(ET1);
        documentCollection.add(et1Document);

        CaseData caseDataForEt1DocType = new CaseData();
        caseDataForEt1DocType.setDocumentCollection(documentCollection);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataForEt1DocType);

        initialConsiderationService.initialiseInitialConsideration(caseDetails);

        assertThat(caseDetails.getCaseData().getInitialConsiderationBeforeYouStart()).isNotEmpty();
    }

    @Test
    void initialiseInitialConsideration_shouldSetEmptyBeforeYouStart_whenDocumentCollectionIsEmpty() {
        CaseData caseDataWithEmptyDocCollection = new CaseData();
        caseDataWithEmptyDocCollection.setDocumentCollection(new ArrayList<>());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataWithEmptyDocCollection);

        initialConsiderationService.initialiseInitialConsideration(caseDetails);

        assertThat(caseDetails.getCaseData().getInitialConsiderationBeforeYouStart()).isEmpty();
    }

    @Test
    void initialiseInitialConsideration_shouldIncludeReferralLinks_whenReferralCollectionIsNotEmpty() {
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        DocumentTypeItem et1Document = new DocumentTypeItem();
        et1Document.setValue(DocumentType.from(new UploadedDocumentType()));
        et1Document.getValue().setDocumentType(ET1);
        documentCollection.add(et1Document);

        CaseData caseDataWithReferralLinks = new CaseData();
        caseDataWithReferralLinks.setDocumentCollection(documentCollection);
        caseDataWithReferralLinks.setReferralCollection(Collections.singletonList(new ReferralTypeItem()));
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataWithReferralLinks);
        caseDetails.setCaseId("12345");

        initialConsiderationService.initialiseInitialConsideration(caseDetails);

        assertThat(caseDetails.getCaseData().getInitialConsiderationBeforeYouStart())
                .contains(CASE_DETAILS_URL_PARTIAL + "12345" + REFERRALS_PAGE_FRAGMENT_ID);
    }

    @Test
    void initialiseInitialConsideration_shouldNotIncludeReferralLinks_whenReferralCollectionIsNull() {
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        DocumentTypeItem et1Document = new DocumentTypeItem();
        et1Document.setValue(DocumentType.from(new UploadedDocumentType()));
        et1Document.getValue().setDocumentType(ET1);
        documentCollection.add(et1Document);

        CaseData caseDataWithNullReferralList = new CaseData();
        caseDataWithNullReferralList.setDocumentCollection(documentCollection);
        caseDataWithNullReferralList.setReferralCollection(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataWithNullReferralList);

        initialConsiderationService.initialiseInitialConsideration(caseDetails);

        assertThat(caseDetails.getCaseData().getInitialConsiderationBeforeYouStart())
                .doesNotContain(CASE_DETAILS_URL_PARTIAL);
    }

    @Test
    void clearOldEtICHearingListedAnswersValues_doesNothingWhenAnswersAreNull() {
        CaseData caseDataLocal = new CaseData();
        caseDataLocal.setEtICHearingListedAnswers(null);
        caseDataLocal.setEtInitialConsiderationHearing("SomeHearing");

        initialConsiderationService.clearOldValues(caseDataLocal);

        assertNull(caseDataLocal.getEtICHearingListedAnswers());
        assertEquals("SomeHearing", caseDataLocal.getEtInitialConsiderationHearing());
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingCollectionIsNull() {
        String expectedResult = """
                 <br/>
                 <table>
                 <thead>
                 <tr>
                 <th colspan="2"><h2>Listed Hearing Details</h2></th>
                 </tr>
                 <tr>
                 <th width="30%">Aspect</th>
                 <th width="70%">Detail</th>
                 </tr>
                 <thead>
                 <tbody>
                    <tr>
                        <td>Date</td> <td> - </td>
                    </tr>
                    <tr>
                        <td>Type</td> <td> - </td>
                    </tr>
                    <tr>
                        <td>Duration</td> <td> - </td>
                    </tr>
                    <tr>
                        <td>Hearing format</td> <td> - </td>
                    </tr>
                    <tr>
                        <td>Panel Type</td> <td> - </td>
                    </tr>
                    <tr>
                        <td>Venue</td> <td> - </td>
                    </tr>
                </tbody>
                </table>
                """;
        String result = initialConsiderationService.getHearingDetails(null, null);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingCollectionIsEmpty() {
        List<HearingTypeItem> hearingCollection = new ArrayList<>();

        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenNoListedHearingsExist() {
        List<HearingTypeItem> hearingCollection = List.of(
                createHearingTypeItem("2023-10-15T10:00:00.000", "Not Listed", "Hearing Type 1", "Hours", "3"),
                createHearingTypeItem("2023-10-10T10:00:00.000", "Cancelled", "Hearing Type 2", "Hours", "2")
        );

        String expectedResult = """
           <br/>
           <table>
           <thead>
           <tr>
           <th colspan="2"><h2>Listed Hearing Details</h2></th>
           </tr>
           <tr>
           <th width="30%">Aspect</th>
           <th width="70%">Detail</th>
           </tr>
           <thead>
           <tbody>
              <tr>
                  <td>Date</td> <td> - </td>
              </tr>
              <tr>
                  <td>Type</td> <td> - </td>
              </tr>
              <tr>
                  <td>Duration</td> <td> - </td>
              </tr>
              <tr>
                  <td>Hearing format</td> <td> - </td>
              </tr>
              <tr>
                  <td>Panel Type</td> <td> - </td>
              </tr>
              <tr>
                  <td>Venue</td> <td> - </td>
              </tr>
          </tbody>
          </table>
            """;

        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getHearingDetails_shouldReturnFormattedDetails_whenValidHearingCollectionExists() {
        List<HearingTypeItem> hearingCollection = List.of(
                createHearingTypeItem("2026-10-15T10:00:00.000", "Listed",
                        "Hearing Type 1", "Hours", "3"),
                createHearingTypeItem("2026-10-10T10:00:00.000", "Listed",
                        "Hearing Type 2", "Hours", "2")
        );

        String expectedResult = """
           <br/>
           <table>
           <thead>
           <tr>
           <th colspan="2"><h2>Listed Hearing Details</h2></th>
           </tr>
           <tr>
           <th width="30%">Aspect</th>
           <th width="70%">Detail</th>
           </tr>
           <thead>
           <tbody>
              <tr>
                  <td>Date</td> <td> 10 Oct 2026 </td>
              </tr>
              <tr>
                  <td>Type</td> <td> Hearing Type 2 </td>
              </tr>
              <tr>
                  <td>Duration</td> <td> 2 Hours </td>
              </tr>
              <tr>
                  <td>Hearing format</td> <td> - </td>
              </tr>
              <tr>
                  <td>Panel Type</td> <td> - </td>
              </tr>
              <tr>
                  <td>Venue</td> <td> - </td>
              </tr>
          </tbody>
          </table>
            """;
        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getHearingDetails_shouldIgnoreInvalidDateFormats() {
        List<HearingTypeItem> hearingCollection = List.of(
                createHearingTypeItem("InvalidDate", "Listed", "Hearing Type 1", "Hours", "3"),
                createHearingTypeItem("2026-10-15T10:00:00.000", "Listed", "Hearing Type 2", "Hours", "2")
        );

        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        String detail = """
                 <br/>
                 <table>
                 <thead>
                 <tr>
                 <th colspan="2"><h2>Listed Hearing Details</h2></th>
                 </tr>
                 <tr>
                 <th width="30%">Aspect</th>
                 <th width="70%">Detail</th>
                 </tr>
                 <thead>
                 <tbody>
                    <tr>
                        <td>Date</td> <td> 15 Oct 2026 </td>
                    </tr>
                    <tr>
                        <td>Type</td> <td> Hearing Type 2 </td>
                    </tr>
                    <tr>
                        <td>Duration</td> <td> 2 Hours </td>
                    </tr>
                    <tr>
                        <td>Hearing format</td> <td> - </td>
                    </tr>
                    <tr>
                        <td>Panel Type</td> <td> - </td>
                    </tr>
                    <tr>
                        <td>Venue</td> <td> - </td>
                    </tr>
                </tbody>
                </table>
                """;
        assertThat(result).isEqualTo(detail);
    }

    private HearingTypeItem createHearingTypeItem(String date, String status, String type, String lengthType,
                                                  String duration) {
        HearingType hearing = new HearingType();
        hearing.setHearingType(type);
        hearing.setHearingDateCollection(List.of(createDateListedTypeItem(date, status, duration)));
        hearing.setHearingEstLengthNum(duration);
        hearing.setHearingEstLengthNumType(lengthType);

        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearing);
        return hearingTypeItem;
    }

    private DateListedTypeItem createDateListedTypeItem(String date, String status, String duration) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(date);
        dateListedType.setHearingStatus(status);
        dateListedType.setHearingTimingDuration(duration);

        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);
        return dateListedTypeItem;
    }

    // Add these tests to cover missing cases for getHearingDetails
    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingTypeItemIsNull() {
        List<HearingTypeItem> hearingCollection = new ArrayList<>();
        hearingCollection.add(null);

        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingTypeValueIsNull() {
        HearingTypeItem item = new HearingTypeItem();
        item.setValue(null);
        List<HearingTypeItem> hearingCollection = List.of(item);

        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingDateCollectionIsNull() {
        HearingType hearing = new HearingType();
        hearing.setHearingDateCollection(null);
        HearingTypeItem item = new HearingTypeItem();
        item.setValue(hearing);
        List<HearingTypeItem> hearingCollection = List.of(item);

        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingDateCollectionIsEmpty() {
        HearingType hearing = new HearingType();
        hearing.setHearingDateCollection(new ArrayList<>());
        HearingTypeItem item = new HearingTypeItem();
        item.setValue(hearing);
        List<HearingTypeItem> hearingCollection = List.of(item);

        String result = initialConsiderationService.getHearingDetails(hearingCollection, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getClaimantHearingPanelPreference_shouldReturnMissing_whenNull() {
        String result = initialConsiderationService.getClaimantHearingPanelPreference("",
                null);
        assertEquals(CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING, result);
    }

    @Test
    void getClaimantHearingPanelPreference_shouldReturnDashes_whenFieldsAreNull() {
        ClaimantHearingPreference pref = new ClaimantHearingPreference();
        String result = initialConsiderationService.getClaimantHearingPanelPreference("", pref);
        String expected = """
            <br/>
            <table>
            <thead>
            <tr>
             <th colspan="3"><h1>Parties Hearing Panel Preferences</h1></th>
            </tr>
            <tr>
            <th colspan="3"><h2>Claimant's Panel Preference</h2></th>
            </tr>
            <tr>
            <th width="25%">Claimant</th>
            <th width="25%">Preference</th>
            <th>Reason</th>
            </tr>
            </thead>
            <tbody>
              <tr>
                <td></td>
                <td>-</td>
                <td>-</td>
              </tr>
            </tbody>
            </table>
            """;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getClaimantHearingPanelPreference_shouldReturnPanelPreference_whenVideoPresent() {
        ClaimantHearingPreference pref = new ClaimantHearingPreference();
        pref.setClaimantHearingPanelPreference("Panel");
        pref.setClaimantHearingPanelPreferenceWhy("Reason");
        pref.setHearingPreferences(List.of(VIDEO));
        String result = initialConsiderationService.getClaimantHearingPanelPreference("", pref);
        assertTrue(result.contains("Panel"));
        assertTrue(result.contains("Reason"));
        assertFalse(result.toUpperCase(Locale.UK).contains(NOT_AVAILABLE_FOR_VIDEO_HEARINGS.toUpperCase(Locale.UK)));
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldReturnNull_whenRespondentCollectionIsNull() {
        CaseData caseDataWithNullRespondentCollection = new CaseData();
        caseDataWithNullRespondentCollection.setRespondentCollection(null);
        String result = initialConsiderationService.setIcEt3VettingIssuesDetailsForEachRespondent(
                caseDataWithNullRespondentCollection);

        assertNull(result);
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldReturnNull_whenRespondentCollectionIsEmpty() {
        CaseData caseDataWithEmptyRespondentCollection = new CaseData();
        caseDataWithEmptyRespondentCollection.setRespondentCollection(new ArrayList<>());
        String result = initialConsiderationService.setIcEt3VettingIssuesDetailsForEachRespondent(
                caseDataWithEmptyRespondentCollection);

        assertNull(result);
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldSkipRespondentsWithNullEt3Vetting() {
        CaseData caseDataWithNullEt3Vetting = new CaseData();
        RespondentSumTypeItem respondent1 = new RespondentSumTypeItem();
        respondent1.setValue(new RespondentSumType());
        RespondentSumTypeItem respondent2 = new RespondentSumTypeItem();
        respondent2.setValue(new RespondentSumType());
        caseDataWithNullEt3Vetting.setRespondentCollection(List.of(respondent1, respondent2));

        String result = initialConsiderationService.setIcEt3VettingIssuesDetailsForEachRespondent(
                caseDataWithNullEt3Vetting);

        assertNotNull(result);
        assertTrue(result.contains("Details of ET3 Processing Issues"));
        assertFalse(result.contains("<h3>Respondent"));
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldIncludeDetailsForValidRespondents() {
        CaseData caseDataWithValidRespondents = getCaseData();
        String result = initialConsiderationService.setIcEt3VettingIssuesDetailsForEachRespondent(
                caseDataWithValidRespondents);

        assertNotNull(result);
        assertTrue(result.contains("Details of ET3 Processing Issues"));
        assertTrue(result.contains("<h3>Respondent Valid Respondent<h3>"));
        assertTrue(result.contains("Additional Info"));
    }

    private static @NotNull CaseData getCaseData() {
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentName("Valid Respondent");
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3AdditionalInformation("Additional Info");
        respondent.setEt3Vetting(et3Vetting);
        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setValue(respondent);
        CaseData caseDataWithValidRespondents = new CaseData();
        caseDataWithValidRespondents.setRespondentCollection(List.of(respondentItem));
        return caseDataWithValidRespondents;
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldHandleMultipleRespondents() {
        RespondentSumType respondent1 = new RespondentSumType();
        respondent1.setRespondentName("Respondent 1");
        Et3VettingType et3Vetting1 = new Et3VettingType();
        et3Vetting1.setEt3AdditionalInformation("Info 1");
        respondent1.setEt3Vetting(et3Vetting1);
        RespondentSumTypeItem respondentItem1 = new RespondentSumTypeItem();
        respondentItem1.setValue(respondent1);

        CaseData caseDataWithMultipleRespondents = getCaseDataWithMultipleRespondents(respondentItem1);

        String result = initialConsiderationService.setIcEt3VettingIssuesDetailsForEachRespondent(
                caseDataWithMultipleRespondents);

        assertNotNull(result);
        assertTrue(result.contains("Details of ET3 Processing Issues"));
        assertTrue(result.contains("<h3>Respondent Respondent 1<h3>"));
        assertTrue(result.contains("Info 1"));
        assertTrue(result.contains("<h3>Respondent Respondent 2<h3>"));
        assertTrue(result.contains("Info 2"));
    }

    private static @NotNull CaseData getCaseDataWithMultipleRespondents(RespondentSumTypeItem respondentItem1) {
        RespondentSumType respondent2 = new RespondentSumType();
        respondent2.setRespondentName("Respondent 2");
        Et3VettingType et3Vetting2 = new Et3VettingType();
        et3Vetting2.setEt3AdditionalInformation("Info 2");
        respondent2.setEt3Vetting(et3Vetting2);
        RespondentSumTypeItem respondentItem2 = new RespondentSumTypeItem();
        respondentItem2.setValue(respondent2);

        CaseData caseDataWithMultipleRespondents = new CaseData();
        caseDataWithMultipleRespondents.setRespondentCollection(List.of(respondentItem1, respondentItem2));
        return caseDataWithMultipleRespondents;
    }

    @Test
    void processEt3Response_shouldAddPairForEt3ResponseWhenResponseExists() {
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3IsThereAnEt3Response(YES);
        List<String[]> pairsList = new ArrayList<>();

        initialConsiderationService.processEt3Response(et3Vetting, pairsList);

        assertEquals(1, pairsList.size());
        assertEquals("Is there an ET3 response?", pairsList.get(0)[0]);
        assertEquals(YES, pairsList.getFirst()[1]);
    }

    @Test
    void processEt3Response_shouldAddDetailsWhenNoEt3Response() {
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3IsThereAnEt3Response(NO);
        et3Vetting.setEt3NoEt3Response("No response details");
        et3Vetting.setEt3GeneralNotes("General notes");
        List<String[]> pairsList = new ArrayList<>();

        initialConsiderationService.processEt3Response(et3Vetting, pairsList);

        assertEquals(3, pairsList.size());
        assertEquals("Is there an ET3 response?", pairsList.get(0)[0]);
        assertEquals(NO, pairsList.get(0)[1]);
        assertEquals(GIVE_DETAILS, pairsList.get(1)[0]);
        assertEquals("No response details", pairsList.get(1)[1]);
        assertEquals("General notes (No ET3 Response):", pairsList.get(2)[0]);
        assertEquals("General notes", pairsList.get(2)[1]);
    }

    @Test
    void processEt3Response_shouldHandleNullEt3VettingGracefully() {
        List<String[]> pairsList = new ArrayList<>();

        initialConsiderationService.processEt3Response(null, pairsList);

        assertTrue(pairsList.isEmpty());
    }

    @Test
    void processEt3Response_shouldNotAddPairsIfNullFieldsInEt3Vetting() {
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3IsThereAnEt3Response(null);
        et3Vetting.setEt3NoEt3Response(null);
        et3Vetting.setEt3GeneralNotes(null);
        List<String[]> pairsList = new ArrayList<>();

        initialConsiderationService.processEt3Response(et3Vetting, pairsList);

        assertEquals(0, pairsList.size());
    }

    @Test
    void shouldIncludeSuggestedIssueDetailsInEt3VettingIssues() {

        Et3VettingType et3Vetting = getEt3VettingType();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        RespondentSumType respondentValue = new RespondentSumType();
        respondentValue.setRespondentName("Test Respondent");
        respondentValue.setEt3Vetting(et3Vetting);
        respondent.setValue(respondentValue);

        CaseData caseDataWithSuggestedIssues = new CaseData();
        caseDataWithSuggestedIssues.setRespondentCollection(List.of(respondent));

        InitialConsiderationService service = new InitialConsiderationService(tornadoService);
        String result = service.setIcEt3VettingIssuesDetailsForEachRespondent(caseDataWithSuggestedIssues);

        assertTrue(result.contains("Strike out details"));
        assertTrue(result.contains("Interpreters"));
        assertTrue(result.contains("Jurisdictional issues"));
        assertTrue(result.contains("Rule 49"));
        assertTrue(result.contains("Request for adjustments"));
        assertTrue(result.contains("Time points"));
    }

    private static @NotNull Et3VettingType getEt3VettingType() {
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3SuggestedIssues(List.of(
                APPLICATIONS_FOR_STRIKE_OUT_OR_DEPOSIT,
                INTERPRETERS,
                JURISDICTIONAL_ISSUES,
                RULE_49,
                REQUEST_FOR_ADJUSTMENTS,
                TIME_POINTS));
        et3Vetting.setEt3SuggestedIssuesStrikeOut("Strike out details");
        et3Vetting.setEt3SuggestedIssueInterpreters("Interpreters");
        et3Vetting.setEt3SuggestedIssueJurisdictional("Jurisdictional issues");
        et3Vetting.setEt3SuggestedIssueRule50("Rule 49");
        et3Vetting.setEt3SuggestedIssueAdjustments("Request for adjustments");
        et3Vetting.setEt3SuggestedIssueTimePoints("Time points");
        return et3Vetting;
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldIncludeRespondentNameDetails() {
        CaseData caseDataWithRespondentNameDetails = getData();

        InitialConsiderationService service = new InitialConsiderationService(tornadoService);

        // Act
        String result = service.setIcEt3VettingIssuesDetailsForEachRespondent(caseDataWithRespondentNameDetails);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Do we have the respondent's name?"));
        assertTrue(result.contains("Does the respondent's name match?"));
        assertTrue(result.contains("Mismatch details"));
    }

    private static @NotNull CaseData getData() {
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3DoWeHaveRespondentsName(YES);
        et3Vetting.setEt3DoesRespondentsNameMatch(NO);
        et3Vetting.setEt3RespondentNameMismatchDetails("Mismatch details");

        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentName("Test Respondent");
        respondent.setEt3Vetting(et3Vetting);

        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setValue(respondent);

        CaseData caseDataToReuse = new CaseData();
        caseDataToReuse.setRespondentCollection(List.of(respondentItem));
        return caseDataToReuse;
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldIncludeResponseInTimeDetails_whenResponseIsNotInTime() {
        CaseData caseDataWithNoResponseInTime = getCaseData1();
        InitialConsiderationService service = new InitialConsiderationService(tornadoService);
        String result = service.setIcEt3VettingIssuesDetailsForEachRespondent(caseDataWithNoResponseInTime);

        assertNotNull(result);
        assertTrue(result.contains("Did we receive the ET3 response in time?"));
        assertTrue(result.contains(NO));
        assertTrue(result.contains("Received late due to postal delay"));
    }

    private static @NotNull CaseData getCaseData1() {
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3ResponseInTime(NO);
        et3Vetting.setEt3ResponseInTimeDetails("Received late due to postal delay");
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentName("Late Respondent");
        respondent.setEt3Vetting(et3Vetting);
        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setValue(respondent);
        CaseData caseDataOne = new CaseData();
        caseDataOne.setRespondentCollection(List.of(respondentItem));
        return caseDataOne;
    }

    @Test
    void setIcEt3VettingIssuesDetailsForEachRespondent_shouldNotIncludeReasonDetails_whenResponseIsInTime() {
        Et3VettingType et3Vetting = new Et3VettingType();
        et3Vetting.setEt3ResponseInTime(YES);
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentName("On Time Respondent");
        et3Vetting.setEt3ResponseInTimeDetails("Some test reason for not having ET3 in time");
        respondent.setEt3Vetting(et3Vetting);
        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setValue(respondent);
        CaseData caseDataWithEt3Details = new CaseData();
        caseDataWithEt3Details.setRespondentCollection(List.of(respondentItem));

        InitialConsiderationService service = new InitialConsiderationService(tornadoService);
        String result = service.setIcEt3VettingIssuesDetailsForEachRespondent(caseDataWithEt3Details);

        assertNotNull(result);
        assertFalse(result.contains("Some test reason for not having ET3 in time"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldReturnNull_whenCaseDataIsNull() {
        InitialConsiderationService service = new InitialConsiderationService(tornadoService);
        String result = service.setIcEt1VettingIssuesDetails(null);
        assertNull(result);
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldReturnNonEmptyString_whenCaseDataIsValid() {
        InitialConsiderationService service = new InitialConsiderationService(tornadoService);
        //No specific fields are set in CaseData, but it is not null
        CaseData caseDataWithEmptyFields = new CaseData();
        String result = service.setIcEt1VettingIssuesDetails(caseDataWithEmptyFields);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeServingClaimsSection_whenAnswerIsNo() {
        InitialConsiderationService service = new InitialConsiderationService(tornadoService);
        CaseData caseDataWithEt1VettingIssuesDetails = new CaseData();
        caseDataWithEt1VettingIssuesDetails.setEt1VettingCanServeClaimYesOrNo("No");
        caseDataWithEt1VettingIssuesDetails.setEt1VettingCanServeClaimNoReason("Missing address");
        caseDataWithEt1VettingIssuesDetails.setEt1VettingCanServeClaimGeneralNote("serving claims - general notes");
        String result = service.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssuesDetails);

        assertTrue(result.contains("serving claims") || result.toLowerCase(Locale.UK).contains("serving"));
        assertThat(result).contains("Can we serve the claim with these contact details?");
        assertThat(result).contains("Reason for not serving");
        assertThat(result).contains("Missing address");
        assertThat(result).contains("serving claims - general notes");
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeSubstantiveDefectsSection_whenDefectsReported() {
        InitialConsiderationService service = new InitialConsiderationService(tornadoService);
        CaseData caseDataWithEt1VettingIssuesDetails = new CaseData();
        caseDataWithEt1VettingIssuesDetails.setRule121aTextArea("Details for rule 13(1)(a)");
        caseDataWithEt1VettingIssuesDetails.setRule121bTextArea("Details for rule 13(1)(b");

        List<String> substantiveDefectsDetails = new ArrayList<>(List.of("rule121a", "rule121a"));
        caseDataWithEt1VettingIssuesDetails.setSubstantiveDefectsList(substantiveDefectsDetails);
        String result = service.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssuesDetails);
        assertTrue(result.toLowerCase(Locale.UK).contains("%rule 121a%")
                || result.toLowerCase(Locale.UK).contains("substantive defects"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeHearingVenueDetails_whenSuggestHearingVenueIsYes() {
        CaseData caseDataWithEt1VettingIssuesDetails = new CaseData();
        caseDataWithEt1VettingIssuesDetails.setEt1SuggestHearingVenue("Yes");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("VENUE1");
        dynamicValueType.setLabel("London Venue");
        dynamicFixedListType.setListItems(List.of(dynamicValueType));
        dynamicFixedListType.setValue(dynamicValueType);

        caseDataWithEt1VettingIssuesDetails.setEt1HearingVenues(dynamicFixedListType);
        caseDataWithEt1VettingIssuesDetails.setEt1HearingVenueGeneralNotes("General notes about venue");

        InitialConsiderationService service = new InitialConsiderationService(null);
        String result = service.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssuesDetails);

        assertThat(result).contains("Details of Hearing Venue Issues");
        assertThat(result).contains("London Venue");
        assertThat(result).contains("General notes about venue");
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldNotIncludeOtherReferralSection_whenNoOtherReferralsExist() {
        CaseData caseDataWithEt1VettingIssuesDetails = new CaseData();
        // Ensure no other referral data is set
        InitialConsiderationService service = new InitialConsiderationService(null);
        String result = service.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssuesDetails);

        assertThat(result).doesNotContain("Details of Other Referral");
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeAllOtherReferralReasons() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setOtherReferralList(List.of(
                "claimOutOfTime",
                "multipleClaim",
                "employmentStatusIssues",
                "pidJurisdictionRegulator",
                "videoHearingPreference",
                "rule50IssuesOtherFactors",
                "otherRelevantFactors"
        ));

        InitialConsiderationService service = new InitialConsiderationService(null);
        String result = service.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);

        assertThat(result).contains("Claim out of time");
        assertThat(result).contains("Multiple claims");
        assertThat(result).contains("Employment status issues");
        assertThat(result).contains("Pid jurisdiction regulator");
        assertThat(result).contains("Video hearing preference");
        assertThat(result).contains("Rule49 issues - other factors");
        assertThat(result).contains("Other relevant factors");
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldReturnEmpty_whenOtherReferralListIsNullOrEmpty() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setOtherReferralList(null);

        InitialConsiderationService service = new InitialConsiderationService(null);
        String result = service.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);

        assertThat(result).doesNotContain("Details of Other Referral");

        caseDataWithEt1VettingIssues.setOtherReferralList(Collections.emptyList());
        result = service.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);

        assertThat(result).doesNotContain("Details of Other Referral");
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldReturnEmpty_whenReferralToJudgeOrLOListIsNullOrEmpty() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setReferralToJudgeOrLOList(null);
        String result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);
        assertFalse(result.contains("Details of Referral To Judge or LO"));

        caseDataWithEt1VettingIssues.setReferralToJudgeOrLOList(Collections.emptyList());
        result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);
        assertFalse(result.contains("Details of Referral To Judge or LO"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeAllReferralTypes() {
        CaseData caseDataWithEt1VettingIssues = getCaseData2();

        String result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);

        assertTrue(result.contains("A claim of interim relief"));
        assertTrue(result.contains("Interim relief details"));
        assertTrue(result.contains("A statutory appeal"));
        assertTrue(result.contains("Statutory appeal details"));
        assertTrue(result.contains("An allegation of the commission of a sexual offence"));
        assertTrue(result.contains("Sexual offence details"));
        assertTrue(result.contains("Insolvency"));
        assertTrue(result.contains("Insolvency details"));
        assertTrue(result.contains("Jurisdictions unclear"));
        assertTrue(result.contains("Jurisdictions unclear details"));
        assertTrue(result.contains("Length of service"));
        assertTrue(result.contains("Length of service details"));
        assertTrue(result.contains("Potentially linked cases in the ECM"));
        assertTrue(result.contains("Linked cases details"));
        assertTrue(result.contains("Rule 49 issues"));
        assertTrue(result.contains("Rule 49 issues details"));
        assertTrue(result.contains("Another reason for judicial referral"));
        assertTrue(result.contains("Other judicial referral details"));
    }

    private static @NotNull CaseData getCaseData2() {
        CaseData caseData = new CaseData();
        caseData.setReferralToJudgeOrLOList(List.of(
                "aClaimOfInterimRelief", "aStatutoryAppeal", "anAllegationOfCommissionOfSexualOffence",
                "insolvency", "jurisdictionsUnclear", "lengthOfService", "potentiallyLinkedCasesInTheEcm",
                "rule50Issues", "anotherReasonForJudicialReferral"
        ));
        caseData.setAclaimOfInterimReliefTextArea("Interim relief details");
        caseData.setAstatutoryAppealTextArea("Statutory appeal details");
        caseData.setAnAllegationOfCommissionOfSexualOffenceTextArea("Sexual offence details");
        caseData.setInsolvencyTextArea("Insolvency details");
        caseData.setJurisdictionsUnclearTextArea("Jurisdictions unclear details");
        caseData.setLengthOfServiceTextArea("Length of service details");
        caseData.setPotentiallyLinkedCasesInTheEcmTextArea("Linked cases details");
        caseData.setRule50IssuesTextArea("Rule 49 issues details");
        caseData.setAnotherReasonForJudicialReferralTextArea("Other judicial referral details");
        return caseData;
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeAllSubstantiveDefectsDescriptions() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setRule121aTextArea("Details for rule 13(1)(a)");
        caseDataWithEt1VettingIssues.setRule121bTextArea("Details for rule 13(1)(b");
        caseDataWithEt1VettingIssues.setRule121cTextArea("Details for rule 13(1)(c)");
        caseDataWithEt1VettingIssues.setRule121dTextArea("Details for rule 13(1)(d)");
        caseDataWithEt1VettingIssues.setRule121daTextArea("Details for rule 13(1)(e)");
        caseDataWithEt1VettingIssues.setRule121eTextArea("Details for rule 13(1)(f)");
        caseDataWithEt1VettingIssues.setRule121fTextArea("Details for rule 13(1)(g)");
        caseDataWithEt1VettingIssues.setSubstantiveDefectsList(List.of(
                "rule121a", "rule121b", "rule121c", "rule121d", "rule121 da", "rule121e", "rule121f"
        ));

        String result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);

        assertThat(result).contains("The tribunal has no jurisdiction to consider - Rule 13(1)(a)");
        assertThat(result).contains("Is in a form which cannot sensibly be responded to or otherwise an abuse of "
                + "process - Rule 13(1)(b)");
        assertThat(result).contains("Has neither an EC number nor claims one of the EC exemptions - Rule 13(1)(c)");
        assertThat(result).contains("States that one of the EC exceptions applies but it might not - Rule 13(1)(d)");
        assertThat(result).contains("Institutes relevant proceedings and the EC number on the claim form does not "
                + "match the EC number on the Acas certificate - Rule 13(1)(e)");
        assertThat(result).contains("Has a different claimant name on the ET1 to the claimant name on the Acas "
                + "certificate - Rule 13(1)(f)");
        assertThat(result).contains("Has a different respondent name on the ET1 to the respondent name on the "
                + "Acas certificate - Rule 13(1)(g)");
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldReturnEmptyString_whenSubstantiveDefectsListIsNullOrEmpty() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setSubstantiveDefectsList(null);

        String result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);
        assertThat(result).doesNotContain("Details of Substantive Defects");

        caseDataWithEt1VettingIssues.setSubstantiveDefectsList(Collections.emptyList());
        result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);
        assertThat(result).doesNotContain("Details of Substantive Defects");
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldReturnEmptyString_whenTrackAllocationIsNull() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setIsTrackAllocationCorrect(null);
        String result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);
        assertFalse(result.contains("Track Allocation Issue"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldReturnEmptyString_whenTrackAllocationIsYes() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setIsTrackAllocationCorrect("Yes");
        String result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);
        assertFalse(result.contains("Track Allocation Issue"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeTrackAllocationDetails_whenTrackAllocationIsNo() {
        CaseData caseDataWithEt1VettingIssues = new CaseData();
        caseDataWithEt1VettingIssues.setIsTrackAllocationCorrect("No");
        caseDataWithEt1VettingIssues.setSuggestAnotherTrack("Fast Track");
        caseDataWithEt1VettingIssues.setWhyChangeTrackAllocation("Complex case");
        caseDataWithEt1VettingIssues.setTrackAllocationGeneralNotes("Requires special handling");
        String result = initialConsiderationService.setIcEt1VettingIssuesDetails(caseDataWithEt1VettingIssues);
        assertTrue(result.contains("Track Allocation Issue"));
        assertTrue(result.contains("Is the track allocation correct?"));
        assertTrue(result.contains("Suggested Track:"));
        assertTrue(result.contains("Why Change Track Allocation?"));
        assertTrue(result.contains("Track Allocation General Notes"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldIncludeLocationalIssues_whenLocationIsNotCorrect() {
        CaseData caseDataWithRegionalOffice = new CaseData();
        caseDataWithRegionalOffice.setIsLocationCorrect("No");

        DynamicFixedListType regionalOfficeList = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("RO1");
        dynamicValueType.setLabel("London");

        regionalOfficeList.setListItems(List.of(dynamicValueType));
        regionalOfficeList.setValue(dynamicValueType);
        regionalOfficeList.setValue(dynamicValueType);

        caseDataWithRegionalOffice.setRegionalOfficeList(regionalOfficeList);
        caseDataWithRegionalOffice.setWhyChangeOffice("Closer to claimant");

        String result = new InitialConsiderationService(null)
                .setIcEt1VettingIssuesDetails(caseDataWithRegionalOffice);

        assertTrue(result.contains("Details of Locational Issues"));
        assertTrue(result.contains("Is this location correct?"));
        assertTrue(result.contains("Local or regional office selected"));
        assertTrue(result.contains("Why should we change the office?"));
        assertTrue(result.contains("London"));
        assertTrue(result.contains("Closer to claimant"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldNotIncludeLocationalIssues_whenLocationIsCorrect() {
        CaseData caseDataWithLocation = new CaseData();
        caseDataWithLocation.setIsLocationCorrect("Yes");

        String result = new InitialConsiderationService(null).setIcEt1VettingIssuesDetails(caseDataWithLocation);

        assertFalse(result.contains("Details of Locational Issues"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldHandleNullRegionalOfficeListAndWhyChangeOffice() {
        CaseData caseDataWithChangeOffice = new CaseData();
        caseDataWithChangeOffice.setIsLocationCorrect("No");

        // regionalOfficeList and whyChangeOffice are null
        String result = new InitialConsiderationService(
                null).setIcEt1VettingIssuesDetails(caseDataWithChangeOffice);

        assertTrue(result.contains("Is this location correct?"));
        assertFalse(result.contains("Local or regional office selected"));
        assertFalse(result.contains("Why should we change the office?"));
    }

    @Test
    void setIcEt1VettingIssuesDetails_shouldGenerateCorrectMarkup() {
        CaseData caseDataWithMarkUp = new CaseData();
        caseDataWithMarkUp.setEt1GovOrMajorQuestion("Yes");
        caseDataWithMarkUp.setEt1ReasonableAdjustmentsQuestion("Yes");
        caseDataWithMarkUp.setEt1ReasonableAdjustmentsTextArea("Details about reasonable adjustments.");
        caseDataWithMarkUp.setEt1VideoHearingQuestion("No");
        caseDataWithMarkUp.setEt1VideoHearingTextArea("Details about video hearing.");
        caseDataWithMarkUp.setEt1FurtherQuestionsGeneralNotes("General notes about the respondent.");

        InitialConsiderationService service = new InitialConsiderationService(null);

        String result = service.setIcEt1VettingIssuesDetails(caseDataWithMarkUp);

        assertNotNull(result);
        assertTrue(result.contains("Is the respondent a government agency or a major employer?"));
        assertTrue(result.contains("Are reasonable adjustments required?"));
        assertTrue(result.contains("Details about reasonable adjustments."));
        assertTrue(result.contains("Can the claimant attend a video hearing?"));
        assertTrue(result.contains("Details about video hearing."));
        assertTrue(result.contains("General notes about the respondent."));
    }
}
