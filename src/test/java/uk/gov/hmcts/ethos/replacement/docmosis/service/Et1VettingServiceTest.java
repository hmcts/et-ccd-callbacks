package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

class Et1VettingServiceTest {

    private Et1VettingService et1VettingService;
    private CaseDetails caseDetails;

    private static final String ET1_DOC_TYPE = "ET1";
    private static final String ACAS_DOC_TYPE = "ACAS Certificate";
    private static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s"
            + "<br>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    private static final String BEFORE_LABEL_ET1 =
            "<br><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS =
            "<br><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS_OPEN_TAB =
            "<br><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
                    + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
    private static final String CLAIMANT_DETAILS = "<hr><h3>Claimant</h3>"
            + "<pre>First name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Last name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String RESPONDENT_DETAILS = "<h3>Respondent %s</h3>"
            + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String BR_WITH_TAB = "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 ";
    private static final String ACAS_CERT_LIST_DISPLAY = "Certificate number %s has been provided.<br>";

    private final String et1BinaryUrl1 = "/documents/et1o0c3e-4efd-8886-0dca-1e3876c3178c/binary";
    private final String acasBinaryUrl1 = "/documents/acas1111-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl2 = "/documents/acas2222-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl3 = "/documents/acas3333-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl4 = "/documents/acas4444-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl5 = "/documents/acas5555-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String caseId = "1655312312192821";

    @BeforeEach
    void setUp() {
        et1VettingService = new Et1VettingService();
        caseDetails = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withRespondentWithAddress("Juan Garcia",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        "2987/6543/01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(caseId);
    }

    @Test
    void initialBeforeLinkLabel_ZeroAcas_shouldReturnEt1Only() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, et1BinaryUrl1));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE, String.format(BEFORE_LABEL_ET1, et1BinaryUrl1), "");
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeLinkLabel_FiveAcas_shouldReturnFiveAcas() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, et1BinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl2));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl3));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl4));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl5));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE,
                String.format(BEFORE_LABEL_ET1, et1BinaryUrl1),
                String.format(BEFORE_LABEL_ACAS, acasBinaryUrl1, "1")
                        + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl2, "2")
                        + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl3, "3")
                        + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl4, "4")
                        + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl5, "5"));
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeLinkLabel_SixAcas_shouldReturnDocTab() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, et1BinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl2));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl3));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl4));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl5));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE,
                "/documents/acas6666-4ef8ca1e3-8c60-d3d78808dca1/binary"));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE,
                String.format(BEFORE_LABEL_ET1, et1BinaryUrl1),
                String.format(BEFORE_LABEL_ACAS_OPEN_TAB, caseId));
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_NoDocumentCollection_shouldReturnWithoutUrl() {
        caseDetails.getCaseData().setDocumentCollection(null);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE, "", "");
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_ClaimantDetails_shouldReturnMarkUp() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(CLAIMANT_DETAILS, "Doris", "Johnson",
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
    void initialBeforeYouStart_TwoRespondentDetails_shouldReturnMarkUp() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(RESPONDENT_DETAILS, "1", "Antonio Vazquez",
                "11 Small Street" + BR_WITH_TAB + "22 House" + BR_WITH_TAB + "Manchester" + BR_WITH_TAB + "M12 42R")
                + String.format(RESPONDENT_DETAILS, "2", "Juan Garcia",
                "32 Sweet Street" + BR_WITH_TAB + "14 House" + BR_WITH_TAB + "Manchester" + BR_WITH_TAB + "M11 4ED");
        assertThat(caseDetails.getCaseData().getEt1VettingRespondentDetailsMarkUp())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_OneAcasNumber_shouldReturnMarkUp() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(ACAS_CERT_LIST_DISPLAY, "1234/5678/90");
        assertThat(caseDetails.getCaseData().getEt1VettingAcasCertListMarkUp())
                .isEqualTo(expected);
    }

    @Test
    void initialBeforeYouStart_NoAcasNumber_shouldReturnMarkUp() {
        caseDetails = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        null)
                .withRespondentWithAddress("Juan Garcia",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertThat(caseDetails.getCaseData().getEt1VettingAcasCertListMarkUp())
                .isEmpty();
    }

    private DocumentTypeItem createDocumentTypeItem(String typeOfDocument, String binaryLink) {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setUploadedDocument(new UploadedDocumentType());
        documentType.getUploadedDocument().setDocumentBinaryUrl("http://dm-store:8080" + binaryLink);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        return documentTypeItem;
    }

}