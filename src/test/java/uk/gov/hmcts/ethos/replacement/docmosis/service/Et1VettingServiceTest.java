package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.VettingJurisdictionCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice.JpaVenueService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.LEEDS;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.LONDON_CENTRAL;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.MANCHESTER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ADDRESS_NOT_ENTERED;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.TRACK_OPEN;

@ExtendWith(SpringExtension.class)
class Et1VettingServiceTest {

    @InjectMocks
    private Et1VettingService et1VettingService;

    private CaseDetails caseDetails;
    private DocumentInfo documentInfo;

    @Mock
    private JpaVenueService jpaVenueService;

    @Mock
    private TornadoService tornadoService;

    private static final String ET1_DOC_TYPE = "ET1";
    private static final String ACAS_DOC_TYPE = "ACAS Certificate";
    private static final String ET1_ATTACHMENT_NAME = "doc1.docx";
    private static final String ET1_ATTACHMENT_DOC_TYPE = "ET1 Attachment";
    private static final String OFFICE = "Manchester";
    private static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s%s";
    private static final String BEFORE_LABEL_ET1 =
            "<br><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS =
            "<br><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ET1_ATTACHMENT =
            "<br><a target=\"_blank\" href=\"%s\">%s (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS_OPEN_TAB =
            "<br><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
                    + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
    private static final String CLAIMANT_DETAILS_PERSON = "<hr><h3>Claimant</h3>"
            + "<pre>First name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Last name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre>";
    private static final String CLAIMANT_DETAILS_COMPANY = "<hr><h3>Claimant</h3>"
        + "<pre>Company name &#09&#09&nbsp; %s"
        + "<br><br>Contact address &#09&#09 %s</pre>";

    private static final String RESPONDENT_DETAILS = "<h3>Respondent %s</h3>"
            + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String BR_WITH_TAB = "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 ";
    private static final String DAG = JurisdictionCode.DAG.name();
    private static final String PID = JurisdictionCode.PID.name();

    private static final String EXPECTED_ADDRESSES_HTML = "<hr><h2>Listing details<hr><h3>Claimant</h3>"
        + "<pre>Contact address &#09&#09 232 Petticoat Square<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 3 House<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 London<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 W10 4AG</pre><br>"
        + "<pre>Work address &#09&#09&#09 11 Small Street<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 22 House<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 M12 42R</pre><hr>"
        + "<h3>Respondent</h3><pre>Contact address &#09&#09 11 Small Street"
        + "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 22 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M12 42R</pre><hr>";
    private static final String EXPECTED_ADDRESSES_HTML_NO_WORK_ADDRESS = "<hr><h2>Listing details<hr><h3>Claimant</h3>"
            + "<pre>Contact address &#09&#09 232 Petticoat Square<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 3 House<br>"
            + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 London<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 W10 4AG</pre>"
            + "<hr><h3>Respondent</h3><pre>Contact address &#09&#09 11 Small Street"
            + "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 22 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
            + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M12 42R</pre><hr>";
    private static final String EXPECTED_RESPONDENT1_ACAS_DETAILS = "<hr><h3>Respondent 1</h3><pre>Name "
        + "&#09&#09&#09&#09&#09&#09&nbsp; Antonio Vazquez<br><br>Contact address &#09&#09 11 Small Street<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 22 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M12 42R</pre><h3>Acas certificate</h3>Certificate number 1234/5678/90 "
        + "has been provided.<br><br><br>";
    private static final String EXPECTED_RESPONDENT2_ACAS_DETAILS = "<hr><h3>Respondent 2</h3><pre>Name "
        + "&#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Sweet Street<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><h3>Acas certificate</h3>Certificate number 2987/6543/01 "
        + "has been provided.<br><br><br>";
    private static final String EXPECTED_RESPONDENT3_ACAS_DETAILS = "<hr><h3>Respondent 3</h3><pre>Name "
        + "&#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Sweet Street<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><h3>Acas certificate</h3>No certificate has been provided."
        + "<br><br><br>";
    private static final String EXPECTED_RESPONDENT4_ACAS_DETAILS = "<hr><h3>Respondent 4</h3><pre>Name "
        + "&#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Sweet Street<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><h3>Acas certificate</h3>No certificate has been provided."
        + "<br><br><br>";
    private static final String EXPECTED_RESPONDENT5_ACAS_DETAILS = "<hr><h3>Respondent 5</h3><pre>Name "
        + "&#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Sweet Street<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><h3>Acas certificate</h3>No certificate has been provided."
        + "<br><br><br>";
    private static final String EXPECTED_RESPONDENT6_ACAS_DETAILS = "<hr><h3>Respondent 6</h3><pre>Name "
        + "&#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Sweet Street<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>"
        + "&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><h3>Acas certificate</h3>No certificate has been provided."
        + "<br><br><br>";
    private static final String EXPECTED_RESPONDENT_DETAILS = "<h3>Respondent 1</h3><pre>Name &#09&#09&#09&#09&#09&#0"
        + "9&nbsp; Antonio Vazquez<br><br>Contact address &#09&#09 11 Small Street<br>&#09&#09&#09&#09&#09&#09&#09&#09"
        + "&#09 22 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 M1"
        + "2 42R</pre><hr><h3>Respondent 2</h3><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact add"
        + "ress &#09&#09 32 Sweet Street<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&"
        + "#09&#09&#09 Manchester<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><hr><h3>Respondent 3</h3><pre>N"
        + "ame &#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Sweet Street<br>&#09&#09&"
        + "#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>&#09&#09&#09&#09"
        + "&#09&#09&#09&#09&#09 M11 4ED</pre><hr><h3>Respondent 4</h3><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; Juan Ga"
        + "rcia<br><br>Contact address &#09&#09 32 Sweet Street<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#"
        + "09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><hr><h3>R"
        + "espondent 5</h3><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Swe"
        + "et Street<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchest"
        + "er<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 M11 4ED</pre><hr><h3>Respondent 6</h3><pre>Name &#09&#09&#09&#09"
        + "&#09&#09&nbsp; Juan Garcia<br><br>Contact address &#09&#09 32 Sweet Street<br>&#09&#09&#09&#09&#09&#09&#09&"
        + "#09&#09 14 House<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Manchester<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09"
        + " M11 4ED</pre><hr>";
    private static final String ET1_BINARY_URL_1 = "/documents/et1o0c3e-4efd-8886-0dca-1e3876c3178c/binary";
    private static final String ACAS_BINARY_URL_1 = "/documents/acas1111-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private static final String ACAS_BINARY_URL_2 = "/documents/acas2222-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private static final String ACAS_BINARY_URL_3 = "/documents/acas3333-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private static final String ACAS_BINARY_URL_4 = "/documents/acas4444-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private static final String ACAS_BINARY_URL_5 = "/documents/acas5555-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private static final String ET1_ATTACHMENT_BINARY_URL =
            "/documents/et1-attachment1234-4ef8ca1e3-8c60-d3d78808dca3/binary";
    private static final String CASE_ID = "1655312312192821";

    private static final String CASE_NAME_AND_DESCRIPTION_HTML = "<h4>%s</h4>%s";
    private static final String ERROR_EXISTING_JUR_CODE = "Jurisdiction code %s already exists.";
    private static final String ERROR_SELECTED_JUR_CODE = "Jurisdiction code %s is selected more than once.";
    private static final String TRIBUNAL_ENGLAND = "England & Wales";
    private static final String TRIBUNAL_OFFICE_LOCATION = "<hr><h3>Tribunal location</h3>"
        + "<pre>Tribunal &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Office &#09&#09&#09&#09&#09 %s</pre><hr>";
    private static final String TRIBUNAL_LOCATION_LABEL = "**<big>%s regional office</big>**";
    private static final String TRACK_ALLOCATION_HTML = "|||\r\n|--|--|\r\n|Track allocation|%s|\r\n";
    private static final String JUR_CODE_HTML = "<hr><h3>Jurisdiction Codes</h3>"
        + "<a target=\"_blank\" href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">"
        + "View all jurisdiction codes and descriptors (opens in new tab)</a><hr>"
        + "<h3>Codes already added</h3>%s<hr>";

    @BeforeEach
    void setUp() {
        et1VettingService = new Et1VettingService(tornadoService, jpaVenueService);
        caseDetails = CaseDataBuilder.builder()
            .withClaimantIndType("Doris", "Johnson")
            .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
            .withClaimantWorkAddress("11 Small Street", "22 House", null,
                "Manchester", "M12 42R", "United Kingdom")
            .withRespondentWithAddress("Antonio Vazquez",
                    "11 Small Street", "22 House", null,
                    "Manchester", "M12 42R", "United Kingdom",
                    "1234/5678/90")
            .withRespondentWithAddress("Juan Garcia",
                    "32 Sweet Street", "14 House", null,
                    "Manchester", "M11 4ED", "United Kingdom",
                    "2987/6543/01")
            .withRespondentWithAddress("Juan Garcia",
                    "32 Sweet Street", "14 House", null,
                    "Manchester", "M11 4ED", "United Kingdom",
                    null)
            .withRespondentWithAddress("Juan Garcia",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withRespondentWithAddress("Juan Garcia",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withRespondentWithAddress("Juan Garcia",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);
    }

    @Test
    void initialBeforeLinkLabel_ZeroAcas_shouldReturnEt1Only() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, ET1_BINARY_URL_1));
        documentTypeItemList.add(createDocumentTypeItem(ET1_ATTACHMENT_DOC_TYPE, ET1_ATTACHMENT_BINARY_URL));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE, String.format(BEFORE_LABEL_ET1, ET1_BINARY_URL_1), "",
                String.format(BEFORE_LABEL_ET1_ATTACHMENT, ET1_ATTACHMENT_BINARY_URL, ET1_ATTACHMENT_NAME));
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeLinkLabel_FiveAcas_shouldReturnFiveAcas() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_1));
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, ET1_BINARY_URL_1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_2));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_3));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_4));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_5));
        documentTypeItemList.add(createDocumentTypeItem(ET1_ATTACHMENT_DOC_TYPE, ET1_ATTACHMENT_BINARY_URL));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE,
                String.format(BEFORE_LABEL_ET1, ET1_BINARY_URL_1),
                String.format(BEFORE_LABEL_ACAS, ACAS_BINARY_URL_1, "1")
                        + String.format(BEFORE_LABEL_ACAS, ACAS_BINARY_URL_2, "2")
                        + String.format(BEFORE_LABEL_ACAS, ACAS_BINARY_URL_3, "3")
                        + String.format(BEFORE_LABEL_ACAS, ACAS_BINARY_URL_4, "4")
                        + String.format(BEFORE_LABEL_ACAS, ACAS_BINARY_URL_5, "5"),
                String.format(BEFORE_LABEL_ET1_ATTACHMENT, ET1_ATTACHMENT_BINARY_URL, ET1_ATTACHMENT_NAME));
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeLinkLabel_SixAcas_shouldReturnDocTab() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, ET1_BINARY_URL_1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_2));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_3));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_4));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, ACAS_BINARY_URL_5));
        documentTypeItemList.add(createDocumentTypeItem(ET1_ATTACHMENT_DOC_TYPE, ET1_ATTACHMENT_BINARY_URL));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE,
                "/documents/acas6666-4ef8ca1e3-8c60-d3d78808dca1/binary"));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE,
                String.format(BEFORE_LABEL_ET1, ET1_BINARY_URL_1),
                String.format(BEFORE_LABEL_ACAS_OPEN_TAB, CASE_ID),
                String.format(BEFORE_LABEL_ET1_ATTACHMENT, ET1_ATTACHMENT_BINARY_URL, ET1_ATTACHMENT_NAME));
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_NoDocumentCollection_shouldReturnWithoutUrl() {
        caseDetails.getCaseData().setDocumentCollection(null);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE, "", "", "");
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_ClaimantPersonDetails_shouldReturnMarkUp() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(CLAIMANT_DETAILS_PERSON, "Doris", "Johnson",
                "232 Petticoat Square" + BR_WITH_TAB + "3 House" + BR_WITH_TAB + "London" + BR_WITH_TAB + "W10 4AG");
        assertThat(caseDetails.getCaseData().getEt1VettingClaimantDetailsMarkUp())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_ClaimantCompanyDetails_shouldReturnMarkUp() {
        caseDetails.getCaseData().getClaimantIndType().setClaimantLastName(null);
        caseDetails.getCaseData().getClaimantIndType().setClaimantFirstNames(null);
        caseDetails.getCaseData().setClaimantTypeOfClaimant("Company");
        caseDetails.getCaseData().setClaimantCompany("Johnson's Company");
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(CLAIMANT_DETAILS_COMPANY, "Johnson's Company",
            "232 Petticoat Square" + BR_WITH_TAB + "3 House" + BR_WITH_TAB + "London" + BR_WITH_TAB + "W10 4AG");
        assertThat(caseDetails.getCaseData().getEt1VettingClaimantDetailsMarkUp())
            .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_OneRespondentDetails_shouldReturnMarkUp() {
        caseDetails = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(RESPONDENT_DETAILS, "", "Antonio Vazquez",
                "11 Small Street" + BR_WITH_TAB + "22 House" + BR_WITH_TAB + "Manchester" + BR_WITH_TAB + "M12 42R");
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentDetailsMarkUp())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_returnRespondentDetailsMarkUp() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentDetailsMarkUp())
                .isEqualTo(EXPECTED_RESPONDENT_DETAILS);
    }

    @Test
    void initialBeforeYouStart_returnRespondentSixAcasDetailsMarkUp() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails1())
            .isEqualTo(EXPECTED_RESPONDENT1_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails2())
            .isEqualTo(EXPECTED_RESPONDENT2_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails3())
            .isEqualTo(EXPECTED_RESPONDENT3_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails4())
            .isEqualTo(EXPECTED_RESPONDENT4_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails5())
            .isEqualTo(EXPECTED_RESPONDENT5_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails6())
            .isEqualTo(EXPECTED_RESPONDENT6_ACAS_DETAILS);
    }

    @Test
    void initialBeforeYouStart_returnFiveRespondentAcasDetailsMarkUp() {
        caseDetails.getCaseData().getRespondentCollection().remove(5);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails1())
                .isEqualTo(EXPECTED_RESPONDENT1_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails2())
                .isEqualTo(EXPECTED_RESPONDENT2_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails3())
                .isEqualTo(EXPECTED_RESPONDENT3_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails4())
                .isEqualTo(EXPECTED_RESPONDENT4_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails5())
                .isEqualTo(EXPECTED_RESPONDENT5_ACAS_DETAILS);
    }

    @Test
    void initialBeforeYouStart_returnFourRespondentAcasDetailsMarkUp() {
        caseDetails.getCaseData().getRespondentCollection().remove(5);
        caseDetails.getCaseData().getRespondentCollection().remove(4);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails1())
                .isEqualTo(EXPECTED_RESPONDENT1_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails2())
                .isEqualTo(EXPECTED_RESPONDENT2_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails3())
                .isEqualTo(EXPECTED_RESPONDENT3_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails4())
                .isEqualTo(EXPECTED_RESPONDENT4_ACAS_DETAILS);
    }

    @Test
    void initialBeforeYouStart_returnThreeRespondentAcasDetailsMarkUp() {
        caseDetails.getCaseData().getRespondentCollection().remove(5);
        caseDetails.getCaseData().getRespondentCollection().remove(4);
        caseDetails.getCaseData().getRespondentCollection().remove(3);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails1())
                .isEqualTo(EXPECTED_RESPONDENT1_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails2())
                .isEqualTo(EXPECTED_RESPONDENT2_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails3())
                .isEqualTo(EXPECTED_RESPONDENT3_ACAS_DETAILS);
    }

    @Test
    void initialBeforeYouStart_returnTwoRespondentAcasDetailsMarkUp() {
        caseDetails.getCaseData().getRespondentCollection().remove(5);
        caseDetails.getCaseData().getRespondentCollection().remove(4);
        caseDetails.getCaseData().getRespondentCollection().remove(3);
        caseDetails.getCaseData().getRespondentCollection().remove(2);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails1())
                .isEqualTo(EXPECTED_RESPONDENT1_ACAS_DETAILS);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails2())
                .isEqualTo(EXPECTED_RESPONDENT2_ACAS_DETAILS);
    }

    @Test
    void initialBeforeYouStart_returnOneRespondentAcasDetailsMarkUp() {
        caseDetails.getCaseData().getRespondentCollection().remove(5);
        caseDetails.getCaseData().getRespondentCollection().remove(4);
        caseDetails.getCaseData().getRespondentCollection().remove(3);
        caseDetails.getCaseData().getRespondentCollection().remove(2);
        caseDetails.getCaseData().getRespondentCollection().remove(1);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails1())
                .isEqualTo(EXPECTED_RESPONDENT1_ACAS_DETAILS);
    }

    @Test
    void initialBeforeYouStart_ClaimantHearingPreferenceIsNull_Et1ReasonableAdjustmentsQuestionReturnNull() {
        caseDetails.getCaseData().setClaimantHearingPreference(null);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1ReasonableAdjustmentsQuestion()).isNull();
        assertThat(caseDetails.getCaseData().getEt1ReasonableAdjustmentsTextArea()).isNull();
    }

    @Test
    void initialBeforeYouStart_ReasonableAdjustmentsIsNull_Et1ReasonableAdjustmentsQuestionReturnNull() {
        caseDetails.getCaseData().setClaimantHearingPreference(new ClaimantHearingPreference());
        caseDetails.getCaseData().getClaimantHearingPreference().setReasonableAdjustments(null);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1ReasonableAdjustmentsQuestion()).isNull();
        assertThat(caseDetails.getCaseData().getEt1ReasonableAdjustmentsTextArea()).isNull();
    }

    @Test
    void initialBeforeYouStart_ReasonableAdjustmentsIsYes_Et1ReasonableAdjustmentsQuestionReturnYes() {
        caseDetails.getCaseData().setClaimantHearingPreference(new ClaimantHearingPreference());
        caseDetails.getCaseData().getClaimantHearingPreference().setReasonableAdjustments(YES);
        caseDetails.getCaseData().getClaimantHearingPreference().setReasonableAdjustmentsDetail("Detail");
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1ReasonableAdjustmentsQuestion()).isEqualTo(YES);
        assertThat(caseDetails.getCaseData().getEt1ReasonableAdjustmentsTextArea()).isEqualTo("Detail");
    }

    @Test
    void initialBeforeYouStart_ReasonableAdjustmentsIsNo_Et1ReasonableAdjustmentsQuestionReturnNo() {
        caseDetails.getCaseData().setClaimantHearingPreference(new ClaimantHearingPreference());
        caseDetails.getCaseData().getClaimantHearingPreference().setReasonableAdjustments(NO);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1ReasonableAdjustmentsQuestion()).isEqualTo(NO);
    }

    @Test
    void generateJurisdictionCodesHtml() {
        CaseData caseData = new CaseData();
        addJurCodeToExistingCollection(caseData, DAG);
        String expected = String.format(JUR_CODE_HTML, String.format(CASE_NAME_AND_DESCRIPTION_HTML, DAG,
            JurisdictionCode.valueOf(DAG).getDescription()));
        assertThat(et1VettingService.generateJurisdictionCodesHtml(caseData.getJurCodesCollection()))
            .isEqualTo(expected);
    }

    @Test
    void validateJurisdictionCodes() {
        CaseData caseData = new CaseData();
        addJurCodeToExistingCollection(caseData, DAG);
        addJurCodeToVettingCollection(caseData, DAG);
        addJurCodeToVettingCollection(caseData, PID);
        addJurCodeToVettingCollection(caseData, PID);

        List<String> expectedErrors = new ArrayList<>();
        expectedErrors.add(String.format(ERROR_EXISTING_JUR_CODE, DAG));
        expectedErrors.add(String.format(ERROR_SELECTED_JUR_CODE, PID));

        assertThat(et1VettingService.validateJurisdictionCodes(caseData))
            .isEqualTo(expectedErrors);
    }

    @Test
    void validateJurisdictionCodes_noErrors() {
        CaseData caseData = new CaseData();
        addJurCodeToVettingCollection(caseData, "DAG");
        addJurCodeToVettingCollection(caseData, "PID");

        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void validateJurisdictionCodes_existingCodeError() {
        CaseData caseData = new CaseData();
        addJurCodeToExistingCollection(caseData, "DAG");
        addJurCodeToVettingCollection(caseData, "DAG");

        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);

        assertThat(errors).containsExactly(String.format(ERROR_EXISTING_JUR_CODE, "DAG"));
    }

    @Test
    void validateJurisdictionCodes_duplicateCodeError() {
        CaseData caseData = new CaseData();
        addJurCodeToVettingCollection(caseData, "PID");
        addJurCodeToVettingCollection(caseData, "PID");

        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);

        assertThat(errors).containsExactly(String.format(ERROR_SELECTED_JUR_CODE, "PID"));
    }

    @Test
    void validateJurisdictionCodes_existingAndDuplicateCodeErrors() {
        CaseData caseData = new CaseData();
        addJurCodeToExistingCollection(caseData, "DAG");
        addJurCodeToVettingCollection(caseData, "DAG");
        addJurCodeToVettingCollection(caseData, "PID");
        addJurCodeToVettingCollection(caseData, "PID");

        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);

        assertThat(errors).containsExactly(
                String.format(ERROR_EXISTING_JUR_CODE, "DAG"),
                String.format(ERROR_SELECTED_JUR_CODE, "PID")
        );
    }

    @Test
    void validateJurisdictionCodes_nullVettingCollection() {
        CaseData caseData = new CaseData();
        caseData.setVettingJurisdictionCodeCollection(null);

        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void validateJurisdictionCodes_emptyVettingCollection() {
        CaseData caseData = new CaseData();
        caseData.setVettingJurisdictionCodeCollection(new ArrayList<>());

        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void testGettingHearingVenueAddressesHtml() {
        caseDetails.getCaseData().setManagingOffice("Manchester");
        caseDetails.getCaseData().setClaimantWorkAddressQuestion("No");
        assertThat(et1VettingService.getAddressesHtml(caseDetails.getCaseData()))
            .isEqualTo(EXPECTED_ADDRESSES_HTML);
    }

    @Test
    void testGettingHearingVenueAddressesHtmlQuestionYes() {
        caseDetails.getCaseData().setManagingOffice("Manchester");
        caseDetails.getCaseData().setClaimantWorkAddressQuestion("Yes");
        assertThat(et1VettingService.getAddressesHtml(caseDetails.getCaseData()))
                .isEqualTo(EXPECTED_ADDRESSES_HTML_NO_WORK_ADDRESS);
    }

    @Test
    void testGettingHearingVenueAddressesHtmlQuestionNull() {
        caseDetails.getCaseData().setManagingOffice("Manchester");
        assertThat(et1VettingService.getAddressesHtml(caseDetails.getCaseData()))
                .isEqualTo(EXPECTED_ADDRESSES_HTML_NO_WORK_ADDRESS);
    }

    @Test
    void testGeneratingHearingVenueList() {
        caseDetails.getCaseData().setManagingOffice(MANCHESTER.getOfficeName());
        DynamicValueType expectedHearingVenue = DynamicValueType.create("code", "Manchester hearing venue");

        when(jpaVenueService.getVenues(MANCHESTER)).thenReturn(List.of(expectedHearingVenue));

        assertThat(et1VettingService.getHearingVenuesList(caseDetails.getCaseData().getManagingOffice())
                .getListItems().get(0)).isEqualTo(expectedHearingVenue);
    }

    @Test
    void populateEt1TrackAllocationHtml() {
        CaseData caseData = new CaseData();
        addJurCodeToVettingCollection(caseData, DAG);
        addJurCodeToExistingCollection(caseData, PID);

        String expected = String.format(TRACK_ALLOCATION_HTML, TRACK_OPEN);
        assertThat(et1VettingService.populateEt1TrackAllocationHtml(caseData))
            .isEqualTo(expected);
        assertThat(caseData.getTrackType())
            .isEqualTo(TRACK_OPEN);
    }

    @Test
    void populateTribunalOfficeFields() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(OFFICE);
        et1VettingService.populateTribunalOfficeFields(caseData);

        String expectedOfficeLocation = String.format(TRIBUNAL_OFFICE_LOCATION, TRIBUNAL_ENGLAND, OFFICE);
        String expectedRegionalOffice = String.format(TRIBUNAL_LOCATION_LABEL, TRIBUNAL_ENGLAND);
        assertThat(caseData.getTribunalAndOfficeLocation())
            .isEqualTo(expectedOfficeLocation);
        assertThat(caseData.getRegionalOffice())
            .isEqualTo(expectedRegionalOffice);
    }

    @Test
    void generateEt1VettingDocument() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
                anyString(), anyString())).thenReturn(documentInfo);
        DocumentInfo documentInfo1 = et1VettingService.generateEt1VettingDocument(new CaseData(), "userToken",
                ENGLANDWALES_CASE_TYPE_ID);
        assertThat(documentInfo).isEqualTo(documentInfo1);
    }

    @Test
    void generateEt1VettingDocumentExceptions() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
                anyString(), anyString())).thenThrow(new InternalException(ERROR_MESSAGE));
        assertThrows(Exception.class, () -> et1VettingService.generateEt1VettingDocument(new CaseData(), "userToken",
                ENGLANDWALES_CASE_TYPE_ID));
    }

    @Test
    void givenChangeOfRegion_shouldSetEt1TribunalRegionToNewOffice() {
        CaseData caseData = new CaseDataBuilder()
            .withManagingOffice(TribunalOffice.LEEDS.getOfficeName())
            .build();
        caseData.setRegionalOfficeList(DynamicFixedListType.of(
            DynamicValueType.create(MANCHESTER.getOfficeName(), MANCHESTER.getOfficeName())));
        caseData.getRegionalOfficeList().setValue(
            DynamicValueType.create(MANCHESTER.getOfficeName(), MANCHESTER.getOfficeName()));
        et1VettingService.populateHearingVenue(caseData);
        assertThat(caseData.getEt1TribunalRegion()).isEqualTo(MANCHESTER.getOfficeName());
    }

    @Test
    void givenNoChangeOfRegion_shouldSetEt1TribunalRegionToManagingOffice() {
        CaseData caseData = new CaseDataBuilder()
                .withManagingOffice(TribunalOffice.LEEDS.getOfficeName())
                .build();
        caseData.setRegionalOfficeList(null);
        et1VettingService.populateHearingVenue(caseData);
        assertThat(caseData.getEt1TribunalRegion()).isEqualTo(LEEDS.getOfficeName());
    }

    @Test
    void populateSuggestedHearingVenue_keepsSelectedValue() {
        CaseData caseData = new CaseDataBuilder()
            .withManagingOffice(LONDON_CENTRAL.getOfficeName())
            .build();
        caseData.setRegionalOfficeList(null);
        DynamicFixedListType dynamicFixedListType = DynamicFixedListType.from(List.of());
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel("RCJ");
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setSuggestedHearingVenues(dynamicFixedListType);
        et1VettingService.populateSuggestedHearingVenues(caseData);
        assertThat(caseData.getSuggestedHearingVenues().getSelectedLabel()).isEqualTo("RCJ");
    }

    @Test
    void clearEt1VettingFields() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        et1VettingService.clearEt1FieldsFromCaseData(caseDetails.getCaseData());
        assertNull(caseDetails.getCaseData().getEt1VettingBeforeYouStart());
        assertNull(caseDetails.getCaseData().getEt1VettingClaimantDetailsMarkUp());
        assertNull(caseDetails.getCaseData().getEt1VettingRespondentDetailsMarkUp());
        assertNull(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails1());
        assertNull(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails2());
        assertNull(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails3());
        assertNull(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails4());
        assertNull(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails5());
        assertNull(caseDetails.getCaseData().getEt1VettingRespondentAcasDetails6());
        assertNull(caseDetails.getCaseData().getExistingJurisdictionCodes());
        assertNull(caseDetails.getCaseData().getRegionalOffice());
    }

    private DocumentTypeItem createDocumentTypeItem(String typeOfDocument, String binaryLink) {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setUploadedDocument(new UploadedDocumentType());
        if (ET1_ATTACHMENT_DOC_TYPE.equals(typeOfDocument)) {
            documentType.getUploadedDocument().setDocumentFilename(ET1_ATTACHMENT_NAME);
        }
        documentType.getUploadedDocument().setDocumentBinaryUrl("http://dm-store:8080" + binaryLink);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        return documentTypeItem;
    }

    private void addJurCodeToExistingCollection(CaseData caseData, String code) {
        JurCodesType newCode = new JurCodesType();
        newCode.setJuridictionCodesList(code);
        JurCodesTypeItem codesTypeItem = new JurCodesTypeItem();
        codesTypeItem.setValue(newCode);
        caseData.setJurCodesCollection(new ArrayList<>());
        caseData.getJurCodesCollection().add(codesTypeItem);
    }

    private void addJurCodeToVettingCollection(CaseData caseData, String code) {
        VettingJurisdictionCodesType newCode = new VettingJurisdictionCodesType();
        newCode.setEt1VettingJurCodeList(code);
        VettingJurCodesTypeItem codesTypeItem = new VettingJurCodesTypeItem();
        codesTypeItem.setValue(newCode);
        if (caseData.getVettingJurisdictionCodeCollection() == null) {
            caseData.setVettingJurisdictionCodeCollection(new ArrayList<>());
        }
        caseData.getVettingJurisdictionCodeCollection().add(codesTypeItem);
    }

    @Test
    void testNullAddress() {
        assertEquals(ADDRESS_NOT_ENTERED, et1VettingService.toAddressWithTab(null));
    }

    @Test
    void testEmptyAddress() {
        assertEquals(ADDRESS_NOT_ENTERED, et1VettingService.toAddressWithTab(new Address()));
    }

    @Test
    void testPartialAddress() {
        Address address = new Address();
        address.setAddressLine1("123 Main St");
        address.setAddressLine2(null);
        address.setAddressLine3("Leeds");
        address.setPostCode(null);
        String result = et1VettingService.toAddressWithTab(address);

        String expected = "123 Main St<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 Leeds";
        assertEquals(expected, result);
    }

}
