package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.WITHDRAWAL_OF_ALL_OR_PART_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.WITHDRAWAL_SETTLED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.GIVE_DETAIL_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class ClaimantTellSomethingElseServiceTest {
    private ClaimantTellSomethingElseService claimantTellSomethingElseService;

    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private TornadoService tornadoService;

    @BeforeEach
    void setUp() {
        claimantTellSomethingElseService =
                new ClaimantTellSomethingElseService(documentManagementService, tornadoService);
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_Blank_ReturnErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(selectedApplication);
        List<String> errors = claimantTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(GIVE_DETAIL_MISSING));
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_HasDoc_NoErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(selectedApplication);
        setDocForSelectedApplication(caseData);
        List<String> errors = claimantTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(0));
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void validateGiveDetails_HasTextBox_NoErrorMsg(String selectedApplication) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(selectedApplication);
        setTextBoxForSelectedApplication(caseData);
        List<String> errors = claimantTellSomethingElseService.validateGiveDetails(caseData);
        assertThat(errors.size(), is(0));
    }

    @Test
    void generateAndAddApplicationPdf() throws IOException {
        CaseData caseData = new CaseData();
        caseData.setClaimantTseSelectApplication("Withdraw all or part of claim");
        DocumentInfo documentInfo = new DocumentInfo("document.pdf", "Withdraw Claim", "binaryUrl/documents/", "<>Some doc</>");
        when(tornadoService.generateEventDocument(any(), any(), any(), any())).thenReturn(documentInfo);

        claimantTellSomethingElseService.generateAndAddApplicationPdf(caseData, "token", "typeId");

        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        DocumentType actual = documentCollection.get(0).getValue();

        DocumentType expected = DocumentType.builder()
                .shortDescription("Withdraw all or part of claim")
                .dateOfCorrespondence(LocalDate.now().toString())
                .topLevelDocuments(WITHDRAWAL_SETTLED)
                .documentType(WITHDRAWAL_OF_ALL_OR_PART_CLAIM)
                .withdrawalSettledDocuments(WITHDRAWAL_OF_ALL_OR_PART_CLAIM)
                .build();

        Assertions.assertThat(documentCollection).hasSize(1);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateAndAddApplicationPdf_Error() {
        CaseData caseData = new CaseData();
        caseData.setClaimantTseSelectApplication("Withdraw all or part of claim");
        try {
            claimantTellSomethingElseService.generateAndAddApplicationPdf(caseData, "token", "typeId");
        } catch (Exception e) {
            assertThat(e.getMessage(), is(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference())));
        }
    }

    @Test
    void populateClaimantTse_Success() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setClaimantTseRule92("Yes");
        caseData.setClaimantTseRule92AnsNoGiveDetails("Some text");
        setTextBoxForSelectedApplication(caseData);
        setDocForSelectedApplication(caseData);
        claimantTellSomethingElseService.populateClaimantTse(caseData);

        ClaimantTse claimantTse = caseData.getClaimantTse();
        assertThat(claimantTse.getContactApplicationType(), is(caseData.getClaimantTseSelectApplication()));
        assertThat(claimantTse.getCopyToOtherPartyYesOrNo(), is(caseData.getClaimantTseRule92()));
        assertThat(claimantTse.getCopyToOtherPartyText(), is(caseData.getClaimantTseRule92AnsNoGiveDetails()));

        TSEApplicationTypeData selectedAppData =
                ClaimantTellSomethingElseHelper.getSelectedApplicationType(caseData);
        assertThat(claimantTse.getContactApplicationText(), is(selectedAppData.getSelectedTextBox()));
        assertThat(claimantTse.getContactApplicationFile(), is(selectedAppData.getUploadedTseDocument()));
    }

    @Test
    void buildApplicationCompleteResponse_Rule92Yes_RespOffline() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseRespNotAvailable("Yes");
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setDocMarkUp("Document");

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_REP_TITLE)
                .withCopyToOtherPartyYesOrNo(YES)
                .withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem latestTSEApplication = new GenericTseApplicationTypeItem();
        latestTSEApplication.setId(UUID.randomUUID().toString());
        latestTSEApplication.setValue(build);

        caseData.setGenericTseApplicationCollection(List.of(latestTSEApplication));

        String response = claimantTellSomethingElseService.buildApplicationCompleteResponse(caseData);
        assertThat(response, is(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE + "Document"));
    }

    @Test
    void buildApplicationCompleteResponse_Rule92Yes_RespOnline() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseRespNotAvailable(NO);
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setDocMarkUp("Document");

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_REP_TITLE)
                .withCopyToOtherPartyYesOrNo(YES)
                .withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem latestTSEApplication = new GenericTseApplicationTypeItem();
        latestTSEApplication.setId(UUID.randomUUID().toString());
        latestTSEApplication.setValue(build);

        caseData.setGenericTseApplicationCollection(List.of(latestTSEApplication));

        String response = claimantTellSomethingElseService.buildApplicationCompleteResponse(caseData);
        assertThat(response, is(String.format(TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE,
                UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7))));
    }

    @Test
    void buildApplicationCompleteResponse_Rule92No() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantTseRespNotAvailable("anything");
        caseData.setClaimantTseSelectApplication(CLAIMANT_TSE_WITHDRAW_CLAIM);
        caseData.setDocMarkUp("Document");

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_REP_TITLE)
                .withCopyToOtherPartyYesOrNo(NO)
                .withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem latestTSEApplication = new GenericTseApplicationTypeItem();
        latestTSEApplication.setId(UUID.randomUUID().toString());
        latestTSEApplication.setValue(build);

        caseData.setGenericTseApplicationCollection(List.of(latestTSEApplication));

        String response = claimantTellSomethingElseService.buildApplicationCompleteResponse(caseData);
        assertThat(response, is(APPLICATION_COMPLETE_RULE92_ANSWERED_NO));
    }

    private static Stream<Arguments> selectedApplicationList() {
        return Stream.of(
            Arguments.of(CLAIMANT_TSE_WITHDRAW_CLAIM)
        );
    }

    private void setTextBoxForSelectedApplication(CaseData caseData) {
        if (caseData.getClaimantTseSelectApplication().equals(CLAIMANT_TSE_WITHDRAW_CLAIM)) {
            caseData.setClaimantTseTextBox13("Some text");
        } else {
            throw new IllegalArgumentException("Unexpected application type");
        }
    }

    private void setDocForSelectedApplication(CaseData caseData) {
        if (caseData.getClaimantTseSelectApplication().equals(CLAIMANT_TSE_WITHDRAW_CLAIM)) {
            caseData.setClaimantTseDocument13(createDocumentType());
        } else {
            throw new IllegalArgumentException("Unexpected application type");
        }
    }

    private UploadedDocumentType createDocumentType() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("binaryUrl/documents/");
        uploadedDocumentType.setDocumentFilename("testFileName");
        uploadedDocumentType.setDocumentUrl("Some doc");
        return uploadedDocumentType;
    }
}
