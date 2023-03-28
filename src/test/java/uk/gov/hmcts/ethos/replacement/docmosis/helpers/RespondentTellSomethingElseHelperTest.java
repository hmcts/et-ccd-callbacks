package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CLAIMANT_NOT_COMPLIED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONTACT_THE_TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_ORDER_OTHER_PARTY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RECONSIDER_JUDGEMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_VARY_OR_REVOKE_AN_ORDER;

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.CyclomaticComplexity"})
class RespondentTellSomethingElseHelperTest {

    private CaseData caseData;

    private static final String RES_TSE_FILE_NAME = "resTse.pdf";
    private static final String RES_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("6000001/2022")
            .build();
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
                Arguments.of(TSE_APP_AMEND_RESPONSE),
                Arguments.of(TSE_APP_CHANGE_PERSONAL_DETAILS),
                Arguments.of(TSE_APP_CLAIMANT_NOT_COMPLIED),
                Arguments.of(TSE_APP_CONSIDER_A_DECISION_AFRESH),
                Arguments.of(TSE_APP_CONTACT_THE_TRIBUNAL),
                Arguments.of(TSE_APP_ORDER_OTHER_PARTY),
                Arguments.of(TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE),
                Arguments.of(TSE_APP_POSTPONE_A_HEARING),
                Arguments.of(TSE_APP_RECONSIDER_JUDGEMENT),
                Arguments.of(TSE_APP_RESTRICT_PUBLICITY),
                Arguments.of(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM),
                Arguments.of(TSE_APP_VARY_OR_REVOKE_AN_ORDER));
    }

    private void setTextBoxForSelectedApplication(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case TSE_APP_AMEND_RESPONSE:
                caseData.setResTseTextBox1("Not Blank");
                break;
            case TSE_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseTextBox2("Not Blank");
                break;
            case TSE_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseTextBox3("Not Blank");
                break;
            case TSE_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseTextBox4("Not Blank");
                break;
            case TSE_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseTextBox5("Not Blank");
                break;
            case TSE_APP_ORDER_OTHER_PARTY:
                caseData.setResTseTextBox6("Not Blank");
                break;
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseTextBox7("Not Blank");
                break;
            case TSE_APP_POSTPONE_A_HEARING:
                caseData.setResTseTextBox8("Not Blank");
                break;
            case TSE_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseTextBox9("Not Blank");
                break;
            case TSE_APP_RESTRICT_PUBLICITY:
                caseData.setResTseTextBox10("Not Blank");
                break;
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseTextBox11("Not Blank");
                break;
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER:
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
            case TSE_APP_AMEND_RESPONSE:
                caseData.setResTseDocument1(createDocumentType());
                break;
            case TSE_APP_CHANGE_PERSONAL_DETAILS:
                caseData.setResTseDocument2(createDocumentType());
                break;
            case TSE_APP_CLAIMANT_NOT_COMPLIED:
                caseData.setResTseDocument3(createDocumentType());
                break;
            case TSE_APP_CONSIDER_A_DECISION_AFRESH:
                caseData.setResTseDocument4(createDocumentType());
                break;
            case TSE_APP_CONTACT_THE_TRIBUNAL:
                caseData.setResTseDocument5(createDocumentType());
                break;
            case TSE_APP_ORDER_OTHER_PARTY:
                caseData.setResTseDocument6(createDocumentType());
                break;
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                caseData.setResTseDocument7(createDocumentType());
                break;
            case TSE_APP_POSTPONE_A_HEARING:
                caseData.setResTseDocument8(createDocumentType());
                break;
            case TSE_APP_RECONSIDER_JUDGEMENT:
                caseData.setResTseDocument9(createDocumentType());
                break;
            case TSE_APP_RESTRICT_PUBLICITY:
                caseData.setResTseDocument10(createDocumentType());
                break;
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                caseData.setResTseDocument11(createDocumentType());
                break;
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER:
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
