package uk.gov.hmcts.ethos.replacement.docmosis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

@ExtendWith(SpringExtension.class)
class CreateReferralServiceTest {
    private CreateReferralService createReferralService;

    @MockBean
    private TornadoService tornadoService;

    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        createReferralService = new CreateReferralService(tornadoService);

        documentInfo = DocumentInfo.builder()
            .description("Referral Summary.pdf")
            .url("https://test.com/documents/random-uuid")
            .build();
    }

    @Test
    void whenCreatingReferral_ReturnDocument() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
            anyString(), anyString())).thenReturn(documentInfo);

        DocumentInfo responseDoc = createReferralService.generateCRDocument(new CaseData(), "", "");
        assertThat(responseDoc, is(documentInfo));
    }

    @Test
    void whenCreatingReferralFails_ThrowException() throws IOException {
        Throwable ioException = new IOException("test");

        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
            anyString(), anyString())).thenThrow(ioException);
        assertThrows(DocumentManagementException.class,
            () -> createReferralService.generateCRDocument(new CaseData(), "",
            ""));
    }

    @Test
    void addErrorDocumentUpload() {
       CaseData caseData = new CaseData();
       List<String> errors = new ArrayList<>();
       DocumentTypeItem documentTypeItem = new DocumentTypeItem();
       DocumentType documentType = new DocumentType();
       documentType.setShortDescription("shortDescription");
       documentTypeItem.setId(UUID.randomUUID().toString());
       documentTypeItem.setValue(documentType);
       caseData.setReferralDocument(List.of(documentTypeItem));
       createReferralService.addDocumentUploadErrors(caseData, errors);
       assertEquals( "Short description is added but document is not uploaded.", errors.get(0));
    }
}