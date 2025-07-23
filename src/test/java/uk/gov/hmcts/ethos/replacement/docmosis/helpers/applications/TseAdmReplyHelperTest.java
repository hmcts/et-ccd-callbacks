package uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;

@ExtendWith(SpringExtension.class)
class TseAdmReplyHelperTest {
    private CaseData caseData;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private TornadoService tornadoService;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .withClaimant("First Last")
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder()
            .withApplicant(CLAIMANT_TITLE)
            .withDate("13 December 2022")
            .withDue("20 December 2022")
            .withType("Withdraw my claim")
            .withDetails("Text")
            .withNumber("1")
            .withResponsesCount("0")
            .withStatus(OPEN_STATE)
            .build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));
    }

    @Test
    void getDocumentTypeItem_returnsNullForEmptyCaseDetails() {
        CaseDetails caseDetails = null;
        DocumentTypeItem actual  = TseAdmReplyHelper.getDocumentTypeItem(documentManagementService, tornadoService,
                caseDetails, "testToken", "docType",
                "correspondenceType");
        assertNull(actual);
    }

    @Test
    void getDocumentTypeItem_returns_documentTypeItem_for_initialised_caseDetails() throws IOException {
        CaseDetails caseDetails = new CaseDetails();
        String selectedTseApp = "testSelectedResTseApplication";
        String documentType = "docType";
        caseData.setResTseSelectApplication(selectedTseApp);
        caseDetails.setCaseData(caseData);
        DocumentInfo expectedDocumentInfo = new DocumentInfo();
        expectedDocumentInfo.setDescription(documentType);
        String correspondenceType = "correspondenceType";

        when(tornadoService.generateEventDocument(any(CaseData.class), any(), any(), any()))
            .thenReturn(expectedDocumentInfo);
        DocumentTypeItem actual  = TseAdmReplyHelper.getDocumentTypeItem(documentManagementService, tornadoService,
                caseDetails, "testToken", documentType, correspondenceType);

        assertNotNull(actual);
        assertThat(actual.getValue().getTypeOfDocument(), is(correspondenceType));
        assertThat(actual.getValue().getShortDescription(), is(selectedTseApp));
    }

    @Test
    void getReplyDocumentRequest_Returns_doc_as_String() throws JsonProcessingException {
        caseData.setTseAdminSelectApplication(getTseAdminSelectApp());
        String actual  = TseAdmReplyHelper.getReplyDocumentRequest(caseData, "testAccessKey");

        assertNotNull(actual);
        assertThat(actual.contains("EM-TRB-EGW-ENG-000991.docx"), is(Boolean.TRUE));
    }

    @Test
    void getTseAdminSelectedApplicationType_returns_selected_application() {
        caseData.setTseAdminSelectApplication(getTseAdminSelectApp());
        String expectedTseAppNumber = caseData.getGenericTseApplicationCollection().getFirst().getValue().getNumber();
        GenericTseApplicationType actual  = TseAdmReplyHelper.getTseAdminSelectedApplicationType(caseData);

        assertNotNull(actual);
        assertThat(actual.getNumber(), is(expectedTseAppNumber));
    }

    private static DynamicFixedListType getTseAdminSelectApp() {
        DynamicFixedListType flt = new DynamicFixedListType();
        DynamicValueType dvt = new DynamicValueType();
        dvt.setLabel("test");
        dvt.setCode("1");
        flt.setValue(dvt);
        flt.setValue(dvt);
        return flt;
    }
}
