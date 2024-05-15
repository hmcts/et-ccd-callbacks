package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.ResourceLoader;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormClaimantMapper.mapClaimant;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_CLAIMANT_FIELD_CLAIMANT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.ET3_FORM_SAMPLE_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_CLAIMANT_VALUE_ET1_CLAIMANT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_CLAIMANT_VALUE_ET3_CLAIMANT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.cloneObject;

class ET3FormClaimantMapperTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;

    @BeforeEach
    void beforeEach() {

        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @MethodSource("provideMapClaimantTestData")
    void testMapClaimant(CaseData caseData, RespondentSumType respondentSumType) {
        mapClaimant(caseData, respondentSumType, pdfFields);
        if (StringUtils.isBlank(respondentSumType.getEt3ResponseClaimantNameCorrection())) {
            assertThat(pdfFields.get(TXT_PDF_CLAIMANT_FIELD_CLAIMANT_NAME))
                    .contains(TEST_PDF_CLAIMANT_VALUE_ET1_CLAIMANT_NAME);
        } else {
            assertThat(pdfFields.get(TXT_PDF_CLAIMANT_FIELD_CLAIMANT_NAME))
                    .contains(TEST_PDF_CLAIMANT_VALUE_ET3_CLAIMANT_NAME);
        }
    }

    private static Stream<Arguments> provideMapClaimantTestData() {
        CaseData caseData = ResourceLoader.fromString(ET3_FORM_SAMPLE_CASE_DATA_FILE, CaseData.class);
        RespondentSumType respondentSumTypeWithResponseClaimantNameCorrection =
                caseData.getRespondentCollection().stream().filter(r -> caseData.getSubmitEt3Respondent()
                        .getSelectedLabel().equals(r.getValue().getRespondentName()))
                .toList().get(0).getValue();
        RespondentSumType respondentSumTypeWithoutResponseClaimantNameCorrection =
                cloneObject(respondentSumTypeWithResponseClaimantNameCorrection, RespondentSumType.class);
        respondentSumTypeWithoutResponseClaimantNameCorrection.setEt3ResponseClaimantNameCorrection(null);
        return Stream.of(Arguments.of(caseData, respondentSumTypeWithResponseClaimantNameCorrection),
                Arguments.of(caseData, respondentSumTypeWithoutResponseClaimantNameCorrection));
    }

}
