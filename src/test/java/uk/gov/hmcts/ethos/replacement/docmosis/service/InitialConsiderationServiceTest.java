package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICSeekComments;
import uk.gov.hmcts.et.common.model.ccd.EtIcudlHearing;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule27;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule28;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@SuppressWarnings({"PMD.UseProperClassLoader", "PMD.LinguisticNaming", "PMD.TooManyMethods", "PMD.ExcessiveImports"})
@ExtendWith(SpringExtension.class)
class InitialConsiderationServiceTest {
    private static final String EXPECTED_RESPONDENT_NAME =
        "| Respondent  name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | Test Corp|\r\n"
            + "|In ET3 by respondent | |\r\n"
            + "\r\n";

    private static final String EXPECTED_RESPONDENT_NAME_2 =
        "| Respondent 1 name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | Test Corp|\r\n"
            + "|In ET3 by respondent | |\r\n"
            + "\r\n"
            + "| Respondent 2 name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | Test Name Two|\r\n"
            + "|In ET3 by respondent | |\r\n"
            + "\r\n";

    private static final String EXPECTED_RESPONDENT_NAME_BLANK =
        "| Respondent  name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | |\r\n"
            + "|In ET3 by respondent | |\r\n"
            + "\r\n";

    private static final String EXPECTED_HEARING_STRING =
        "|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | 01 Jul 2022|\r\n"
            + "|Type | Preliminary Hearing(CM)|\r\n"
            + "|Duration | 1.5 Hours|";

    private static final String EXPECTED_HEARING_BLANK =
        "|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | -|\r\n"
            + "|Type | -|\r\n"
            + "|Duration | -|";

    private static final String EXPECTED_JURISDICTION_HTML = "<h2>Jurisdiction codes</h2><a "
        + "target=\"_blank\" href=\"https://judiciary.sharepoint"
        + ".com/:b:/s/empjudgesew/EZowDqUAYpBEl9NkTirLUdYBjXdpi3-7b18HlsDqZNV3xA?e=tR7Wof\">View all jurisdiction "
        + "codes and descriptors (opens in new tab)"
        + "</a><br><br><strong>DAG</strong> - Discrimination, including indirect discrimination, harassment or "
        + "victimisation or discrimination based on association or perception on grounds of "
        + "age<br><br><strong>SXD</strong> - Discrimination, including indirect discrimination, discrimination based on"
        + " association or perception, harassment or victimisation on grounds of sex, marriage and civil partnership or"
        + " gender reassignment<br><br><hr>";

    private static final String EXPECTED_JURISDICTION_SCOTLAND_HTML = "<h2>Jurisdiction codes</h2><a "
        + "target=\"_blank\" href=\"https://judiciary.sharepoint"
        + ".com/:b:/r/sites/ScotlandEJs/Shared%20Documents/Jurisdictional%20Codes%20List/ET%20jurisdiction%20list%20"
        + "(2019).pdf?csf=1&web=1&e=9bCQ8P\">View all jurisdiction codes and descriptors (opens in new tab)"
        + "</a><br><br><strong>DAG</strong> - Discrimination, including indirect discrimination, harassment or "
        + "victimisation or discrimination based on association or perception on grounds of "
        + "age<br><br><strong>SXD</strong> - Discrimination, including indirect discrimination, discrimination based on"
        + " association or perception, harassment or victimisation on grounds of sex, marriage and civil partnership or"
        + " gender reassignment<br><br><hr>";

    private CaseData caseDataEmpty;
    private CaseData caseData;
    private DocumentInfo documentInfo;

    private InitialConsiderationService initialConsiderationService;
    @MockBean
    private TornadoService tornadoService;
    @MockBean
    private DocumentManagementService documentManagementService;

    @BeforeEach
    void setUp() throws Exception {
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
    void getEarliestHearingDate() {
        assertThat(initialConsiderationService.getEarliestHearingDate(new ArrayList<>()))
            .isEmpty();
        assertThat(initialConsiderationService.getEarliestHearingDate(generateHearingDates()))
            .isEqualTo(Optional.of(LocalDate.of(2022, 1, 7)));
        assertThat(initialConsiderationService.getEarliestHearingDate(generateHearingDatesWithEmpty()))
            .isEqualTo(Optional.of(LocalDate.of(2022, 1, 7)));
    }

    @Test
    void getHearingDetailsTest() {
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
        assertThat(respondentName)
                .isEqualTo(EXPECTED_RESPONDENT_NAME_2);
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
        assertThrows(Exception.class, () -> initialConsiderationService.generateDocument(new CaseData(), "userToken",
                ENGLANDWALES_CASE_TYPE_ID));
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

        initialConsiderationService.clearHiddenValue(caseData, SCOTLAND_CASE_TYPE_ID);

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

        initialConsiderationService.clearHiddenValue(caseData, SCOTLAND_CASE_TYPE_ID);

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

        initialConsiderationService.clearHiddenValue(caseData, SCOTLAND_CASE_TYPE_ID);

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
    }

    @Test
    void setIsHearingAlreadyListed_shouldBeSetToNo_whenNoHearings() {
        caseData.setEtInitialConsiderationHearing("|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | -|\r\n"
            + "|Type | -|\r\n"
            + "|Duration | -|");

        initialConsiderationService.setIsHearingAlreadyListed(caseData, SCOTLAND_CASE_TYPE_ID);
        assertThat(caseData.getEtICHearingAlreadyListed()).isEqualTo(NO);
    }

    @Test
    void setIsHearingAlreadyListed_shouldBeSetToYes_whenThereAreHearings() {
        caseData.setEtInitialConsiderationHearing("|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | 16 May 2022|\r\n"
            + "|Type | Hearing|\r\n"
            + "|Duration | 60 Days|");

        initialConsiderationService.setIsHearingAlreadyListed(caseData, SCOTLAND_CASE_TYPE_ID);
        assertThat(caseData.getEtICHearingAlreadyListed()).isEqualTo(YES);
    }

    @Test
    void setIsHearingAlreadyListed_shouldIgnoreEntirely_whenCaseTypeIsEnglandWales() {
        caseData.setEtInitialConsiderationHearing("|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | 16 May 2022|\r\n"
            + "|Type | Hearing|\r\n"
            + "|Duration | 60 Days|");

        initialConsiderationService.setIsHearingAlreadyListed(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getEtICHearingAlreadyListed()).isNull();
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

    private CaseData generateCaseData(String fileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
            .getResource(fileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseData.class);
    }

    private List<DateListedTypeItem> generateHearingDates() {
        return List.of(createDate("2022-07-15T10:00:00.000"),
            createDate("2022-07-15T10:00:00.000"),
            createDate("2022-05-20T10:00:00.000"),
            createDate("2022-03-22T10:00:00.000"),
            createDate("2022-01-07T10:00:00.000"));
    }

    private List<DateListedTypeItem> generateHearingDatesWithEmpty() {
        return List.of(createDate("2022-07-15T10:00:00.000"),
            createDate("2022-07-15T10:00:00.000"),
            new DateListedTypeItem(),
            createDate("2022-03-22T10:00:00.000"),
            createDate("2022-01-07T10:00:00.000"));
    }

    private DateListedTypeItem createDate(String dateString) {
        DateListedTypeItem hearingDate = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(dateString);
        hearingDate.setValue(dateListedType);

        return hearingDate;
    }
}
