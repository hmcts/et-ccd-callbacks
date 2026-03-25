package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getLast;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.getNearestHearingToReferral;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.setReferralSubject;

@Component
public class CreateReferralMultiplesCreateReferralCallbackHandler extends CallbackHandlerBase {

    private static final String CREATE_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>Your referral has been sent. Replies and instructions will appear in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";
    private final ReferralService referralService;
    private final UserIdamService userIdamService;
    private final DocumentManagementService documentManagementService;
    private final CaseLookupService caseLookupService;

    @Autowired
    public CreateReferralMultiplesCreateReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ReferralService referralService,
        UserIdamService userIdamService,
        DocumentManagementService documentManagementService,
        CaseLookupService caseLookupService
    ) {
        super(caseDetailsConverter);
        this.referralService = referralService;
        this.userIdamService = userIdamService;
        this.documentManagementService = documentManagementService;
        this.caseLookupService = caseLookupService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
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
        try {
            return toCallbackResponse(aboutToSubmitReferralDetails(
                    toMultipleRequest(caseDetails),
                    authorizationToken
                ));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create referral for multiple", exception);
        }
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(completeCreateReferral(
                    toMultipleRequest(caseDetails)
                ));
    }

    private ResponseEntity<MultipleCallbackResponse> aboutToSubmitReferralDetails(
        MultipleRequest ccdRequest,
        String userToken
    ) throws IOException {
        MultipleDetails details = ccdRequest.getCaseDetails();
        MultipleData caseData = details.getCaseData();
        caseData.setReferralSubject(setReferralSubject(caseData.getReferralSubject()));
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        CaseData leadCase = caseLookupService.getLeadCaseFromMultipleAsAdmin(details);
        caseData.setReferredBy(String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()));

        String caseTypeId = details.getCaseTypeId();
        DocumentInfo documentInfo = referralService.generateDocument(caseData, leadCase, userToken, caseTypeId);
        String nextHearingDate = getNearestHearingToReferral(leadCase, "None");

        ReferralHelper.createReferral(
            caseData,
            String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()),
            documentManagementService.addDocumentToDocumentField(documentInfo),
            nextHearingDate
        );

        String referralNumber = getLast(caseData.getReferralCollection()).getValue().getReferralNumber();
        referralService.sendEmail(details, leadCase, referralNumber, true, userDetails.getName());
        clearReferralDataFromCaseData(caseData);
        return multipleResponse(caseData, null);
    }

    private ResponseEntity<MultipleCallbackResponse> completeCreateReferral(MultipleRequest ccdRequest) {
        String body = String.format(CREATE_REFERRAL_BODY, ccdRequest.getCaseDetails().getCaseId());
        return ResponseEntity.ok(MultipleCallbackResponse.builder().confirmation_body(body).build());
    }
}
