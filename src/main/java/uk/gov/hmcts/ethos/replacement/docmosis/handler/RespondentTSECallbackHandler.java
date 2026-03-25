package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.respondent.RespondentTellSomethingElseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.RESPONDENT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class RespondentTSECallbackHandler extends CallbackHandlerBase {

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_NO = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>The tribunal will consider all correspondence and let you know what happens next.</p>";
    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>You have sent a copy of your application to the claimant. They will have until %s to respond.</p>"
        + "<p>If they do respond, they are expected to copy their response to you.</p>"
        + "<p>You may be asked to supply further information. "
        + "The tribunal will consider all correspondence and let you know what happens next.</p>";
    private final VerifyTokenService verifyTokenService;
    private final RespondentTellSomethingElseService resTseService;
    private final TseService tseService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    public RespondentTSECallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        RespondentTellSomethingElseService resTseService,
        TseService tseService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.resTseService = resTseService;
        this.tseService = tseService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("respondentTSE");
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
        return toCallbackResponse(aboutToSubmitRespondentTse(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(completeRespondentApplication(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondentTse(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var details = ccdRequest.getCaseDetails();
        CaseData caseData = details.getCaseData();
        tseService.createApplication(caseData, RESPONDENT_REP_TITLE);
        resTseService.sendEmails(details, userToken);
        resTseService.generateAndAddTsePdf(caseData, userToken, details.getCaseTypeId());
        tseService.clearApplicationData(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> completeRespondentApplication(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<GenericTseApplicationTypeItem> applications =
            ccdRequest.getCaseDetails().getCaseData().getGenericTseApplicationCollection();
        GenericTseApplicationTypeItem latestApplication = applications.getLast();

        String body;
        if (YES.equals(latestApplication.getValue().getCopyToOtherPartyYesOrNo())) {
            body = String.format(
                APPLICATION_COMPLETE_RULE92_ANSWERED_YES,
                UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7)
            );
        } else {
            body = APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
