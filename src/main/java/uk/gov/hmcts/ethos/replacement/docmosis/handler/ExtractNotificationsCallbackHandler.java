package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.NotificationsExcelReportService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Component
public class ExtractNotificationsCallbackHandler extends CallbackHandlerBase {

    private final NotificationsExcelReportService notificationsExcelReportService;

    @Autowired
    public ExtractNotificationsCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        NotificationsExcelReportService notificationsExcelReportService
    ) {
        super(caseDetailsConverter);
        this.notificationsExcelReportService = notificationsExcelReportService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("extractNotifications");
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var multipleRequest = toMultipleRequest(caseDetails);
        List<String> errors = new ArrayList<>();
        var multipleDetails = multipleRequest.getCaseDetails();
        notificationsExcelReportService.generateReportAsync(multipleDetails, authorizationToken, errors);
        return toCallbackResponse(getMultipleCallbackRespEntity(errors, multipleDetails));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String body = """
                ### Extract task submitted
                Once completed the generated extract will be available to download on the </br>
                notifications tab of the multiple. </br>
                """;

        return toSubmittedCallbackResponse(ResponseEntity.ok(
            MultipleCallbackResponse.builder()
                .confirmation_body(body)
                .build()
        ));
    }
}
