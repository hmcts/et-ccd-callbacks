package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class BundlesCallbackHelperTest {

    @Test
    void buildSubmittedResponse_includesConfirmationContent() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(CaseDataBuilder.builder().build())
                .build();

        var response = BundlesCallbackHelper.buildSubmittedResponse(ccdRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CCDCallbackResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getConfirmation_header()).isEqualTo(BundlesCallbackHelper.SUBMITTED_CONFIRMATION_HEADER);
        assertThat(body.getConfirmation_body()).isEqualTo(BundlesCallbackHelper.SUBMITTED_CONFIRMATION_BODY);
        assertThat(body.getData()).isEqualTo(ccdRequest.getCaseDetails().getCaseData());
    }
}
