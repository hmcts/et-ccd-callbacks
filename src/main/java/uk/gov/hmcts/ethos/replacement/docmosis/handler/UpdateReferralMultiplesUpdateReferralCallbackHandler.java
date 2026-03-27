package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;

import java.io.IOException;
import java.util.List;

@Component
public class UpdateReferralMultiplesUpdateReferralCallbackHandler extends MultipleCallbackHandlerBase {

    private static final String NONE = "None";
    private final UserIdamService userIdamService;
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;
    private final CaseLookupService caseLookupService;

    @Autowired
    public UpdateReferralMultiplesUpdateReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        UserIdamService userIdamService,
        ReferralService referralService,
        DocumentManagementService documentManagementService,
        CaseLookupService caseLookupService
    ) {
        super(caseDetailsConverter);
        this.userIdamService = userIdamService;
        this.referralService = referralService;
        this.documentManagementService = documentManagementService;
        this.caseLookupService = caseLookupService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
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
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        try {
            return aboutToSubmitUpdateReferralDetails(
                    multipleRequest,
                    authorizationToken
                );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to update referral details for multiple", exception);
        }
    }

    private Object aboutToSubmitUpdateReferralDetails(
        uk.gov.hmcts.et.common.model.multiples.MultipleRequest ccdRequest,
        String userToken
    ) throws IOException {
        var caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setReferralSubject(
            uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.setReferralSubject(
                caseData.getReferralSubject()
            )
        );

        var userDetails = userIdamService.getUserDetails(userToken);
        var leadCase = caseLookupService.getLeadCaseFromMultipleAsAdmin(ccdRequest.getCaseDetails());
        var nextHearingDate = uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper
            .getNearestHearingToReferral(leadCase, NONE);
        var name = String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName());
        uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.updateReferral(caseData, name, nextHearingDate);

        var referral = caseData.getReferralCollection()
            .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();
        var caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        var documentInfo = referralService.generateDocument(caseData, leadCase, userToken, caseTypeId);
        referral.setReferralSummaryPdf(documentManagementService.addDocumentToDocumentField(documentInfo));

        uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData(caseData);
        return uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse(caseData, null);
    }
}
