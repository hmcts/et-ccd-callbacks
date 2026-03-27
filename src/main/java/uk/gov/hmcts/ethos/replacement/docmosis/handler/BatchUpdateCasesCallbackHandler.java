package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUpdateService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Slf4j
@Component
public class BatchUpdateCasesCallbackHandler extends MultipleCallbackHandlerBase {

    private static final String LOG_MESSAGE = " ---> received notification request for multiple reference : {}";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final MultipleUpdateService multipleUpdateService;

    @Autowired
    public BatchUpdateCasesCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        MultipleUpdateService multipleUpdateService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.multipleUpdateService = multipleUpdateService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("batchUpdateCases");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return false;
    }

    @Override
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        log.info("BATCH UPDATE" + LOG_MESSAGE, multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            log.error(INVALID_TOKEN, authorizationToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        var multipleDetails = multipleRequest.getCaseDetails();

        try {
            multipleUpdateService.bulkUpdateLogic(authorizationToken, multipleDetails, errors);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to batch update multiple", exception);
        }

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
