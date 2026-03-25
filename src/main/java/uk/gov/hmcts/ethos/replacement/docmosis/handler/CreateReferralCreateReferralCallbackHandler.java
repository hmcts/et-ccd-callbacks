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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.setReferralSubject;

@Component
public class CreateReferralCreateReferralCallbackHandler extends CallbackHandlerBase {

    private static final String CREATE_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>Your referral has been sent. Replies and instructions will appear in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final ReferralService referralService;
    private final UserIdamService userIdamService;
    private final DocumentManagementService documentManagementService;
    private final EmailService emailService;
    private final String referralTemplateId;

    @Autowired
    public CreateReferralCreateReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        ReferralService referralService,
        UserIdamService userIdamService,
        DocumentManagementService documentManagementService,
        EmailService emailService,
        @Value("${template.referral}") String referralTemplateId
    ) {
        super(caseDetailsConverter);
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.referralService = referralService;
        this.userIdamService = userIdamService;
        this.documentManagementService = documentManagementService;
        this.emailService = emailService;
        this.referralTemplateId = referralTemplateId;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("createReferral");
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
        return toCallbackResponse(aboutToSubmitReferralDetails(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(completeCreateReferral(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitReferralDetails(
        CCDRequest ccdRequest,
        String userToken
    ) {
        var details = ccdRequest.getCaseDetails();
        CaseData caseData = details.getCaseData();
        caseData.setReferralSubject(setReferralSubject(caseData.getReferralSubject()));
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        String referralNumber = String.valueOf(ReferralHelper.getNextReferralNumber(caseData.getReferralCollection()));

        caseData.setReferredBy(String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()));
        DocumentInfo documentInfo = referralService.generateCRDocument(caseData, userToken, details.getCaseTypeId());
        ReferralHelper.createReferral(
            caseData,
            String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()),
            documentManagementService.addDocumentToDocumentField(documentInfo)
        );

        if (StringUtils.isNotEmpty(caseData.getReferentEmail())) {
            String caseLink = emailService.getExuiCaseLink(details.getCaseId());
            emailService.sendEmail(
                referralTemplateId,
                caseData.getReferentEmail(),
                ReferralHelper.buildPersonalisation(caseData, referralNumber, true, userDetails.getName(), caseLink)
            );
        }

        clearReferralDataFromCaseData(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> completeCreateReferral(CCDRequest ccdRequest) {
        String body = String.format(CREATE_REFERRAL_BODY, ccdRequest.getCaseDetails().getCaseId());
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
