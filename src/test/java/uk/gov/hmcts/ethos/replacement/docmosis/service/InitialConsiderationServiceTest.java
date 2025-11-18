package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.PHONE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CVP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CVP_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_NOT_LISTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JSA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_FINAL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_PRELIMINARY_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RESPONDENT_HEARING_PANEL_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.SEEK_COMMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.TELEPHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.UDL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CASE_DETAILS_URL_PARTIAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.NOT_AVAILABLE_FOR_VIDEO_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.REFERRALS_PAGE_FRAGMENT_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class InitialConsiderationServiceTest {
    private static final LocalDateTime EARLIEST_FUTURE_HEARING_DATE = LocalDateTime.now().plusDays(5);
    private static final LocalDateTime SECOND_FUTURE_HEARING_DATE = LocalDateTime.now().plusDays(9);

    private static final String EXPECTED_RESPONDENT_NAME = """
        | Respondent 1 name given | |
        |-------------|:------------|
        |In ET1 by claimant | Test Corp|
        |In ET3 by respondent | |
        
        """;

    private static final String EXPECTED_RESPONDENT_NAME_2 = """
        | Respondent 1 name given | |
        |-------------|:------------|
        |In ET1 by claimant | Test Corp|
        |In ET3 by respondent | |

        | Respondent 2 name given | |
        |-------------|:------------|
        |In ET1 by claimant | Test Name Two|
        |In ET3 by respondent | |

        """;

    private static final String EXPECTED_RESPONDENT_NAME_BLANK = """
        | Respondent  name given | |
        |-------------|:------------|
        |In ET1 by claimant | |
        |In ET3 by respondent | |

        """;

    private static final String EXPECTED_HEARING_STRING =
        "|Hearing details | |\n"
            + "|-------------|:------------|\n"
            + "|Date | "
            + EARLIEST_FUTURE_HEARING_DATE.toString("dd MMM yyyy")
            + "|\r\n"
            + "|Type | Hearing|\n"
            + "|Duration | 3.5 Hours|"
            + "\n\n";

    private static final String EXPECTED_HEARING_DETAILS_STRING = """
        |Hearing details | |
        |-------------|:------------|
        |Date | 16 May 2022|
        |Type | Hearing|
        |Duration | 60 Days|
        """;

    private static final String EXPECTED_HEARING_BLANK = String.format(HEARING_DETAILS, "-", "-", "-");

    private static final String EXPECTED_JURISDICTION_HTML = "<h2>Jurisdiction codes</h2><a "
            + "target=\"_blank\" href=\"https://judiciary.sharepoint.com/sites/empjudgesew/Shared%20Documents/Forms/"
            + "AllItems.aspx?id=%2Fsites%2Fempjudgesew%2FShared%20Documents%2FET%20Jurisdiction%20List%2F"
            + "Jurisdiction%20list%20October%202024.pdf&viewid=9cee6d50-61e5-4d87-92d2-8c9444f00c95&parent=%2F"
            + "sites%2Fempjudgesew%2FShared%20Documents%2FET%20Jurisdiction%20List\">View all jurisdiction "
            + "codes and descriptors (opens in new tab)"
            + "</a><br><br><h4>DAG</h4>Discrimination, including harassment or discrimination based on "
            + "association or perception on grounds of age<h4>SXD</h4>Discrimination, including "
            + "indirect discrimination, discrimination based on association or perception, or harassment on "
            + "grounds of sex, marriage and civil partnership<hr>";

    private static final String EXPECTED_JURISDICTION_SCOTLAND_HTML = "<h2>Jurisdiction codes</h2><a "
        + "target=\"_blank\" href=\"https://judiciary.sharepoint"
        + ".com/:w:/r/sites/ScotlandEJs/Shared%20Documents/Jurisdictional%20Codes%20List"
        + "/Jurisdiction%20list%20July%202024%20.doc?d=wfa6ba431b0b941ffa0b82504fd093af0&csf=1&web=1&e=Dm6Hda\">"
        + "View all jurisdiction codes and descriptors (opens in new tab)"
        + "</a><br><br><h4>DAG</h4>Discrimination, including harassment or discrimination "
        + "based on association or perception on grounds of age<h4>SXD</h4>"
        + "Discrimination, including indirect discrimination, discrimination based on association or perception, "
        + "or harassment on grounds of sex, marriage and civil partnership<hr>";

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
        CaseDetails caseDetails = new CaseDetails();
        caseData.setDocumentCollection(null);
        caseDetails.setCaseData(caseData);

        initialConsiderationService.initialiseInitialConsideration(caseDetails);

        assertThat(caseDetails.getCaseData().getInitialConsiderationBeforeYouStart()).isEmpty();
    }

    @Test
    void getRespondentNameWithPanelPreference_shouldReturnFormattedDetails_whenRespondentIsValid() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());
        respondent.getValue().setRespondentName("Test Corp");
        respondent.getValue().setResponseRespondentName("Test Response");

        String respondentDetails = initialConsiderationService.getRespondentName(List.of(respondent));
        assertThat(respondentDetails).isEqualTo(String.format(RESPONDENT_NAME, "1",
                "Test Corp", "Test Response"));
    }

    @Test
    void getRespondentNameDetails_shouldReturnFormattedDetails_whenRespondentHasNoResponseName() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());
        respondent.getValue().setRespondentName("Test Corp");

        String respondentDetails = initialConsiderationService.getRespondentName(List.of(respondent));
        assertThat(respondentDetails).isEqualTo(String.format(RESPONDENT_NAME, 1, "Test Corp", ""));
    }

    @Test
    void getRespondentNameDetails_shouldReturnFormattedDetails_whenRespondentHasNoName() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());
        respondent.getValue().setResponseRespondentName("Test Response");

        String respondentDetails = initialConsiderationService.getRespondentName(List.of(respondent));
        assertThat(respondentDetails).isEqualTo(String.format(RESPONDENT_NAME, 1, "", "Test Response"));
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
        respondent.setValue(new RespondentSumType());
        respondent.getValue().setRespondentHearingPanelPreference("judge");
        respondent.getValue().setRespondentHearingPanelPreferenceReason("I deserve it");

        String hearingPanelPreferenceDetails = initialConsiderationService.getIcRespondentHearingPanelPreference(
                List.of(respondent));
        assertThat(hearingPanelPreferenceDetails).isEqualTo(
                String.format(RESPONDENT_HEARING_PANEL_PREFERENCE, "judge", "I deserve it"));
    }

    @Test
    void getIcHearingPanelPreference_shouldReturnFormattedDetails_whenRespondentHasNoPreferenceAndNoReason() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());

        String hearingPanelPreferenceDetails = initialConsiderationService.getIcRespondentHearingPanelPreference(
                List.of(respondent));
        assertThat(hearingPanelPreferenceDetails).isEqualTo(
                String.format(RESPONDENT_HEARING_PANEL_PREFERENCE, "-", "-"));
    }

    @Test
    void getIcHearingPanelPreference_shouldReturnFormattedDetailsForMultipleRespondents() {
        RespondentSumTypeItem respondent1 = new RespondentSumTypeItem();
        respondent1.setValue(new RespondentSumType());
        respondent1.getValue().setRespondentHearingPanelPreference("judge");
        respondent1.getValue().setRespondentHearingPanelPreferenceReason("I deserve it");

        RespondentSumTypeItem respondent2 = new RespondentSumTypeItem();
        respondent2.setValue(new RespondentSumType());
        respondent2.getValue().setRespondentHearingPanelPreference("panel");
        respondent2.getValue().setRespondentHearingPanelPreferenceReason("Fair trial");

        String hearingPanelPreferenceDetails = initialConsiderationService.getIcRespondentHearingPanelPreference(
                List.of(respondent1, respondent2));
        assertThat(hearingPanelPreferenceDetails).isEqualTo(
                String.format(RESPONDENT_HEARING_PANEL_PREFERENCE, "judge", "I deserve it")
                        + String.format(RESPONDENT_HEARING_PANEL_PREFERENCE, "panel", "Fair trial"));
    }

    @Test
    void setRespondentDetails_shouldReturnFormattedDetails_whenRespondentCollectionIsValid() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());
        respondent.getValue().setRespondentName("Test Corp");
        respondent.getValue().setResponseRespondentName("Test Response");
        respondent.getValue().setRespondentHearingPanelPreference("Judge");
        respondent.getValue().setRespondentHearingPanelPreferenceReason("Fair trial");
        respondent.getValue().setEt3ResponseHearingRespondent(List.of("Video"));

        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(List.of(respondent));

        String result = initialConsiderationService.setRespondentDetails(caseData);

        assertThat(result).isEqualTo(
                String.format(RESPONDENT_NAME, 1, "Test Corp", "Test Response")
                        + String.format(RESPONDENT_HEARING_PANEL_PREFERENCE, "Judge", "Fair trial")
        );
    }

    @Test
    void setRespondentDetails_shouldIncludeNotAvailableForVideo_whenRespondentNotAvailableForVideo() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());
        respondent.getValue().setRespondentName("Test Corp");
        respondent.getValue().setResponseRespondentName("Test Response");
        respondent.getValue().setRespondentHearingPanelPreference("Judge");
        respondent.getValue().setRespondentHearingPanelPreferenceReason("Fair trial");
        respondent.getValue().setEt3ResponseHearingRespondent(List.of("Telephone"));

        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(List.of(respondent));

        String result = initialConsiderationService.setRespondentDetails(caseData);

        assertThat(result).isEqualTo(
                String.format(RESPONDENT_NAME, 1, "Test Corp", "Test Response")
                        + String.format(RESPONDENT_HEARING_PANEL_PREFERENCE, "Judge", "Fair trial")
                        + NOT_AVAILABLE_FOR_VIDEO_HEARINGS.toUpperCase(Locale.UK)
        );
    }

    @Test
    void setRespondentDetails_shouldReturnEmptyString_whenRespondentCollectionIsEmpty() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>());

        String result = initialConsiderationService.setRespondentDetails(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void setRespondentDetails_shouldHandleNullRespondentCollection() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(null);

        String result = initialConsiderationService.setRespondentDetails(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void setRespondentDetails_shouldHandleNullValuesInRespondent() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(new RespondentSumType());
        respondent.getValue().setRespondentName(null);
        respondent.getValue().setResponseRespondentName(null);
        respondent.getValue().setRespondentHearingPanelPreference(null);
        respondent.getValue().setRespondentHearingPanelPreferenceReason(null);
        respondent.getValue().setEt3ResponseHearingRespondent(null);

        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(List.of(respondent));

        String result = initialConsiderationService.setRespondentDetails(caseData);

        assertThat(result).isEqualTo(
                String.format(RESPONDENT_NAME, 1, "", "")
                        + String.format(RESPONDENT_HEARING_PANEL_PREFERENCE, "-", "-")
                        + NOT_AVAILABLE_FOR_VIDEO_HEARINGS.toUpperCase(Locale.UK)
        );
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
        String hearingDetails = initialConsiderationService.getHearingDetails(caseData.getHearingCollection());
        assertThat(hearingDetails)
            .isEqualTo(EXPECTED_HEARING_BLANK);
    }

    @Test
    void getHearingDetailsTest() {
        setFutureHearingDate(caseData);
        String hearingDetails = initialConsiderationService.getHearingDetails(caseData.getHearingCollection());
        assertThat(hearingDetails)
            .isEqualTo(EXPECTED_HEARING_STRING);
    }

    @Test
    void getRespondentNameTest() {
        String respondentName = initialConsiderationService.getRespondentName(caseData.getRespondentCollection());
        assertThat(respondentName)
            .isEqualTo(EXPECTED_RESPONDENT_NAME);
    }

    @Test
    void getRespondentTwoNameTest() {
        caseData = CaseDataBuilder.builder()
                .withRespondent("Test Corp", YES, "2022-03-01", false)
                .withRespondent("Test Name Two", YES, "2022-03-01", false)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID).getCaseData();
        String respondentName = initialConsiderationService.getRespondentName(caseData.getRespondentCollection());
        assertThat(respondentName).isEqualTo(EXPECTED_RESPONDENT_NAME_2);
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
        String hearingDetails = initialConsiderationService.getHearingDetails(caseDataEmpty.getHearingCollection());
        assertThat(hearingDetails)
            .isEqualTo(EXPECTED_HEARING_BLANK);
    }

    @Test
    void missingJurisdictionCollectionTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(caseDataEmpty.getJurCodesCollection(),
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(jurisdictionCodesHtml)
            .isEmpty();
    }

    @Test
    void invalidJurisdictionCollectionTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(generateInvalidJurisdictionCodes(),
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(jurisdictionCodesHtml)
            .isEmpty();
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
    void missingRespondentCollectionTest() {
        String respondentName =
            initialConsiderationService.getRespondentName(caseDataEmpty.getRespondentCollection());
        assertThat(respondentName)
            .isEqualTo(EXPECTED_RESPONDENT_NAME_BLANK);
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
    void getClaimantHearingPanelPreferenceTest() {
        ClaimantHearingPreference preference = new ClaimantHearingPreference();
        preference.setClaimantHearingPanelPreference("Preference");
        preference.setClaimantHearingPanelPreferenceWhy("Reason");
        preference.setHearingPreferences(List.of(VIDEO, PHONE));
        caseData.setClaimantHearingPreference(preference);

        String result = String.format(initialConsiderationService.getClaimantHearingPanelPreference(
                caseData.getClaimantHearingPreference()));

        String expected = """
            |Claimant's hearing panel preference | |
            |-------------|:------------|
            |Panel Preference | Preference|
            |Reason for Panel Preference | Reason|
            """;
        assertEquals(expected, result);
    }

    @Test
    void getClaimantHearingPanelPreferenceTest_No_Video_Hearing() {
        ClaimantHearingPreference preference = new ClaimantHearingPreference();
        preference.setClaimantHearingPanelPreference("Preference");
        preference.setClaimantHearingPanelPreferenceWhy("Reason");
        caseData.setClaimantHearingPreference(preference);

        String result = String.format(initialConsiderationService.getClaimantHearingPanelPreference(
                caseData.getClaimantHearingPreference()));

        String expected = """
            |Claimant's hearing panel preference | |
            |-------------|:------------|
            |Panel Preference | Preference|
            |Reason for Panel Preference | Reason|
            
            NOT AVAILABLE FOR VIDEO HEARINGS
            """;
        assertEquals(expected, result);
    }

    @Test
    void getClaimantHearingPanelPreference_NullPreference() {
        String result = initialConsiderationService.getClaimantHearingPanelPreference(null);

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
    void getEarliestHearingDateForListedHearings_shouldReturnEarliestFutureDate_whenValidListedHearingsExist() {
        List<DateListedTypeItem> hearingDates = List.of(
                createDate("2026-10-15T10:00:00.000", "Listed"),
                createDate("2026-10-10T10:00:00.000", "Listed"),
                createDate("2026-10-20T10:00:00.000", "Listed")
        );

        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(hearingDates);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    @Test
    void getEarliestHearingDateForListedHearings_shouldReturnEmpty_whenNoListedHearingsExist() {
        List<DateListedTypeItem> hearingDates = List.of(
                createDate("2023-10-15T10:00:00.000", "Not Listed"),
                createDate("2023-10-10T10:00:00.000", "Cancelled")
        );

        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(hearingDates);

        assertThat(result).isEmpty();
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
        Optional<LocalDate> result = initialConsiderationService.getEarliestHearingDateForListedHearings(null);

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
        assertThat(result.get()).isEqualTo(LocalDate.of(2026, 10, 15));
    }

    @Test
    void clearOldEtICHearingListedAnswersValues_clearsAllFieldsWhenAnswersExist() {
        EtICHearingListedAnswers hearingListedAnswers = getEtICHearingListedAnswers();

        CaseData caseDataForListedAnswers = new CaseData();
        caseDataForListedAnswers.setEtICHearingListedAnswers(hearingListedAnswers);
        caseDataForListedAnswers.setEtInitialConsiderationHearing("SomeHearing");

        initialConsiderationService.clearOldEtICHearingListedAnswersValues(caseDataForListedAnswers);

        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtInitialConsiderationListedHearingType());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICIsHearingWithJsaReasonOther());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICIsHearingWithMembers());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICJsaFinalHearingReasonOther());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICMembersFinalHearingReasonOther());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers()
                .getEtICIsHearingWithJudgeOrMembersFurtherDetails());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICIsHearingWithJudgeOrMembersReason());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers()
                .getEtICIsFinalHearingWithJudgeOrMembersJsaReason());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers()
                .getEtICIsFinalHearingWithJudgeOrMembersReason());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICIsHearingWithJsa());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICHearingListed());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers().getEtICIsHearingWithJudgeOrMembers());
        assertNull(caseDataForListedAnswers.getEtICHearingListedAnswers()
                .getEtICIsHearingWithJudgeOrMembersReasonOther());
        assertNull(caseDataForListedAnswers.getEtInitialConsiderationHearing());
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
        et1Document.getValue().setDocumentType(ET1_DOC_TYPE);
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
        et1Document.getValue().setDocumentType(ET1_DOC_TYPE);
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
        et1Document.getValue().setDocumentType(ET1_DOC_TYPE);
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

        initialConsiderationService.clearOldEtICHearingListedAnswersValues(caseDataLocal);

        assertNull(caseDataLocal.getEtICHearingListedAnswers());
        assertEquals("SomeHearing", caseDataLocal.getEtInitialConsiderationHearing());
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingCollectionIsNull() {
        String result = initialConsiderationService.getHearingDetails(null);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenHearingCollectionIsEmpty() {
        List<HearingTypeItem> hearingCollection = new ArrayList<>();

        String result = initialConsiderationService.getHearingDetails(hearingCollection);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getHearingDetails_shouldReturnMissingMessage_whenNoListedHearingsExist() {
        List<HearingTypeItem> hearingCollection = List.of(
                createHearingTypeItem("2023-10-15T10:00:00.000", "Not Listed", "Hearing Type 1", "Hours", "3"),
                createHearingTypeItem("2023-10-10T10:00:00.000", "Cancelled", "Hearing Type 2", "Hours", "2")
        );

        String result = initialConsiderationService.getHearingDetails(hearingCollection);
        assertThat(result).isEqualTo(HEARING_MISSING);
    }

    @Test
    void getHearingDetails_shouldReturnFormattedDetails_whenValidHearingCollectionExists() {
        List<HearingTypeItem> hearingCollection = List.of(
                createHearingTypeItem("2026-10-15T10:00:00.000", "Listed",
                        "Hearing Type 1", "Hours", "3"),
                createHearingTypeItem("2026-10-10T10:00:00.000", "Listed",
                        "Hearing Type 2", "Hours", "2")
        );

        String result = initialConsiderationService.getHearingDetails(hearingCollection);
        String detail = String.format(HEARING_DETAILS, "10 Oct 2026", "Hearing Type 2", "2 Hours");
        assertThat(result).isEqualTo(detail);
    }

    @Test
    void getHearingDetails_shouldIgnoreInvalidDateFormats() {
        List<HearingTypeItem> hearingCollection = List.of(
                createHearingTypeItem("InvalidDate", "Listed", "Hearing Type 1", "Hours", "3"),
                createHearingTypeItem("2026-10-15T10:00:00.000", "Listed", "Hearing Type 2", "Hours", "2")
        );

        String result = initialConsiderationService.getHearingDetails(hearingCollection);
        String detail = String.format(HEARING_DETAILS, "15 Oct 2026", "Hearing Type 2", "2 Hours");
        assertThat(result).isEqualTo(detail);
    }

    private HearingTypeItem createHearingTypeItem(String date, String status, String type, String lengthType, String duration) {
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
}
