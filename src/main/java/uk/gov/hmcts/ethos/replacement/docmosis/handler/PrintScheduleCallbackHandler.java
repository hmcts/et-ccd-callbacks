package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.MultipleDocGenerationCallbackService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleScheduleService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntityDocInfo;

@Component
public class PrintScheduleCallbackHandler extends MultipleCallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final MultipleScheduleService multipleScheduleService;
    private final MultipleDocGenerationCallbackService multipleDocGenerationCallbackService;

    @Autowired
    public PrintScheduleCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        MultipleScheduleService multipleScheduleService,
        MultipleDocGenerationCallbackService multipleDocGenerationCallbackService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.multipleScheduleService = multipleScheduleService;
        this.multipleDocGenerationCallbackService = multipleDocGenerationCallbackService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("printSchedule");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return printSchedule(multipleRequest, authorizationToken);
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return multipleDocGenerationCallbackService.printDocumentConfirmation(
                    multipleRequest,
                    authorizationToken
                );
    }

    private ResponseEntity<MultipleCallbackResponse> printSchedule(
        uk.gov.hmcts.et.common.model.multiples.MultipleRequest request,
        String userToken
    ) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = request.getCaseDetails();
        DocumentInfo documentInfo = multipleScheduleService.bulkScheduleLogic(userToken, multipleDetails, errors);
        return getMultipleCallbackRespEntityDocInfo(errors, multipleDetails, documentInfo);
    }
}
