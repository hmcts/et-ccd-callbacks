package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralReplyDataFromCaseData;

@Component
public class ReplyToReferralReplyReferralCallbackHandler extends CallbackHandlerBase {

    private static final String REPLY_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>We have recorded your reply. You can view it in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final UserIdamService userIdamService;
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final String referralTemplateId;

    @Autowired
    public ReplyToReferralReplyReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        UserIdamService userIdamService,
        ReferralService referralService,
        DocumentManagementService documentManagementService,
        EmailService emailService,
        FeatureToggleService featureToggleService,
        @Value("${template.referral}") String referralTemplateId
    ) {
        super(caseDetailsConverter);
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.userIdamService = userIdamService;
        this.referralService = referralService;
        this.documentManagementService = documentManagementService;
        this.emailService = emailService;
        this.featureToggleService = featureToggleService;
        this.referralTemplateId = referralTemplateId;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toCallbackResponse(aboutToSubmitReferralReply(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(completeReplyToReferral(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitReferralReply(
        CCDRequest ccdRequest,
        String userToken
    ) {
        var details = ccdRequest.getCaseDetails();
        CaseData caseData = details.getCaseData();
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        String referralCode = caseData.getSelectReferral().getValue().getCode();

        String name = String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName());
        ReferralHelper.createReferralReply(caseData, name, featureToggleService.isWorkAllocationEnabled());

        DocumentInfo documentInfo = referralService.generateCRDocument(caseData, userToken, details.getCaseTypeId());
        ReferralType referral = ReferralHelper.getSelectedReferral(caseData);
        referral.setReferralSummaryPdf(documentManagementService.addDocumentToDocumentField(documentInfo));

        if (StringUtils.isNotEmpty(caseData.getReplyToEmailAddress())) {
            String caseLink = emailService.getExuiCaseLink(details.getCaseId());
            emailService.sendEmail(
                referralTemplateId,
                caseData.getReplyToEmailAddress(),
                ReferralHelper.buildPersonalisation(caseData, referralCode, false, userDetails.getName(), caseLink)
            );
        }

        clearReferralReplyDataFromCaseData(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> completeReplyToReferral(CCDRequest ccdRequest) {
        String body = String.format(REPLY_REFERRAL_BODY, ccdRequest.getCaseDetails().getCaseId());
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
