package uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;

class ClaimantTellSomethingElseHelperTest {

    private CaseData caseData;
    private static final String CLAIMANT_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";
    public static final String CLAIMANT_TSE_FILE_NAME = "Claimant Contact the tribunal.pdf";

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("6000001/2022")
                .withGenericTseApplicationTypeItem("Claimant Rep", "2023-02-16")
                .build();
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void getDocumentRequest_TextOnly_ReturnString(String selectedApplication) throws JsonProcessingException {
        caseData.setClaimantTseSelectApplication(selectedApplication);
        setTextBoxForSelectedApplication(caseData);
        caseData.setClaimantTseRule92(YES);

        String expectedDocumentString = "{"
                + "\"accessKey\":\"key\","
                + "\"templateName\":\"" + CLAIMANT_TSE_TEMPLATE_NAME + "\","
                + "\"outputName\":\"" + CLAIMANT_TSE_FILE_NAME + "\","
                + "\"data\":{"
                + "\"resTseApplicant\":\"Claimant\","
                + "\"caseNumber\":\"" + caseData.getEthosCaseReference() + "\","
                + "\"resTseSelectApplication\":\"" + selectedApplication + "\","
                + "\"resTseApplicationDate\":\"2023-02-16\","
                + "\"resTseDocument\":null,"
                + "\"resTseTextBox\":\"Not Blank\","
                + "\"resTseCopyToOtherPartyYesOrNo\":\"Yes\","
                + "\"resTseCopyToOtherPartyTextArea\":null"
                + "}"
                + "}";

        assertThat(ClaimantTellSomethingElseHelper.getDocumentRequest(caseData, "key"))
                .isEqualTo(expectedDocumentString);
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void getDocumentRequest_DocOnly_ReturnString(String selectedApplication) throws JsonProcessingException {
        caseData.setClaimantTseSelectApplication(selectedApplication);
        setDocForSelectedApplication(caseData);
        caseData.setClaimantTseRule92(YES);

        String expectedDocumentString = "{"
                + "\"accessKey\":\"key\","
                + "\"templateName\":\"" + CLAIMANT_TSE_TEMPLATE_NAME + "\","
                + "\"outputName\":\"" + CLAIMANT_TSE_FILE_NAME + "\","
                + "\"data\":{"
                + "\"resTseApplicant\":\"Claimant\","
                + "\"caseNumber\":\"" + caseData.getEthosCaseReference() + "\","
                + "\"resTseSelectApplication\":\"" + selectedApplication + "\","
                + "\"resTseApplicationDate\":\"2023-02-16\","
                + "\"resTseDocument\":\"DocName.txt\","
                + "\"resTseTextBox\":null,"
                + "\"resTseCopyToOtherPartyYesOrNo\":\"Yes\","
                + "\"resTseCopyToOtherPartyTextArea\":null"
                + "}"
                + "}";

        assertThat(ClaimantTellSomethingElseHelper.getDocumentRequest(caseData, "key"))
                .isEqualTo(expectedDocumentString);
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void getDocumentRequest_BothDocAndText_ReturnString(String selectedApplication) throws JsonProcessingException {
        caseData.setClaimantTseSelectApplication(selectedApplication);
        setTextBoxForSelectedApplication(caseData);
        setDocForSelectedApplication(caseData);
        caseData.setClaimantTseRule92(YES);

        String expectedDocumentString = "{"
                + "\"accessKey\":\"key\","
                + "\"templateName\":\"" + CLAIMANT_TSE_TEMPLATE_NAME + "\","
                + "\"outputName\":\"" + CLAIMANT_TSE_FILE_NAME + "\","
                + "\"data\":{"
                + "\"resTseApplicant\":\"Claimant\","
                + "\"caseNumber\":\"" + caseData.getEthosCaseReference() + "\","
                + "\"resTseSelectApplication\":\"" + selectedApplication + "\","
                + "\"resTseApplicationDate\":\"2023-02-16\","
                + "\"resTseDocument\":\"DocName.txt\","
                + "\"resTseTextBox\":\"Not Blank\","
                + "\"resTseCopyToOtherPartyYesOrNo\":\"Yes\","
                + "\"resTseCopyToOtherPartyTextArea\":null"
                + "}"
                + "}";

        assertThat(ClaimantTellSomethingElseHelper.getDocumentRequest(caseData, "key"))
                .isEqualTo(expectedDocumentString);
    }

    private static Stream<Arguments> selectedApplicationList() {
        return Stream.of(
                Arguments.of(CLAIMANT_TSE_WITHDRAW_CLAIM));
    }

    private void setTextBoxForSelectedApplication(CaseData caseData) {
        if (caseData.getClaimantTseSelectApplication().equals(CLAIMANT_TSE_WITHDRAW_CLAIM)) {
            caseData.setClaimantTseTextBox13("Not Blank");
        }
    }

    private void setDocForSelectedApplication(CaseData caseData) {
        if (caseData.getClaimantTseSelectApplication().equals(CLAIMANT_TSE_WITHDRAW_CLAIM)) {
            caseData.setClaimantTseDocument13(createDocumentType());
        }
    }

    private UploadedDocumentType createDocumentType() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("binaryUrl/documents/");
        uploadedDocumentType.setDocumentFilename("DocName.txt");
        uploadedDocumentType.setDocumentUrl("documentUrl");
        return uploadedDocumentType;
    }
}