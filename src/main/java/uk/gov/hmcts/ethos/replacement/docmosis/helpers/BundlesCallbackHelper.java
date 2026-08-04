package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;

public final class BundlesCallbackHelper {

    public static final String SUBMITTED_CONFIRMATION_HEADER =
            "<h1>You have sent your hearing documents to the tribunal</h1>";
    public static final String SUBMITTED_CONFIRMATION_BODY = """
        <html>
            <body>
                <tag><h2>What happens next</h2></tag>
                <h2>The tribunal will let you know
                if they have any questions about the hearing documents you have submitted.</h2>
            </body>
        </html>""";

    private BundlesCallbackHelper() {
    }

    public static ResponseEntity<CCDCallbackResponse> buildSubmittedResponse(CCDRequest ccdRequest) {
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_header(SUBMITTED_CONFIRMATION_HEADER)
                .confirmation_body(SUBMITTED_CONFIRMATION_BODY)
                .build());
    }
}
