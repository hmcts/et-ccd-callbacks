package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;

@ExtendWith(SpringExtension.class)
class DigitalCaseFileServiceTest {

    @MockBean
    private BundleApiClient bundleApiClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private DigitalCaseFileService digitalCaseFileService;
    private CaseData caseData;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        digitalCaseFileService = new DigitalCaseFileService(authTokenGenerator, bundleApiClient);
        caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("123456/2021")
                .withDocumentCollection(ET1)
                .withDocumentCollection(ET1_ATTACHMENT)
                .build();
        caseData.getDocumentCollection().get(0).getValue().setDateOfCorrespondence("2000-01-01");
        caseData.getDocumentCollection().get(1).getValue().setExcludeFromDcf(List.of(YES));
        caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1234123412341234");
        when(bundleApiClient.asyncStitchBundle(any(), any(), any())).thenReturn(ResourceLoader.stitchBundleRequest());
        when(authTokenGenerator.generate()).thenReturn("authToken");
    }

    @Test
    void getReplyToReferralDCFLink_shouldReturnDigitalCaseFile() {
        DigitalCaseFileType digitalCaseFileType = new DigitalCaseFileType();
        digitalCaseFileType.setUploadedDocument(new UploadedDocumentType());
        digitalCaseFileType.getUploadedDocument()
            .setDocumentBinaryUrl("http://dm-store:8080/documents/acas1111-4ef8ca1e3-8c60-d3d78808dca1/binary");
        caseData.setDigitalCaseFile(digitalCaseFileType);

        String actual = digitalCaseFileService.getReplyToReferralDCFLink(caseData);

        String expected = "<a target=\"_blank\" href=\"/documents/acas1111-4ef8ca1e3-8c60-d3d78808dca1/binary\">"
            + "Digital case file (opens in new tab)</a><br>";
        assertEquals(expected, actual);
    }

    @Test
    void getReplyToReferralDCFLink_shouldReturnTribunalCaseFile() {
        List<DocumentTypeItem> documentTypeItemList = getTribunalCaseFile();
        caseData.setDocumentCollection(documentTypeItemList);

        String actual = digitalCaseFileService.getReplyToReferralDCFLink(caseData);

        String expected = "<a target=\"_blank\" href=\"/documents/acas1111-4ef8ca1e3-8c60-d3d78808dca1/binary\">"
            + "Digital case file (opens in new tab)</a><br>";
        assertEquals(expected, actual);
    }

    @NotNull
    private static List<DocumentTypeItem> getTribunalCaseFile() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("http://dm-store:8080/documents/acas1111-4ef8ca1e3-8c60-d3d78808dca1/binary");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);
        documentType.setTypeOfDocument(TRIBUNAL_CASE_FILE);
        documentType.setMiscDocuments(TRIBUNAL_CASE_FILE);

        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);

        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(documentTypeItem);
        return documentTypeItemList;
    }

    @Test
    void getReplyToReferralDCFLink_shouldReturnEmpty() {
        String actual = digitalCaseFileService.getReplyToReferralDCFLink(caseData);
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotThrowNullPointerIfNoFileOrDocs() {
        caseData.setDigitalCaseFile(null);
        caseData.setDocumentCollection(null);
        assertDoesNotThrow(() -> digitalCaseFileService.getReplyToReferralDCFLink(caseData));
    }

    @Test
    void createDcf() {
        caseData.setUploadOrRemoveDcf("Create");
        assertDoesNotThrow(() -> digitalCaseFileService.createUploadRemoveDcf("authToken", caseDetails));
        assertEquals("DCF Updating: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN),
                caseData.getDigitalCaseFile().getStatus());
    }

    @Test
    void emptyDcfDocumentType_shouldReturnEmpty() {
        List<DocumentTypeItem> documentTypeItems = getTribunalCaseFile();
        documentTypeItems.get(0).getValue().setUploadedDocument(null);
        caseData.setDocumentCollection(documentTypeItems);
        assertEquals("", digitalCaseFileService.getReplyToReferralDCFLink(caseData));
    }

    @Test
    void emptyBinaryUrl_shouldReturnEmpty() {
        List<DocumentTypeItem> documentTypeItems = getTribunalCaseFile();
        documentTypeItems.get(0).getValue().getUploadedDocument().setDocumentBinaryUrl(null);
        caseData.setDocumentCollection(documentTypeItems);
        assertEquals("", digitalCaseFileService.getReplyToReferralDCFLink(caseData));
    }

}
