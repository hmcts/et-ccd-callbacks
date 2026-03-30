package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.ET_ENGLAND_AND_WALES;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.ET_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService.SUBMITTED_BODY;

@Slf4j
@Component
public class ClaimantRespondToNotificationHandler extends CallbackHandlerBase {

    private static final String CLAIMANT_RESPOND_TO_NOTIFICATION = "claimantRespondToNotification";

    private final PseRespondToTribunalService pseRespondToTribunalService;

    @Autowired
    public ClaimantRespondToNotificationHandler(CaseDetailsConverter caseDetailsConverter,
                                                PseRespondToTribunalService pseRespondToTribunalService) {
        super(caseDetailsConverter);
        this.pseRespondToTribunalService = pseRespondToTribunalService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of(ET_ENGLAND_AND_WALES, ET_SCOTLAND);
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of(CLAIMANT_RESPOND_TO_NOTIFICATION);
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
    public boolean shouldRetrySubmitted() {
        return true;
    }

    @Override
    public CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        CaseData caseData = toCaseDetails(caseDetails).getCaseData();

        pseRespondToTribunalService.saveClaimantResponse(caseData);
        pseRespondToTribunalService.clearClaimantNotificationDetails(caseData);

        return CCDCallbackResponse.builder().data(caseData).build();
    }

    @Override
    public SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var convertedCaseDetails = toCaseDetails(caseDetails);
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        pseRespondToTribunalService.sendEmailsForClaimantResponse(convertedCaseDetails, authorizationToken);

        return CCDCallbackResponse.builder()
                .confirmation_body(SUBMITTED_BODY.formatted(EMPTY_STRING))
                .build();
    }
}
