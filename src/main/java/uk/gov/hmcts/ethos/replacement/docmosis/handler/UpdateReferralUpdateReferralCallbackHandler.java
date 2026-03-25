package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.getNearestHearingToReferral;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.setReferralSubject;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.updateReferral;

@Component
public class UpdateReferralUpdateReferralCallbackHandler extends CallbackHandlerBase {

    private final UserIdamService userIdamService;
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;

    @Autowired
    public UpdateReferralUpdateReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        UserIdamService userIdamService,
        ReferralService referralService,
        DocumentManagementService documentManagementService
    ) {
        super(caseDetailsConverter);
        this.userIdamService = userIdamService;
        this.referralService = referralService;
        this.documentManagementService = documentManagementService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("updateReferral");
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toCallbackResponse(aboutToSubmitUpdateReferralDetails(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitUpdateReferralDetails(
        CCDRequest ccdRequest,
        String userToken
    ) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setUpdateReferralSubject(setReferralSubject(caseData.getUpdateReferralSubject()));
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        String nextHearingDate = getNearestHearingToReferral(caseData, "None");
        String name = String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName());
        updateReferral(caseData, name, nextHearingDate);
        ReferralType referral = caseData.getReferralCollection()
            .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();

        DocumentInfo documentInfo = referralService.generateCRDocument(caseData, userToken,
            ccdRequest.getCaseDetails().getCaseTypeId());
        referral.setReferralSummaryPdf(documentManagementService.addDocumentToDocumentField(documentInfo));

        clearReferralDataFromCaseData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }
}
