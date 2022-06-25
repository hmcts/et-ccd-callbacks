package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.VettingJurisdictionCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.CASE_NAME_AND_DESCRIPTION_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ERROR_EXISTING_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ERROR_SELECTED_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.JUR_CODE_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.TRACK_OPEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.TRACk_ALLOCATION_HTML;

class Et1VettingServiceTest {

    private Et1VettingService et1VettingService;
    private CaseDetails caseDetails;

    private static final String ET1_DOC_TYPE = "ET1";
    private static final String ACAS_DOC_TYPE = "ACAS Certificate";
    private static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s"
            + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    private static final String BEFORE_LABEL_ET1 =
            "<br/><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS =
            "<br/><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS_OPEN_TAB =
            "<br/><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
                    + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
    private static final String DAG = JurisdictionCode.DAG.name();
    private static final String PID = JurisdictionCode.PID.name();

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
        caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
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
        et1VettingService.initialiseEt1Vetting(caseDetails);
        String expected = String.format(BEFORE_LABEL_TEMPLATE, "", "");
        assertThat(caseDetails.getCaseData().getEt1VettingBeforeYouStart())
                .isEqualTo(expected);
    }

    @Test
    void generateJurisdictionCodesHtml() {
        CaseData caseData = new CaseData();
        addJurCodeToExistingCollection(caseData, DAG);

        assertEquals(String.format(JUR_CODE_HTML, String.format(CASE_NAME_AND_DESCRIPTION_HTML, DAG,
                JurisdictionCode.valueOf(DAG).getDescription())),
            et1VettingService.generateJurisdictionCodesHtml(caseData.getJurCodesCollection()));
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

        assertEquals(expectedErrors, et1VettingService.validateJurisdictionCodes(caseData));
    }

    @Test
    void populateEt1TrackAllocationHtml() {
        CaseData caseData = new CaseData();
        addJurCodeToVettingCollection(caseData, DAG);
        addJurCodeToExistingCollection(caseData, PID);

        assertEquals(String.format(TRACk_ALLOCATION_HTML, TRACK_OPEN),
            et1VettingService.populateEt1TrackAllocationHtml(caseData));
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

}