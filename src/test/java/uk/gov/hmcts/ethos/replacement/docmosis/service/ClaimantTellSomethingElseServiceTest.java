package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.GIVE_DETAIL_MISSING;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class ClaimantTellSomethingElseServiceTest {
    private ClaimantTellSomethingElseService claimantTellSomethingElseService;

    @BeforeEach
    void setUp() {
        claimantTellSomethingElseService = new ClaimantTellSomethingElseService();
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

    private static Stream<Arguments> selectedApplicationList() {
        return Stream.of(
            Arguments.of(CLAIMANT_TSE_WITHDRAW_CLAIM)
        );
    }

    private void setTextBoxForSelectedApplication(CaseData caseData) {
        switch (caseData.getClaimantTseSelectApplication()) {
            case CLAIMANT_TSE_WITHDRAW_CLAIM:
                caseData.setClaimantTseTextBox13("Some text");
                break;
            default:
                throw new IllegalArgumentException("Unexpected application type");
        }
    }

    private void setDocForSelectedApplication(CaseData caseData) {
        switch (caseData.getClaimantTseSelectApplication()) {
            case CLAIMANT_TSE_WITHDRAW_CLAIM:
                caseData.setClaimantTseDocument13(createDocumentType());
                break;
            default:
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
