package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralReplyDataFromCaseData;

@Component
public class ReplyToReferralMultiplesReplyReferralCallbackHandler extends MultipleCallbackHandlerBase {

    private static final String REPLY_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>We have recorded your reply. You can view it in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";
    private final UserIdamService userIdamService;
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;
    private final FeatureToggleService featureToggleService;
    private final CaseLookupService caseLookupService;

    @Autowired
    public ReplyToReferralMultiplesReplyReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        UserIdamService userIdamService,
        ReferralService referralService,
        DocumentManagementService documentManagementService,
        FeatureToggleService featureToggleService,
        CaseLookupService caseLookupService
    ) {
        super(caseDetailsConverter);
        this.userIdamService = userIdamService;
        this.referralService = referralService;
        this.documentManagementService = documentManagementService;
        this.featureToggleService = featureToggleService;
        this.caseLookupService = caseLookupService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("replyToReferral");
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
        try {
            return aboutToSubmitReferralReply(
                    multipleRequest,
                    authorizationToken
                );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to reply to referral for multiple", exception);
        }
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        return completeReplyToReferral(
                    multipleRequest
                );
    }

    private ResponseEntity<MultipleCallbackResponse> aboutToSubmitReferralReply(
        MultipleRequest request,
        String userToken
    ) throws IOException {
        MultipleDetails caseDetails = request.getCaseDetails();
        MultipleData caseData = caseDetails.getCaseData();
        UserDetails userDetails = userIdamService.getUserDetails(userToken);

        String referralCode = caseData.getSelectReferral().getValue().getCode();
        String name = String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName());
        ReferralHelper.createReferralReply(caseData, name, featureToggleService.isWorkAllocationEnabled());

        String caseTypeId = caseDetails.getCaseTypeId();
        CaseData leadCase = caseLookupService.getLeadCaseFromMultipleAsAdmin(request.getCaseDetails());
        DocumentInfo documentInfo = referralService.generateDocument(caseData, leadCase, userToken, caseTypeId);

        ReferralType referral = ReferralHelper.getSelectedReferral(caseData);
        referral.setReferralSummaryPdf(documentManagementService.addDocumentToDocumentField(documentInfo));
        referralService.sendEmail(caseDetails, leadCase, referralCode, false, name);

        clearReferralReplyDataFromCaseData(caseData);
        return multipleResponse(caseData, null);
    }

    private ResponseEntity<MultipleCallbackResponse> completeReplyToReferral(MultipleRequest request) {
        String body = String.format(REPLY_REFERRAL_BODY, request.getCaseDetails().getCaseId());
        return ResponseEntity.ok(MultipleCallbackResponse.builder().confirmation_body(body).build());
    }
}
