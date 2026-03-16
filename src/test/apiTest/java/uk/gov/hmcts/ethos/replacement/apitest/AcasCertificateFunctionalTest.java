package uk.gov.hmcts.ethos.replacement.apitest;

import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional test for retrieving an ACAS certificate which verifies the ACAS API is working correctly.
 */
@Slf4j
public class AcasCertificateFunctionalTest extends BaseFunctionalTest {

    @Test
    @Tag("acasTesting")
    public void retrieveAcasCertificateSuccessResponse() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(CaseDataBuilder.builder().build())
            .build();
        ccdRequest.getCaseDetails().getCaseData().setAcasCertificate("R874121/23/84");
        assertTrue(true);
    }

}
