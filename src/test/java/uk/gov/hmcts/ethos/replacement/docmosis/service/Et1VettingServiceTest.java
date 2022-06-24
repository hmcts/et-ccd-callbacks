package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.ET1VettingJurisdictionCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ACAS_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.BEFORE_LINK_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.CASE_NAME_AND_DESCRIPTION_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ERROR_EXISTING_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ERROR_SELECTED_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.JUR_CODE_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.TRACK_OPEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.TRACk_ALLOCATION_HTML;

class Et1VettingServiceTest {

    private Et1VettingService et1VettingService;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        et1VettingService = new Et1VettingService();
        caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1655312312192821");
    }

    @Test
    void initialBeforeLinkLabel_Exist_shouldReturnBinaryUrl() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE,
                "http://dm-store:8080/documents/et10dcae-4efd-8886-0dca-1e3876c3178c/binary"));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE,
                "http://dm-store:8080/documents/acas76ce-4ef8ca1e3-8c60-d3d78808dca1/binary"));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialBeforeYouStart(caseDetails);
        assertEquals(String.format(BEFORE_LINK_LABEL,
                        "/documents/et10dcae-4efd-8886-0dca-1e3876c3178c/binary",
                        "/documents/acas76ce-4ef8ca1e3-8c60-d3d78808dca1/binary"),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void initialBeforeLinkLabel_NotExist_shouldReturnDefaultUrl() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialBeforeYouStart(caseDetails);
        assertEquals(String.format(BEFORE_LINK_LABEL,
                        "/cases/case-details/1655312312192821#Documents",
                        "/cases/case-details/1655312312192821#Documents"),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void initialBeforeYouStart_NoDocumentCollection_shouldReturnDefaultUrl() {
        et1VettingService.initialBeforeYouStart(caseDetails);
        assertEquals(String.format(BEFORE_LINK_LABEL,
                        "/cases/case-details/1655312312192821#Documents",
                        "/cases/case-details/1655312312192821#Documents"),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void generateJurisdictionCodesHtml() {
        CaseData caseData = new CaseData();
        addJurCodeToExistingCollection(caseData, "UDL");

        assertEquals(String.format(JUR_CODE_HTML, String.format(CASE_NAME_AND_DESCRIPTION_HTML, "UDL",
                JurisdictionCode.valueOf("UDL").getDescription())),
            et1VettingService.generateJurisdictionCodesHtml(caseData.getJurCodesCollection()));
    }

    @Test
    void validateJurisdictionCodes() {
        CaseData caseData = new CaseData();
        addJurCodeToExistingCollection(caseData, "DAG");
        addJurCodeToVettingCollection(caseData, "DAG");
        addJurCodeToVettingCollection(caseData, "PID");
        addJurCodeToVettingCollection(caseData, "PID");

        List<String> expectedErrors = new ArrayList<>();
        expectedErrors.add(String.format(ERROR_EXISTING_JUR_CODE, "DAG"));
        expectedErrors.add(String.format(ERROR_SELECTED_JUR_CODE, "PID"));

        assertEquals(expectedErrors, et1VettingService.validateJurisdictionCodes(caseData));
    }

    @Test
    void populateEt1TrackAllocationHtml() {
        CaseData caseData = new CaseData();
        addJurCodeToVettingCollection(caseData, "DOD");
        addJurCodeToExistingCollection(caseData, "DDA");

        et1VettingService.populateEt1TrackAllocationHtml(caseData);
        assertEquals(String.format(TRACk_ALLOCATION_HTML, TRACK_OPEN),
            caseData.getTrackAllocation());
    }


    private DocumentTypeItem createDocumentTypeItem(String typeOfDocument, String binaryLink) {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setUploadedDocument(new UploadedDocumentType());
        documentType.getUploadedDocument().setDocumentBinaryUrl(binaryLink);
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
        ET1VettingJurisdictionCodesType newCode = new ET1VettingJurisdictionCodesType();
        newCode.setEt1VettingJurCodeList(code);
        VettingJurCodesTypeItem codesTypeItem = new VettingJurCodesTypeItem();
        codesTypeItem.setValue(newCode);
        if (caseData.getVettingJurisdictionCodeCollection() == null) {
            caseData.setVettingJurisdictionCodeCollection(new ArrayList<>());
        }
        caseData.getVettingJurisdictionCodeCollection().add(codesTypeItem);
    }

}