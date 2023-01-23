package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.CyclomaticComplexity"})
class RespondentTellSomethingElseHelperTest {

    private CaseData caseData;

    private static final String RES_TSE_FILE_NAME = "resTse.pdf";
    private static final String RES_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";

    private static final String SELECTED_APP_AMEND_RESPONSE = "Amend response";
    private static final String SELECTED_APP_CHANGE_PERSONAL_DETAILS = "Change personal details";
    private static final String SELECTED_APP_CLAIMANT_NOT_COMPLIED = "Claimant not complied";
    private static final String SELECTED_APP_CONSIDER_A_DECISION_AFRESH = "Consider a decision afresh";
    private static final String SELECTED_APP_CONTACT_THE_TRIBUNAL = "Contact the tribunal";
    private static final String SELECTED_APP_ORDER_OTHER_PARTY = "Order other party";
    private static final String SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE =
            "Order a witness to attend to give evidence";
    private static final String SELECTED_APP_POSTPONE_A_HEARING = "Postpone a hearing";
    private static final String SELECTED_APP_RECONSIDER_JUDGEMENT = "Reconsider judgement";
    private static final String SELECTED_APP_RESTRICT_PUBLICITY = "Restrict publicity";
    private static final String SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM = "Strike out all or part of a claim";
    private static final String SELECTED_APP_VARY_OR_REVOKE_AN_ORDER = "Vary or revoke an order";

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().build();
        caseData.setEthosCaseReference("6000001/2022");
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void getDocumentRequest_TextOnly_ReturnString(String selectedApplication) throws JsonProcessingException {
        caseData.setResTseSelectApplication(selectedApplication);
        setTextBoxForSelectedApplication(caseData);

        String expectedDocumentString = "{"
            + "\"accessKey\":\"key\","
            + "\"templateName\":\"" + RES_TSE_TEMPLATE_NAME + "\","
            + "\"outputName\":\"" + RES_TSE_FILE_NAME + "\","
            + "\"data\":{"
                + "\"caseNumber\":\"" + caseData.getEthosCaseReference() + "\","
                + "\"resTseSelectApplication\":\"" + selectedApplication + "\","
                + "\"resTseDocument\":null,"
                + "\"resTseTextBox\":\"Not Blank\""
                + "}"
            + "}";

        assertThat(RespondentTellSomethingElseHelper.getDocumentRequest(caseData, "key"))
                .isEqualTo(expectedDocumentString);
    }

    private static Stream<Arguments> selectedApplicationList() {
        return Stream.of(
                Arguments.of(SELECTED_APP_AMEND_RESPONSE),
                Arguments.of(SELECTED_APP_CHANGE_PERSONAL_DETAILS),
                Arguments.of(SELECTED_APP_CLAIMANT_NOT_COMPLIED),
                Arguments.of(SELECTED_APP_CONSIDER_A_DECISION_AFRESH),
                Arguments.of(SELECTED_APP_CONTACT_THE_TRIBUNAL),
                Arguments.of(SELECTED_APP_ORDER_OTHER_PARTY),
                Arguments.of(SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE),
                Arguments.of(SELECTED_APP_POSTPONE_A_HEARING),
                Arguments.of(SELECTED_APP_RECONSIDER_JUDGEMENT),
                Arguments.of(SELECTED_APP_RESTRICT_PUBLICITY),
                Arguments.of(SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM),
                Arguments.of(SELECTED_APP_VARY_OR_REVOKE_AN_ORDER));
    }

    private void setTextBoxForSelectedApplication(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                caseData.setResTseTextBox1("Not Blank");
                break;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseTextBox2("Not Blank");
                break;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseTextBox3("Not Blank");
                break;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseTextBox4("Not Blank");
                break;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseTextBox5("Not Blank");
                break;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                caseData.setResTseTextBox6("Not Blank");
                break;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseTextBox7("Not Blank");
                break;
            case SELECTED_APP_POSTPONE_A_HEARING:
                caseData.setResTseTextBox8("Not Blank");
                break;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseTextBox9("Not Blank");
                break;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                caseData.setResTseTextBox10("Not Blank");
                break;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseTextBox11("Not Blank");
                break;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                caseData.setResTseTextBox12("Not Blank");
                break;
            default:
                break;
        }
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void getDocumentRequest_DocOnly_ReturnString(String selectedApplication) throws JsonProcessingException {
        caseData.setResTseSelectApplication(selectedApplication);
        setDocForSelectedApplication(caseData);

        String expectedDocumentString = "{"
            + "\"accessKey\":\"key\","
            + "\"templateName\":\"" + RES_TSE_TEMPLATE_NAME + "\","
            + "\"outputName\":\"" + RES_TSE_FILE_NAME + "\","
            + "\"data\":{"
                + "\"caseNumber\":\"" + caseData.getEthosCaseReference() + "\","
                + "\"resTseSelectApplication\":\"" + selectedApplication + "\","
                + "\"resTseDocument\":\"DocName.txt\","
                + "\"resTseTextBox\":null"
                + "}"
            + "}";

        assertThat(RespondentTellSomethingElseHelper.getDocumentRequest(caseData, "key"))
                .isEqualTo(expectedDocumentString);
    }

    private void setDocForSelectedApplication(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                caseData.setResTseDocument1(createDocumentType());
                break;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseDocument2(createDocumentType());
                break;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseDocument3(createDocumentType());
                break;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseDocument4(createDocumentType());
                break;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseDocument5(createDocumentType());
                break;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                caseData.setResTseDocument6(createDocumentType());
                break;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseDocument7(createDocumentType());
                break;
            case SELECTED_APP_POSTPONE_A_HEARING:
                caseData.setResTseDocument8(createDocumentType());
                break;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseDocument9(createDocumentType());
                break;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                caseData.setResTseDocument10(createDocumentType());
                break;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseDocument11(createDocumentType());
                break;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                caseData.setResTseDocument12(createDocumentType());
                break;
            default:
                break;
        }
    }

    private UploadedDocumentType createDocumentType() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("binaryUrl/documents/");
        uploadedDocumentType.setDocumentFilename("DocName.txt");
        uploadedDocumentType.setDocumentUrl("documentUrl");
        return uploadedDocumentType;
    }

    @ParameterizedTest
    @MethodSource("selectedApplicationList")
    void getDocumentRequest_BothDocAndText_ReturnString(String selectedApplication) throws JsonProcessingException {
        caseData.setResTseSelectApplication(selectedApplication);
        setTextBoxForSelectedApplication(caseData);
        setDocForSelectedApplication(caseData);

        String expectedDocumentString = "{"
            + "\"accessKey\":\"key\","
            + "\"templateName\":\"" + RES_TSE_TEMPLATE_NAME + "\","
            + "\"outputName\":\"" + RES_TSE_FILE_NAME + "\","
            + "\"data\":{"
                + "\"caseNumber\":\"" + caseData.getEthosCaseReference() + "\","
                + "\"resTseSelectApplication\":\"" + selectedApplication + "\","
                + "\"resTseDocument\":\"DocName.txt\","
                + "\"resTseTextBox\":\"Not Blank\""
                + "}"
            + "}";

        assertThat(RespondentTellSomethingElseHelper.getDocumentRequest(caseData, "key"))
                .isEqualTo(expectedDocumentString);
    }

}